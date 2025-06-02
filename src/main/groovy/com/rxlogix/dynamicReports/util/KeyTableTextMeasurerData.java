package com.rxlogix.dynamicReports.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class KeyTableTextMeasurerData implements ITextMeasurerData {
    List<Map<String, KeyFieldTextMeasurerData>> data = new LinkedList<Map<String, KeyFieldTextMeasurerData>>();
    Map<String, KeyFieldTextMeasurerData> currentRow;

    public void addData(String key, int length, int offset) {
        if (currentRow == null || currentRow.get(key) != null) {
            currentRow = new HashMap<String, KeyFieldTextMeasurerData>();
            data.add(currentRow);
        }
        currentRow.put(key, new KeyFieldTextMeasurerData(length, offset));
    }

    @Override
    public int getLength() {
        return data.size();
    }

    @Override
    public int getOffset() {
        int i = 0;
        for (; i < data.size(); i++) {
            Map<String, KeyFieldTextMeasurerData> row = data.get(i);
            for (KeyFieldTextMeasurerData data : row.values()) {
                if (data.isFieldPartiallyRendered()) {
                	return i;
                }
            }
        }
        return i;
    }

    @Override
    public boolean isFieldPartiallyRendered() {
        return getLength() > getOffset();
    }

    public String toString() {
        return String.valueOf(data);
    }
}