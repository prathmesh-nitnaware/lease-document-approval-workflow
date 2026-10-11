<#
.SYNOPSIS
    Reproducible bootstrap script for the WSL2 target node 'lease-target'.

.DESCRIPTION
    Safely tears down and reconstructs the isolated 'lease-target' Ubuntu 24.04 WSL2
    instance from a verified base archive. Injects the Ansible control node's ED25519
    public key via standard input, configures SSH on port 2222, activates systemd,
    verifies host keys via exact SHA-256 fingerprint matching, updates known_hosts with
    the exact verified key, and validates passwordless sudo.
#>

[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# ==============================================================================
# Configuration & Constants
# ==============================================================================
$TargetDistroName          = "lease-target"
$ControlDistroName         = "Ubuntu-24.04"
$BaseArchivePath           = "C:\WSL\ubuntu-24.04-base.tar"
$TargetInstallDir          = "C:\WSL\lease-target"
$TargetSshPort             = 2222
$AnsibleUser               = "ansible"
$HardcodedUnregisterTarget = "lease-target"

function Assert-ExitCodeZero {
    param(
        [Parameter(Mandatory = $true)]
        [int]$ExitCode,
        [Parameter(Mandatory = $true)]
        [string]$StepDescription
    )
    if ($ExitCode -ne 0) {
        Write-Error "CRITICAL FAILURE: Step '$StepDescription' exited with non-zero code ($ExitCode). Halting immediately."
        exit $ExitCode
    }
}

function Get-WslDistrosStrict {
    $rawOutput = & wsl.exe -l -v 2>&1
    $wslExit = $LASTEXITCODE
    Assert-ExitCodeZero -ExitCode $wslExit -StepDescription "Execute wsl.exe -l -v"

    $headerFound = $false
    $distros = @()

    foreach ($line in $rawOutput) {
        $cleanLine = ($line -replace "`0", "").Trim()
        if ([string]::IsNullOrWhiteSpace($cleanLine)) { continue }

        if ($cleanLine -match '^\s*NAME\s+STATE\s+VERSION\s*$') {
            $headerFound = $true
            continue
        }

        if ($headerFound) {
            if ($cleanLine -match '^\s*(?<default>\*?)\s*(?<name>\S+)\s+(?<state>\S+)\s+(?<version>\d+)\s*$') {
                $distros += [PSCustomObject]@{
                    Name      = $Matches['name']
                    State     = $Matches['state']
                    Version   = [int]$Matches['version']
                    IsDefault = ($Matches['default'] -eq '*')
                    Raw       = $cleanLine
                }
            } else {
                Write-Error "CRITICAL FAILURE: Malformed distribution line in 'wsl.exe -l -v': '$cleanLine'. Fails closed."
                exit 1
            }
        }
    }

    if (-not $headerFound) {
        Write-Error "CRITICAL FAILURE: Expected header 'NAME STATE VERSION' not found in 'wsl.exe -l -v' output. Fails closed."
        exit 1
    }

    if ($distros.Count -eq 0) {
        Write-Error "CRITICAL FAILURE: Zero distributions parsed from 'wsl.exe -l -v'. Fails closed."
        exit 1
    }

    return $distros
}

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host " Week 14: Isolated Target Bootstrap Preflight Validations" -ForegroundColor Cyan
Write-Host " Target Distro : $TargetDistroName" -ForegroundColor Cyan
Write-Host " Control Distro: $ControlDistroName" -ForegroundColor Cyan
Write-Host " Base Archive  : $BaseArchivePath" -ForegroundColor Cyan
Write-Host " Install Dir   : $TargetInstallDir" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# ==============================================================================
# Phase 1: Pre-Destruction Prerequisite Validations
# ==============================================================================

# 1.1 Validate Base Archive Existence & Structural Integrity
Write-Host "`n[Check 1/5] Validating base archive existence and structure..." -ForegroundColor Yellow
if (-not (Test-Path -LiteralPath $BaseArchivePath -PathType Leaf)) {
    Write-Error "Base archive not found at '$BaseArchivePath'. Aborting."
    exit 1
}

$archiveItem = Get-Item -LiteralPath $BaseArchivePath
if ($archiveItem.Length -lt 100MB) {
    Write-Error "Base archive size ($($archiveItem.Length) bytes) is below minimum threshold (100MB). Aborting."
    exit 1
}

# Synchronous execution capturing output without pipeline or Out-Null
$tarTestOutput = & tar.exe -tf $BaseArchivePath 2>&1
$tarExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $tarExit -StepDescription "Validate base tar archive structural integrity"
Write-Host "  -> Base archive verified readable and structurally valid ($([math]::Round($archiveItem.Length / 1MB, 2)) MB)." -ForegroundColor Green

# 1.2 Verify Control Distro Existence by Exact Name
Write-Host "`n[Check 2/5] Verifying control node '$ControlDistroName' by exact name..." -ForegroundColor Yellow
$currentDistros = Get-WslDistrosStrict
$controlFound = $currentDistros | Where-Object { $_.Name -ceq $ControlDistroName }
if (-not $controlFound) {
    Write-Error "Control node '$ControlDistroName' not found in WSL distro list. Aborting."
    exit 1
}
Write-Host "  -> Control node '$ControlDistroName' detected (State: $($controlFound.State), Version: $($controlFound.Version))." -ForegroundColor Green

# 1.3 Read Control Node ED25519 Public Key
Write-Host "`n[Check 3/5] Verifying control node ED25519 public key..." -ForegroundColor Yellow
$controlPubKeyRaw = & wsl.exe -d $ControlDistroName -u ansible -- bash -c '
    set -e
    if [ ! -f ~/.ssh/id_ed25519.pub ]; then
        echo "MISSING_KEY" >&2
        exit 1
    fi
    cat ~/.ssh/id_ed25519.pub
' 2>&1
$pubKeyExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $pubKeyExit -StepDescription "Read control node public key"

$controlPubKey = ($controlPubKeyRaw | Out-String).Trim()
if (-not $controlPubKey.StartsWith("ssh-ed25519 ")) {
    Write-Error "Control node public key is invalid or not an ED25519 key. Value: '$controlPubKey'"
    exit 1
}
Write-Host "  -> Control node public key verified: $($controlPubKey.Substring(0, 32))..." -ForegroundColor Green

# 1.4 Re-query Distro Table & Verify Target State and Install Directory Alignment
Write-Host "`n[Check 4/5] Re-querying WSL distros and inspecting directory alignment..." -ForegroundColor Yellow
$freshDistroList = Get-WslDistrosStrict
$targetFound = $freshDistroList | Where-Object { $_.Name -ceq $TargetDistroName }
$targetExists = ($null -ne $targetFound)

# Enforce that TargetDistroName is never a protected distro
if ($TargetDistroName -ceq $ControlDistroName -or $TargetDistroName -ceq "docker-desktop") {
    Write-Error "FATAL SAFETY VIOLATION: TargetDistroName matches a protected distro! Aborting."
    exit 1
}

if ($targetExists) {
    Write-Host "  -> Distro '$TargetDistroName' is currently registered." -ForegroundColor DarkGray
    if (Test-Path -LiteralPath $TargetInstallDir) {
        $existingDirItems = Get-ChildItem -LiteralPath $TargetInstallDir -Force
        Write-Host "     Inspecting directory '$TargetInstallDir' ($($existingDirItems.Count) item(s))..." -ForegroundColor DarkGray
        $unexpectedItems = $existingDirItems | Where-Object { $_.Name -notmatch '^(ext4\.vhdx|ext4\.vhdx\.tmp)$' }
        if ($unexpectedItems) {
            Write-Error "SAFETY VIOLATION: Directory '$TargetInstallDir' contains unexpected files: $($unexpectedItems.Name -join ', '). Manual review required before unregistering. Aborting."
            exit 1
        }
        Write-Host "     Directory contains exclusively WSL disk files. Permitted for WSL to manage upon unregister." -ForegroundColor Green
    }
} else {
    Write-Host "  -> Distro '$TargetDistroName' is NOT currently registered." -ForegroundColor DarkGray
    if (Test-Path -LiteralPath $TargetInstallDir) {
        $existingDirItems = Get-ChildItem -LiteralPath $TargetInstallDir -Force
        if ($existingDirItems.Count -gt 0) {
            Write-Error "SAFETY VIOLATION: Target distro is absent, but directory '$TargetInstallDir' contains $($existingDirItems.Count) file(s). Manual review required before importing. Aborting."
            exit 1
        }
    }
}

# 1.5 Display Fresh WSL Table Before Confirmation
Write-Host "`n[Check 5/5] Fresh WSL Distro Table:" -ForegroundColor Yellow
Write-Host "---------------- CURRENT WSL STATE ----------------" -ForegroundColor DarkGray
$wslTableDisplay = & wsl.exe -l -v 2>&1
$wslTableExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $wslTableExit -StepDescription "Display current WSL table"
Write-Host ($wslTableDisplay | Out-String).Trim() -ForegroundColor DarkGray
Write-Host "---------------------------------------------------" -ForegroundColor DarkGray

# ==============================================================================
# Phase 2: Interactive Confirmation (Strict - No -Force Bypass)
# ==============================================================================
Write-Host "`n=================================================================" -ForegroundColor Magenta
if ($targetExists) {
    Write-Host " WARNING: Distro '$TargetDistroName' is registered and will be UNREGISTERED." -ForegroundColor Magenta
} else {
    Write-Host " Distro '$TargetDistroName' is not registered and will be imported cleanly." -ForegroundColor Magenta
}
Write-Host " Protected distros '$ControlDistroName' and 'docker-desktop' will NOT be touched." -ForegroundColor Magenta
Write-Host " Unregister target is hardcoded strictly to: '$HardcodedUnregisterTarget'." -ForegroundColor Magenta
Write-Host "=================================================================" -ForegroundColor Magenta

$confirmation = Read-Host "Type 'CONFIRM-RESET' to proceed with resetting '$TargetDistroName'"
if ($confirmation -cne "CONFIRM-RESET") {
    Write-Host "Operation aborted by user. No modifications were performed." -ForegroundColor Yellow
    exit 0
}

# ==============================================================================
# Phase 3: Destruction of Existing Target & Directory State Assertion
# ==============================================================================
if ($targetExists) {
    Write-Host "`nTerminating '$TargetDistroName'..." -ForegroundColor Yellow
    $termOutput = & wsl.exe --terminate $TargetDistroName 2>&1
    # Terminate may return 0 or non-zero if already stopped; non-fatal

    Write-Host "Unregistering '$HardcodedUnregisterTarget'..." -ForegroundColor Yellow
    $unregOutput = & wsl.exe --unregister $HardcodedUnregisterTarget 2>&1
    $unregExit = $LASTEXITCODE
    Assert-ExitCodeZero -ExitCode $unregExit -StepDescription "Unregister $HardcodedUnregisterTarget"
    Write-Host "  -> Unregistered '$HardcodedUnregisterTarget' successfully." -ForegroundColor Green
}

# Post-unregister check: directory must be absent or completely empty (No automatic deletion)
Write-Host "Validating install directory state post-unregister..." -ForegroundColor Yellow
if (Test-Path -LiteralPath $TargetInstallDir) {
    $remainingItems = Get-ChildItem -LiteralPath $TargetInstallDir -Force
    if ($remainingItems.Count -gt 0) {
        Write-Error "CRITICAL FAILURE: Directory '$TargetInstallDir' still contains residual files after unregistering: $($remainingItems.Name -join ', '). Automatic deletion is forbidden. Inspect manually. Aborting."
        exit 1
    }
    Write-Host "  -> Directory '$TargetInstallDir' is empty." -ForegroundColor Green
} else {
    $newDir = New-Item -ItemType Directory -Path $TargetInstallDir -Force
    Write-Host "  -> Created empty directory '$TargetInstallDir'." -ForegroundColor Green
}

# ==============================================================================
# Phase 4: Import and Initialize Fresh Target
# ==============================================================================
Write-Host "`nImporting '$TargetDistroName' from $BaseArchivePath..." -ForegroundColor Yellow
$importOutput = & wsl.exe --import $TargetDistroName $TargetInstallDir $BaseArchivePath --version 2 2>&1
$importExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $importExit -StepDescription "Import $TargetDistroName"

# 4.1 Configure /etc/wsl.conf for systemd and default user
Write-Host "Configuring systemd and default user in /etc/wsl.conf..." -ForegroundColor Yellow
$wslConfScript = "cat << 'EOF' > /etc/wsl.conf
[boot]
systemd=true

[user]
default=$AnsibleUser
EOF"
$wslConfOutput = & wsl.exe -d $TargetDistroName -u root -- bash -c $wslConfScript 2>&1
$wslConfExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $wslConfExit -StepDescription "Write /etc/wsl.conf"

# 4.2 Restart Target Distro to activate systemd
Write-Host "Restarting '$TargetDistroName' to initialize systemd..." -ForegroundColor Yellow
$termPostConf = & wsl.exe --terminate $TargetDistroName 2>&1
$termPostConfExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $termPostConfExit -StepDescription "Terminate target after wsl.conf update"
Start-Sleep -Seconds 3

# Trigger systemd boot
$bootTrigger = & wsl.exe -d $TargetDistroName -u root -- /bin/true 2>&1
$bootTriggerExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $bootTriggerExit -StepDescription "Trigger systemd boot"

# 4.3 Verify systemd Operational Readiness
Write-Host "Verifying systemd operational status..." -ForegroundColor Yellow
$systemdStateRaw = & wsl.exe -d $TargetDistroName -u root -- systemctl is-system-running 2>&1
$systemdExitCode = $LASTEXITCODE
$cleanSystemdState = ($systemdStateRaw | Out-String).Trim()

$isSystemdOperational = ($cleanSystemdState -eq "running" -and $systemdExitCode -eq 0) -or ($cleanSystemdState -eq "degraded" -and $systemdExitCode -eq 1)

if (-not $isSystemdOperational) {
    Write-Host "CRITICAL: Systemd did not achieve an acceptable operational state!" -ForegroundColor Red
    Write-Host "Reported State : '$cleanSystemdState'" -ForegroundColor Red
    Write-Host "Exit Code      : $systemdExitCode" -ForegroundColor Red
    Write-Host "Collecting failed systemd units for diagnostics..." -ForegroundColor Red
    $failedUnits = & wsl.exe -d $TargetDistroName -u root -- systemctl --failed --no-pager 2>&1
    Write-Host ($failedUnits | Out-String) -ForegroundColor Red
    Write-Error "Systemd is not in an operational state ('running' [0] or 'degraded' [1]). Aborting."
    exit 1
}
Write-Host "  -> systemd operational state verified: '$cleanSystemdState' (exit code $systemdExitCode)." -ForegroundColor Green

# 4.4 Verify Ubuntu 24.04 Release Metadata
Write-Host "Verifying target OS release metadata..." -ForegroundColor Yellow
$osReleaseRaw = & wsl.exe -d $TargetDistroName -u root -- bash -c 'source /etc/os-release && echo "$ID $VERSION_ID"' 2>&1
$osReleaseExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $osReleaseExit -StepDescription "Read /etc/os-release"
$osRelease = ($osReleaseRaw | Out-String).Trim()

if ($osRelease -notmatch "ubuntu 24\.04") {
    Write-Error "Target OS is not Ubuntu 24.04. Reported: '$osRelease'. Aborting."
    exit 1
}
Write-Host "  -> Target OS verified: $osRelease" -ForegroundColor Green

# ==============================================================================
# Phase 5: User & Sudoers Configuration
# ==============================================================================
Write-Host "`nSetting up user '$AnsibleUser' and passwordless sudo..." -ForegroundColor Yellow
$userSudoScript = "
    set -e
    if ! id -u $AnsibleUser >/dev/null 2>&1; then
        useradd -m -s /bin/bash -G sudo $AnsibleUser
    fi
    echo '$AnsibleUser ALL=(ALL) NOPASSWD:ALL' > /etc/sudoers.d/ansible
    chmod 0440 /etc/sudoers.d/ansible
"
$userSudoOutput = & wsl.exe -d $TargetDistroName -u root -- bash -c $userSudoScript 2>&1
$userSudoExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $userSudoExit -StepDescription "Create user and configure sudoers"

# Validate sudoers syntax with visudo
Write-Host "Validating /etc/sudoers.d/ansible with visudo..." -ForegroundColor Yellow
$visudoOutput = & wsl.exe -d $TargetDistroName -u root -- visudo -cf /etc/sudoers.d/ansible 2>&1
$visudoExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $visudoExit -StepDescription "Validate sudoers with visudo"
Write-Host "  -> Sudoers syntax verified valid." -ForegroundColor Green

# ==============================================================================
# Phase 6: SSH Server Installation & Port 2222 Configuration
# ==============================================================================
Write-Host "`nConfiguring OpenSSH server on port $TargetSshPort..." -ForegroundColor Yellow
$sshdInstallScript = "
    set -e
    export DEBIAN_FRONTEND=noninteractive
    apt-get update -qq
    apt-get install -y -qq openssh-server
    ssh-keygen -A
    mkdir -p /etc/ssh/sshd_config.d
    cat << 'EOF' > /etc/ssh/sshd_config.d/ansible.conf
Port $TargetSshPort
PasswordAuthentication no
PubkeyAuthentication yes
AuthorizedKeysFile .ssh/authorized_keys
EOF
"
$sshdInstallOutput = & wsl.exe -d $TargetDistroName -u root -- bash -c $sshdInstallScript 2>&1
$sshdInstallExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $sshdInstallExit -StepDescription "Install and configure OpenSSH server"

# Validate sshd configuration syntax
Write-Host "Validating sshd configuration syntax (sshd -t)..." -ForegroundColor Yellow
$sshdTestOutput = & wsl.exe -d $TargetDistroName -u root -- sshd -t 2>&1
$sshdTestExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $sshdTestExit -StepDescription "Validate sshd configuration"

# Enable and start SSH service via systemctl
Write-Host "Enabling and starting ssh.service via systemctl..." -ForegroundColor Yellow
$sshStartOutput = & wsl.exe -d $TargetDistroName -u root -- systemctl enable --now ssh.service 2>&1
$sshStartExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $sshStartExit -StepDescription "Enable and start ssh.service"

# Verify TCP listener on port 2222
Write-Host "Verifying TCP listening socket on port $TargetSshPort..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
$listenerCheckRaw = & wsl.exe -d $TargetDistroName -u root -- ss -tlpn "sport = :$TargetSshPort" 2>&1
$listenerCheckExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $listenerCheckExit -StepDescription "Check TCP listener with ss"
$listenerOutput = ($listenerCheckRaw | Out-String).Trim()

if ([string]::IsNullOrWhiteSpace($listenerOutput) -or $listenerOutput -notmatch "\bLISTEN\b.*:$TargetSshPort\b") {
    Write-Error "sshd listener verification failed: no socket in LISTEN state on port $TargetSshPort. Output: '$listenerOutput'."
    exit 1
}
Write-Host "  -> Verified active TCP listener on port $TargetSshPort:" -ForegroundColor Green
Write-Host "     $listenerOutput" -ForegroundColor DarkGray

# ==============================================================================
# Phase 7: Authorized Key Injection via Base64 Stdin
# ==============================================================================
Write-Host "`nInjecting control public key into target authorized_keys..." -ForegroundColor Yellow

$pubKeyBase64 = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($controlPubKey))
$injectScript = "
    set -e
    mkdir -p /home/$AnsibleUser/.ssh
    chmod 700 /home/$AnsibleUser/.ssh
    echo '$pubKeyBase64' | base64 -d > /home/$AnsibleUser/.ssh/authorized_keys
    chmod 600 /home/$AnsibleUser/.ssh/authorized_keys
    chown -R $AnsibleUser:$AnsibleUser /home/$AnsibleUser/.ssh
