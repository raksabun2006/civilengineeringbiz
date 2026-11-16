-- =============================================================================
-- Flyway Database Migration: V1__init_schema.sql
-- System: EngineerBiz - Civil Engineering Business Management System
-- Target Database: PostgreSQL 17
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- 1. COMPANY MANAGEMENT
-- =============================================================================
CREATE TABLE company_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    company_name_en VARCHAR(150) NOT NULL,
    company_name_km VARCHAR(150),
    registration_number VARCHAR(50),
    vat_tin VARCHAR(50),
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    website VARCHAR(150),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL DEFAULT 'Phnom Penh',
    country VARCHAR(60) NOT NULL DEFAULT 'Cambodia',
    default_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    logo_url VARCHAR(500),
    bank_name VARCHAR(100),
    bank_account_name VARCHAR(100),
    bank_account_number VARCHAR(50),
    bank_swift_code VARCHAR(20),
    bakong_id VARCHAR(100)
);

-- =============================================================================
-- 2. IDENTITY & ACCESS MANAGEMENT
-- =============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    job_title VARCHAR(100),
    avatar_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMPTZ,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_status ON users(status) WHERE deleted_at IS NULL;

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uk_permissions_code UNIQUE (code)
);

CREATE INDEX idx_permissions_module ON permissions(module);

CREATE TABLE user_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by UUID,
    CONSTRAINT uk_user_permissions_user_perm UNIQUE (user_id, permission_id),
    CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_permissions_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- =============================================================================
-- 3. CLIENT MANAGEMENT
-- =============================================================================
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    client_code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    name_khmer VARCHAR(150),
    client_type VARCHAR(30) NOT NULL DEFAULT 'CORPORATE',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    vat_tin VARCHAR(50),
    primary_contact_name VARCHAR(100),
    primary_contact_email VARCHAR(150),
    primary_contact_phone VARCHAR(30),
    billing_address VARCHAR(255),
    city VARCHAR(100),
    payment_term_days INT DEFAULT 30,
    credit_limit NUMERIC(15,2),
    notes TEXT,
    CONSTRAINT uk_clients_code UNIQUE (client_code)
);

CREATE INDEX idx_clients_status ON clients(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_clients_name ON clients(name) WHERE deleted_at IS NULL;

-- =============================================================================
-- 4. ENGINEERING SERVICES
-- =============================================================================
CREATE TABLE engineering_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(40) NOT NULL,
    description TEXT,
    standard_rate NUMERIC(15,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    unit_of_measure VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_engineering_services_code UNIQUE (code)
);

CREATE INDEX idx_eng_services_category ON engineering_services(category) WHERE deleted_at IS NULL;

-- =============================================================================
-- 5. PROJECT MANAGEMENT
-- =============================================================================
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    project_code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    city VARCHAR(100) DEFAULT 'Phnom Penh',
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    client_id UUID NOT NULL,
    project_manager_id UUID,
    service_id UUID,
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNING',
    start_date DATE,
    target_end_date DATE,
    actual_end_date DATE,
    contract_value NUMERIC(15,2),
    estimated_budget NUMERIC(15,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    completion_percentage NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT uk_projects_code UNIQUE (project_code),
    CONSTRAINT fk_projects_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_projects_pm FOREIGN KEY (project_manager_id) REFERENCES users(id),
    CONSTRAINT fk_projects_service FOREIGN KEY (service_id) REFERENCES engineering_services(id)
);

CREATE INDEX idx_projects_client ON projects(client_id);
CREATE INDEX idx_projects_status ON projects(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_projects_pm ON projects(project_manager_id);

CREATE TABLE project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(40) NOT NULL,
    is_lead BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMPTZ,
    CONSTRAINT uk_project_members_assignment UNIQUE (project_id, user_id, role),
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_project_members_user ON project_members(user_id);

CREATE TABLE project_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    project_id UUID NOT NULL,
    record_date DATE NOT NULL,
    overall_percentage NUMERIC(5,2) NOT NULL,
    planned_percentage NUMERIC(5,2),
    actual_percentage NUMERIC(5,2),
    summary TEXT NOT NULL,
    site_issues TEXT,
    weather_condition VARCHAR(100),
    recorded_by UUID NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_progress_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_project_progress_date ON project_progress(project_id, record_date);

CREATE TABLE project_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    project_id UUID NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    reason TEXT,
    changed_by UUID NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_proj_status_hist_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_proj_status_hist_project ON project_status_history(project_id, changed_at);

-- =============================================================================
-- 6. BOQ & COST ESTIMATION
-- =============================================================================
CREATE TABLE estimates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    estimate_number VARCHAR(30) NOT NULL,
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    current_version_number INT NOT NULL DEFAULT 1,
    total_direct_cost NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_indirect_cost NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    profit_margin_rate NUMERIC(5,2) DEFAULT 0.00,
    profit_amount NUMERIC(15,2) DEFAULT 0.00,
    grand_total NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    approved_by UUID,
    approved_at TIMESTAMPTZ,
    CONSTRAINT uk_estimates_number UNIQUE (estimate_number),
    CONSTRAINT fk_estimates_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX idx_estimates_project ON estimates(project_id);
CREATE INDEX idx_estimates_status ON estimates(status) WHERE deleted_at IS NULL;

CREATE TABLE estimate_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    estimate_id UUID NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    quantity NUMERIC(15,3) NOT NULL DEFAULT 0.000,
    unit_rate_labor NUMERIC(15,2) DEFAULT 0.00,
    unit_rate_material NUMERIC(15,2) DEFAULT 0.00,
    unit_rate_equipment NUMERIC(15,2) DEFAULT 0.00,
    unit_rate_subcontractor NUMERIC(15,2) DEFAULT 0.00,
    unit_price NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    display_order INT NOT NULL DEFAULT 0,
    notes TEXT,
    CONSTRAINT fk_estimate_items_estimate FOREIGN KEY (estimate_id) REFERENCES estimates(id) ON DELETE CASCADE
);

CREATE INDEX idx_estimate_items_est ON estimate_items(estimate_id, display_order);

CREATE TABLE estimate_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    estimate_id UUID NOT NULL,
    version_number INT NOT NULL,
    snapshot_payload JSONB NOT NULL,
    total_amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    change_summary TEXT,
    created_snapshot_by UUID NOT NULL,
    created_snapshot_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_estimate_versions_revision UNIQUE (estimate_id, version_number),
    CONSTRAINT fk_estimate_versions_estimate FOREIGN KEY (estimate_id) REFERENCES estimates(id) ON DELETE CASCADE
);

