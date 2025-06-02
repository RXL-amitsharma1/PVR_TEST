package com.rxlogix.dynamicReports.util;

import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRCommonText;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.fill.JRMeasuredText;
import net.sf.jasperreports.engine.fill.TextMeasurer;
import net.sf.jasperreports.engine.util.JRStyledText;

/**
 * Custom text measurer for the report fields.
 * <p/>
 * The measures sets a variable with rendered symbols count to use the
 * variable on the report's net page
 *
 * @author Sergey Gologuzov (sgologuzov@gmail.com)
 */
public class KeyFieldTextMeasurer extends TextMeasurer {
    private static final String KEY_FIELD_TEXT_MEASURER_DATA = "KEY_FIELD_TEXT_MEASURER_DATA";

    private String key;

    public KeyFieldTextMeasurer(JasperReportsContext jasperReportsContext, JRCommonText textElement) {
        super(jasperReportsContext, textElement);
        this.key = textElement.getKey();
    }

    @Override
    public JRMeasuredText measure(JRStyledText styledText, int remainingTextStart,
                                  int availableStretchHeight, boolean canOverflow) {
        JRMeasuredText measuredText = super.measure(styledText, remainingTextStart, availableStretchHeight, canOverflow);
        if (key != null) {
            int index = key.indexOf('.');
            Map<String, ITextMeasurerData> map = (HashMap<String, ITextMeasurerData>) jasperReportsContext.getValue(KEY_FIELD_TEXT_MEASURER_DATA);
            if (map == null) {
                map = new HashMap<String, ITextMeasurerData>();
                jasperReportsContext.setValue(KEY_FIELD_TEXT_MEASURER_DATA, map);
            }
            if (index == -1) {
                //Single TextField
                map.put(key, new KeyFieldTextMeasurerData(styledText.length(), measuredText.getTextOffset()));
            } else {
                // Table TextField
                KeyTableTextMeasurerData data = (KeyTableTextMeasurerData) map.get(key.substring(0, index));
                if (data == null) {
                    data = new KeyTableTextMeasurerData();
                    map.put(key.substring(0, index), data);
                }
                data.addData(key, styledText.length(), measuredText.getTextOffset());
            }
        }
        return measuredText;
    }

    public static boolean isFieldPartiallyRendered(JasperReportsContext jasperReportsContext, String key) {
        Map<String, ITextMeasurerData> map = (HashMap<String, ITextMeasurerData>) jasperReportsContext.getValue(KEY_FIELD_TEXT_MEASURER_DATA);
        if (map == null) {
            return false;
        }
        ITextMeasurerData data = map.get(key);
        if (data == null) {
            return false;
        }
        return data.isFieldPartiallyRendered();
    }

    public static int getFieldTextOffset(JasperReportsContext jasperReportsContext, String key) {
        Map<String, ITextMeasurerData> map = (HashMap<String, ITextMeasurerData>) jasperReportsContext.getValue(KEY_FIELD_TEXT_MEASURER_DATA);
        if (map == null) {
            return 0;
        }
        ITextMeasurerData data = map.get(key);
        if (data == null) {
            return 0;
        }
        return data.getOffset();
    }

    public static void resetKeyFieldTextMeasurerData(JasperReportsContext jasperReportsContext) {
        jasperReportsContext.setValue(KEY_FIELD_TEXT_MEASURER_DATA, new HashMap<String, KeyFieldTextMeasurerData>());
    }
}