<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.config.GlobalDateRangeInformation; com.rxlogix.util.ViewHelper" %>
<div class="row globalDateRange p-t-0">
    <div class="col-xs-12">
        <label class="dateRangeLabel"><g:message code="app.label.DateRange"/></label>
                <g:select
                        name="globalDateRangeInformationAutoROD.dateRangeEnum"
                        from="${ViewHelper.getDateRange()}"
                        optionValue="display"
                        optionKey="name"
                        value="${autoReasonOfDelayInstance?.globalDateRangeInformationAutoROD?.dateRangeEnum ?: DateRangeEnum.CUMULATIVE}"
                        class="form-control"/>
                <g:hiddenField name="autoReasonOfDelayInstance.globalDateRangeInformationAutoROD.id" value="${autoReasonOfDelayInstance?.globalDateRangeInformationAutoROD?.id}"/>
    </div>

    <div class="col-xs-12">
        <g:field type="number" class="top-buffer form-control natural-number relativeDateRangeValue"
                     min="${GlobalDateRangeInformation.constrainedProperties.relativeDateRangeValue.min}"
                     max="${GlobalDateRangeInformation.constrainedProperties.relativeDateRangeValue.max}"
                     name="globalDateRangeInformationAutoROD.relativeDateRangeValue"
                     placeholder="${message(code: 'enter.x.here')}"
                     style="display: none; width: 50%;"
                     value="${autoReasonOfDelayInstance?.globalDateRangeInformationAutoROD?.relativeDateRangeValue ?: 1}"
        />
        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000">Enter Valid Number</div>

        <div class="fuelux datePickerParentDiv">
            <div class="datepicker" id="datePickerFromDiv" style="display:none">
                <g:message code="app.dateFilter.from"/>
                <div class="input-group">
                    %{--I have to convert it back to string for date picker to get correct string value with respect to timezone--}%
                    <g:textField name="globalDateRangeInformationAutoROD.dateRangeStartAbsolute"
                                   value="${renderShortFormattedDate(date: autoReasonOfDelayInstance?.globalDateRangeInformationAutoROD?.dateRangeStartAbsolute)}"
                                   placeholder="${message(code: 'select.start.date')}"
                                   class="form-control"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

            <div class="toolbarInline datepicker" id="datePickerToDiv" style="display:none">
                <g:message code="app.dateFilter.to"/>
                <div class="input-group">
                <g:textField name="globalDateRangeInformationAutoROD.dateRangeEndAbsolute"
                               value="${renderShortFormattedDate(date: autoReasonOfDelayInstance?.globalDateRangeInformationAutoROD?.dateRangeEndAbsolute)}"
                               placeholder="${message(code: 'select.end.date')}"
                               class="form-control"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

        </div>
    </div>
</div>



