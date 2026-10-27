-- GEA / MIS analytics fact tables (imported from bigdata dumps).
-- Not tenant-filtered: platform analytics reads these unscoped.

CREATE TABLE IF NOT EXISTS gea_program (
    id          VARCHAR(64) PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    kind        VARCHAR(32)  NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS gea_program_record (
    id                  BIGSERIAL PRIMARY KEY,
    program_id          VARCHAR(64)  NOT NULL REFERENCES gea_program (id),
    record_type         VARCHAR(32)  NOT NULL,
    external_key        VARCHAR(256),
    gender              VARCHAR(32),
    region              VARCHAR(64),
    sector              VARCHAR(512),
    registered          BOOLEAN,
    disability          BOOLEAN,
    age_band            VARCHAR(32),
    literacy            VARCHAR(64),
    application_type    VARCHAR(64),
    application_status  VARCHAR(128),
    requested_amount    NUMERIC(18, 2),
    approved_amount     NUMERIC(18, 2),
    training_type       VARCHAR(256)
);

CREATE INDEX IF NOT EXISTS idx_gea_program_record_program
    ON gea_program_record (program_id);
CREATE INDEX IF NOT EXISTS idx_gea_program_record_type
    ON gea_program_record (record_type);
CREATE INDEX IF NOT EXISTS idx_gea_program_record_app_type
    ON gea_program_record (application_type);
CREATE INDEX IF NOT EXISTS idx_gea_program_record_gender
    ON gea_program_record (gender);
CREATE INDEX IF NOT EXISTS idx_gea_program_record_region
    ON gea_program_record (region);
CREATE INDEX IF NOT EXISTS idx_gea_program_record_sector
    ON gea_program_record (sector);

INSERT INTO gea_program (id, name, kind, active) VALUES
    ('nkosuo',                'Nkosuo Loans & Grants',        'loan_grant', TRUE),
    ('capbuss',               'CAPBuSS',                      'loan_grant', TRUE),
    ('getp',                  'GETP',                         'loan_grant', TRUE),
    ('getp_crg2',             'GETP CRG2',                    'loan_grant', TRUE),
    ('yaws',                  'YAW Training',                 'training',   TRUE),
    ('nkosuo_training',       'Nkosuo Training',              'training',   TRUE),
    ('palladium',             'Palladium Baseline',           'business',   TRUE),
    ('ernest_v1',             'Ernest Loan Applications v1',  'loan_grant', TRUE),
    ('ernest_v2',             'Ernest Loan Applications v2',  'loan_grant', TRUE),
    ('nkosuo_disbursed',      'Nkosuo Disbursed',             'loan_grant', TRUE),
    ('ice_grants',            'ICE Grants',                   'loan_grant', TRUE),
    ('ice_regional',          'ICE Regional Finalists',       'loan_grant', TRUE),
    ('capbuss_beneficiaries', 'CAPBuSS Beneficiaries',        'loan_grant', TRUE),
    ('yaw_logicel',           'YAW Logicel Training',         'training',   TRUE),
    ('yaws_capbuss',          'YAWs CAPBuSS Beneficiaries',   'loan_grant', TRUE)
ON CONFLICT (id) DO NOTHING;
