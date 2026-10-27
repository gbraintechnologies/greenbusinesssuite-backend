-- Refresh GEA analytics aggregates (run after loading gea_program_record).
-- Distinct business identity = phone when present, else per-row id.

BEGIN;

CREATE TEMP TABLE _gea_biz ON COMMIT DROP AS
SELECT
    program_id,
    record_type,
    COALESCE(NULLIF(BTRIM(phone), ''), 'id:' || id::text) AS biz_id,
    gender,
    region,
    sector,
    registered,
    disability,
    age_band,
    literacy,
    approved_amount
FROM gea_program_record;

CREATE INDEX ON _gea_biz (record_type, program_id);
CREATE INDEX ON _gea_biz (biz_id);

TRUNCATE gea_analytics_stat;

-- GENERAL (loan_grant + business)
INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'general', 'all', 'total', '', COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz
WHERE record_type IN ('loan_grant', 'business');

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'general', 'all', 'registered',
       CASE WHEN registered IS TRUE THEN 'Registered'
            WHEN registered IS FALSE THEN 'Non-registered'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz
WHERE record_type IN ('loan_grant', 'business')
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'general', 'all', 'gender', COALESCE(gender, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz
WHERE record_type IN ('loan_grant', 'business')
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'general', 'all', 'region', region, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz
WHERE record_type IN ('loan_grant', 'business') AND region IS NOT NULL
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'general', 'all', 'sector', sector, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz
WHERE record_type IN ('loan_grant', 'business') AND sector IS NOT NULL
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'general', 'all', 'disability',
       CASE WHEN disability IS TRUE THEN 'With disability'
            WHEN disability IS FALSE THEN 'Without disability'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz
WHERE record_type IN ('loan_grant', 'business')
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'general', 'all', 'age', COALESCE(age_band, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz
WHERE record_type IN ('loan_grant', 'business')
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'general', 'all', 'literacy', COALESCE(literacy, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz
WHERE record_type IN ('loan_grant', 'business')
GROUP BY 4;

-- LOAN / GRANT — all + per program
INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', 'all', 'total', '', COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant';

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', 'all', 'disbursed', '', COALESCE(SUM(approved_amount), 0)
FROM _gea_biz WHERE record_type = 'loan_grant';

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', program_id, 'total', '', COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY program_id;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', program_id, 'disbursed', '', COALESCE(SUM(approved_amount), 0)
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY program_id;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', 'all', 'registered',
       CASE WHEN registered IS TRUE THEN 'Registered'
            WHEN registered IS FALSE THEN 'Non-registered'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', program_id, 'registered',
       CASE WHEN registered IS TRUE THEN 'Registered'
            WHEN registered IS FALSE THEN 'Non-registered'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', 'all', 'gender', COALESCE(gender, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', program_id, 'gender', COALESCE(gender, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', 'all', 'region', region, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant' AND region IS NOT NULL
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', program_id, 'region', region, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant' AND region IS NOT NULL
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', 'all', 'sector', sector, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant' AND sector IS NOT NULL
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', program_id, 'sector', sector, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant' AND sector IS NOT NULL
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', 'all', 'disability',
       CASE WHEN disability IS TRUE THEN 'With disability'
            WHEN disability IS FALSE THEN 'Without disability'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', program_id, 'disability',
       CASE WHEN disability IS TRUE THEN 'With disability'
            WHEN disability IS FALSE THEN 'Without disability'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', 'all', 'age', COALESCE(age_band, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'loan_grant', program_id, 'age', COALESCE(age_band, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'loan_grant'
GROUP BY program_id, 4;

-- TRAINING — all + per program
INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', 'all', 'total', '', COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training';

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', program_id, 'total', '', COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training'
GROUP BY program_id;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', 'all', 'registered',
       CASE WHEN registered IS TRUE THEN 'Registered'
            WHEN registered IS FALSE THEN 'Non-registered'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training'
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', program_id, 'registered',
       CASE WHEN registered IS TRUE THEN 'Registered'
            WHEN registered IS FALSE THEN 'Non-registered'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training'
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', 'all', 'gender', COALESCE(gender, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training'
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', program_id, 'gender', COALESCE(gender, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training'
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', 'all', 'region', region, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training' AND region IS NOT NULL
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', program_id, 'region', region, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training' AND region IS NOT NULL
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', 'all', 'sector', sector, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training' AND sector IS NOT NULL
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', program_id, 'sector', sector, COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training' AND sector IS NOT NULL
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', 'all', 'disability',
       CASE WHEN disability IS TRUE THEN 'With disability'
            WHEN disability IS FALSE THEN 'Without disability'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training'
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', program_id, 'disability',
       CASE WHEN disability IS TRUE THEN 'With disability'
            WHEN disability IS FALSE THEN 'Without disability'
            ELSE 'Other / undisclosed' END,
       COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training'
GROUP BY program_id, 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', 'all', 'age', COALESCE(age_band, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training'
GROUP BY 4;

INSERT INTO gea_analytics_stat (scope, program_id, dimension, name, value)
SELECT 'training', program_id, 'age', COALESCE(age_band, 'Other / undisclosed'), COUNT(DISTINCT biz_id)::numeric
FROM _gea_biz WHERE record_type = 'training'
GROUP BY program_id, 4;

COMMIT;

SELECT scope, program_id, dimension, COUNT(*) AS rows
FROM gea_analytics_stat
GROUP BY 1, 2, 3
ORDER BY 1, 2, 3
LIMIT 50;
