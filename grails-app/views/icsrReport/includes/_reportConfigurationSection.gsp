<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container ">
    <g:hiddenField name="owner" id="owner"
                   value="${configurationInstance?.owner?.id ?: sec.loggedInUserInfo(field: 'id')}"/>

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header report-header-collapse">
            <label class="rxmain-container-header-label report-lable-collapse">
                <g:message code="app.label.selectionCriteria"/>
            </label>
        </div>

        <div class="rxmain-container-content report-content">

            %{-- Report Criteria & Sections --}%
            <g:render template="includes/reportCriteriaSection" model="[configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, editMode: editMode]"/>

        </div>
    </div>
</div>
