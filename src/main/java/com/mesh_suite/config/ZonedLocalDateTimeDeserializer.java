package com.mesh_suite.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public class ZonedLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    private static final DateTimeFormatter ISO_WITH_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter LOCAL_ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ZonedLocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }

        if (token == JsonToken.START_ARRAY) {
            // Handle legacy arrays: [year, month, day, hour, min, sec, nano]
            JsonNode array = p.getCodec().readTree(p);
            int year = array.get(0).asInt(1970);
            int month = array.get(1).asInt(1);
            int day = array.get(2).asInt(1);
            int hour = array.size() > 3 ? array.get(3).asInt(0) : 0;
            int min = array.size() > 4 ? array.get(4).asInt(0) : 0;
            int sec = array.size() > 5 ? array.get(5).asInt(0) : 0;
            int nano = array.size() > 6 ? array.get(6).asInt(0) : 0;
            return LocalDateTime.of(year, month, day, hour, min, sec, nano);
        }

        String dateStr = p.getValueAsString();
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            // Try OffsetDateTime for zoned/millis (e.g., "2025-11-18T16:10:28.957Z")
            if (dateStr.contains("Z") || dateStr.contains("+") || dateStr.contains("-")) {
                OffsetDateTime odt = OffsetDateTime.parse(dateStr, ISO_WITH_OFFSET);
                LocalDateTime ldt = odt.toLocalDateTime();
                // Adjust nano for short millis (e.g., .957 → 957000000)
                if (odt.getNano() % 1000000 != 0) {  // If millis truncated
                    ldt = ldt.with(ChronoField.NANO_OF_SECOND, odt.getNano());
                }
                return ldt;
            }
        } catch (DateTimeParseException e) {
            // Fall through
        }

        try {
            // Fallback: Local ISO (e.g., "2025-11-18T09:18:00")
            return LocalDateTime.parse(dateStr, LOCAL_ISO);
        } catch (DateTimeParseException e) {
            throw ctxt.weirdStringException(dateStr, LocalDateTime.class, "Invalid date: " + e.getMessage());
        }
    }
}