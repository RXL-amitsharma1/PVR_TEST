<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.reportTemplate.PercentageOptionEnum; com.rxlogix.util.ViewHelper; com.rxlogix.reportTemplate.MeasureTypeEnum" %>

<div class="row measureOptions" style="padding-top: 20px;" hidden="hidden" id="colMeas${colMeasIndex}-meas${measIndex}">
    <div class="col-xs-12" style="padding-left: 30px; padding-right: 30px;">
        <div class="row measureOptionsBorder">

            <i class="fa fa-times add-cursor closeMeasureOptions" style="float: right;"></i>
            <g:hiddenField class="measureType" name="colMeas${colMeasIndex}-meas${measIndex}-type" value="${measure?.type}" />
            <g:hiddenField class="measureSort" name="colMeas${colMeasIndex}-meas${measIndex}-sort" value="${measure?.sort}" />
            <g:hiddenField class="measureSortLevel" name="colMeas${colMeasIndex}-meas${measIndex}-sortLevel" value="${measure?.sort}" />
            <div class="row d-t-border">
            <div class="rxmain-container-header"><a class="theme-color case-count-strip" href="javascript:void(0)"><i class="openCloseIcon fa fa-lg click fa-caret-down"></i>  <span class="fieldSectionHeader"></span></a></div>
                <div class="case-count-section row p-10">
                    <div class="col-xs-2">
                        <label><g:message code="app.label.name" /></label>
                        <input class="form-control inputMeasureName" name="colMeas${colMeasIndex}-meas${measIndex}-name" value="${measure?.name}">
                    </div>
                    <div class="col-xs-3">
                        <label><g:message code="app.label.countType" /></label>
                        <g:select class="form-control" name="colMeas${colMeasIndex}-meas${measIndex}-dateRangeCount"
                                  from="${ViewHelper.getDataTabulationCounts()}" value="${measure?.dateRangeCount}"
                                  optionKey="name" optionValue="display"/>
                    </div>
                    <div class="col-xs-2">
                        <div style="display: none;">
                            <label>&nbsp;</label>
                            <input class="form-control measureRelativeDateRangeValue" required="required" min="1"
                                   id="colMeas${colMeasIndex}-meas${measIndex}-relativeDateRangeValue"
                                   name="colMeas${colMeasIndex}-meas${measIndex}-relativeDateRangeValue"
                                   placeholder="${message(code: 'enter.x.here')}"
                                   style="width: 50%;" type="number"
                                   value="${measure?.relativeDateRangeValue ?: '1'}"/>
                        </div>
                        <div class="fuelux customPeriodDatePickers" style="padding: 5px;" hidden="hidden">
                            <g:hiddenField class="form-control customPeriodFrom" hidden="hidden" type="text"
                                           name="colMeas${colMeasIndex}-meas${measIndex}-customPeriodFrom"
                                           value="${renderShortFormattedDate(date: measure?.customPeriodFrom)}"/>

                            <div class="datepicker" id="colMeas${colMeasIndex}-meas${measIndex}-datePickerFrom">
                                <g:message code="app.dateFilter.from"/>
                                <div class="input-group">
                                    <input placeholder="${message(code: 'select.start.date')}" class="form-control" type="text"/>
                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>

                            <g:hiddenField class="form-control customPeriodTo" hidden="hidden" type="text"
                                           name="colMeas${colMeasIndex}-meas${measIndex}-customPeriodTo"
                                           value="${renderShortFormattedDate(date: measure?.customPeriodTo)}" />

                            <div class="datepicker" id="colMeas${colMeasIndex}-meas${measIndex}-datePickerTo">
                                <g:message code="app.dateFilter.to"/>
                                <div class="input-group">
                                    <input placeholder="Select End Date" class="form-control" type="text"/>
                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-2 percentageOption">
                        <label><g:message code="app.percentageOptionEnum.label" /></label>
                        <select class="form-control" name="colMeas${colMeasIndex}-meas${measIndex}-percentageOption" id="colMeas${colMeasIndex}-meas${measIndex}-percentageOption">
                            <g:each var="op" in="${PercentageOptionEnum.values()}">
                                <option ${(measure?.percentageOption==op)?"selected":""} value="${op.name()}"><g:message code="${op.i18nKey}" /></option>
                            </g:each>
                        </select>
                    </div>
                    <div class="col-xs-3">
                        <div>
                            <div class="form-inline">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="colMeas${colMeasIndex}-meas${measIndex}-showTotal" value="${measure?.showTotal}"/>
                                    <label id="colMeas${colMeasIndex}-meas${measIndex}-showTotalLabel"  class="no-bold add-cursor" for="colMeas${colMeasIndex}-meas${measIndex}-showTotal"> <g:message code="show.total"/></label>
                                </div>
                            </div>
                            <div class="form-inline topXPane m-t-5" ${measure?.type == MeasureTypeEnum.CASE_LIST ? "hidden" : ""}>
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="colMeas${colMeasIndex}-meas${measIndex}-showTopX"
                                                value="${measure?.showTopX}" class="showTopX"/>
                                    <label id="colMeas${colMeasIndex}-meas${measIndex}-showTopXLabel" class="no-bold add-cursor" for="colMeas${colMeasIndex}-meas${measIndex}-showTopX"> <g:message code="show.topX"/></label>
                                </div>
                                <g:textField name="colMeas${colMeasIndex}-meas${measIndex}-topXCount"
                                             value="${measure?.topXCount}" class="form-control topXCount" disabled="${!measure?.showTopX}"/>
                                <label id="colMeas${colMeasIndex}-meas${measIndex}-topXCountLabel" class="no-bold add-cursor"><g:message code="show.values"/></label>
                            </div>
                        </div>
                        <div class="row" ${measure?.type == MeasureTypeEnum.CASE_LIST ? "hidden" : ""}>
                            <div class="col-sm-12 form-inline m-t-5">

                                <div class="checkbox checkbox-primary">
                                    <g:checkBox type="checkbox" class="showTopColumn" value="${measure?.topColumnX > 0}" name="colMeas${colMeasIndex}-meas${measIndex}-showTopColumn"/>
                                    <label class="no-bold add-cursor" id="colMeas${colMeasIndex}-meas${measIndex}-showTopColumnLabel" for="colMeas${colMeasIndex}-meas${measIndex}-showTopColumn"><g:message code="show.topX"/></label>

                                </div>
                                <g:textField class="form-control topColumnX" type="number" name="colMeas${colMeasIndex}-meas${measIndex}-topColumnX"
                                             value="${measure?.topColumnX}" disabled="${!(measure?.topColumnX > 0)}" style="width: 50px"/><g:message code="app.label.columns"/>
                                <g:select from="${ViewHelper.topColumnTypeEnum()}" name="colMeas${colMeasIndex}-meas${measIndex}-topColumnType" value="${measure?.topColumnType}"
                                          optionKey="name" optionValue="display" style="width: 150px"
                                          class="form-control topColumnType m-t-5" disabled="${!(measure?.topColumnX > 0)}"/>

                            </div>
                        </div>
                    </div>
                </div>

