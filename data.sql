-- Create permissions table
CREATE TABLE IF NOT EXISTS public.permissions
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    module VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    sub_module VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT permissions_name_key UNIQUE (name)
);

-- Insert some permissions
INSERT INTO public.permissions (name, description, module, action, sub_module)
VALUES
    ('company.branch:search', 'Search Company Branch', 'company', 'search', 'branch'),
    ('jurisdictions:create', 'Create Jurisdictions', 'jurisdictions', 'create', NULL),
    ('jurisdictions:read_all', 'Read All Jurisdictions', 'jurisdictions', 'read_all', NULL),
    ('jurisdictions:read', 'Read Jurisdiction', 'jurisdictions', 'read', NULL),
    ('jurisdictions:edit', 'Edit Jurisdiction', 'jurisdictions', 'edit', NULL),
    ('jurisdictions:search', 'Search Jurisdiction', 'jurisdictions', 'search', NULL),
    ('currency:create', 'Create Currency', 'currency', 'create', NULL),
    ('currencies:read_all', 'Read All Currencies', 'currencies', 'read_all', NULL),
    ('currency:read', 'Read Currency', 'currency', 'read', NULL),
    ('currency:edit', 'Edit Currency', 'currency', 'edit', NULL),
    ('currency.denomination:create', 'Create Currency Denomination', 'currency', 'create', 'denomination'),
    ('currency.denomination:read_all', 'Read All Currency Denomination', 'currency', 'read_all', 'denomination'),
    ('currency.denomination:read', 'Read Currency Denomination', 'currency', 'read', 'denomination'),
    ('company.assign:create', 'Create Assign Company', 'company', 'create', 'assign'),
    ('company.assign:read', 'Read Assigned Company', 'company', 'read', 'assign'),
    ('company:read', 'Read Company', 'company', 'read', NULL),
    ('permission:create', 'Create Permission', 'permission', 'create', NULL),
    ('permission:read', 'Read Permission', 'permission', 'read', NULL),
    ('permission.assign:assign', 'Assign Permission', 'permission', 'assign', 'assign'),
    ('permission:delete', 'Delete Permission', 'permission', 'delete', NULL)

ON CONFLICT (name) DO NOTHING;

CREATE SEQUENCE IF NOT EXISTS users_id_seq;
CREATE SEQUENCE IF NOT EXISTS user_company_id_seq;
CREATE SEQUENCE IF NOT EXISTS password_reset_token_id_seq;
CREATE SEQUENCE IF NOT EXISTS refresh_token_id_seq;
CREATE SEQUENCE IF NOT EXISTS roles_id_seq;

-- Create roles table
CREATE TABLE IF NOT EXISTS public.roles
(
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert ADMIN role
-- INSERT INTO public.roles (role_name, description, location)
-- VALUES ('ADMIN', 'Administrator role', NULL)
-- ON CONFLICT (role_name) DO NOTHING;

CREATE TABLE IF NOT EXISTS public.users
(
    id BIGINT NOT NULL DEFAULT nextval('users_id_seq'::regclass),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    username VARCHAR(255) NOT NULL,
    reset_code VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    profile_image VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    role_name VARCHAR(255) NOT NULL,
    created_on TIMESTAMP,
    updated_on TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_username_key UNIQUE (username)
);

-- Create user_company table
CREATE TABLE IF NOT EXISTS public.user_company
(
    id INTEGER NOT NULL DEFAULT nextval('user_company_id_seq'::regclass),
    company_name VARCHAR(255) NOT NULL,
    description VARCHAR(255) UNIQUE,
    status VARCHAR(50),
    primary_contact_name VARCHAR(255) NOT NULL,
    primary_contact_email VARCHAR(255) NOT NULL,
    primary_contact_phone_number VARCHAR(255),
    company_logo VARCHAR(255),
    company_address VARCHAR(255),
    company_digital_address VARCHAR(255),
    industry VARCHAR(255),
    company_merchant_momo_number VARCHAR(255),
    company_bank_name VARCHAR(255),
    tax_id VARCHAR(255),
    start_of_day_time TIME WITHOUT TIME ZONE,
    end_of_day_time TIME WITHOUT TIME ZONE,
    primary_currency VARCHAR(50) NOT NULL,
    secondary_currency VARCHAR(50)[],
    company_admin_id BIGINT,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_on TIMESTAMP WITH TIME ZONE,
    updated_on TIMESTAMP WITH TIME ZONE,
    deleted_on TIMESTAMP WITH TIME ZONE,
    company_code VARCHAR(255),
    build_status VARCHAR(255),
    driver_name VARCHAR(255),
    db_url VARCHAR(255),
    company_identifier VARCHAR(255),
    assigned_form_ids BIGINT[],
    CONSTRAINT user_company_pkey PRIMARY KEY (id),
    CONSTRAINT user_company_company_name_key UNIQUE (company_name),
    CONSTRAINT user_company_company_admin_id_fkey FOREIGN KEY (company_admin_id)
        REFERENCES public.users (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


-- Insert admin user with all fields
INSERT INTO public.users (
    first_name,
    last_name,
    username,
    reset_code,
    email,
    password,
    profile_image,
    phone_number,
    status,
    role_name,
    created_on,
    updated_on,
    enabled,
    updated_at
)
VALUES (
    'Mesh',
    'Admin',
    'mesh_admin',
    NULL,
    'mesh@logiciel.com',
    '$2a$10$xJwL5v5Jz5UZJZ5UZJZ5Ue', -- Replace with properly hashed password
    'https://mesh.suite/profile/admin.png',
    '+1134327890',
    'ACTIVE',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    true,
    CURRENT_TIMESTAMP
);

-- Insert a fully populated company record with all fields
INSERT INTO public.user_company (
    company_name,
    description,
    status,
    primary_contact_name,
    primary_contact_email,
    primary_contact_phone_number,
    company_logo,
    company_address,
    company_digital_address,
    industry,
    company_merchant_momo_number,
    company_bank_name,
    tax_id,
    start_of_day_time,
    end_of_day_time,
    primary_currency,
    secondary_currency,
    company_admin_id,
    created_by,
    updated_by,
    is_deleted,
    created_on,
    updated_on,
    deleted_on,
    company_code,
    build_status,
    driver_name,
    db_url,
    company_identifier,
    assigned_form_ids
) VALUES (
    'Mesh Suite Ltd',
    'Leading business management solutions provider',
    'ACTIVE',
    'Mesh Admin',
    'mesh.admin@logiciel.com',
    '+1134327890',
    'https://mesh.suite/logo.png',
    '335 Mesh Business Street, Accra',
    'GA-123-4567',
    'Technology',
    '0244123456',
    'Ghana Commercial Bank',
    'TAX12345678',
    '08:00:00',
    '17:00:00',
    'GHS',
    ARRAY['USD', 'EUR'],
    (SELECT id FROM public.users WHERE email = 'mesh@logiciel.com'),
    'system',
    'system',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL,
    'EXC001',
    'COMPLETE',
    'org.postgresql.Driver',
    'jdbc:postgresql://localhost:5432/mesh_db',
    'EXC-2023-001',
    ARRAY[1, 2, 3]
);

-- Set ownership (optional)
ALTER TABLE IF EXISTS public.user_company OWNER TO postgres;
ALTER TABLE IF EXISTS public.users OWNER TO postgres;
ALTER TABLE IF EXISTS public.roles OWNER TO postgres;
ALTER TABLE IF EXISTS public.password_reset_token OWNER TO postgres;
ALTER TABLE IF EXISTS public.refresh_token OWNER TO postgres;