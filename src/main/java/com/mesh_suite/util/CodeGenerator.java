package com.mesh_suite.util;


import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class CodeGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int TEMP_PASSWORD_LENGTH = 10;

    public String generateTemporaryPassword() {
        return generateRandomString(PASSWORD_CHARS, TEMP_PASSWORD_LENGTH);
    }

    public String generateVerificationCode() {
        return generateRandomString(ALPHA_NUMERIC, CODE_LENGTH);
    }

    private String generateRandomString(String characterSet, int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(characterSet.length());
            sb.append(characterSet.charAt(index));
        }

        return sb.toString();
    }
}
