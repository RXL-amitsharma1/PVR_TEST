<%@ page import="com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.enums.ReportFormatEnum; grails.util.Holders;com.rxlogix.Constants" %>
<g:set var="action" value="list"/>

<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.viewResult.title"/></title>
    <style type="text/css">
    .glyphicon-refresh-animate {
        -webkit-animation: spin .7s linear infinite;
        -moz-animation: spin .7s linear infinite;
        -ms-animation: spin .7s linear infinite;
        -o-animation: spin .7s linear infinite;
        animation: spin .7s linear infinite;
    }
    #caseList_wrapper > .dt-layout-row:first-child, #openCaseList_wrapper > .dt-layout-row:first-child {
        margin-top : 0px;
        padding-right:0px;
    }
    </style>
    <asset:javascript src="app/case.js"/>
    <script type="text/javascript">
        var caseObjConfig = {
            caseDataLinkUrl: '${createLink(controller: 'report', action: 'drillDown')}',
            removeCaseUrl: "${createLink(controller: 'caseList',action: 'removeCaseFromList', params: ['cid': caseSeries.id])}",
            moveCasesToListUrl: "${createLink(controller: 'caseList',action: 'moveCasesToList', params: ['cid': caseSeries.id])}",
            refreshCaseUrl: "${createLink(controller: 'caseList',action: 'refreshCaseList',id: caseSeries?.id)}",
            listUrl: "${createLink(controller: 'caseList',action: 'list', params: ['id': caseSeries.id])}",
            openListUrl: "${createLink(controller: 'caseList',action: 'openCasesList', params: ['id': caseSeries.id])}",
            removedListUrl: "${createLink(controller: 'caseList',action: 'removedCasesList', params: ['id': caseSeries.id])}",
            addCaseUrl: "${createLink(controller: 'caseList',action: 'addCaseToList')}${_csrf?("?"+_csrf.parameterName+"="+_csrf.token):""}",
            addCaseNumberComment: "${createLink(controller: 'caseList',action: 'updateCommentToCaseNumber')}",
            updateTags: "${createLink(controller: 'caseList',action: 'updateTags',params: ['cid':caseSeries.id])}",
            generateDraftUrlWithId: "${createLink(controller: 'periodicReportConfigurationRest', action: 'generateDraft',id: executedReportConfiguration?.id)}",
            showVersionColumn: ${showVersionColumn},
            fetchAllTags: '${createLink(controller: 'caseList',action: 'fetchAllTags')}',
            caseSeriesId: "${id}",
            dateRangeType: "${caseSeries.dateRangeType.toString()}",
            safetySource: "${Holders.config.safety.source}",
            <sec:ifAnyGranted roles="ROLE_CASE_SERIES_EDIT">
            showAddRemoveCaseControls: true
            </sec:ifAnyGranted>
            <sec:ifNotGranted roles="ROLE_CASE_SERIES_EDIT">
            showAddRemoveCaseControls: false
            </sec:ifNotGranted>
        };
        $(function () {
            console.log("Initializing case list table");
            caseObj.caseList.init_case_list_table(${caseSeries.isTemporary});
            console.log("Loading table options for caseList");
            caseObj.caseList.init_open_case_list_table(${caseSeries.isTemporary});
            caseObj.caseList.init_removed_case_list_table(${caseSeries.isTemporary});
            $(document).on('shown.bs.tab', function (e) {
                if ($(e.target).attr("href").indexOf("open") > -1) {
                    $("#tableColumns tbody").remove();
                    loadTableOption('#openCaseList');
                } else if ($(e.target).attr("href").indexOf("removed") > -1) {
                    $("#tableColumns tbody").remove();
                    loadTableOption('#removedCaseList');
                } else {
                    $("#tableColumns tbody").remove();
                    loadTableOption('#caseList');
                }
            });

            loadTableOption('#caseList');

            $(document).on('click', '#caseList button.addCase', function () {
                caseObj.caseList.init_add_case_modal();
            });
            $(document).on('click', '.refreshCase', function () {
                window.location.href = caseObjConfig.refreshCaseUrl;
            });
            $("div.body").on('click', "i.addComment", function () {
                caseObj.caseList.init_add_case_comment_modal($(this).data('caseNumberId'), unescapeHTML($(this).data('content')), $(this).data('caseNumber'));
            }).popover({
                selector: 'i.showPopover',
                trigger: 'hover focus',
                html: true
            });
            $(".add-case-to-list").on('click', function () {
                caseObj.caseList.add_case_to_list();
            });
            $(".close-add-case").on('click', function () {
                $("#addCaseModal").find("#justification").val("");
                $("#addCaseModal").find('#importCasesSection').attr('hidden','hidden');
                $("#addCaseModal").find(':file').val('').parents('.input-group').find(':text').val('');
                $("#addCaseModal").find('#versionNumber, #caseNumber').removeAttr('disabled').siblings('label').find('span').show();
                $('.add-case-to-list').removeAttr('disabled');
            });

            $(".add-comment-to-case").on('click', function () {
                caseObj.caseList.add_comment_to_case_number();
            });

            $(".regenerate-report").on('click', function(){
               caseObj.caseList.generate_report_as_draft();
            });

            $(".btn-export").on('click', function() {
                this.href = updateURLParameter(this.href, "sort", caseObj.caseList.getSortColumn());
                this.href = updateURLParameter(this.href, "direction", caseObj.caseList.getOrderDirection());
                this.href = updateURLParameter(this.href, "caseListType", caseObj.caseList.currentActiveCaseListTab());
            });

            if(location.hash != "") {
                var hash = location.hash,
                    hashPieces = hash.split('?'),
                    activeTab = $('[href$="' + hashPieces[0] + '"]');
                activeTab && activeTab.tab('show');
                updateDropdownSelection.on(activeTab)();
            }
            $(".dropdown-menu li a").on('click', updateDropdownSelection);
        });

        var createExportLink = function () {
            return '${createLink(controller: 'caseList', action: 'list')}';
        };

        var updateDropdownSelection = function() {
            $('.dropdown-menu li.active').removeClass('active');
            $(this).parents(".btn-group").find('.selection').text($(this).text());
            $(this).parents(".btn-group").find('.selection').val($(this).text());
        };

    /**
     * http://stackoverflow.com/a/10997390/11236
     */
    function updateURLParameter(url, param, paramVal){
        var newAdditionalURL = "";
        var tempArray = url.split("?");
        var baseURL = tempArray[0];
        var additionalURL = tempArray[1];
        var temp = "";
        if (additionalURL) {
            tempArray = additionalURL.split("&");
            for (var i=0; i<tempArray.length; i++){
                if(tempArray[i].split('=')[0] != param){
                    newAdditionalURL += temp + tempArray[i];
                    temp = "&";
                }
            }
        }

        var rows_txt = temp + "" + param + "=" + paramVal;
        return baseURL + "?" + newAdditionalURL + rows_txt;
    }
    </script>
