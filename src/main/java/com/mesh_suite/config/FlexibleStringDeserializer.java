package com.mesh_suite.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;

/**
 * Deserializes a JSON field into a String regardless of whether
 * the source value is a JSON string, integer, or long.
 *
 * Required because the Orchard API sends trans_ref as a bare integer
 * in callback payloads:
 *
 *   Callback:  "trans_ref": 4243846303      ← integer, no quotes
 *   TSC:       "trans_ref": "031059294635"  ← string, with quotes
 *
 * Using this deserializer on trans_ref makes TransactionStatusResponse
 * handle both response shapes correctly without any type errors.
 */
public class FlexibleStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctx)
            throws IOException {

        return switch (parser.currentToken()) {
            case VALUE_STRING      -> parser.getText();
            case VALUE_NUMBER_INT  -> parser.getLongValue() + ""; // handles int and long
            case VALUE_NULL        -> null;
            default -> throw new JsonMappingException(parser,
                    "Cannot deserialize trans_ref: unexpected token "
                    + parser.currentToken());
        };
    }
}