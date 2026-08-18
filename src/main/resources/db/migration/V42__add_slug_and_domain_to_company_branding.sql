

-- Add slug and domain fields to company branding
ALTER TABLE company_branding
ADD COLUMN slug VARCHAR(255) UNIQUE,
ADD COLUMN domain VARCHAR(255) UNIQUE;

-- Create indexes for faster lookups
CREATE INDEX idx_company_branding_slug ON company_branding(slug);
CREATE INDEX idx_company_branding_domain ON company_branding(domain);