"
$injectOutput = & wsl.exe -d $TargetDistroName -u root -- bash -c $injectScript 2>&1
$injectExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $injectExit -StepDescription "Inject authorized_keys via base64 stream"

# Verify content matches
$targetAuthKeyRaw = & wsl.exe -d $TargetDistroName -u root -- cat "/home/$AnsibleUser/.ssh/authorized_keys" 2>&1
$authReadExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $authReadExit -StepDescription "Read target authorized_keys"
$targetAuthKey = ($targetAuthKeyRaw | Out-String).Trim()

if ($targetAuthKey -cne $controlPubKey) {
    Write-Error "Integrity verification failed: Injected authorized_keys does not match control node public key! Aborting."
    exit 1
}

# Verify permissions & ownership
$authStatsRaw = & wsl.exe -d $TargetDistroName -u root -- stat -c "%a %U:%G" "/home/$AnsibleUser/.ssh/authorized_keys" 2>&1
$authStatExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $authStatExit -StepDescription "Stat authorized_keys"
$authStatsClean = ($authStatsRaw | Out-String).Trim()

if ($authStatsClean -ne "600 $AnsibleUser`:$AnsibleUser") {
    Write-Error "Permission/ownership check failed: Expected '600 $AnsibleUser`:$AnsibleUser', got '$authStatsClean'. Aborting."
    exit 1
}
Write-Host "  -> Public key verified (exact content, ownership, 0600 permissions)." -ForegroundColor Green

