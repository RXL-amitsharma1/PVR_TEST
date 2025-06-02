<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>
<div class="row dateRange">
    <div class="col-xs-12">
        <label><g:message code="app.label.DateRange"/></label>
        <div class="row">
            <div class="col-xs-12">
                <g:select name="queriesRCA[${i}].dateRangeInformationForQueryRCA.dateRangeEnum"
                          from="${ViewHelper.getDateRange(isForPeriodicReport, !isForPeriodicReport)}"
                          optionValue="display"
                          optionKey="name"
                          value="${queryRCAInstance?.dateRangeInformationForQueryRCA?.dateRangeEnum?: DateRangeEnum.PR_DATE_RANGE}"
                          class="form-control dateRangeEnumClass"/>
            </div>
        </div>

    </div>
    <div class="col-xs-12">
        <g:textField class="top-buffer form-control relativeDateRangeValue" name="queriesRCA[${i}].dateRangeInformationForQueryRCA.relativeDateRangeValue"
               placeholder="${message(code: 'enter.x.here')}"
               style="display: none; width: 50%;"
               value="${queryRCAInstance?.dateRangeInformationForQueryRCA?.relativeDateRangeValue ?: 1}"/>
        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000"> <g:message code="app.query.value.invalid.number" /> </div>

        <div class="fuelux datePickerParentDiv">
            <div class="datepicker" id="queriesRCA[${i}].datePickerFromDiv" style="display:none">
                <div style="margin-top: 10px;">
                    <g:message code="app.dateFilter.from"/>
                </div>
                <div class="input-group">
                    <g:textField name="queriesRCA[${i}].dateRangeInformationForQueryRCA.dateRangeStartAbsolute"
                                 value="${renderShortFormattedDate(date: queryRCAInstance?.dateRangeInformationForQueryRCA?.dateRangeStartAbsolute)}"
                                 placeholder="${message(code: 'app.label.startDate')}"
                                 class="form-control"/>

                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

            <div class="datepicker" id="queriesRCA[${i}].datePickerToDiv" style="display:none">
                <div style="margin-top: 10px;">
                    <g:message code="app.dateFilter.to" />
                </div>
                <div class="input-group">
                    <g:textField name="queriesRCA[${i}].dateRangeInformationForQueryRCA.dateRangeEndAbsolute"
                                 value="${renderShortFormattedDate(date: queryRCAInstance?.dateRangeInformationForQueryRCA?.dateRangeEndAbsolute)}"
                                 placeholder="${message(code: 'app.label.endDate')}"
                                 class="form-control"/>

                    <g:render template="/includes/widgets/datePickerTemplate" />
                </div>
            </div>

        </div>
    </div>
</div>



