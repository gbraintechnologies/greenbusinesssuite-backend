INSERT INTO gea_program (id, name, kind, active) VALUES
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