# ==============================================================================
# Phase 8: Strict Host-Key Fingerprint Verification & Known-Hosts Update
# ==============================================================================
Write-Host "`nVerifying ED25519 Host Key Authenticity via Fingerprint Comparison..." -ForegroundColor Yellow

# 8.1 Extract target host's actual ED25519 fingerprint from host key file
$targetFpRaw = & wsl.exe -d $TargetDistroName -u root -- ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub 2>&1
$targetFpExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $targetFpExit -StepDescription "Extract target ED25519 host key fingerprint"
$targetFingerprint = ($targetFpRaw | Out-String).Trim()
Write-Host "  -> Target host key fingerprint : $targetFingerprint" -ForegroundColor Cyan

# 8.2 Scan port 2222 once from control node
$scanScript = "ssh-keyscan -p $TargetSshPort -t ed25519 127.0.0.1 2>/dev/null"
$scannedKeyLineRaw = & wsl.exe -d $ControlDistroName -u ansible -- bash -c $scanScript 2>&1
$scanExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $scanExit -StepDescription "Scan target host key from control node"
$scannedKeyLine = ($scannedKeyLineRaw | Out-String).Trim()

if ([string]::IsNullOrWhiteSpace($scannedKeyLine) -or -not $scannedKeyLine.Contains("ssh-ed25519")) {
    Write-Error "Failed to scan ED25519 host key from 127.0.0.1:$TargetSshPort."
    exit 1
}

