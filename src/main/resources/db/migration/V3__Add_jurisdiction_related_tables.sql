-- Create Jurisdiction Table
CREATE TABLE IF NOT EXISTS jurisdiction (
      id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
      name VARCHAR(255),
      country_id BIGINT
);

-- Create ParentAddressScheme Table
CREATE TABLE IF NOT EXISTS parent_address_scheme (
       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       name VARCHAR(255),
       jurisdiction_id BIGINT,
       input_type VARCHAR(255),
       FOREIGN KEY (jurisdiction_id) REFERENCES jurisdiction(id) ON DELETE CASCADE
);

-- Create ParentAddressSchemeEntries Table
CREATE TABLE IF NOT EXISTS parent_address_scheme_entries (
       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       name VARCHAR(255),
       parent_address_scheme_id BIGINT,
       FOREIGN KEY (parent_address_scheme_id) REFERENCES parent_address_scheme(id) ON DELETE CASCADE
);

-- Create ChildAddressSchemeEntries Table
CREATE TABLE IF NOT EXISTS child_address_scheme_entries (
      id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
      name VARCHAR(255),
      parent_address_scheme_entries_id BIGINT,
      FOREIGN KEY (parent_address_scheme_entries_id) REFERENCES parent_address_scheme_entries(id) ON DELETE CASCADE
);
