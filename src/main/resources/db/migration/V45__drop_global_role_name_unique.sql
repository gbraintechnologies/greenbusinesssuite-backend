-- Roles are tenant-scoped via uk_roles_tenant_role_name (tenant_id, role_name).
-- The legacy global UNIQUE(role_name) blocks provisioning ADMIN/CLIENT per company.
ALTER TABLE roles DROP CONSTRAINT IF EXISTS uk716hgxp60ym1lifrdgp67xt5k;