# 8.3 Compute fingerprint of that EXACT scanned line in the control node
$scannedKeyBase64 = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($scannedKeyLine))
$fpComputeScript = "echo '$scannedKeyBase64' | base64 -d | ssh-keygen -lf -"
$scannedFpRaw = & wsl.exe -d $ControlDistroName -u ansible -- bash -c $fpComputeScript 2>&1
$scannedFpExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $scannedFpExit -StepDescription "Compute fingerprint of scanned key"
$scannedFingerprint = ($scannedFpRaw | Out-String).Trim()
Write-Host "  -> Scanned key fingerprint    : $scannedFingerprint" -ForegroundColor Cyan

# Compare SHA256 digests
$targetSha = [regex]::Match($targetFingerprint, 'SHA256:[A-Za-z0-9+/=]+').Value
$scannedSha = [regex]::Match($scannedFingerprint, 'SHA256:[A-Za-z0-9+/=]+').Value

if (-not $targetSha -or -not $scannedSha -or ($targetSha -cne $scannedSha)) {
    Write-Error "SECURITY MISMATCH: Scanned host key fingerprint does not match target host key! Target: '$targetSha', Scanned: '$scannedSha'. Aborting."
    exit 1
}
Write-Host "  -> Fingerprints MATCH ($targetSha). Host key is authentic." -ForegroundColor Green

