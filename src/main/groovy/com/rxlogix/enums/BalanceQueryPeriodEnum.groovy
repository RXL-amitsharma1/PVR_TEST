package com.rxlogix.enums

enum BalanceQueryPeriodEnum {

    ETL_START_DATE("ETL Execution Date Range"),
    LAST_X_ETL("Last X ETL"),
    LAST_X_DAYS("Last X Days"),
    CASE_LIST("Case List")

    private final String value

    BalanceQueryPeriodEnum(String value) {
        this.value = value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }
}