<g:each in="${caseInfo}" var="section">

    <div class="rxmain-container rxmain-container-top" style="margin-bottom:5px">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <i class="fa fa-caret-right fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    ${section.key.replaceAll("\\_", " ")}
                </label>
            </div>

            <div class="rxmain-container-content rxmain-container-hide">

                <g:each in="${section.value}" var="subsection" status="j">
                    <g:if test="${section.value.size() > 1}">
                        <div style="border: 1px #cccccc solid; margin-bottom: 5px; border-radius: 10px;padding: 5px;">

                    </g:if>
                    <div class="row">
                    <g:each in="${subsection}" var="field" status="i">

                        <div class="col-md-2">
                            <g:set var="name" value="${field.key.replaceAll("\\_", " ")}"/>
                            <g:if test="${section.value.size() == 1}">
                                <g:set var="location" value="${section.key.replaceAll("\\_", " ")} / ${name}"/>
                            </g:if>
                            <g:else>
                                <g:set var="location" value="${section.key.replaceAll("\\_", " ")} #${j+1} / ${name}"/>
                            </g:else>
                            <label class="fieldLabel" style="cursor: pointer"
                                   data-name="${name}"
                                   data-value="${field?.value['value']}"
                                   data-priority="${field?.value['priority']}"
                                   data-isReviewable="${field.value['isReviewable']}"
                                   data-location="${location}">${field.value['label']}</label>
                            <div class ="fieldLevel">${field.value['value']}</div>
                        </div>
                        <g:if test="${(i % 4 == 5)}">
                            </div><div class="row">
                        </g:if>
                    </g:each>
                    </div>
                    <g:if test="${section.value.size() > 1}">

                        </div>
                    </g:if>
                </g:each>
            </div>
        </div>
    </div>

</g:each>