-- =============================================================================
-- 7. QUOTATION & CONTRACT
-- =============================================================================
CREATE TABLE quotations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    quotation_number VARCHAR(30) NOT NULL,
    project_id UUID NOT NULL,
    estimate_id UUID,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    issue_date DATE NOT NULL,
    valid_until DATE NOT NULL,
    subtotal NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    discount_rate NUMERIC(5,2) DEFAULT 0.00,
    discount_amount NUMERIC(15,2) DEFAULT 0.00,
    tax_rate NUMERIC(5,2) DEFAULT 0.00,
    tax_amount NUMERIC(15,2) DEFAULT 0.00,
    grand_total NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_terms TEXT,
    scope_of_work TEXT,
    exclusions TEXT,
    client_notes TEXT,
    CONSTRAINT uk_quotations_number UNIQUE (quotation_number),
    CONSTRAINT fk_quotations_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_quotations_estimate FOREIGN KEY (estimate_id) REFERENCES estimates(id)
);

CREATE INDEX idx_quotations_project ON quotations(project_id);
CREATE INDEX idx_quotations_status ON quotations(status) WHERE deleted_at IS NULL;

CREATE TABLE quotation_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    quotation_id UUID NOT NULL,
    item_code VARCHAR(50),
    description TEXT NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    quantity NUMERIC(15,3) NOT NULL,
    unit_price NUMERIC(15,2) NOT NULL,
    total_amount NUMERIC(15,2) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_quotation_items_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE
);

CREATE INDEX idx_quotation_items_quote ON quotation_items(quotation_id, display_order);

CREATE TABLE contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    contract_number VARCHAR(30) NOT NULL,
    project_id UUID NOT NULL,
    quotation_id UUID,
    title VARCHAR(200) NOT NULL,
    contract_type VARCHAR(35) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    signed_date DATE,
    effective_date DATE NOT NULL,
    completion_date DATE NOT NULL,
    original_value NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    revised_value NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    retention_percentage NUMERIC(5,2) DEFAULT 5.00,
    retention_amount NUMERIC(15,2) DEFAULT 0.00,
    tax_rate NUMERIC(5,2) DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    terms_and_conditions TEXT,
    CONSTRAINT uk_contracts_number UNIQUE (contract_number),
    CONSTRAINT fk_contracts_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_contracts_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id)
);

CREATE INDEX idx_contracts_project ON contracts(project_id);
CREATE INDEX idx_contracts_status ON contracts(status) WHERE deleted_at IS NULL;

CREATE TABLE contract_amendments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    contract_id UUID NOT NULL,
    amendment_number VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PROPOSED',
    amount_adjustment NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    days_extension INT NOT NULL DEFAULT 0,
    approved_date DATE,
    approved_by UUID,
    CONSTRAINT uk_contract_amendments_number UNIQUE (contract_id, amendment_number),
    CONSTRAINT fk_contract_amendments_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

-- =============================================================================
-- 8. FINANCE MANAGEMENT
-- =============================================================================
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    invoice_number VARCHAR(30) NOT NULL,
    project_id UUID NOT NULL,
    contract_id UUID,
    invoice_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    retention_deduction NUMERIC(15,2) DEFAULT 0.00,
    tax_rate NUMERIC(5,2) DEFAULT 0.00,
    tax_amount NUMERIC(15,2) DEFAULT 0.00,
    total_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    paid_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    balance_due NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate_to_khr NUMERIC(12,4),
    notes TEXT,
    CONSTRAINT uk_invoices_number UNIQUE (invoice_number),
    CONSTRAINT fk_invoices_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_invoices_contract FOREIGN KEY (contract_id) REFERENCES contracts(id)
);