<sec:ifAnyGranted roles="ROLE_CUSTOM_EXPRESSION">
            <div class="row m-t-10 d-t-border case-count-section" style="margin: 0">
                <div class="rxmain-container-header">
                    <a class="theme-color showHideAdvancedSettings add-cursor"><i class="openCloseIcon fa fa-lg fa-caret-right "></i> <g:message code="app.dataTabulation.AdvancedSettings" default="Advanced Settings" /></a>
                </div>
                <div class="col-xs-12 advancedSettings p-10" style="display: none">


            <div class="row">
                <div class="col-xs-6">
                    <label><g:message code="app.label.drillDownTemplate" /></label>
                    <g:select type="hidden" name="colMeas${colMeasIndex}-meas${measIndex}-drillDown" from="${[]}"
                                   data-value="${measure?.drillDownTemplate?.id}" class="form-control drilldownTemplate"/>
                </div>

                <div class="col-xs-2">
                    <label>&nbsp;</label><div>
                    <a href="${templateQueryInstance?.id ?createLink(controller: 'template' , action: 'view', id: templateQueryInstance?.id):'#'}"
                       title="${message(code: 'app.label.viewTemplate')}" target="_blank" class="pv-ic templateQueryIcon templateViewButton glyphicon glyphicon-info-sign ${templateQueryInstance?.id ? '' : 'hide'}"></a>
                </div>
                </div>
            </div>
            <div class="row m-b-10">
                <div class="col-xs-2"><label><g:message code="app.label.specialChartSettings" /></label></div>
                <div class="col-xs-3">
                    <label><g:message code="app.label.measureValues" /></label>
                    <select name="colMeas${colMeasIndex}-meas${measIndex}-valuesChartType" value="${measure?.valuesChartType}" class="form-control">
                        <option ${measure?.valuesChartType == "" ? "selected" : ""} value=""><g:message code="app.label.show.as.default"/></option>
                        <option ${measure?.valuesChartType == "hide" ? "selected" : ""} value="hide"><g:message code="app.label.show.as.hide"/></option>
                        <option ${measure?.valuesChartType == "line" ? "selected" : ""} value="line"><g:message code="app.label.show.as.line"/></option>
                        <option ${measure?.valuesChartType == "spline" ? "selected" : ""} value="spline"><g:message code="app.label.show.as.spline"/></option>
                        <option ${measure?.valuesChartType == "column" ? "selected" : ""} value="column"><g:message code="app.label.show.as.column"/></option>
                        <option ${measure?.valuesChartType == "area" ? "selected" : ""} value="area"><g:message code="app.label.show.as.area"/></option>
                    </select>
                </div>
                <div class="col-xs-2">
                    <label><g:message code="app.label.percentageValues" /></label>
                    <select name="colMeas${colMeasIndex}-meas${measIndex}-percentageChartType" disabled value="${measure?.percentageChartType}" class="percentageChartType form-control">
                        <option ${!measure?.percentageChartType ? "selected" : ""} value=""><g:message code="app.label.show.as.hide"/></option>
                        <option ${measure?.percentageChartType == "line" ? "selected" : ""} value="line"><g:message code="app.label.show.as.line"/></option>
                        <option ${measure?.percentageChartType == "spline" ? "selected" : ""} value="spline"><g:message code="app.label.show.as.spline"/></option>
                        <option ${measure?.percentageChartType == "column" ? "selected" : ""} value="column"><g:message code="app.label.show.as.column"/></option>
                        <option ${measure?.percentageChartType == "area" ? "selected" : ""} value="area"><g:message code="app.label.show.as.area"/></option>
                    </select>
                </div>
                <div class="col-xs-2 valueAxisLabelDiv">
                    <div class="checkbox checkbox-primary">
                        <input type="checkbox" name="colMeas${colMeasIndex}-meas${measIndex}-overrideValueAxisLabel" id="colMeas${colMeasIndex}-meas${measIndex}-overrideValueAxisLabel" ${measure?.valueAxisLabel!=null?"checked":""} autocomplete="off" class="changed-input overrideValueAxisLabel">

                        <label class="add-cursor" id="colMeas${colMeasIndex}-meas${measIndex}-overrideValueAxisLabelLabel" for="colMeas${colMeasIndex}-meas${measIndex}-overrideValueAxisLabel">
                        <g:message code="app.label.overwriteValueAxisLabel"/>
                        </label>
                    </div>
                    <input class="form-control valueAxisLabel" ${measure?.valueAxisLabel==null?"disabled":""} name="colMeas${colMeasIndex}-meas${measIndex}-valueAxisLabel" value="${measure?.valueAxisLabel}">
                </div>
                <div class="col-xs-3 valueAxisLabelDiv">
                    <div class="checkbox checkbox-primary">
                        <input type="checkbox" name="colMeas${colMeasIndex}-meas${measIndex}-overridePercentageAxisLabel" id="colMeas${colMeasIndex}-meas${measIndex}-overridePercentageAxisLabel" ${measure?.percentageAxisLabel!=null?"checked":""} autocomplete="off" class="changed-input overridePercentageAxisLabel">
                        <label class="add-cursor" id="colMeas${colMeasIndex}-meas${measIndex}-overridePercentageAxisLabelLabel" for="colMeas${colMeasIndex}-meas${measIndex}-overridePercentageAxisLabel">
                        <g:message code="app.label.overwritePercentageAxisLabel"/>
                        </label>
                    </div>
                    <input class="form-control percentageAxisLabel" ${measure?.percentageAxisLabel==null?"disabled":""} name="colMeas${colMeasIndex}-meas${measIndex}-percentageAxisLabel" value="${measure?.percentageAxisLabel}">
                </div>

            </div>
            </div>
          </div>
            <div class="row d-t-border m-t-10 case-count-section" style="margin: 0">
                <div class="col-xs-12 colorConditionsCantainer">
                    <g:hiddenField name="colMeas${colMeasIndex}-meas${measIndex}-colorConditions"
                                   value="${measure?.colorConditions}" class="colorConditionsJson"/>
                    <g:render template="includes/colorConditionsTable"/>
                </div>
            </div>
</sec:ifAnyGranted>
        </div>
    </div>
</div>
</div>
<g:render template="/includes/widgets/infoTemplate" model="[messageBody: message(code: 'app.data.tabulation.measures.warning.info')]"/>
<g:render template="/includes/widgets/warningTemplate" model="[messageBody: message(code: 'app.data.tabulation.showWorldMap.warning'), warningModalId:'showWorldMapWarning', queryType: ' ' ]"/>
<g:render template="/includes/widgets/warningTemplate" model="[messageBody: message(code: 'app.data.tabulation.measures.drilldown.warning'), warningModalId:'drillDownWarning', queryType: ' ' ]"/>
<g:render template="/includes/widgets/warningTemplate" model="[messageBody: message(code: 'app.data.tabulation.measures.imageAsChart.warningModal'), warningModalId:'chartExportAsImageWarning', queryType: ' ' ]"/>