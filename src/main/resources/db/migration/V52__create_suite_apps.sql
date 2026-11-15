CREATE TABLE IF NOT EXISTS suite_apps (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    tagline VARCHAR(255),
    description TEXT,
    href VARCHAR(512) NOT NULL,
    category VARCHAR(120),
    published BOOLEAN NOT NULL DEFAULT FALSE,
    logo_url VARCHAR(1024),
    mark VARCHAR(8),
    accent VARCHAR(64),
    wash VARCHAR(64),
    sort_order INTEGER,
    created_on TIMESTAMP,
    updated_on TIMESTAMP
);

INSERT INTO suite_apps (
    name, tagline, description, href, category, published, mark, accent, wash, sort_order, created_on, updated_on
)
SELECT
    'Autobus',
    'AI-powered business operations',
    'Customers, sales, marketing, and your team in one operations workspace.',
    'https://useautobus.com/',
    'Operations',
    TRUE,
    'A',
    'bg-[#14532d]',
    'bg-[#f0fdf4]',
    1,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM suite_apps WHERE name = 'Autobus');

INSERT INTO suite_apps (
    name, tagline, description, href, category, published, mark, accent, wash, sort_order, created_on, updated_on
)
SELECT
    'GREENPOS',
    'Point of sale for growing shops',
    'Take sales, track stock, and close the day from a POS built for African businesses.',
    'https://www.usegreenpos.com/',
    'Point of sale',
    TRUE,
    'G',
    'bg-[#0b57d0]',
    'bg-[#e8f0fe]',
    2,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM suite_apps WHERE name = 'GREENPOS');
