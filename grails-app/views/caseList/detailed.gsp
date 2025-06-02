<%@ page import="grails.util.Holders; com.rxlogix.enums.ReportExecutionStatusEnum; com.rxlogix.enums.ReportTypeEnum; com.rxlogix.enums.ReportFormatEnum;grails.converters.JSON" %>
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
    #caseList_wrapper > .dt-layout-row:first-child {
        margin-top:0px !important;
    }
    </style>
    <asset:javascript src="app/detailedCase.js"/>
    <asset:stylesheet src="detailedCase.css" />
    <script type="text/javascript">
        var caseObjConfig = {
            caseDataLinkUrl: '${createLink(controller: 'report', action: 'drillDown')}',
            refreshCaseUrl: "${createLink(controller: 'caseList',action: 'refreshCaseList',id: caseSeries?.id)}",
            listUrl: "${createLink(controller: 'caseList',action: 'list', params: ['id': caseSeries.id,detailed:true])}",
            updateTags: "${createLink(controller: 'caseList',action: 'updateTags',params: ['cid':caseSeries.id])}",
            generateDraftUrlWithId: "${createLink(controller: 'periodicReportConfigurationRest', action: 'generateDraft',id: executedReportConfiguration?.id)}",
            showVersionColumn: ${showVersionColumn},
            fetchAllTags: '${createLink(controller: 'caseList',action: 'fetchAllTags')}',
            caseSeriesId: "${id}",
            userid:"${sec.loggedInUserInfo(field: 'id')}",
            showAddRemoveCaseControls: false,
            <g:applyCodec encodeAs="none">
            additionalColumns: '${renderPrimaryCaseSeriesColumns()}',
            secondaryColumns: '${renderSecondaryCaseSeriesColumns()}'
            </g:applyCodec>
        };
        $(function () {
            console.log("Initializing detailed case list table");
            caseObj.caseList.init_case_list_table();
            console.log("Loading table options for detailed caseList");
            loadTableOption('#caseList');
            $(".btn-export").on('click', function () {
                this.href = updateURLParameter(this.href, "sort", 'CASE_NUM');
                this.href = updateURLParameter(this.href, "direction", 'ASC');
                this.href = updateURLParameter(this.href, "caseListType", 'caseList');
            });

            $('i#resetTable').on('click', function () {
                caseObj.caseList.resetTable();
            });
        });

        var createExportLink = function () {
            return '${createLink(controller: 'caseList', action: 'list')}';
        };


        /**
         * http://stackoverflow.com/a/10997390/11236
         */
        function updateURLParameter(url, param, paramVal) {
            var newAdditionalURL = "";
            var tempArray = url.split("?");
            var baseURL = tempArray[0];
            var additionalURL = tempArray[1];
            var temp = "";
            if (additionalURL) {
                tempArray = additionalURL.split("&");
                for (var i = 0; i < tempArray.length; i++) {
                    if (tempArray[i].split('=')[0] != param) {
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
    <g:set var="isDraftOrFinal"
           value="${params.isInDraftMode || executedReportConfiguration?.status in [ReportExecutionStatusEnum.GENERATED_DRAFT, ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT]}"/>
</g:if>
<div class="col-md-12">
    <rx:container title="${message(code: 'app.label.report.case.list')}: ${name.encodeAsHTML()}" options="${true}" customButtons="${g.render(template: "/caseList/includes/customHeaderButtons")}">
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
            <div class="btn-group">
                <button type="button" class="btn btn-default dropdown-toggle waves-effect waves-light"
                        data-toggle="dropdown" aria-expanded="false">
                    <i class="md md-list icon-white"></i>
                    <span class="selection">${message(code: 'app.caseSeries.label')}</span>
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu" role="menu">
                    <li><g:link fragment="caseSeries" data-toggle="tab"
                                params="[cid: caseSeries.id, detailed: true]">${message(code: 'app.caseSeries.label')}</g:link></li>
                </ul>
            </div>

            <div class="btn-group">
                <g:link class="btn btn-default btn-export waves-effect waves-light"
                        action="${action}"
                        params="[id                  : id, outputFormat: ReportFormatEnum.PDF.name(), showVersionColumn: showVersionColumn,
                                 excludeCriteriaSheet: caseSeries?.isTemporary ?: '', filePostfix: params.filePostfix, detailed: true, parentId: params.parentId]"
                        data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.pdf')}">
                    <asset:image src="pdf-icon.png" class="pdf-icon" height="16" width="16"/>
                </g:link>
                <g:link class="btn btn-default btn-export waves-effect waves-light"
                        action="${action}"
                        params="[id                  : id, outputFormat: ReportFormatEnum.XLSX.name(), showVersionColumn: showVersionColumn,
                                 excludeCriteriaSheet: caseSeries?.isTemporary ?: '', filePostfix: params.filePostfix, detailed: true, parentId: params.parentId]"
                        data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.excel')}">
                    <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/>
                </g:link>
                <g:link class="btn btn-default btn-export waves-effect waves-light"
                        action="${action}"
                        params="[id                  : id, outputFormat: ReportFormatEnum.DOCX.name(), showVersionColumn: showVersionColumn,
                                 excludeCriteriaSheet: caseSeries?.isTemporary ?: '', filePostfix: params.filePostfix, detailed: true, parentId: params.parentId]"
                        data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.word')}">
                    <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/>
                </g:link>
                <g:link class="btn btn-default btn-export waves-effect waves-light"
                        action="${action}"
                        params="[id                  : id, outputFormat: ReportFormatEnum.PPTX.name(), showVersionColumn: showVersionColumn,
                                 excludeCriteriaSheet: caseSeries?.isTemporary ?: '', filePostfix: params.filePostfix, detailed: true, parentId: params.parentId]"
                        data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'save.as.powerpoint')}">
                    <asset:image src="powerpoint-icon.png" class="powerpoint-icon" height="16" width="16"/>
                </g:link>
            </div>
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

            <div id="case-list-conainter" class="list pv-caselist">
                <div>
                    <div class="tab-content">
                        <div role="tabpanel" class="tab-pane active" id="caseSeries">
                            <div>
                                <table id="caseList" class="table table-striped pv-list-table dataTable no-footer"
                                       style="width: 100%">
                                    <thead>
                                    <tr>
                                        <th></th>
                                        <th></th>
                                        <th style="min-width: 90px" data-id="CASE_NUM"><g:message code="app.caseList.CASE_NUM"/></th>
                                        <th data-id="VERSION_NUM"><g:message code="app.caseList.VERSION_NUM"/></th>
                                        <th><g:message code="app.label.tags"/></th>
                                        <g:renderPrimaryCaseSeriesFields/>
                                    </tr>
                                    </thead>
                                </table>
                            </div>
                        </div>

                        <div>
                            <div class="row">
                                <div class="col-lg-2"><g:message code="caseSeries.legend.label"/>:</div>

                                <div class="col-lg-2"><span class="glyphicon glyphicon-tag"
                                                            style="color: green"></span> <g:message
                                        code="caseSeries.legend.newCases.label"/></div>

                                <div class="col-lg-2"><span class="glyphicon glyphicon-tag"
                                                            style="color: purple"></span> <g:message
                                        code="caseSeries.legend.manuallyAddedCases.label"/></div>

                                <div class="col-lg-2"><span class="glyphicon glyphicon-tag"
                                                            style="color: darkblue"></span> <g:message
                                        code="caseSeries.legend.movedFromOpen.label"/></div>

                                <div class="col-lg-2"><span class="glyphicon glyphicon-tag"
                                                            style="color: orange"></span> <g:message
                                        code="caseSeries.legend.higherVersionExists.label"/></div>

                                <div class="col-lg-2"></div>
                            </div>
                        </div>
                    </div>

                </div>
                <g:render template="includes/caseListTagModal" model="[caseSeriesId: caseSeries.id]"/>
                <g:render template="includes/confirmation"/>
            </div>
        </div>
    </rx:container>
</div>

<g:render template="/includes/widgets/saveCaseSeries"/>
</body>
</html>
