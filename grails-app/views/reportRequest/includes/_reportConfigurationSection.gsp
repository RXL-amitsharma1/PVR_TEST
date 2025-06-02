<%@ page import="com.rxlogix.config.ReportRequestField; com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.selectionCriteria"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <g:set var="userService" bean="userService"/>
            %{-- Report Criteria & Sections --}%
            <g:render template="includes/reportCriteriaSection"
                      model="[reportRequestInstance: reportRequestInstance, currentUser: userService.currentUser]"/>
            <g:render template="includes/customFields" model="[section: ReportRequestField.Section.SELECTION]"/>

        </div>
    </div>
</div>
