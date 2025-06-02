<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container ">
    <g:set var="userService" bean="userService"/>
    <g:set var="currentUser" value="${userService.getUser()}"/>
    <g:hiddenField name="owner" id="owner" value="${configurationInstance?.owner?.id ?: currentUser.id}"/>

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header report-header-collapse">
            <label class="rxmain-container-header-label report-lable-collapse">
                <g:message code="app.label.selectionCriteria"/>
            </label>
        </div>

        <div class="rxmain-container-content report-content">

            %{-- Report Criteria & Sections --}%
            <g:render template="/configuration/includes/reportCriteriaSection"
                      model="[configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, currentUser: currentUser, editMode: editMode]"/>

        </div>
    </div>
</div>
