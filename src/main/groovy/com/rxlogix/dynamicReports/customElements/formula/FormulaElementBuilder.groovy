package com.rxlogix.dynamicReports.customElements.formula

import net.sf.dynamicreports.report.builder.component.GenericElementBuilder
import net.sf.dynamicreports.report.definition.expression.DRIExpression

class FormulaElementBuilder extends GenericElementBuilder {
    private static final String NAMESPACE = "http://www.rxlogix.com/customElements"
    private static final String NAME = "formula"
    static final String TEXT_PARAMETER_NAME = "TEXT"

    protected FormulaElementBuilder() {
        super(NAMESPACE, NAME)
    }

    FormulaElementBuilder setText(String textExpression) {
        addParameter(TEXT_PARAMETER_NAME, textExpression)
        return this
    }

    FormulaElementBuilder setText(DRIExpression<String> textExpression) {
        addParameter(TEXT_PARAMETER_NAME, textExpression)
        return this
    }
}
