<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.config.GlobalDateRangeInformation; com.rxlogix.util.ViewHelper" %>
<div class="row globalDateRange p-t-0">
    <div class="col-xs-12">
        <label class="dateRangeLabel"><g:message code="app.label.DateRange"/></label>
                <g:select
                        name="globalDateRangeInformation.dateRangeEnum"
                        from="${ViewHelper.getDateRange()}"
                        optionValue="display"
                        optionKey="name"
                        value="${configurationInstance?.globalDateRangeInformation?.dateRangeEnum ?: DateRangeEnum.CUSTOM}"
                        class="form-control"/>
                <g:hiddenField name="configurationInstance.globalDateRangeInformation.id" value="${configurationInstance?.globalDateRangeInformation?.id}"/>
    </div>

    <div class="col-xs-12">
        <g:field type="number" class="top-buffer form-control natural-number relativeDateRangeValue"
                     min="${GlobalDateRangeInformation.constrainedProperties.relativeDateRangeValue.min}"
                     max="${GlobalDateRangeInformation.constrainedProperties.relativeDateRangeValue.max}"
                     name="globalDateRangeInformation.relativeDateRangeValue"
                     placeholder="${message(code: 'enter.x.here')}"
                     style="display: none; width: 50%;"
                     value="${configurationInstance?.globalDateRangeInformation?.relativeDateRangeValue ?: 1}"
        />
        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000">Enter Valid Number</div>

        <div class="fuelux datePickerParentDiv">
            <div class="datepicker" id="datePickerFromDiv" style="display:none">
                <g:message code="app.dateFilter.from"/>
                <span class="required-indicator">*</span>
                <div class="input-group">
                    %{--I have to convert it back to string for date picker to get correct string value with respect to timezone--}%
                    <g:textField name="globalDateRangeInformation.dateRangeStartAbsolute"
                                   value="${renderShortFormattedDate(date: configurationInstance?.globalDateRangeInformation?.dateRangeStartAbsolute)}"
                                   placeholder="${message(code: 'select.start.date')}"
                                   class="form-control"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

            <div class="toolbarInline datepicker" id="datePickerToDiv" style="display:none">
                <g:message code="app.dateFilter.to"/>
                <span class="required-indicator">*</span>
                <div class="input-group">
                <g:textField name="globalDateRangeInformation.dateRangeEndAbsolute"
                               value="${renderShortFormattedDate(date: configurationInstance?.globalDateRangeInformation?.dateRangeEndAbsolute)}"
                               placeholder="${message(code: 'select.end.date')}"
                               class="form-control"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

        </div>
    </div>
</div>


