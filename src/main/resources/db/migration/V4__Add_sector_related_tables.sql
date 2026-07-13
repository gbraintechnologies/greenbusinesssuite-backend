-- Create SectorSetup Table
CREATE TABLE IF NOT EXISTS sector_setup (
      id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
      country_name VARCHAR(255)
);

-- Create Sectors Table
CREATE TABLE IF NOT EXISTS sectors (
     id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
     sector_setup_id BIGINT,
     parent_sector VARCHAR(255),
     FOREIGN KEY (sector_setup_id) REFERENCES sector_setup(id) ON DELETE CASCADE
);

-- Create Sub Sectors Table
CREATE TABLE IF NOT EXISTS sub_sectors (
     sector_id BIGINT,
     sub_sector VARCHAR(255),
     FOREIGN KEY (sector_id) REFERENCES sectors(id) ON DELETE CASCADE
);
