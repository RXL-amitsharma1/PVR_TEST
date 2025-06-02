<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.querySections" />
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show" id="templateQueriesContainer">
            <g:render template="/autoReasonOfDelay/includes/queriesRCA" model="['theInstance':autoReasonOfDelayInstance,isForPeriodicReport:false,isForIcsrReport:false]" />
        </div>

    </div>
</div>
