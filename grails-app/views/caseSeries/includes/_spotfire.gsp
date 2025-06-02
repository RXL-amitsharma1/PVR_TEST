
            <div class="${isForPeriodicReport?"":"form-inline"}">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="generateSpotfireCheckbox" class="spotfireElement" value="${seriesInstance?.generateSpotfire ? "true" : "false"}"
                                checked="${seriesInstance?.generateSpotfire ? "true" : "false"}"/>
                    <label for="generateSpotfireCheckbox">
                        <g:message code="app.spotfire.caseSeries.generate.spotfire"/>
                    </label>
                </div>

                <label>
                    <g:message code="app.spotfire.file.type" default="File Type"/>:
                </label>

                <g:if test="${grailsApplication.config.spotfire.fileType.drug.enabled}">
                    <div class="radio radio-primary radio-inline">
                        <input type="radio" name="type" id="spotfireDrug" value="drug" class="spotfireElement"/>
                        <label for="spotfireDrug">
                            <g:message code="app.spotfire.drug"/>
                        </label>
                    </div>
                </g:if>
                <g:if test="${grailsApplication.config.spotfire.fileType.vaccine.enabled}">
                    <div class="radio radio-primary radio-inline">
                        <input type="radio" name="type" id="spotfireVaccine" value="vacc" class="spotfireElement"/>
                        <label for="spotfireVaccine">
                            <g:message code="app.spotfire.vaccine"/>
                        </label>
                    </div>
                </g:if>
                <g:if test="${grailsApplication.config.spotfire.fileType.pmpr.enabled}">
                    <div class="radio radio-primary radio-inline">
                        <input type="radio" name="type" id="spotfirePMPR" value="pmpr" class="spotfireElement"/>
                        <label for="spotfirePMPR">
                            <g:message code="app.spotfire.pmpr" default="PMPR"/>
                        </label>
                    </div>
                </g:if>

            </div>

            <input type="hidden" name="fullFileName" id="fullFileName" style="width: calc(100% - 100px);">
    <input type="hidden" name="generateSpotfire" id="generateSpotfire" value="${seriesInstance.generateSpotfire ?: ""}">