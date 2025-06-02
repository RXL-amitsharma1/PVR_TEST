<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.config.GlobalDateRangeInformation; com.rxlogix.util.ViewHelper" %>
<div class="row globalDateRange p-t-0">
    <div class="col-xs-12">
        <label><g:message code="app.label.DateRange"/></label>
                <g:select
                        name="caseSeriesDateRangeInformation.dateRangeEnum"
                        from="${ViewHelper.getDateRange()}"
                        optionValue="display"
                        optionKey="name"
                        value="${dateRangeInformation?.dateRangeEnum ?: DateRangeEnum.CUSTOM}"
                        class="form-control"/>
                <g:hiddenField name="seriesInstance.caseSeriesDateRangeInformation.id" value="${dateRangeInformation?.id}"/>
    </div>

    <div class="col-xs-12">
        <g:field type="number"  class="top-buffer form-control natural-number relativeDateRangeValue"
                 min="${GlobalDateRangeInformation.constrainedProperties.relativeDateRangeValue.min}"
                 max="${GlobalDateRangeInformation.constrainedProperties.relativeDateRangeValue.max}"
                 name="caseSeriesDateRangeInformation.relativeDateRangeValue"
                 placeholder="${message(code: 'enter.x.here')}"
                 style="display: none; width: 50%;"
                 value="${dateRangeInformation?.relativeDateRangeValue ?: 1}"/>
        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000"><g:message code="caseSeries.global.date.range.not.valid.number"/></div>

        <div class="fuelux datePickerParentDiv">
            <div class="text datepicker" id="datePickerFromDiv" style="display:none">
                <g:message code="app.dateFilter.from"/>
                <div class="input-group">
                    %{--I have to convert it back to string for date picker to get correct string value with respect to timezone--}%
                    <g:textField name="caseSeriesDateRangeInformation.dateRangeStartAbsolute"
                                   value="${renderShortFormattedDate(date: dateRangeInformation?.dateRangeStartAbsolute)}"
                                   placeholder="${message(code: 'select.start.date')}"
                                   class="form-control"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

            <div class="toolbarInline datepicker" id="datePickerToDiv" style="display:none">
                <g:message code="app.dateFilter.to"/>
                <div class="input-group">
                <g:textField name="caseSeriesDateRangeInformation.dateRangeEndAbsolute"
                               value="${renderShortFormattedDate(date: dateRangeInformation?.dateRangeEndAbsolute)}"
                               placeholder="${message(code: 'select.end.date')}"
                               class="form-control"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

        </div>
    </div>
</div>


