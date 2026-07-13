package com.mesh_suite.constant.company;

public enum CompanyStatus {
    ACTIVE, INACTIVE, SUSPENDED,ALL;

    public enum RecurringType {
        NON_RECURRING,
        DAILY,
        WEEKLY,
        BI_WEEKLY,
        MONTHLY,
        QUARTERLY,
        ANNUAL
    }
}
