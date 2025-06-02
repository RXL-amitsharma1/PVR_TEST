package com.rxlogix.dynamicReports.reportTypes.xlsx

enum ExcelSubtotalFunction {
    AVERAGE(1, 101),
    COUNT(2, 102),
    COUNTA(3, 103),
    MAX(4, 104),
    MIN(5, 105),
    PRODUCT(6, 106),
    STDEV(7, 107),
    STDEVP(8, 108),
    SUM(9, 109),
    VAR(10, 110),
    VARP(11, 111)

    int includesHidden
    int ignoresHidden

    private ExcelSubtotalFunction(int includesHidden, int ignoresHidden) {
        this.includesHidden = includesHidden
        this.ignoresHidden = ignoresHidden
    }
}
