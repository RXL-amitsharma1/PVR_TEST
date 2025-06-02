<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.Constants; com.rxlogix.config.SourceProfile" %>
<script>
    var queryReportFieldsOptsBySource = "${createLink(controller: "query",action: "userReportFieldsOptsBySource")}";
    var queryDefaultReportFieldsOpts = "${createLink(controller: "query",action: "userDefaultReportFieldsOpts",params: [lastModified: ViewHelper.getCacheLastModified(currentUser,session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'])])}";
</script>
<g:if test="${!reportConfiguration}">
<asset:javascript src="app/query/copyPasteValues.js"/>
</g:if>

<div id="toAddContainer" class="toAddContainer m-t-5">

    %{--This is used for the criteria builder (the first line where selections are made) --}%
    <div id="errorMessageOperator" class="col-xs-4 col-md-offset-7 errorMessageOperator"><g:message code="app.query.value.invalid.number" /></div>

    %{--This is used inside the builder container to light up errors after user added it and then changed it --}%
    <div id="errorMessageOperatorInline" class="col-xs-4 col-md-offset-7 errorMessageOperatorInline" style="display: none"><g:message code="app.query.value.invalid.number" /></div>

    <div class="col-xs-3 expressionsNoPadFirst" style="position: relative;padding-left: 30px!important;width: 30%">
        <i data-toggle="popover" id="selectPopover" data-trigger="hover" data-container="body" data-html="true"
           data-content="${message(code: ('app.query.field.no.description'))}" class="fa fa-info-circle m-t-5 iPopover"
           style="font-size: 18px;cursor: pointer;padding-right: 10px;position: absolute;z-index: 1;left: 5px;"></i>
        <select name="selectField" id="selectDataSourceField" class="form-control expressionField">
        </select>
    </div>
    <div class="col-xs-3 expressionsOperator">
        <g:select name="selectOperator" from="${[]}"
                  class="form-control expressionOp"
                  noSelection="['': message(code: 'select.operator')]"/>
    </div>

    <div class="col-xs-3">
        <div id="showValue" class="row expressionsNoPad showValue">
            <g:textField name="selectValue" id="selectValue"
                         class="form-control expressionValueText" placeholder="${message(code: 'value')}"/>
        </div>

        <div id="showSelect" class="row expressionsNoPad">
            <g:select name="selectSelect" id="selectSelect" from="${[]}"
                      class="form-control expressionValueSelect"
                      noSelection="['': message(code:'select.value')]" multiple="true"/>
        </div>

        <div id="showSelectAuto" class="row expressionsNoPad">
            <select hidden="hidden" name="selectSelectAuto" id="selectSelectAuto"
                    class="form-control expressionValueSelectAuto"></select>
        </div>

        <div id="showSelectNonCache" class="row expressionsNoPad">
            <select hidden="hidden" name="selectSelectNonCache" id="selectSelectNonCache"
                    class="form-control expressionValueSelectNonCache"></select>
        </div>

        <div id="showDate" class="row fuelux expressionsNoPad">
            <div class="datepicker expressionValueDate" id="selectDate">
                <div class="input-group">
                    <input class="form-control expressionValueDateInput" id="selectDateInput" name="selectDate"
                           type="text"/>

                    <div class="input-group-btn">
                        <button type="button" class="btn btn-primary dropdown-toggle"
                                data-toggle="dropdown">
                            <span class="glyphicon glyphicon-calendar"></span>
                            <span class="sr-only"><g:message code="scheduler.toggleCalendar" /></span>
                        </button>

                        <div class="dropdown-menu dropdown-menu-right datepicker-calendar-wrapper rx-datepicker"
                             role="menu">
                            <div class="datepicker-calendar">
                                <div class="datepicker-calendar-header">
                                    <button type="button" class="prev"><span
                                            class="glyphicon glyphicon-chevron-left"></span><span
                                            class="sr-only"><g:message code="scheduler.previousMonth" /></span></button>
                                    <button type="button" class="next"><span
                                            class="glyphicon glyphicon-chevron-right"></span><span
                                            class="sr-only"><g:message code="scheduler.nextMonth" /></span></button>
                                    <button type="button" class="title">
                                        <span class="month">
                                            <span data-month="0"><g:message code="scheduler.january" /></span>
                                            <span data-month="1"><g:message code="scheduler.february" /></span>
                                            <span data-month="2"><g:message code="scheduler.march" /></span>
                                            <span data-month="3"><g:message code="scheduler.april" /></span>
                                            <span data-month="4"><g:message code="scheduler.may" /></span>
                                            <span data-month="5"><g:message code="scheduler.june" /></span>
                                            <span data-month="6"><g:message code="scheduler.july" /></span>
                                            <span data-month="7"><g:message code="scheduler.august" /></span>
                                            <span data-month="8"><g:message code="scheduler.september" /></span>
                                            <span data-month="9"><g:message code="scheduler.october" /></span>
                                            <span data-month="10"><g:message code="scheduler.november" /></span>
                                            <span data-month="11"><g:message code="scheduler.december" /></span>
                                        </span> <span class="year"></span>
                                    </button>
                                </div>
                                <table class="datepicker-calendar-days">
                                    <thead>
                                    <tr>
                                        <th><g:message code="scheduler.short.sunday" /></th>
                                        <th><g:message code="scheduler.short.monday" /></th>
                                        <th><g:message code="scheduler.short.tuesday" /></th>
                                        <th><g:message code="scheduler.short.wednesday" /></th>
                                        <th><g:message code="scheduler.short.thursday" /></th>
                                        <th><g:message code="scheduler.short.friday" /></th>
                                        <th><g:message code="scheduler.short.saturday" /></th>
                                    </tr>
                                    </thead>
                                    <tbody></tbody>
                                </table>

                                <div class="datepicker-calendar-footer">
                                    <button type="button" class="datepicker-today"><g:message code="scheduler.today" /></button>
                                </div>
                            </div>

                            <div class="datepicker-wheels" aria-hidden="true">
                                <div class="datepicker-wheels-month">
                                    <h2 class="header"><g:message code="scheduler.month" /></h2>
                                    <ul>
                                        <li data-month="0"><button type="button"><g:message code="scheduler.short.january" /></button></li>
                                        <li data-month="1"><button type="button"><g:message code="scheduler.short.february" /></button></li>
                                        <li data-month="2"><button type="button"><g:message code="scheduler.short.march" /></button></li>
                                        <li data-month="3"><button type="button"><g:message code="scheduler.short.April" /></button></li>
                                        <li data-month="4"><button type="button"><g:message code="scheduler.may" /></button></li>
                                        <li data-month="5"><button type="button"><g:message code="scheduler.short.june" /></button></li>
                                        <li data-month="6"><button type="button"><g:message code="scheduler.short.july" /></button></li>
                                        <li data-month="7"><button type="button"><g:message code="scheduler.short.august" /></button></li>
                                        <li data-month="8"><button type="button"><g:message code="schedulere.short.september" /></button></li>
                                        <li data-month="9"><button type="button"><g:message code="scheduler.short.octoberr" /></button></li>
                                        <li data-month="10"><button type="button"><g:message code="scheduler.short.november" /></button></li>
                                        <li data-month="11"><button type="button"><g:message code="scheduler.short.december" /></button></li>
                                    </ul>
                                </div>

                                <div class="datepicker-wheels-year">
                                    <h2 class="header"><g:message code="scheduler.year" /></h2>
                                    <ul></ul>
                                </div>

                                <div class="datepicker-wheels-footer clearfix">
                                    <button type="button" class="btn datepicker-wheels-back"><span
                                            class="glyphicon glyphicon-arrow-left"></span><span
                                            class="sr-only"><g:message code="scheduler.return.to.calendar" /></span></button>
                                    <button type="button"
                                            class="btn datepicker-wheels-select"><g:message code="scheduler.select" /> <span
                                            class="sr-only"><g:message code="scheduler.month.and.year" /></span></button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="showSelectRpField" class="row fuelux expressionsNoPad">
            <select name="selectRptField" id="selectRptField" class="form-control selectField expressionValueRptField" data-prefix="${Constants.RPT_INPUT_PREFIX}">
            </select>
        </div>

        <div class="row extraValues"></div>
    </div>

    <div class="col-xs-1 expressionsNoPad">
            <i class="fa fa-search hideDicIcon" id="searchEvents" data-toggle="modal" data-target="#eventModal" data-hide-dictionary-group="true"></i>
            <i class="fa fa-search hideDicIcon" id="searchProducts" data-toggle="modal" data-target="#productModal" data-hide-dictionary-group="true"></i>
            <i class="fa fa-search hideDicIcon" id="searchStudies" data-toggle="modal" data-target="#studyModal"></i>

        <i class="fa fa-pencil-square-o copy-n-paste modal-link"></i>
    </div>

    <div class="col-xs-1 expressionsNoPad addExpressionButton hidden">
        <g:submitButton type="button" name="Add" id="addExpression"  value="${message(code: "default.button.add.label")}" class="btn btn-primary" />
    </div>

    <div class="col-xs-1 expressionsNoPad">
        <div>
            <div class="row poiSection">
                <div class="col-xs-12">
                    <span class="checkbox checkbox-primary" style="float: left">
                     <g:checkBox name="isPOIInput" class="poiInput" title="Placeholder Input"/><label></label>
                    </span>
                    <g:message code="app.label.free.text.field" />
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12" style="white-space: nowrap;">
                    <span class="checkbox checkbox-primary" style="float: left">
                     <g:checkBox name="isRptField" class="rptFieldInput" title="Field to Field Compare"/>
                        <label>&nbsp;</label>
                    </span>
                    <g:message code="app.label.field.comparison" />
                </div>
            </div>
        </div>
    </div>
</div>
<g:if test="${editable}">
    <g:render template="/query/copyPasteModal" />
</g:if>