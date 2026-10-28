package com.example.leaseworkflow.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DemoDocumentGeneratorTest {

    @Test
    void generateDemoDocuments() throws IOException {
        File demoDir = new File("demo-documents");
        if (!demoDir.exists()) {
            demoDir.mkdirs();
        }

        // 1. Clean Commercial Lease (Low Risk, Happy Path)
        File doc1 = new File(demoDir, "01_Commercial_Lease_Agreement_AcmeCorp.pdf");
        createCommercialLease(doc1);
        assertTrue(doc1.exists());

        // 2. Retail Lease with Discrepancy (Elevated Risk, Reviewer Request Changes)
        File doc2 = new File(demoDir, "02_Retail_Lease_Flagged_Discrepancy.pdf");
        createDiscrepantRetailLease(doc2);
        assertTrue(doc2.exists());

        // 3. Clean Residential Lease (Low Risk, Residential Onboarding)
        File doc3 = new File(demoDir, "03_Residential_Tenancy_Agreement_Skyline.pdf");
        createResidentialLease(doc3);
        assertTrue(doc3.exists());

        // 4. Floor Plan Addendum Image (Multi-file upload demo)
        File doc4 = new File(demoDir, "04_Storefront_Floor_Plan_Addendum.png");
        createFloorPlanImage(doc4);
        assertTrue(doc4.exists());

        // Verify that DocumentIntelligenceService extracts terms and assesses risk accurately
        com.example.leaseworkflow.service.DocumentIntelligenceService ai = new com.example.leaseworkflow.service.DocumentIntelligenceService();

        byte[] b1 = java.nio.file.Files.readAllBytes(doc1.toPath());
        var a1 = ai.analyze(doc1.getName(), b1, "Commercial");
        org.junit.jupiter.api.Assertions.assertEquals("$4,500.00", a1.extractedRent());
        org.junit.jupiter.api.Assertions.assertEquals("$9,000.00", a1.extractedDeposit());
        org.junit.jupiter.api.Assertions.assertEquals("24 Months", a1.extractedTerm());
        org.junit.jupiter.api.Assertions.assertEquals("LOW", a1.riskLevel());

        byte[] b2 = java.nio.file.Files.readAllBytes(doc2.toPath());
        var a2 = ai.analyze(doc2.getName(), b2, "Commercial");
        org.junit.jupiter.api.Assertions.assertEquals("$2,800.00", a2.extractedRent());
        org.junit.jupiter.api.Assertions.assertEquals("12 Months", a2.extractedTerm());
        org.junit.jupiter.api.Assertions.assertEquals("MEDIUM", a2.riskLevel());

        byte[] b3 = java.nio.file.Files.readAllBytes(doc3.toPath());
        var a3 = ai.analyze(doc3.getName(), b3, "Residential");
        org.junit.jupiter.api.Assertions.assertEquals("$1,850.00", a3.extractedRent());
        org.junit.jupiter.api.Assertions.assertEquals("$2,500.00", a3.extractedDeposit());
        org.junit.jupiter.api.Assertions.assertEquals("12 Months", a3.extractedTerm());
        org.junit.jupiter.api.Assertions.assertEquals("LOW", a3.riskLevel());
    }

    private void createCommercialLease(File target) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 790;

                // Header Banner
                cs.setNonStrokingColor(new Color(15, 23, 42)); // Navy
                cs.addRect(40, y - 45, 515, 50);
                cs.fill();

                cs.beginText();
                cs.setFont(fontBold, 15);
                cs.setNonStrokingColor(Color.WHITE);
                cs.newLineAtOffset(55, y - 20);
                cs.showText("COMMERCIAL PROPERTY LEASE AGREEMENT");
                cs.endText();

                cs.beginText();
                cs.setFont(fontRegular, 8);
                cs.setNonStrokingColor(new Color(148, 163, 184));
                cs.newLineAtOffset(55, y - 35);
                cs.showText("STANDARD ENTERPRISE REVENUE LEASE - STATE OF CALIFORNIA JURISDICTION");
                cs.endText();

                y -= 75;

                // Section: Document Metadata
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("1. PARTIES & PREMISES SUMMARY");
                cs.endText();

                y -= 16;
                String[] parties = {
                    "Landlord: Horizon Commercial Real Estate Holdings LLC (Tax ID: US-994821)",
                    "Tenant / Entity: Acme Logistics Global Corporation (ID: EMP-2026-ACME)",
                    "Premises: Suite 400, Horizon Business Center, 500 Enterprise Way, San Jose, CA",
                    "Permitted Commercial Use: Corporate offices, technology research, logistics operations"
                };
                for (String p : parties) {
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.setNonStrokingColor(new Color(51, 65, 85));
                    cs.newLineAtOffset(60, y);
                    cs.showText(p);
                    cs.endText();
                    y -= 14;
                }

                y -= 10;
                // Section: Core Financial Terms (Triggers DocumentIntelligenceService parser)
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("2. CORE FINANCIAL & OCCUPANCY TERMS (VERIFIED)");
                cs.endText();

                y -= 16;
                String[] terms = {
                    "Lease Term: 24 Months (Commencing 2026-11-01 through 2028-10-31)",
                    "Monthly Rent: $4,500.00 (Due promptly on the 1st day of each billing calendar month)",
                    "Security Deposit: $9,000.00 (Guaranteed escrow deposit held with Chase Corporate Trust)",
                    "Late Fee Clause: 5% assessed on outstanding balances exceeding 5 calendar days grace period"
                };
                for (String t : terms) {
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.setNonStrokingColor(new Color(15, 23, 42));
                    cs.newLineAtOffset(60, y);
                    cs.showText(t);
                    cs.endText();
                    y -= 14;
                }

                y -= 10;
                // Section: Legal Covenants & Indemnification
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("3. MANDATORY COVENANTS, INDEMNITY & DEFAULT PROCEDURES");
                cs.endText();

                y -= 16;
                String[] covenants = {
                    "Clause 4.1 Indemnification & Liability Allocation: Tenant shall indemnify, defend, and hold harmless",
                    "Landlord, its officers, directors, agents, and employees against any and all claims, liabilities, costs,",
                    "or damages arising out of tenant's occupancy or operations upon the premises.",
                    "",
                    "Clause 6.3 Commercial Insurance Covenant: Comprehensive General Liability coverage of at least",
                    "$2,000,000 per occurrence naming Landlord as an additional insured certificate holder.",
                    "",
                    "Clause 8.2 Default & Termination Procedures: In the event of monetary or covenant default,",
                    "Landlord shall provide 10 business days written notice of default to cure before termination proceedings."
                };
                for (String c : covenants) {
                    if (c.isEmpty()) {
                        y -= 6;
                        continue;
                    }
                    cs.beginText();
                    cs.setFont(fontRegular, 8.5f);
                    cs.setNonStrokingColor(new Color(71, 85, 105));
                    cs.newLineAtOffset(60, y);
                    cs.showText(c);
                    cs.endText();
                    y -= 13;
                }

                y -= 20;
                // Signatures Block
                cs.beginText();
                cs.setFont(fontBold, 10);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("4. SIGNATURES & EXECUTION");
                cs.endText();

                y -= 25;
                cs.beginText();
                cs.setFont(fontOblique, 9);
                cs.setNonStrokingColor(new Color(100, 116, 139));
                cs.newLineAtOffset(60, y);
                cs.showText("Executed by Authorized Legal Representatives on October 28, 2026:");
                cs.endText();

                y -= 25;
                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(60, y);
                cs.showText("Tenant Signatory: Robert Sterling, VP Operations");
                cs.endText();

                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.newLineAtOffset(320, y);
                cs.showText("Landlord Signatory: Elena Rostova, Asset Director");
                cs.endText();

                y -= 35;
                cs.setNonStrokingColor(new Color(226, 232, 240));
                cs.addRect(40, y, 515, 1);
                cs.fill();

                y -= 15;
                cs.beginText();
                cs.setFont(fontRegular, 7.5f);
                cs.setNonStrokingColor(new Color(148, 163, 184));
                cs.newLineAtOffset(50, y);
                cs.showText("Document ID: DOC-2026-ACME-8849 | SHA-256 Validated | Automated AI Compliance Ready");
                cs.endText();
            }
            doc.save(target);
        }
    }

    private void createDiscrepantRetailLease(File target) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 790;

                // Header Banner
                cs.setNonStrokingColor(new Color(180, 83, 9)); // Amber / Warning
                cs.addRect(40, y - 45, 515, 50);
                cs.fill();

                cs.beginText();
                cs.setFont(fontBold, 15);
                cs.setNonStrokingColor(Color.WHITE);
                cs.newLineAtOffset(55, y - 20);
                cs.showText("RETAIL STOREFRONT LEASE AGREEMENT (DRAFT)");
                cs.endText();

                cs.beginText();
                cs.setFont(fontRegular, 8);
                cs.setNonStrokingColor(new Color(254, 243, 199));
                cs.newLineAtOffset(55, y - 35);
                cs.showText("REVIEW DRAFT - INTENDED FOR COMMERCIAL RETAIL SPACE - INSPECTION REQUIRED");
                cs.endText();

                y -= 75;

                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("1. PROPOSED LEASE PARTICULARS");
                cs.endText();

                y -= 16;
                String[] parties = {
                    "Landlord: Westfield Promenade Properties LLC",
                    "Applicant: GreenLeaf Artisan Cafe LLC (ID: EMP-2026-GLC)",
                    "Premises: Unit 12B, Ground Floor Retail Walk, 2200 Grand Ave",
                    "Declared Category: Commercial Lease"
                };
                for (String p : parties) {
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.setNonStrokingColor(new Color(51, 65, 85));
                    cs.newLineAtOffset(60, y);
                    cs.showText(p);
                    cs.endText();
                    y -= 14;
                }

                y -= 10;
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("2. PROPOSED FINANCIAL TERMS");
                cs.endText();

                y -= 16;
                String[] terms = {
                    "Lease Term: 12 Months",
                    "Monthly Rent: $2,800.00 (Payable monthly in advance)",
                    "Deposit Terms: Security deposit to be determined following officer risk assessment."
                };
                for (String t : terms) {
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.setNonStrokingColor(new Color(15, 23, 42));
                    cs.newLineAtOffset(60, y);
                    cs.showText(t);
                    cs.endText();
                    y -= 14;
                }

                y -= 10;
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("3. SPECIAL COVENANTS & CLAUSES (FLAGGED DISCREPANCY)");
                cs.endText();

                y -= 16;
                // Intentionally contains "residential tenancy" to trigger AI discrepancy detection!
                String[] warnings = {
                    "Section 4.1 Permitted Storefront Activities: Cafe service, packaged goods, and acoustic seating.",
                    "",
                    "Section 7.4 Maintenance: Tenant handles interior fixtures; common area costs prorated monthly.",
                    "",
                    "Section 9.2 Residential Tenancy Protection Provision (DISCREPANCY):",
                    "The parties stipulate that residential tenancy peace, domestic tenant habitability standards, and",
                    "residential rent stabilization statutory ordinances shall govern the premises.",
                    "",
                    "Section 11. Termination & Default Notice: 15 business days written notice of default."
                };
                for (String w : warnings) {
                    if (w.isEmpty()) {
                        y -= 6;
                        continue;
                    }
                    cs.beginText();
                    cs.setFont(w.contains("DISCREPANCY") ? fontBold : fontRegular, 8.5f);
                    cs.setNonStrokingColor(w.contains("DISCREPANCY") ? new Color(180, 83, 9) : new Color(71, 85, 105));
                    cs.newLineAtOffset(60, y);
                    cs.showText(w);
                    cs.endText();
                    y -= 13;
                }

                y -= 25;
                cs.beginText();
                cs.setFont(fontOblique, 8.5f);
                cs.setNonStrokingColor(new Color(148, 163, 184));
                cs.newLineAtOffset(60, y);
                cs.showText("[Officer Attention: Missing commercial indemnification and mismatched residential clause]");
                cs.endText();

                y -= 35;
                cs.setNonStrokingColor(new Color(226, 232, 240));
                cs.addRect(40, y, 515, 1);
                cs.fill();

                y -= 15;
                cs.beginText();
                cs.setFont(fontRegular, 7.5f);
                cs.setNonStrokingColor(new Color(148, 163, 184));
                cs.newLineAtOffset(50, y);
                cs.showText("Draft ID: DRAFT-2026-RETAIL-402 | Intended for GSD Compliance Inspection Audit");
                cs.endText();
            }
            doc.save(target);
        }
    }

    private void createResidentialLease(File target) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 790;

                // Header Banner
                cs.setNonStrokingColor(new Color(5, 150, 105)); // Emerald Green
                cs.addRect(40, y - 45, 515, 50);
                cs.fill();

                cs.beginText();
                cs.setFont(fontBold, 15);
                cs.setNonStrokingColor(Color.WHITE);
                cs.newLineAtOffset(55, y - 20);
                cs.showText("STANDARD RESIDENTIAL LEASE AGREEMENT");
                cs.endText();

                cs.beginText();
                cs.setFont(fontRegular, 8);
                cs.setNonStrokingColor(new Color(209, 250, 229));
                cs.newLineAtOffset(55, y - 35);
                cs.showText("MULTI-FAMILY RESIDENTIAL APARTMENT LEASE - CERTIFIED RESIDENTIAL ONBOARDING");
                cs.endText();

                y -= 75;

                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("1. RESIDENCE & RESIDENT PARTICULARS");
                cs.endText();

                y -= 16;
                String[] parties = {
                    "Landlord / Property Manager: Skyline Towers Realty Management Inc.",
                    "Resident Applicant: Dr. David Miller (ID: TEN-2026-8891)",
                    "Dwelling Unit: Apartment 14C, Skyline Residential Tower, 1200 Pacific Heights Blvd",
                    "Occupancy Type: Private Single-Family Residential Tenancy"
                };
                for (String p : parties) {
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.setNonStrokingColor(new Color(51, 65, 85));
                    cs.newLineAtOffset(60, y);
                    cs.showText(p);
                    cs.endText();
                    y -= 14;
                }

                y -= 10;
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("2. FINANCIAL OBLIGATIONS & OCCUPANCY TERM");
                cs.endText();

                y -= 16;
                String[] terms = {
                    "Lease Term: 12 Months (Commencing 2026-11-15 through 2027-11-14)",
                    "Monthly Rent: $1,850.00 (Due on the first calendar day of each month)",
                    "Security Deposit: $2,500.00 (Held in insured residential tenant escrow account)",
                    "Utilities: Water, trash, and sewer included in base monthly rent"
                };
                for (String t : terms) {
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.setNonStrokingColor(new Color(15, 23, 42));
                    cs.newLineAtOffset(60, y);
                    cs.showText(t);
                    cs.endText();
                    y -= 14;
                }

                y -= 10;
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(50, y);
                cs.showText("3. RESIDENTIAL RULES, INDEMNITY & DEFAULT PROCEDURES");
                cs.endText();

                y -= 16;
                String[] covenants = {
                    "Clause 5.1 Indemnification: Resident shall indemnify Landlord from claims arising from guest negligence.",
                    "",
                    "Clause 7.2 Quiet Enjoyment & Pet Guidelines: Domestic pets under 35 lbs permitted with pet addendum.",
                    "",
                    "Clause 9.1 Default & Termination: Either party may provide 30-day written notice of default to cure."
                };
                for (String c : covenants) {
                    if (c.isEmpty()) {
                        y -= 6;
                        continue;
                    }
                    cs.beginText();
                    cs.setFont(fontRegular, 8.5f);
                    cs.setNonStrokingColor(new Color(71, 85, 105));
                    cs.newLineAtOffset(60, y);
                    cs.showText(c);
                    cs.endText();
                    y -= 13;
                }

                y -= 25;
                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.setNonStrokingColor(new Color(15, 23, 42));
                cs.newLineAtOffset(60, y);
                cs.showText("Resident Signature: Dr. David Miller");
                cs.endText();

                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.newLineAtOffset(320, y);
                cs.showText("Manager Signature: Clara Oswald, CPM");
                cs.endText();

                y -= 35;
                cs.setNonStrokingColor(new Color(226, 232, 240));
                cs.addRect(40, y, 515, 1);
                cs.fill();

                y -= 15;
                cs.beginText();
                cs.setFont(fontRegular, 7.5f);
                cs.setNonStrokingColor(new Color(148, 163, 184));
                cs.newLineAtOffset(50, y);
                cs.showText("Document ID: RES-2026-SKYLINE-14C | Verified Tenant Residential Agreement");
                cs.endText();
            }
            doc.save(target);
        }
    }

    private void createFloorPlanImage(File target) throws IOException {
        int width = 800;
        int height = 500;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark background matching blueprint
        g.setColor(new Color(15, 23, 42));
        g.fillRect(0, 0, width, height);

        // Header
        g.setColor(new Color(59, 130, 246));
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("EXHIBIT B: COMMERCIAL STOREFRONT FLOOR PLAN & SPACE SPECIFICATIONS", 30, 40);

        g.setColor(new Color(148, 163, 184));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("Lease Request Addendum — Space 400 | Net Leasable Area: 3,450 Sq. Ft.", 30, 65);

        // Blueprint grid
        g.setColor(new Color(30, 41, 59));
        for (int x = 30; x < width - 30; x += 40) {
            g.drawLine(x, 90, x, height - 50);
        }
        for (int y = 90; y < height - 50; y += 40) {
            g.drawLine(30, y, width - 30, y);
        }

        // Main Room Box
        g.setColor(new Color(99, 102, 241));
        g.drawRect(50, 110, 440, 310);

        g.setColor(new Color(99, 102, 241, 40));
        g.fillRect(50, 110, 440, 310);

        // Conference Room
        g.setColor(new Color(16, 185, 129));
        g.drawRect(510, 110, 240, 140);
        g.setColor(new Color(16, 185, 129, 40));
        g.fillRect(510, 110, 240, 140);

        // Storage / Server Room
        g.setColor(new Color(245, 158, 11));
        g.drawRect(510, 270, 240, 150);
        g.setColor(new Color(245, 158, 11, 40));
        g.fillRect(510, 270, 240, 150);

        // Labels
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Open Workstations & Reception Area", 120, 260);

        g.drawString("Executive Conference", 540, 180);
        g.drawString("Server & IT Storage", 550, 350);

        // Footer stamp
        g.setColor(new Color(52, 211, 153));
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.drawString("APPROVED ARCHITECTURAL ATTACHMENT -- SIGNED & VERIFIED", 30, height - 20);

        g.dispose();
        ImageIO.write(img, "png", target);
    }
}
