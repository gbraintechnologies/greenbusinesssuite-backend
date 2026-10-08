package com.mesh_suite.util;

import java.util.Map;

public final class LegacyMaps {

    private LegacyMaps() {
    }

    public static String text(Map<String, Object> source, String... keys) {
        if (source == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = source.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isEmpty() && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return null;
    }

    public static Long number(Map<String, Object> source, String... keys) {
        String text = text(source, keys);
        if (text == null) {
            return null;
        }
        try {
            return Long.valueOf(text.contains(".") ? text.substring(0, text.indexOf('.')) : text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
