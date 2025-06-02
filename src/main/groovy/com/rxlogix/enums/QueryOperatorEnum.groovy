package com.rxlogix.enums

public enum QueryOperatorEnum {
    EQUALS('='), NOT_EQUAL('<>'), LESS_THAN('<'), LESS_THAN_OR_EQUAL('<='), GREATER_THAN('>'), GREATER_THAN_OR_EQUAL('>='),
    //The following are for String operations, and their values are not used
    CONTAINS('contains'), ADVANCE_CONTAINS('advanceContains'), DOES_NOT_CONTAIN('notContain'), START_WITH('startWith'), DOES_NOT_START('notStartWith'), ENDS_WITH('endWith'), DOES_NOT_END('notEndWith'),
    YESTERDAY('lastXDays'), LAST_WEEK('lastXWeeks'), LAST_MONTH('lastXMonths'), LAST_YEAR('lastXYears'),
    LAST_X_DAYS('lastXDays'), LAST_X_WEEKS('lastXWeeks'), LAST_X_MONTHS('lastXMonths'), LAST_X_YEARS('lastXYears'),
    TOMORROW('nextXDays'), NEXT_WEEK('nextXWeeks'), NEXT_MONTH('nextXMonths'), NEXT_YEAR('nextXYears'),
    NEXT_X_DAYS('nextXDays'), NEXT_X_WEEKS('nextXWeeks'), NEXT_X_MONTHS('nextXMonths'), NEXT_X_YEARS('nextXYears'),
    IS_EMPTY('isNull'), IS_NOT_EMPTY('isNotNull')

    private final String val

    QueryOperatorEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public static QueryOperatorEnum[] getNumericAndStringOperators() {
        return [EQUALS, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, IS_EMPTY, IS_NOT_EMPTY, DOES_NOT_CONTAIN, CONTAINS, START_WITH, DOES_NOT_START, ENDS_WITH, DOES_NOT_END]
    }

    public static QueryOperatorEnum[] getNumericOperators() {
        return [EQUALS, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, IS_EMPTY, IS_NOT_EMPTY]
    }

    public static QueryOperatorEnum[] getStringOperators() {
        return [EQUALS, NOT_EQUAL, DOES_NOT_CONTAIN, CONTAINS, ADVANCE_CONTAINS, START_WITH, DOES_NOT_START, ENDS_WITH, DOES_NOT_END, IS_EMPTY, IS_NOT_EMPTY]
    }

    public static QueryOperatorEnum[] getDateOperators() {
        return (getNumericOperators() + [YESTERDAY, LAST_WEEK, LAST_MONTH, LAST_YEAR, LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS,
                                         TOMORROW, NEXT_WEEK, NEXT_MONTH, NEXT_YEAR, NEXT_X_DAYS, NEXT_X_WEEKS, NEXT_X_MONTHS, NEXT_X_YEARS])
    }

    public static QueryOperatorEnum[] getValuelessOperators() {
        return [YESTERDAY, LAST_WEEK, LAST_MONTH, LAST_YEAR, IS_EMPTY, IS_NOT_EMPTY, TOMORROW, NEXT_WEEK, NEXT_MONTH, NEXT_YEAR]
    }

    public static QueryOperatorEnum[] getNumericValueDateOperators() {
        return ([LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS, NEXT_X_DAYS, NEXT_X_WEEKS, NEXT_X_MONTHS, NEXT_X_YEARS])
    }

    public static QueryOperatorEnum[] getEmbaseOperators() {
        //Handled in JS based on the order in the below list
        return [CONTAINS, EQUALS]
    }

    public getI18nKey() {
        return "app.queryOperator.${this.name()}"
    }
}