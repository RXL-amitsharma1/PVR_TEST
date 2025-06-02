<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
<g:set var="hasBlank" value="${params.fromTemplate?configurationInstance.templateQueries?.find{ tq->tq.queryValueLists?.find {qvl-> qvl.parameterValues?.find{ it.value==null || it.value=="" }}}:false}"/>
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click  ${params.fromTemplate &&  !hasBlank? "collapseSectionForFromTemplate" : ""} " data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.reportSections"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show" id="templateQueriesContainer">
            <g:render template="/templateQuery/templateQueries" model="['theInstance':configurationInstance,isForPeriodicReport:isForPeriodicReport,isForIcsrReport:isForIcsrReport]" />
        </div>

    </div>
</div>