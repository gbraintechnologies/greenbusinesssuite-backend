CREATE TABLE IF NOT EXISTS forms_response_data (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    form_id BIGINT,
    is_completed BOOLEAN DEFAULT FALSE,
    company_id BIGINT,
    user_id BIGINT,
    status VARCHAR(255) DEFAULT 'PENDING',
    created_on TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE IF NOT EXISTS forms_input_data (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    form_data_id BIGINT NOT NULL,
    CONSTRAINT fk_input_data_form_data FOREIGN KEY (form_data_id)
        REFERENCES forms_response_data(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS forms_section_data (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    form_section_id BIGINT,
    input_data_id BIGINT NOT NULL,
    CONSTRAINT fk_section_data_input_data FOREIGN KEY (input_data_id)
        REFERENCES forms_input_data(id) ON DELETE NO ACTION
);


CREATE TABLE IF NOT EXISTS forms_field_data (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    display_type VARCHAR(255),
    field_name VARCHAR(255),
    form_field_id BIGINT,
    is_statistical_field BOOLEAN DEFAULT FALSE,
    response VARCHAR(255),
    statistical_function VARCHAR(255),
    form_section_id BIGINT NOT NULL,
    CONSTRAINT fk_field_data_section FOREIGN KEY (form_section_id)
        REFERENCES forms_section_data(id) ON DELETE NO ACTION
);
