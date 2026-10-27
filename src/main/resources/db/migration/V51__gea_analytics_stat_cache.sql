-- Pre-aggregated GEA analytics (distinct phone / biz identity).
-- Populated by refresh_gea_analytics_stats.sql after ETL; API reads this for fast responses.

CREATE TABLE IF NOT EXISTS gea_analytics_stat (
    id           BIGSERIAL PRIMARY KEY,
    scope        VARCHAR(32)  NOT NULL,
    program_id   VARCHAR(64)  NOT NULL DEFAULT 'all',
    dimension    VARCHAR(32)  NOT NULL,
    name         VARCHAR(512) NOT NULL DEFAULT '',
    value        NUMERIC(20, 2) NOT NULL DEFAULT 0,
    refreshed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_gea_analytics_stat UNIQUE (scope, program_id, dimension, name)
);

CREATE INDEX IF NOT EXISTS idx_gea_analytics_stat_lookup
    ON gea_analytics_stat (scope, program_id, dimension);

COMMENT ON TABLE gea_analytics_stat IS
    'Cached distinct-phone analytics aggregates. Refresh after GEA ETL reload.';
