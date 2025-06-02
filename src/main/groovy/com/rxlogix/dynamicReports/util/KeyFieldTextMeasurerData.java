package com.rxlogix.dynamicReports.util;

class KeyFieldTextMeasurerData implements ITextMeasurerData {
    private int length;
    private int offset;

    KeyFieldTextMeasurerData(int length, int offset) {
        this.length = length;
        this.offset = offset;
    }

    @Override
    public int getLength() {
        return length;
    }
    
    @Override
    public int getOffset() {
        return offset;
    }
    
    @Override
    public boolean isFieldPartiallyRendered() {
        return length > offset;
    }

    public String toString() {
        return "{length: " + length + ", offset: " + offset + "}";
    }
}