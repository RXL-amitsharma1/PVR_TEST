package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.ChartDataSource

/**
 * Created by gologuzov on 10.09.16.
 */
class ValueDataSource implements ChartDataSource<Number> {
    private List<Map<String, ?>> data
    private String dataSheetName
    private String column
    private List totalRowIndices


    ValueDataSource(List<Map<String, ?>> data) {
        this(data, null, null, [])
    }

    ValueDataSource(List<Map<String, ?>> data, String dataSheetName, String column, List totalRowIndices) {
        this.data = data
        this.dataSheetName = dataSheetName
        this.column = column
        this.totalRowIndices = totalRowIndices
    }

    @Override
    int getPointCount() {
        return data.size()
    }

    @Override
    Number getPointAt(int index) {
        return data[index].y
    }

    @Override
    boolean isReference() {
        return true
    }

    @Override
    boolean isNumeric() {
        return true
    }

    @Override
    boolean hasFormulaString() {
        return dataSheetName != null && column != null
    }

    @Override
    String getFormulaString() {
        def startIndex = 4
        def endIndex = startIndex + data.size() + totalRowIndices.size() - 1
        def formula = []
        def currentIndex = startIndex
        totalRowIndices.each {
            if (currentIndex == (it + startIndex)) {
                currentIndex++; return;
            }
            if (currentIndex < endIndex) {
                formula.add("'${dataSheetName}'!\$${column}\$${currentIndex}:\$${column}\$${startIndex + (it - 1)}")
                currentIndex = startIndex + it + 1
            }
        }
        return formula.join(",")
    }

    @Override
    boolean isMultiLevel() {
        return false
    }
}