CREATE INDEX idx_invoices_project ON invoices(project_id);
CREATE INDEX idx_invoices_status ON invoices(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_invoices_due ON invoices(due_date);

CREATE TABLE invoice_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    invoice_id UUID NOT NULL,
    item_code VARCHAR(50),
    description TEXT NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    quantity NUMERIC(15,3) NOT NULL,
    unit_price NUMERIC(15,2) NOT NULL,
    total_amount NUMERIC(15,2) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

CREATE INDEX idx_invoice_items_inv ON invoice_items(invoice_id, display_order);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    invoice_id UUID NOT NULL,
    payment_reference VARCHAR(60) NOT NULL,
    payment_date DATE NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate_to_khr NUMERIC(12,4),
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    bank_transaction_ref VARCHAR(100),
    receipt_number VARCHAR(50),
    notes TEXT,
    recorded_by UUID NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_payments_reference UNIQUE (payment_reference),
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE INDEX idx_payments_invoice ON payments(invoice_id);

CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    expense_number VARCHAR(30) NOT NULL,
    project_id UUID,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(35) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    expense_date DATE NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    tax_amount NUMERIC(15,2) DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    vendor_name VARCHAR(150),
    invoice_reference VARCHAR(100),
    receipt_url VARCHAR(500),
    notes TEXT,
    claimed_by UUID NOT NULL,
    approved_by UUID,
    approved_at TIMESTAMPTZ,
    CONSTRAINT uk_expenses_number UNIQUE (expense_number),
    CONSTRAINT fk_expenses_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX idx_expenses_project ON expenses(project_id);
CREATE INDEX idx_expenses_status ON expenses(status) WHERE deleted_at IS NULL;

-- =============================================================================
-- 9. MATERIALS & EQUIPMENT
-- =============================================================================
CREATE TABLE materials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    item_code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100),
    unit_of_measure VARCHAR(20) NOT NULL,
    unit_cost NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    quantity_on_hand NUMERIC(15,3) NOT NULL DEFAULT 0.000,
    reorder_threshold NUMERIC(15,3) DEFAULT 0.000,
    storage_location VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_materials_code UNIQUE (item_code)
);

CREATE INDEX idx_materials_category ON materials(category) WHERE deleted_at IS NULL;

CREATE TABLE equipment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    asset_code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    model VARCHAR(100),
    serial_number VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE_IN_YARD',
    assigned_project_id UUID,
    purchase_date DATE,
    purchase_price NUMERIC(15,2),
    hourly_rate NUMERIC(15,2),
    daily_rate NUMERIC(15,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    CONSTRAINT uk_equipment_asset_code UNIQUE (asset_code),
    CONSTRAINT fk_equipment_assigned_proj FOREIGN KEY (assigned_project_id) REFERENCES projects(id)
);

CREATE INDEX idx_equipment_status ON equipment(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_equipment_assigned_proj ON equipment(assigned_project_id);

CREATE TABLE inventory_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    material_id UUID NOT NULL,
    project_id UUID,
    movement_type VARCHAR(35) NOT NULL,
    movement_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    quantity NUMERIC(15,3) NOT NULL,
    unit_cost NUMERIC(15,2) NOT NULL,
    total_cost NUMERIC(15,2) NOT NULL,
    reference_number VARCHAR(100),
    notes TEXT,
    performed_by UUID NOT NULL,
    CONSTRAINT fk_inv_movements_material FOREIGN KEY (material_id) REFERENCES materials(id),
    CONSTRAINT fk_inv_movements_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX idx_inv_movements_material ON inventory_movements(material_id);
CREATE INDEX idx_inv_movements_project ON inventory_movements(project_id);

-- =============================================================================
-- 10. DOCUMENT MANAGEMENT
-- =============================================================================
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    document_code VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    project_id UUID,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    revision VARCHAR(20) DEFAULT 'Rev-0',
    description TEXT,
    uploaded_by UUID NOT NULL,
    CONSTRAINT uk_documents_code UNIQUE (document_code),
    CONSTRAINT fk_documents_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX idx_documents_project ON documents(project_id);
CREATE INDEX idx_documents_type ON documents(document_type) WHERE deleted_at IS NULL;

-- =============================================================================
-- 11. SYSTEM MANAGEMENT (NOTIFICATIONS & AUDIT LOGS)
-- =============================================================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    recipient_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    channel VARCHAR(20) NOT NULL DEFAULT 'IN_APP',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    reference_entity_id UUID,
    reference_entity_type VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, is_read);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    entity_name VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(30) NOT NULL,
    performed_by UUID,
    performed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    notes TEXT
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_logs_time ON audit_logs(performed_at DESC);
