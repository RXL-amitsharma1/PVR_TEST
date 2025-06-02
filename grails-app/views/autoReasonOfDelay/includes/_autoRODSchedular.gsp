<%@ page import="java.text.SimpleDateFormat; com.rxlogix.util.DateUtil; grails.util.Environment; com.rxlogix.config.AutoReasonOfDelay;  com.rxlogix.config.ApplicationSettings; com.rxlogix.config.CaseSeries; com.rxlogix.user.User; com.rxlogix.user.UserGroup; com.rxlogix.Constants; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormatEnum; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil" %>
<g:set var="userService" bean="userService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>
<asset:javascript src="app/emailAttachmentSplit.js"/>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click"
               data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.scheduler"/><span class="required-indicator">*</span>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">

            <div class="row">
                <div class="col-xs-12 col-md-6">
                    <g:render template="/configuration/schedulerTemplate" model="[autoReasonOfDelayInstance: autoReasonOfDelayInstance, isAutoRODSchedular: true]"/>
                    <g:hiddenField name="isEnabled" id="isEnabled" value="${autoReasonOfDelayInstance?.isEnabled}"/>
                    <g:hiddenField name="schedulerTime" value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(currentUser)}"/>
                    <g:scheduleDateJSON value="${autoReasonOfDelayInstance?.scheduleDateJSON ?: null}" name="scheduleDateJSON"></g:scheduleDateJSON>
                    <input type="hidden" name="configSelectedTimeZone" id="configSelectedTimeZone"
                           value="${autoReasonOfDelayInstance?.configSelectedTimeZone ?: userService.getCurrentUser()?.preference?.timeZone}"/>
                    <input type="hidden" id="timezoneFromServer" name="timezone"
                           value="${DateUtil.getTimezone(currentUser)}"/>
                </div>
            </div>
        </div>
    </div>
</div>
