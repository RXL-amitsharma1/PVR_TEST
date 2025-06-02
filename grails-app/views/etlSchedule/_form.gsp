<%@ page import="java.text.SimpleDateFormat; com.rxlogix.util.DateUtil; grails.util.Environment" %>
<div class="row">
    <div class="col-md-6">
        <label class="control-label" >
            <g:message code="etlMaster.scheduleName.label"/> : ${etlScheduleInstance?.scheduleName}
        </label>
    </div>
 </div>
<div class="horizontalRuleFull"></div>
<div class="row">

    <div class="col-xs-12 col-md-6">
        <g:render template="/configuration/schedulerTemplate" model="[etlScheduleInstance: etlScheduleInstance, isEtlScheduler: true]"/>
        <g:hiddenField id="isDisabled" name="isDisabled"  value="${etlScheduleInstance?.isDisabled}"/>
        <g:hiddenField id="startDateTime" name="startDateTime" value="${etlScheduleInstance?.startDateTime}"/>
        <g:hiddenField id="repeatInterval" name="repeatInterval" value="${etlScheduleInstance?.repeatInterval}" />
        <g:hiddenField id="timezoneFromServer" name="timezone" value="${com.rxlogix.util.DateUtil.getTimezone(userService.getUser())}"/>
        <input type="hidden" id="emailToUserSelected"  value="${etlScheduleInstance?.emailToUsers ?: ''}"/>
    </div>
</div>
<g:render template="/configuration/includes/emailConfiguration" model="[emailConfiguration: etlScheduleInstance?.emailConfiguration]"/>
<g:render template="/email/includes/copyPasteEmailModal"/>
