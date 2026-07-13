package com.mesh_suite.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class UniqueIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    // yyMMddHHmmss = 12 chars
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private UniqueIdGenerator() {}

    /**
     * Orchard-compliant External Transaction ID (exttrid).
     *
     * Format:  TXN + yyMMddHHmmss + 3-digit random
     * Example: TXN250917143522381
     * Length:  18 chars — must stay ≤ 20 (Orchard limit, resp_code 025)
     *
     *  Do not extend this format beyond 20 total characters.
     */
    public static String generateTransRef() {
        String timestamp = LocalDateTime.now().format(TIME_FMT);
        int rand = RANDOM.nextInt(900) + 100;
        return "TXN" + timestamp + rand;
    }

    /**
     * Invoice ID (not constrained by Orchard)
     */
    public static String generateInvoiceId(Long billId) {
        return "INV-" + billId + "-" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