</head>

<body>

<g:if test="${executedReportConfiguration != null}">
<g:set var="isDraftOrFinal" value="${params.isInDraftMode || executedReportConfiguration?.status in [ReportExecutionStatusEnum.GENERATED_DRAFT, ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT]}"/>
</g:if>
<div class="col-md-12">
    <rx:container title="${message(code: 'app.label.report.case.list')}: ${name.encodeAsHTML()}" options="${true}">
        <g:render template="/includes/layout/flashErrorsDivs"/>
        <g:render template="/includes/layout/inlineAlerts"/>
        <div style="margin-bottom: 10px;">
            <g:if test="${params.parentId && sourceSection}">
                <g:link controller="report" action="show" params="[id: params.parentId]"><g:renderDynamicReportName
                        executedTemplateQuery="${sourceSection}" executedConfiguration="${sourceSection.executedConfiguration}"/></g:link> > ${caseSeries.description ?: params.filePostfix}
            </g:if>
            <g:else>
                <g:link controller="executedCaseSeries" action="show" params="[id: id]"><g:message
                        code="app.label.view"/></g:link>
                > ${name}
            </g:else>
        </div>

        <div class="body">
            <g:if test="${executedReportConfiguration != null}">
                <div class="btn-group">
                    <g:link class="btn btn-default waves-effect waves-light"
                            controller="report" action="criteria" params="[id: executedReportConfiguration.id, isInDraftMode: params.isInDraftMode]">
                        <i class="md md-description icon-white"></i>
                        <g:message code="app.label.reportCriteria"/>
                    </g:link>
                </div>
                <g:if test="${executedReportConfiguration.hasGeneratedCasesData && isDraftOrFinal}">
                    <%
                        boolean isDraftButton = (params.boolean('isInDraftMode') || executedReportConfiguration.status == ReportExecutionStatusEnum.GENERATED_DRAFT)
                        boolean canShowFinal = (executedReportConfiguration.status in [ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT, ReportExecutionStatusEnum.SUBMITTED]
                                && executedReportConfiguration.finalLastRunDate != null)
                        boolean shouldShowDropdown = (isDraftButton && canShowFinal) || (!isDraftButton) // Final button always shows Draft option
                    %>
                    <div class="btn-group">
                        <button type="button" class="btn btn-default dropdown-toggle waves-effect waves-light"
                                data-toggle="dropdown" aria-expanded="false">
                            <i class="md md-assignment icon-white"></i>
                            <span>${message(code: isDraftButton ? 'app.periodicReport.executed.draft.label' : 'app.periodicReport.executed.final.label')}</span>
                            <span class="caret"></span>
                        </button>
                        <g:if test="${shouldShowDropdown}">
                            <ul class="dropdown-menu" role="menu">
                                <g:if test="${isDraftButton}">
                                    <g:if test="${canShowFinal}">
                                        <li><g:link controller="report" action="showFirstSection"
                                                    params="[id: executedReportConfiguration.id, isInDraftMode: false]">
                                            ${message(code: 'app.periodicReport.executed.final.label')}
                                        </g:link></li>
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <li><g:link controller="report" action="showFirstSection"
                                                params="[id: executedReportConfiguration.id, isInDraftMode: true]">
                                        ${message(code: 'app.periodicReport.executed.draft.label')}
                                    </g:link></li>
                                </g:else>
                            </ul>
                        </g:if>
                    </div>
                </g:if>
            </g:if>
            <div class="btn-group">
                <button type="button" class="btn btn-default dropdown-toggle waves-effect waves-light"
                        data-toggle="dropdown" aria-expanded="false">
                    <i class="md md-list icon-white"></i>
                    <span class="selection">${message(code: 'app.caseSeries.label')} </span>
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu" role="menu">
                    <li><g:link fragment="caseSeries" data-toggle="tab" params="[cid: caseSeries.id]">${message(code: 'app.caseSeries.label')}</g:link></li>
                    <li><g:link fragment="openCases" data-toggle="tab" params="[cid: caseSeries.id]">${message(code: 'app.openCases.label')}</g:link></li>
                    <g:if test="${executedReportConfiguration}">
                        <li id="removedCasesListTab"><g:link fragment="removedCases" data-toggle="tab">${message(code: 'app.removedCases.label')}</g:link></li>
                        <li>
                            <g:if test="${cumulativeCaseSeries}">
                                <g:link controller="caseList" action="index" id="${executedReportConfiguration.id}"
                                        params="[cumulativeType: !cumulativeType, isInDraftMode: params.isInDraftMode]">
                                    <g:if test="${!cumulativeType}">
                                        <g:message code="app.view.cumulative.cases"/>
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.view.cases"/>
                                    </g:else>
                                </g:link>
                            </g:if>
                        </li>
                    </g:if>
                    <g:if test="${isDraftOrFinal}">
                        <li class="divider"></li>
                        <g:set var="userService" bean="userService"/>
                        <g:set var="sections" value="${executedReportConfiguration.fetchExecutedTemplateQueriesByCompletedStatus()}"/>
                        <g:if test="${(sections.size() > 1) && !sections.find { sec -> !sec.isVisible() }}">
                            <li><g:link controller="report" action="viewMultiTemplateReport"
                                        params="[id: executedReportConfiguration.id, isInDraftMode: params.isInDraftMode]"><g:message code="app.label.entire.report"/></g:link></li>
                        </g:if>
                        <g:each var="executedTemplateQuery"
                                in="${sections}">
                            <g:if test="${executedTemplateQuery.isVisible()}">
                                <li><g:link controller="report" action="show" params="[id: params.boolean('isInDraftMode') ? (executedTemplateQuery.draftReportResult?.id ?: executedTemplateQuery.reportResult?.id) : executedTemplateQuery.reportResult?.id, isInDraftMode: params.isInDraftMode]">
                                    <g:renderDynamicReportName executedConfiguration="${executedReportConfiguration}"
                                                               executedTemplateQuery="${executedTemplateQuery}"/>
                                </g:link></li>
                            </g:if>
                        </g:each>
                        <g:if test="${executedReportConfiguration.attachments?.size() > 0}">
                            <li class="divider"></li>
                            <g:each var="attachment"
                                    in="${executedReportConfiguration.attachments?.findAll { it.isVisible() }?.sort { it.sortNumber }}">
                                <li><g:link action="downloadAttachment" params="[id: attachment.id]">
                                    file: ${attachment.name}
                                </g:link></li>
                            </g:each>
                        </g:if>
                    </g:if>
                </ul>
            </div>
            <div class="btn-group">
                <g:link class="btn btn-default btn-export waves-effect waves-light"
                        action="${action}"
                        params="[id: id, outputFormat: ReportFormatEnum.PDF.name(), showVersionColumn: showVersionColumn,
                                 excludeCriteriaSheet: caseSeries?.isTemporary ?: '', filePostfix: params.filePostfix, parentId: params.parentId]"
                        data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.pdf')}">
                    <asset:image src="pdf-icon.png" class="pdf-icon" height="16" width="16"/>
                </g:link>
                <g:link class="btn btn-default btn-export waves-effect waves-light"
                        action="${action}"
                        params="[id: id, outputFormat: ReportFormatEnum.XLSX.name(), showVersionColumn:showVersionColumn,
                                 excludeCriteriaSheet: caseSeries?.isTemporary ?: '', filePostfix: params.filePostfix, parentId: params.parentId]"
                        data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.excel')}">
                    <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/>
                </g:link>
                <g:link class="btn btn-default btn-export waves-effect waves-light"
                        action="${action}"
                        params="[id: id, outputFormat: ReportFormatEnum.DOCX.name(), showVersionColumn:showVersionColumn,
                                 excludeCriteriaSheet: caseSeries?.isTemporary ?: '', filePostfix: params.filePostfix, parentId: params.parentId]"
                        data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.word')}">
                    <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/>
                </g:link>
                <g:link class="btn btn-default btn-export waves-effect waves-light"
                        action="${action}"
                        params="[id: id, outputFormat: ReportFormatEnum.PPTX.name(), showVersionColumn:showVersionColumn,
                                 excludeCriteriaSheet: caseSeries?.isTemporary ?: '', filePostfix: params.filePostfix, parentId: params.parentId]"
                        data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.powerpoint')}">
                    <asset:image src="powerpoint-icon.png" class="powerpoint-icon" height="16" width="16"/>
                </g:link>
            </div>
            <div class="btn-group">
                <g:link class="btn btn-default waves-effect waves-light" target="_blank"
                        controller="configuration" action="create" params="[selectedCaseSeries: caseSeries?.id]">
                    <g:message code="app.label.run.adhoc.report"/>
                </g:link>
            </div>
            <g:if test="${executedReportConfiguration != null && !caseSeries?.isTemporary && (executedReportConfiguration.status != ReportExecutionStatusEnum.SUBMITTED)}">
                <div class="btn-group">
                    <button class="btn btn-default refreshCase" ${caseSeries.isSpotfireCaseSeries?"disabled":""}>
                        <span class="md md-refresh icon-white"></span>
                        <g:message code="app.refresh.case.btn.label"/>
                    </button>
                </div>
            </g:if>
            <g:if test="${caseSeries?.isTemporary}">
                <div class="btn-group">
                    <button class="btn pv-btn-grey "
                            data-toggle="modal" data-target="#saveCaseSeries"
                            data-title="${caseSeries.seriesName}"
                            title="${g.message(code: 'app.label.save.caseSeries')}"
                            data-cid="${caseSeries.id}">
                        <g:message code="default.button.save.label"/>
                    </button>
                </div>
            </g:if>

        <span id="case-list-conainter" class="list pv-caselist">
            <div>
                <div class="tab-content">
                    <div role="tabpanel" class="tab-pane active" id="caseSeries">
                        <div>
                            <table id="caseList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                                <thead>
                                <tr>
                                    <th>
                                        <sec:ifAnyGranted roles="ROLE_CASE_SERIES_EDIT">
                                            <g:if test="${canAddEditCases && !params.viewOnly && !caseSeries?.isTemporary}">
                                                <g:if test="${caseSeries.isSpotfireCaseSeries}">
                                                    <button class="glyphicon glyphicon-plus addCase" disabled style="color: green;opacity: 0.3"></button><br/>
                                                </g:if>
                                                <g:else>
                                                    <button class="glyphicon glyphicon-plus addCase" style="color: green"></button><br/>
                                                </g:else>
                                                <button class="glyphicon glyphicon-minus removeCasesBtn hide" style="color: red"></button>
                                            </g:if>
                                        </sec:ifAnyGranted>
                                    </th>
                                    <th></th>
                                    <th style="min-width: 90px"><g:message code="app.caseList.caseNumber"/></th>
                                    <th><g:message code="app.caseList.versionNumber"/></th>
                                    <th><g:message code="app.label.tags"/></th>
                                    <g:if test = "${Holders.config.safety.source != Constants.PVCM}">
                                        <th><g:message code="app.caseList.type"/></th>
                                    </g:if>
                                    <g:else>
                                        <th><g:message code="app.caseList.type.pvcm"/></th>
                                    </g:else>
                                    <th><g:message code="app.caseList.productFamily"/></th>
                                    <th><g:message code="app.caseList.eventPI"/></th>
                                    <th><g:message code="app.caseList.seriousness"/></th>
                                    <th><g:message code="app.caseList.listedness"/></th>
                                    <g:if test = "${Holders.config.safety.source != Constants.PVCM}">
                                        <th><g:message code="app.caseList.causality"/></th>
                                    </g:if>
                                    <th><g:message code="app.caseList.lockedDate"/></th>
                                    <g:if test="${caseSeries.dateRangeType.toString()==message(code : 'app.caseList.eventReceiptDate')}">
                                        <th><g:message code="app.caseList.eventSequenceNumber"/></th>
                                        <th><g:message code="app.caseList.eventReceiptDate"/></th>
                                        <th><g:message code="app.caseList.eventPreferredTerm"/></th>
                                        <th><g:message code="app.caseList.eventSeriousness"/></th>
                                    </g:if>
                                    <g:if test="${!caseSeries?.isTemporary}">
                                        <th><g:message code=""/></th>
                                    </g:if>
                                </tr>
                                </thead>
                            </table>
                        </div>
                    </div>
                    <div role="tabpanel" class="tab-pane" id="openCases">
                        <div>
                            <table id="openCaseList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                                <thead>
                                <tr>
                                    <th></th>
                                    <th></th>
                                    <th><g:message code="app.caseList.caseNumber"/></th>
                                    <th><g:message code="app.caseList.versionNumber"/></th>
                                    <th><g:message code="app.label.tags"/></th>
                                    <g:if test = "${Holders.config.safety.source != Constants.PVCM}">
                                        <th><g:message code="app.caseList.type"/></th>
                                    </g:if>
                                    <g:else>
                                        <th><g:message code="app.caseList.type.pvcm"/></th>
                                    </g:else>
                                    <th><g:message code="app.caseList.productFamily"/></th>
                                    <th><g:message code="app.caseList.eventPI"/></th>
                                    <th><g:message code="app.caseList.seriousness"/></th>
                                    <th><g:message code="app.caseList.listedness"/></th>
                                    <g:if test = "${Holders.config.safety.source != Constants.PVCM}">
                                        <th><g:message code="app.caseList.causality"/></th>
                                    </g:if>
                                    <th><g:message code="app.caseList.lockedDate"/></th>
                                    <g:if test="${caseSeries.dateRangeType.toString()==message(code : 'app.caseList.eventReceiptDate')}">
                                        <th><g:message code="app.caseList.eventSequenceNumber"/></th>
                                        <th><g:message code="app.caseList.eventReceiptDate"/></th>
                                        <th><g:message code="app.caseList.eventPreferredTerm"/></th>
                                        <th><g:message code="app.caseList.eventSeriousness"/></th>
                                    </g:if>
                                    <g:if test="${!caseSeries?.isTemporary}">
                                        <th><g:message code="app.caseList.comments"/></th>
                                    </g:if>
                                </tr>
                                </thead>
                            </table>
                        </div>
                    </div>
                    <div role="tabpanel" class="tab-pane" id="removedCases">
                        <div>
                            <table id="removedCaseList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                                <thead>
                                <tr>
                                    <th>
                                        <g:if test="${canAddEditCases && !params.viewOnly && !caseSeries?.isTemporary}">
                                            <span class="glyphicon glyphicon-plus addCasesBtn hide"  style="color: green"></span><br/>
                                        </g:if>
                                    </th>
                                    <th></th>
                                    <th style="min-width: 100px"><g:message code="app.caseList.caseNumber"/></th>
                                    <th><g:message code="app.caseList.version"/></th>
                                    <th><g:message code="app.label.tags"/></th>
                                    <g:if test = "${Holders.config.safety.source != Constants.PVCM}">
                                        <th><g:message code="app.caseList.type"/></th>
                                    </g:if>
                                    <g:else>
                                        <th><g:message code="app.caseList.type.pvcm"/></th>
                                    </g:else>
                                    <th><g:message code="app.caseList.productFamily"/></th>
                                    <th><g:message code="app.caseList.eventPI"/></th>
                                    <th><g:message code="app.caseList.seriousness"/></th>
                                    <th><g:message code="app.caseList.listedness"/></th>
                                    <g:if test = "${Holders.config.safety.source != Constants.PVCM}">
                                        <th><g:message code="app.caseList.causality"/></th>
                                    </g:if>
                                    <th><g:message code="app.caseList.lockedDate"/></th>
                                    <g:if test="${caseSeries.dateRangeType.toString()==message(code : 'app.caseList.eventReceiptDate')}">
                                        <th><g:message code="app.caseList.eventSequenceNumber"/></th>
                                        <th><g:message code="app.caseList.eventReceiptDate"/></th>
                                        <th><g:message code="app.caseList.eventPreferredTerm"/></th>
                                        <th><g:message code="app.caseList.eventSeriousness"/></th>
                                    </g:if>
                                    <g:if test="${!caseSeries?.isTemporary}">
                                        <th><g:message code="app.caseList.comments"/></th>
                                    </g:if>
                                </tr>
                                </thead>
                            </table>
                        </div>
                    </div>
                    <div>
                        <div class="row">
                            <div class="col-lg-2"><g:message code="caseSeries.legend.label" />:</div>
                            <div class="col-lg-2"><span class="glyphicon glyphicon-tag" style="color: green"></span> <g:message code="caseSeries.legend.newCases.label" /></div>
                            <div class="col-lg-2"><span class="glyphicon glyphicon-tag" style="color: purple"></span> <g:message code="caseSeries.legend.manuallyAddedCases.label" /></div>
                            <div class="col-lg-2"><span class="glyphicon glyphicon-tag" style="color: darkblue"></span> <g:message code="caseSeries.legend.movedFromOpen.label" /></div>
                            <div class="col-lg-2"><span class="glyphicon glyphicon-tag" style="color: orange"></span> <g:message code="caseSeries.legend.higherVersionExists.label" /></div>
                            <div class="col-lg-2"></div>
                        </div>
                    </div>
                </div>


            </div>
            <g:render template="includes/addCaseModal"
                      model="[caseSeriesId: id, cumulativeType: cumulativeType]"/>
            <g:render template="includes/addComment" model="[caseSeriesId: caseSeries.id]"/>
            <g:render template="includes/caseListTagModal" model="[caseSeriesId: caseSeries.id]"/>
            <g:render template="includes/confirmation"/>
    </div>
    </div>
</rx:container>
</div>

<g:render template="/includes/widgets/saveCaseSeries"/>
</body>
</html>
