# Week 13 — Target Node Configuration Specification

## 1. Overview & Architecture

The objective of Week 13 is to define and enforce all server prerequisites on a target node (`lease-target`) using an automated, idempotent Ansible playbook. The target node hosts the **Lease Document Approval Workflow** application containerized with embedded Tomcat (port 8081), fronted by an Nginx reverse proxy (port 80), and supported by an internal MySQL 8 container (port 3306, non-published).

### Architecture Diagram

```
+---------------------------------------------------------------------------------+
| Target Node: lease-target (Ubuntu 24.04 on WSL2)                                 |
|                                                                                 |
|   Port 80 (Public / All interfaces)                                             |
|        │                                                                        |
|        ▼                                                                        |
|   +──────────────+      Proxy Pass (127.0.0.1:8081)                             |
|   |    Nginx     | ────────────────────────────────────┐                        |
|   +──────────────+                                     │                        |
|                                                        ▼                        |
|   Bridge Network: lease-net                +───────────────────────+            |
|   (Internal container communication)       | lease-workflow-app    |            |
|                                            | (Container port 8081) |            |
|                                            +───────────────────────+            |
|                                                        │                        |
|                                                        │ JDBC                   |
|                                                        │ (lease-mysql:3306)     |
|                                                        ▼                        |
|                                            +───────────────────────+            |
|                                            | lease-mysql (MySQL 8) |            |
|                                            | Volume: lease-mysql-  |            |
|                                            |         data          |            |
|                                            | Port 3306: UNPUBLISHED|            |
|                                            +───────────────────────+            |
+---------------------------------------------------------------------------------+
```

---

## 2. Server Prerequisites & Enforcement Mapping

The following table specifies every server prerequisite and the corresponding Ansible module enforcing it idempotently.

| Category | Prerequisite Item | Target State / Attributes | Enforcing Ansible Module |
| :--- | :--- | :--- | :--- |
| **Package Repositories** | Docker Official APT Key & Repo | GPG keyring installed, repo added to `/etc/apt/sources.list.d/docker.list` | `ansible.builtin.get_url`, `ansible.builtin.apt_repository` |
| **System Packages** | Docker Engine & CLI | `docker-ce`, `docker-ce-cli`, `containerd.io`, `docker-buildx-plugin`, `docker-compose-plugin` (state: present) | `ansible.builtin.apt` |
| **System Packages** | Web Server & Utilities | `nginx`, `curl`, `ca-certificates`, `gnupg`, `python3-docker` (or virtualenv/pip for docker SDK) | `ansible.builtin.apt` |
| **User & Groups** | Docker Group | Group `docker` exists | `ansible.builtin.group` |
| **User & Groups** | Application Service User | User `leaseapp`: system account, no interactive login (`/usr/sbin/nologin`), home `/opt/lease-workflow`, member of `docker` group | `ansible.builtin.user` |
| **Directories** | Application Base & Config | `/opt/lease-workflow`, `/opt/lease-workflow/config`, `/opt/lease-workflow/logs` (owner: `leaseapp`, group: `leaseapp`, mode: `0750`) | `ansible.builtin.file` |
| **Directories** | System Log Directory | `/var/log/lease-workflow` (owner: `leaseapp`, group: `leaseapp`, mode: `0755`) | `ansible.builtin.file` |
| **Files & Templates** | Environment File | `/opt/lease-workflow/config/app.env` containing DB credentials, Spring profile (`prod`), Server Port (`8081`). Owner: `leaseapp:leaseapp`, permissions: `0600` | `ansible.builtin.template` |
| **Files & Templates** | Nginx Site Configuration | `/etc/nginx/sites-available/lease-workflow` proxying `/` to `http://127.0.0.1:8081` with proxy headers. Mode: `0644`, owner: `root:root` | `ansible.builtin.template` |
| **Files & Links** | Nginx Site Enablement | Symlink `/etc/nginx/sites-enabled/lease-workflow` pointing to `/etc/nginx/sites-available/lease-workflow` | `ansible.builtin.file` |
| **Files & Cleanup** | Nginx Default Site Removal | `/etc/nginx/sites-enabled/default` absent | `ansible.builtin.file` |
| **Services** | Docker Daemon | Service `docker` enabled and running | `ansible.builtin.systemd` |
| **Services** | Nginx Web Server | Service `nginx` enabled and started; reloaded on config change | `ansible.builtin.systemd` |
| **Containers** | Docker Bridge Network | Dedicated bridge network `lease-net` created | `community.docker.docker_network` |
| **Containers** | Persistent Volume | Named volume `lease-mysql-data` created | `community.docker.docker_volume` |
| **Containers** | MySQL 8 Container | Container `lease-mysql` running `mysql:8.0` on `lease-net`, volume attached to `/var/lib/mysql`, port 3306 internal only (NOT published to host), health check enabled | `community.docker.docker_container` |
| **Containers** | Application Container | Container `lease-workflow-app` running `prathmeshn2605/lease-workflow-app:{{ image_tag }}`, env file `/opt/lease-workflow/config/app.env`, connected to `lease-net`, published port `127.0.0.1:8081:8081` | `community.docker.docker_container` |
| **Verification** | Actuator Health Check | HTTP GET `http://127.0.0.1/actuator/health` via Nginx, status code 200, status `UP`, DB `UP` | `ansible.builtin.uri` |

---

## 3. Network & Port Security Matrix

| Component | Port | Binding | Exposure | Justification |
| :--- | :--- | :--- | :--- | :--- |
| **Nginx Reverse Proxy** | `80` | `0.0.0.0:80` | Host / Public | Primary public HTTP entry point for users and health monitoring |
| **Spring Boot App** | `8081` | `127.0.0.1:8081` | Loopback Only | Restricted to local loopback; direct external access bypassed to enforce Nginx proxy rules |
| **MySQL Database** | `3306` | Internal Docker Network (`lease-net`) | Container-only | **Zero host publishing**; only accessible to containers joined to `lease-net` |
| **SSH Daemon** | `2222` | `0.0.0.0:2222` | Host / WSL Bridge | Custom non-default port to prevent collision with control distro SSH port in shared WSL2 network namespace |

---

## 4. Application Environment Configuration (`app.env`)

The application container reads configuration from `/opt/lease-workflow/config/app.env`:

```properties
SERVER_PORT=8081
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://lease-mysql:3306/lease_workflow?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=leaseapp
SPRING_DATASOURCE_PASSWORD={{ vault_db_password }}
```

- **File Mode**: `0600`
- **Owner**: `leaseapp:leaseapp`
- **Secrets Management**: DB root and user passwords managed via `ansible-vault`.

---

## 5. Nginx Reverse Proxy Configuration

Nginx configuration `/etc/nginx/sites-available/lease-workflow`:

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name _;

    client_max_body_size 50M;

    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 6. Target Node Reachability & Systemd Requirements

- **WSL Distribution**: `lease-target` (Ubuntu 24.04 LTS).
- **Systemd Enabled**: Enabled via `/etc/wsl.conf`:
  ```ini
  [boot]
  systemd=true
  ```
- **SSH Service**: `openssh-server` running on port 2222 with ed25519 public key authentication for the `ansible` user with passwordless `sudo` rights.
- **Ansible Control Node**: Existing WSL2 Ubuntu distro with Ansible and `community.docker` installed.
