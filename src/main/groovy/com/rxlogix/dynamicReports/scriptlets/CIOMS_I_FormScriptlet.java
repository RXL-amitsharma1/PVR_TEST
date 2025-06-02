package com.rxlogix.dynamicReports.scriptlets;

import com.rxlogix.dynamicReports.util.KeyFieldTextMeasurer;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.engine.JasperReportsContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CIOMS_I_FormScriptlet extends JRDefaultScriptlet {
    private static final Log log = LogFactory.getLog(CIOMS_I_FormScriptlet.class);

    @Override
    public void beforeDetailEval() {
        try {
            KeyFieldTextMeasurer.resetKeyFieldTextMeasurerData((JasperReportsContext) getParameterValue(JRParameter.JASPER_REPORTS_CONTEXT));
        } catch (JRScriptletException e) {
            log.error(e.getMessage());
        }
    }
}