# 8.4 Write the EXACT verified scanned key line into known_hosts
Write-Host "Updating control node known_hosts with verified key..." -ForegroundColor Yellow
$knownHostsUpdateScript = "
    set -e
    mkdir -p ~/.ssh && chmod 700 ~/.ssh
    touch ~/.ssh/known_hosts && chmod 600 ~/.ssh/known_hosts
    ssh-keygen -f ~/.ssh/known_hosts -R '[127.0.0.1]:$TargetSshPort' 2>/dev/null || true
    echo '$scannedKeyBase64' | base64 -d >> ~/.ssh/known_hosts
    chmod 600 ~/.ssh/known_hosts
"
$knownHostsUpdateRaw = & wsl.exe -d $ControlDistroName -u ansible -- bash -c $knownHostsUpdateScript 2>&1
$knownHostsUpdateExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $knownHostsUpdateExit -StepDescription "Update control node known_hosts with verified key"

# Verify the entry is recognized for [127.0.0.1]:2222
$knownHostsCheckRaw = & wsl.exe -d $ControlDistroName -u ansible -- bash -c "ssh-keygen -F '[127.0.0.1]:$TargetSshPort' -f ~/.ssh/known_hosts" 2>&1
$knownHostsCheckExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $knownHostsCheckExit -StepDescription "Verify known_hosts entry exists"
Write-Host "  -> Control node known_hosts entry verified." -ForegroundColor Green

