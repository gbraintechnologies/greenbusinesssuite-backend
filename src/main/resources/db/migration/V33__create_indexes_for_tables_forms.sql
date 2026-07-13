-- Index the forms table
CREATE INDEX IF NOT EXISTS idx_forms_company_id_is_deleted ON forms (company_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_forms_created_on ON forms (created_on);

-- Index the form_sections table
CREATE INDEX IF NOT EXISTS idx_form_sections_form_id ON form_sections (form_id);

-- Index the form_field table
CREATE INDEX IF NOT EXISTS idx_form_field_section_id_is_deleted ON form_field (form_section_id, is_deleted);
-- Index the form_data table
CREATE INDEX IF NOT EXISTS idx_form_data_form_id_company_id_user_id ON form_data (form_id, company_id, user_id);
CREATE INDEX IF NOT EXISTS idx_form_data_status ON form_data (status);

-- Index the form_data_section table
CREATE INDEX IF NOT EXISTS idx_form_data_section_form_section_id ON form_data_section (form_section_id);

-- Index the form_data_field table
CREATE INDEX IF NOT EXISTS idx_form_data_field_field_id_section_id ON form_data_field (form_field_id, form_section_id);

-- Index for forms_response_data table
CREATE INDEX IF NOT EXISTS idx_forms_response_data_form_id_company_id_user_id ON forms_response_data (form_id, company_id, user_id);
CREATE INDEX IF NOT EXISTS idx_forms_response_data_status ON forms_response_data (status);

-- Index for forms_input_data table
CREATE INDEX IF NOT EXISTS idx_forms_input_data_form_data_id ON forms_input_data (form_data_id);

-- Index for forms_section_data table
CREATE INDEX IF NOT EXISTS idx_forms_section_data_input_data_id ON forms_section_data (input_data_id);

-- Index for forms_field_data table
CREATE INDEX IF NOT EXISTS idx_forms_field_data_field_id_section_id ON forms_field_data (form_field_id, form_section_id);

-- Index to optimize user_company_files
CREATE INDEX IF NOT EXISTS idx_user_company_files_user_id_company_id ON user_company_files (user_id, company_id);
CREATE INDEX IF NOT EXISTS idx_user_company_files_form_id ON user_company_files (form_id);
CREATE INDEX IF NOT EXISTS idx_user_company_files_created_on ON user_company_files (created_on);

-- Index for notification_message table
CREATE INDEX IF NOT EXISTS idx_notification_message_sender ON notification_message (sender);
CREATE INDEX IF NOT EXISTS idx_notification_message_type_recurring ON notification_message (message_type, recurring_type);
CREATE INDEX IF NOT EXISTS idx_notification_message_trigger_time ON notification_message (trigger_time);
CREATE INDEX IF NOT EXISTS idx_notification_message_created_on ON notification_message (created_on);
CREATE INDEX IF NOT EXISTS idx_notification_message_start_end_date ON notification_message (start_date, end_date);

-- Index for notification_recipients table
CREATE INDEX IF NOT EXISTS idx_notification_recipients_notification_id ON notification_recipients (notification_id);
CREATE INDEX IF NOT EXISTS idx_notification_recipients_recipient ON notification_recipients (recipient);

-- Index for company_branding table
CREATE INDEX IF NOT EXISTS idx_company_branding_tenancy_id ON company_branding (tenancy_id);
CREATE INDEX IF NOT EXISTS idx_company_branding_company_id ON company_branding (company_id);
CREATE INDEX IF NOT EXISTS idx_company_branding_tenancy_id_company_id ON company_branding (tenancy_id, company_id);
