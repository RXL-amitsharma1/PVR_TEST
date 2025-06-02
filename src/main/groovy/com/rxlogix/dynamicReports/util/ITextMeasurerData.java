package com.rxlogix.dynamicReports.util;

public interface ITextMeasurerData {
    int getLength();

    int getOffset();

    boolean isFieldPartiallyRendered();
}