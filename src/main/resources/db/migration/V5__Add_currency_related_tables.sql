-- Create CurrencySetup Table
CREATE TABLE IF NOT EXISTS currency_setup (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    currency VARCHAR(255),
    symbol VARCHAR(255),
    country_name VARCHAR(255),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_on TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Create Denomination Table
CREATE TABLE IF NOT EXISTS denomination (
      id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
      currency_setup_id BIGINT,
      amount DECIMAL(10, 2),
      name VARCHAR(255),
      denomination_type VARCHAR(255),
      FOREIGN KEY (currency_setup_id) REFERENCES currency_setup(id) ON DELETE CASCADE
);
