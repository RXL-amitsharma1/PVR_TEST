package com.rxlogix.reportTemplate

public enum MeasureTypeEnum {
// value in brackets - code in column name in report result json, for ex., for CASE_COUNT values are coming like
//    GP_1_CC11: <value>, for VERSION_COUNT - GP_1_CV11: <value>, for EVENT_COUNT - GP_1_CE11: <value>,....
// format is GP_<COLUMN_N>_<MEASURE_TYPE><UNKNOWN><SET_NUMBER>
// COLUMN_N - column number iterator inside set, starts with 0
// MEASURE_TYPE - one ofe above or one of HeaderTabDTO.MEASURE_TYPE_TO_ALIASES(first column count, second - percentage (both), third - total)
// SET_NUMBER starts with 1
// UNKNOWN - has values 1 as period value, 4 - for total, 2 - for cumulative
//
// also there can be CASE_COUNT<SET_NUMBER> and INTERVAL_CASE_COUNT<SET_NUMBER> without GP
// row columns has forma ROW_N , N- iterator starts with 1


    CASE_COUNT("CC"),
    VERSION_COUNT("CV"),
    EVENT_COUNT("CE"),
    PRODUCT_EVENT_COUNT("CP"),
    REPORT_COUNT("CR"),
    CASE_LIST("CASE_LIST"),
    COMPLIANCE_RATE("PA"),
    ROW_COUNT("CB")

    final String code

    MeasureTypeEnum(String code) {
        this.code = code
    }

    public getI18nKey() {
        return "app.measureTypeEnum.${this.name()}"
    }

    public getCode() {
        return code
    }

    public static MeasureTypeEnum getByCode(String code) {
        return MeasureTypeEnum.values().find { it.code == code }
    }

}
