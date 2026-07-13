-- Migration for Forms, FormSections, and FormField

-- Create Forms Table
CREATE TABLE IF NOT EXISTS forms (
       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       name VARCHAR(255),
       company_id BIGINT,
       url VARCHAR(255),
       description VARCHAR(255),
       form_instruction VARCHAR(255),
       user_mandatory BOOLEAN DEFAULT FALSE,
       deadline TIMESTAMP WITH TIME ZONE,
       publish_status VARCHAR(255) NOT NULL,
       is_deleted BOOLEAN DEFAULT FALSE,
       is_template BOOLEAN DEFAULT FALSE,
       layout VARCHAR(255) DEFAULT 'GENERAL',
       created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       deleted_on TIMESTAMP,
       assign_date TIMESTAMP
);

-- Create FormSections Table
CREATE TABLE IF NOT EXISTS form_sections (
       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       name VARCHAR(255),
       description VARCHAR(255),
       instruction VARCHAR(255),
       form_id BIGINT,
       ordering INTEGER,
       is_deleted BOOLEAN DEFAULT FALSE,
       created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       deleted_on TIMESTAMP,
       FOREIGN KEY (form_id) REFERENCES forms(id) ON DELETE CASCADE
);

-- Create FormField Table
CREATE TABLE IF NOT EXISTS form_field (
        id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        name VARCHAR(255),
        description VARCHAR(255),
        label VARCHAR(255),
        place_holder VARCHAR(255),
        form_section_id BIGINT,
        instruction VARCHAR(255),
        ordering INTEGER,
        is_deleted BOOLEAN DEFAULT FALSE,
        field_data_type VARCHAR(255),
        valid_pattern VARCHAR(255),
        is_statistical_field BOOLEAN DEFAULT FALSE,
        statistical_function VARCHAR(255),
        display_type VARCHAR(255),
        is_mandatory BOOLEAN DEFAULT FALSE,
        horizontal_align BOOLEAN DEFAULT FALSE,
        created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        deleted_on TIMESTAMP,
        FOREIGN KEY (form_section_id) REFERENCES form_sections(id) ON DELETE CASCADE
);

-- Create FormField Choice Values Table
CREATE TABLE IF NOT EXISTS form_field_choice_values (
      form_field_id BIGINT,
      choice_value VARCHAR(255),
      FOREIGN KEY (form_field_id) REFERENCES form_field(id) ON DELETE CASCADE
);

-- Create FormData Table
CREATE TABLE IF NOT EXISTS form_data (
       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       form_id BIGINT,
       is_completed BOOLEAN DEFAULT FALSE,
       company_id BIGINT,
       user_id BIGINT,
       status VARCHAR(255) DEFAULT 'PENDING',
       created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create InputData Table
CREATE TABLE IF NOT EXISTS input_data (
        id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
        form_data_id BIGINT NOT NULL,
        FOREIGN KEY (form_data_id) REFERENCES form_data(id) ON DELETE CASCADE
);

-- Create FormDataSection Table
CREATE TABLE IF NOT EXISTS form_data_section (
       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       form_section_id BIGINT,
       input_data_id BIGINT NOT NULL,
       FOREIGN KEY (input_data_id) REFERENCES input_data(id) ON DELETE CASCADE
);

-- Create FormDataField Table
CREATE TABLE IF NOT EXISTS form_data_field (
     id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
     form_field_id BIGINT,
     field_name VARCHAR(255),
     response VARCHAR(255),
     is_statistical_field BOOLEAN DEFAULT FALSE,
     statistical_function VARCHAR(255),
     display_type VARCHAR(255),
     form_section_id BIGINT NOT NULL,
     FOREIGN KEY (form_section_id) REFERENCES form_data_section(id) ON DELETE CASCADE
);
