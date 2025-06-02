<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container ">
    <g:hiddenField name="owner" id="owner"
                   value="${seriesInstance?.owner?.id ?: sec.loggedInUserInfo(field: 'id')}"/>
    <div class="rxmain-container">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.selectionCriteria"/>
            </label>
        </div>

        <div class="rxmain-container-content">

            %{-- Report Criteria & Sections --}%
            <g:render template="includes/reportCriteriaSection" model="[seriesInstance: seriesInstance, editMode: editMode]"/>

        </div>
    </div>
</div>
