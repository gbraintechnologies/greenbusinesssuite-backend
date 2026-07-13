package com.mesh_suite.util;

import java.security.SecureRandom;
import java.util.Base64;

public class FormUtils {
    private static final int RANDOM_STRING_LENGTH = 16;

    public enum PublishStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED,
        UNPUBLISHED;
    }

    public enum DenominationType {
        Coin, Note
    }

    public static String generateUniqueApiKey() {
        String randomString = generateRandomString();
        long timestamp = System.currentTimeMillis();
        String apiKey = timestamp + "-" + randomString;

        return apiKey;
    }

    private static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[RANDOM_STRING_LENGTH];
        random.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }
}
