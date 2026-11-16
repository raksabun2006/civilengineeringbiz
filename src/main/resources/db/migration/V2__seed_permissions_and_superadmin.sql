-- =============================================================================
-- Flyway Database Migration: V2__seed_permissions_and_superadmin.sql
-- System: EngineerBiz - Civil Engineering Business Management System
-- Target Database: PostgreSQL 17
-- =============================================================================

-- 1. SEED SYSTEM PERMISSIONS
INSERT INTO permissions (id, code, name, module, description) VALUES
    (gen_random_uuid(), 'USER_VIEW', 'View Users', 'IAM', 'Can view user accounts and profile listings'),
    (gen_random_uuid(), 'USER_CREATE', 'Create User', 'IAM', 'Can register new staff user accounts'),
    (gen_random_uuid(), 'USER_UPDATE', 'Update User', 'IAM', 'Can modify user details and permissions'),
    (gen_random_uuid(), 'USER_DELETE', 'Delete User', 'IAM', 'Can deactivate or soft-delete user accounts'),
    (gen_random_uuid(), 'USER_MANAGE', 'Manage IAM', 'IAM', 'Full administrative control over user accounts and security'),

    (gen_random_uuid(), 'COMPANY_VIEW', 'View Company Profile', 'COMPANY', 'Can view corporate registration and banking details'),
    (gen_random_uuid(), 'COMPANY_UPDATE', 'Update Company Profile', 'COMPANY', 'Can modify corporate registration and banking details'),

    (gen_random_uuid(), 'CLIENT_VIEW', 'View Clients', 'CLIENT', 'Can view client directory and contact records'),
    (gen_random_uuid(), 'CLIENT_CREATE', 'Create Client', 'CLIENT', 'Can register new clients and corporate accounts'),
    (gen_random_uuid(), 'CLIENT_UPDATE', 'Update Client', 'CLIENT', 'Can update client details and payment terms'),
    (gen_random_uuid(), 'CLIENT_DELETE', 'Delete Client', 'CLIENT', 'Can soft-delete client accounts'),

    (gen_random_uuid(), 'SERVICE_VIEW', 'View Services', 'SERVICE', 'Can view engineering service catalog and rates'),
    (gen_random_uuid(), 'SERVICE_CREATE', 'Create Service', 'SERVICE', 'Can add new civil engineering services to catalog'),
    (gen_random_uuid(), 'SERVICE_UPDATE', 'Update Service', 'SERVICE', 'Can modify engineering service details and pricing'),
    (gen_random_uuid(), 'SERVICE_DELETE', 'Delete Service', 'SERVICE', 'Can soft-delete engineering services'),

    (gen_random_uuid(), 'PROJECT_VIEW', 'View Projects', 'PROJECT', 'Can view project records, progress, and members'),
    (gen_random_uuid(), 'PROJECT_CREATE', 'Create Project', 'PROJECT', 'Can initiate and create new engineering projects'),
    (gen_random_uuid(), 'PROJECT_UPDATE', 'Update Project', 'PROJECT', 'Can update project details, members, and log site progress'),
    (gen_random_uuid(), 'PROJECT_DELETE', 'Delete Project', 'PROJECT', 'Can soft-delete project records'),

    (gen_random_uuid(), 'ESTIMATE_VIEW', 'View BOQ Estimates', 'ESTIMATION', 'Can view cost estimates and detailed BOQ breakdowns'),
    (gen_random_uuid(), 'ESTIMATE_CREATE', 'Create BOQ Estimate', 'ESTIMATION', 'Can create BOQ cost estimates for projects'),
    (gen_random_uuid(), 'ESTIMATE_UPDATE', 'Update BOQ Estimate', 'ESTIMATION', 'Can update rate breakdowns and itemized quantities'),
    (gen_random_uuid(), 'ESTIMATE_APPROVE', 'Approve BOQ Estimate', 'ESTIMATION', 'Can approve draft estimates into official cost baselines'),

    (gen_random_uuid(), 'QUOTATION_VIEW', 'View Quotations', 'QUOTATION', 'Can view client price quotations'),
    (gen_random_uuid(), 'QUOTATION_CREATE', 'Create Quotation', 'QUOTATION', 'Can generate new quotations from estimates'),
    (gen_random_uuid(), 'QUOTATION_UPDATE', 'Update Quotation', 'QUOTATION', 'Can revise quotations and change statuses'),
    (gen_random_uuid(), 'QUOTATION_APPROVE', 'Approve Quotation', 'QUOTATION', 'Can approve quotations for client delivery'),

    (gen_random_uuid(), 'CONTRACT_VIEW', 'View Contracts', 'CONTRACT', 'Can view signed contracts and variation orders'),
    (gen_random_uuid(), 'CONTRACT_CREATE', 'Create Contract', 'CONTRACT', 'Can create formal legal contracts from quotations'),
    (gen_random_uuid(), 'CONTRACT_UPDATE', 'Update Contract', 'CONTRACT', 'Can update contract terms and propose variation orders'),
    (gen_random_uuid(), 'CONTRACT_APPROVE', 'Approve Variation Orders', 'CONTRACT', 'Can approve variation orders adjusting contract scope and value'),

    (gen_random_uuid(), 'FINANCE_VIEW', 'View Financial Records', 'FINANCE', 'Can view invoices, payment receipts, and site expenses'),
    (gen_random_uuid(), 'FINANCE_CREATE', 'Create Invoices & Expenses', 'FINANCE', 'Can issue milestone invoices and submit petty cash claims'),
    (gen_random_uuid(), 'FINANCE_UPDATE', 'Update Financial Records', 'FINANCE', 'Can record payment receipts and modify invoices'),
    (gen_random_uuid(), 'FINANCE_APPROVE', 'Approve Payments & Expenses', 'FINANCE', 'Can approve expenses for reimbursement and void invoices'),

    (gen_random_uuid(), 'INVENTORY_VIEW', 'View Materials & Fleet', 'INVENTORY', 'Can view stock inventory, ledger, and equipment status'),
    (gen_random_uuid(), 'INVENTORY_CREATE', 'Register Materials & Plant', 'INVENTORY', 'Can register new material stock and machinery'),
    (gen_random_uuid(), 'INVENTORY_UPDATE', 'Manage Stock & Deployments', 'INVENTORY', 'Can dispatch materials to sites and deploy equipment'),
    (gen_random_uuid(), 'INVENTORY_DELETE', 'Decommission Equipment', 'INVENTORY', 'Can decommission assets and soft-delete materials'),

    (gen_random_uuid(), 'DOCUMENT_VIEW', 'View Drawings & Documents', 'DOCUMENT', 'Can access engineering drawings, calculation sheets, and permits'),
    (gen_random_uuid(), 'DOCUMENT_CREATE', 'Upload Documents', 'DOCUMENT', 'Can upload new drawings and attachments'),
    (gen_random_uuid(), 'DOCUMENT_UPDATE', 'Update Document Revisions', 'DOCUMENT', 'Can update drawing revision levels and metadata'),
    (gen_random_uuid(), 'DOCUMENT_DELETE', 'Delete Documents', 'DOCUMENT', 'Can soft-delete technical documentation'),

    (gen_random_uuid(), 'AUDIT_VIEW', 'View Audit Trail', 'AUDIT', 'Can view immutable compliance audit logs');