# ==============================================================================
# Phase 9: End-to-End Connectivity Self-Test with Strict Host Key Checking
# ==============================================================================
Write-Host "`nPerforming SSH & Sudo verification with StrictHostKeyChecking=yes..." -ForegroundColor Yellow
$sshVerifyScript = "ssh -o BatchMode=yes -o StrictHostKeyChecking=yes -p $TargetSshPort $AnsibleUser@127.0.0.1 'echo SSH_CONNECTION_OK && sudo id'"
$testResultRaw = & wsl.exe -d $ControlDistroName -u ansible -- bash -c $sshVerifyScript 2>&1
$testResultExit = $LASTEXITCODE
Assert-ExitCodeZero -ExitCode $testResultExit -StepDescription "Strict SSH self-test"
$testResult = ($testResultRaw | Out-String).Trim()

Write-Host "  -> Self-test output: $testResult" -ForegroundColor Green

if ($testResult -notmatch "SSH_CONNECTION_OK" -or $testResult -notmatch "uid=0\(root\)") {
    Write-Error "Verification failed: output does not confirm successful SSH login and sudo elevation."
    exit 1
}

Write-Host "`n=================================================================" -ForegroundColor Green
Write-Host " SUCCESS: '$TargetDistroName' has been cleanly bootstrapped!" -ForegroundColor Green
Write-Host " - Ubuntu 24.04 verified with systemd operational" -ForegroundColor Green
Write-Host " - SSH active on port $TargetSshPort, authenticated via ED25519" -ForegroundColor Green
Write-Host " - Target host key verified by SHA256 fingerprint matching" -ForegroundColor Green
Write-Host " - known_hosts updated with exact verified key" -ForegroundColor Green
Write-Host " - Passwordless sudo confirmed for '$AnsibleUser'" -ForegroundColor Green
Write-Host " - Ready for clean-node Ansible playbook execution" -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Green
