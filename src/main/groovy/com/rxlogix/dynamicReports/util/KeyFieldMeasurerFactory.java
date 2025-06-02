package com.rxlogix.dynamicReports.util;

import net.sf.jasperreports.engine.JRCommonText;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.fill.JRTextMeasurer;
import net.sf.jasperreports.engine.fill.TextMeasurerFactory;

/**
 * Custom text measurer factory for report fields.
 * <p/>
 * This factory produces {@link KeyFieldTextMeasurer} instances.
 *
 * @author Sergey Gologuzov (sgologuzov@gmail.com)
 */
public class KeyFieldMeasurerFactory extends TextMeasurerFactory {
    /**
     * Returns a {@link KeyFieldTextMeasurer} instance for the text object.
     */
    @Override
    public JRTextMeasurer createMeasurer(JasperReportsContext jasperReportsContext, JRCommonText text) {
        return new KeyFieldTextMeasurer(jasperReportsContext, text);
    }
}
