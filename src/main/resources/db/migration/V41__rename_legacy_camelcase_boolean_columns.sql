-- Fix columns created by PhysicalNamingStrategyStandardImpl (e.g. isdeleted)
-- so they match CamelCaseToUnderscoresNamingStrategy / Flyway schema (is_deleted).

CREATE OR REPLACE FUNCTION rename_column_if_legacy(
    p_table TEXT,
    p_legacy TEXT,
    p_canonical TEXT
) RETURNS VOID AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = p_table
          AND column_name = p_legacy
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = p_table
          AND column_name = p_canonical
    ) THEN
        EXECUTE format(
            'ALTER TABLE %I RENAME COLUMN %I TO %I',
            p_table, p_legacy, p_canonical
        );
    END IF;
END;
$$ LANGUAGE plpgsql;

-- forms
SELECT rename_column_if_legacy('forms', 'isdeleted', 'is_deleted');
SELECT rename_column_if_legacy('forms', 'istemplate', 'is_template');
SELECT rename_column_if_legacy('forms', 'isanonymous', 'is_anonymous');
SELECT rename_column_if_legacy('forms', 'multipleforms', 'multiple_forms');
SELECT rename_column_if_legacy('forms', 'usermandatory', 'user_mandatory');
SELECT rename_column_if_legacy('forms', 'publishstatus', 'publish_status');
SELECT rename_column_if_legacy('forms', 'forminstruction', 'form_instruction');
SELECT rename_column_if_legacy('forms', 'redirecturl', 'redirect_url');
SELECT rename_column_if_legacy('forms', 'companyid', 'company_id');
SELECT rename_column_if_legacy('forms', 'createdon', 'created_on');
SELECT rename_column_if_legacy('forms', 'updatedon', 'updated_on');
SELECT rename_column_if_legacy('forms', 'deletedon', 'deleted_on');
SELECT rename_column_if_legacy('forms', 'assigndate', 'assign_date');

-- form_sections
SELECT rename_column_if_legacy('form_sections', 'isdeleted', 'is_deleted');
SELECT rename_column_if_legacy('form_sections', 'formid', 'form_id');
SELECT rename_column_if_legacy('form_sections', 'createdon', 'created_on');
SELECT rename_column_if_legacy('form_sections', 'updatedon', 'updated_on');
SELECT rename_column_if_legacy('form_sections', 'deletedon', 'deleted_on');

-- form_field
SELECT rename_column_if_legacy('form_field', 'isdeleted', 'is_deleted');
SELECT rename_column_if_legacy('form_field', 'ismandatory', 'is_mandatory');
SELECT rename_column_if_legacy('form_field', 'isstatisticalfield', 'is_statistical_field');
SELECT rename_column_if_legacy('form_field', 'horizontalalign', 'horizontal_align');
SELECT rename_column_if_legacy('form_field', 'placeholder', 'place_holder');
SELECT rename_column_if_legacy('form_field', 'formsectionid', 'form_section_id');
SELECT rename_column_if_legacy('form_field', 'fielddatatype', 'field_data_type');
SELECT rename_column_if_legacy('form_field', 'maxlength', 'max_length');
SELECT rename_column_if_legacy('form_field', 'validpattern', 'valid_pattern');
SELECT rename_column_if_legacy('form_field', 'statisticalfunction', 'statistical_function');
SELECT rename_column_if_legacy('form_field', 'displaytype', 'display_type');
SELECT rename_column_if_legacy('form_field', 'createdon', 'created_on');
SELECT rename_column_if_legacy('form_field', 'updatedon', 'updated_on');
SELECT rename_column_if_legacy('form_field', 'deletedon', 'deleted_on');

-- currency_setup / discount_data
SELECT rename_column_if_legacy('currency_setup', 'isdeleted', 'is_deleted');
SELECT rename_column_if_legacy('currency_setup', 'is_Deleted', 'is_deleted');
SELECT rename_column_if_legacy('discount_data', 'isdeleted', 'is_deleted');

DROP FUNCTION rename_column_if_legacy(TEXT, TEXT, TEXT);
