<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.icsr.profile.conf.scheduling.criteria"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show icsrProfileSections" id="templateQueriesContainer">
            <g:render template="/templateQuery/templateQueries"
                      model="['theInstance': configurationInstance, isForPeriodicReport: false, isForIcsrReport: false, isForIcsrProfile: true]"/>
        </div>

    </div>
</div>
<g:render template="/configuration/includes/emailConfigurationDistributionChannel"
          model="[emailConfiguration: configurationInstance?.emailConfiguration]"/>
<g:render template="/email/includes/copyPasteEmailModal"/>