-- 2. SEED DEFAULT SUPERADMIN USER
-- Default credentials: admin@engineerbiz.com / Admin@123456
INSERT INTO users (
    id, email, password_hash, full_name, phone, job_title, status
) VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin@engineerbiz.com',
    crypt('Admin@123456', gen_salt('bf', 12)),
    'Managing Director',
    '+855 12 999 888',
    'Principal Executive Engineer',
    'ACTIVE'
);

-- 3. ASSIGN ALL PERMISSIONS TO SUPERADMIN USER
INSERT INTO user_permissions (id, user_id, permission_id, granted_by)
SELECT
    gen_random_uuid(),
    'a0000000-0000-0000-0000-000000000001',
    p.id,
    'a0000000-0000-0000-0000-000000000001'
FROM permissions p;

-- 4. SEED DEFAULT COMPANY PROFILE
INSERT INTO company_profile (
    id, company_name_en, company_name_km, registration_number, vat_tin,
    email, phone, website, address_line1, city, country, default_currency,
    bank_name, bank_account_name, bank_account_number, bank_swift_code
) VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'EngineerBiz Engineering & Construction Co., Ltd.',
    'អេងជីនារប៊ីស អេនជីនារីង និង សំណង់ ឯ.ក',
    '00012345/2026',
    'K001-902600123',
    'contact@engineerbiz.com.kh',
    '+855 23 888 999',
    'https://www.engineerbiz.com.kh',
    '#168 Preah Monivong Blvd, Boeung Keng Kang 1',
    'Phnom Penh',
    'Cambodia',
    'USD',
    'Advanced Bank of Asia (ABA)',
    'ENGINEERBIZ CO LTD',
    '000 123 456',
    'ABARKHPP'
);
