-- The shared-schema models require tenant_id. Existing rows belong to the
-- original single tenant. Users keep their company identifier when they have one.

ALTER TABLE users ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(255);
UPDATE users
SET tenant_id = COALESCE(NULLIF(btrim(company_identifier), ''), 'mesh_suite_db')
WHERE tenant_id IS NULL;
ALTER TABLE users ALTER COLUMN tenant_id SET NOT NULL;

DO $$
DECLARE
    target text;
BEGIN
    FOREACH target IN ARRAY ARRAY[
        'roles',
        'token',
        'refresh_tokens',
        'password_reset_tokens',
        'notification_message',
        'media_center',
        'payment',
        'invoice',
        'forms',
        'forms_input_data',
        'form_sections',
        'form_field',
        'forms_section_data',
        'forms_field_data',
        'forms_response_data',
        'discount_data',
        'discounts',
        'denomination',
        'currency_setup',
        'billing',
        'api_key',
        'user_company_files',
        'business_profile'
    ]
    LOOP
        IF to_regclass(format('public.%I', target)) IS NOT NULL THEN
            EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(255)', target);
            EXECUTE format('UPDATE %I SET tenant_id = %L WHERE tenant_id IS NULL', target, 'mesh_suite_db');
            EXECUTE format('ALTER TABLE %I ALTER COLUMN tenant_id SET NOT NULL', target);
        END IF;
    END LOOP;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_roles_tenant_role_name') THEN
        ALTER TABLE roles ADD CONSTRAINT uk_roles_tenant_role_name UNIQUE (tenant_id, role_name);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_users_tenant_username') THEN
        ALTER TABLE users ADD CONSTRAINT uk_users_tenant_username UNIQUE (tenant_id, username);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_users_tenant_email') THEN
        ALTER TABLE users ADD CONSTRAINT uk_users_tenant_email UNIQUE (tenant_id, email);
    END IF;
END $$;
