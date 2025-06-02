<%@ page import="com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.util.ViewHelper" %>

<div class="row columnMeasureSet" sequence="${index}" id="colMeas${index}_template" style="border: dashed 1px #D4D4D4; border-radius: 5px; padding: 10px; width: 270px; margin-right: 30px; margin-top: 10px; float: left;">
    <i class="fa fa-times add-cursor removeColumnMeasure" style="float: right;"></i>
    <div class="col-xs-12" style="margin-top: 0px;">
        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.columns" /></label>
            </div>
        </div>
        <div class="row columnsContainer rowsAndColumnsContainer rowsAndColumnsContainerBorder">
            <div class="col-xs-12 no-padding columnScroll">
                <div hidden="hidden"><g:render template="toAddColumn"></g:render></div>
                <div id="columnsContainer${index}" class="containerToBeSelected columnsContainer"></div>
            </div>
        </div>
        <g:templateListAsJSONStringHidden name="columns${index}" list="${columnMeasure?.columnList}" selectedLocale="${selectedLocale}"/>
        <div class="row" style="margin-top: 5px; margin-bottom: 3px;">
            <div class="col-xs-4 p-l-0">
                <label><g:message code="app.label.measures" /><span class="required-indicator">*</span></label>
            </div>
            <div class="col-xs-8 p-r-0" style="margin-top: 0px;">
                <g:select class="form-control selectMeasure" noSelection="['':message(code:'select.measure')]" name="selectMeasure${index}"
                          from="${ViewHelper.getDataTabulationMeasures()}" optionKey="name" optionValue="display"/>
            </div>
        </div>
        <div class="row measuresContainerBorder">
            <div class="col-xs-12 no-padding columnScroll">
                <div class="measuresContainer"></div>
            </div>
        </div>
        <g:hiddenField class="validMeasureIndex" name="colMeas${index}-validMeasureIndex" />
        <div class="row">
            <div class="col-xs-12 m-t-5 col-xs-12 p-l-0">
                <div class="checkbox checkbox-primary">
                    <g:checkBox class="showTotalIntervalCases" name="showTotalIntervalCases${index}"
                                value="${columnMeasure?.showTotalIntervalCases}" />
                    <label class="no-bold add-cursor " id="showTotalIntervalCasesLabel${index}" for="showTotalIntervalCases${index}">
                      <g:message code="show.total.interval.cases" />
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox class="showTotalCumulativeCases" name="showTotalCumulativeCases${index}"
                                value="${columnMeasure?.showTotalCumulativeCases}" />
                    <label class="no-bold add-cursor " id="showTotalCumulativeCasesLabel${index}" for="showTotalCumulativeCases${index}">
                      <g:message code="show.total.cumulative.cases"/>
                    </label>
                </div>
            </div>
        </div>
    </div>
</div>