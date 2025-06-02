<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container ">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-right fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.globalCriteria"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-hide">

            %{-- Report Criteria & Sections --}%
            <g:render template="includes/reportCriteriaSection" model="[configurationInstance: configurationInstance]"/>

        </div>
    </div>
</div>
