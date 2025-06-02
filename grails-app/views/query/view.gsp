<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ViewQuery.title"/></title>
%{--    <asset:javascript src="backbone/underscore.js"/>--}%
    <asset:javascript src="vendorUi/backbone/underscore-1.8.3.js"/>
    <asset:javascript src="vendorUi/backbone/backbone-min.js"/>
    <asset:javascript src="select2/select2-treeview.js"/>
    <asset:javascript src="app/query/queryBuilder.js"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:stylesheet src="query.css"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <g:javascript>
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var extraValuesUrl = "${createLink(controller: 'query', action: 'extraValues')}";
        var embaseOperatorsURL = "${createLink(controller: 'query', action: 'getEmbaseOperators')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getNonSetQueries')}";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
        var queryType = "${queryType}";
        var editable = ${editable};
        var disableInactiveTabs = true;
    </g:javascript>
</head>

<body>
<div class="col-md-12">
    <rx:container title="${title}">

        <g:render template="/includes/layout/flashErrorsDivs"/>

        <div class="container-fluid ">
            <div class="row">
                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.queryName"/></label>
                            <div class="word-wrapper" style="white-space: break-spaces;">${query.name}</div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.description"/></label>
                            <div class="word-wrapper">${query.description}</div>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.tag"/></label>
                            <g:if test="${query.tags?.name}">
                                <g:each in="${query.tags?.name}">
                                    <div>${it}</div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none" /></div>
                            </g:else>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="shared.with"/></label>
                            <g:if test="${query.shareWithUsers || query.shareWithGroups}">
                                <g:if test="${query.shareWithUsers}">
                                    <g:each in="${query.shareWithUsers}">
                                        <div>${it.reportRequestorValue}</div>
                                    </g:each>
                                </g:if>
                                <g:if test="${query.shareWithGroups}">
                                    <g:each in="${query.shareWithGroups}">
                                        <div>${it.name}</div>
                                    </g:each>
                                </g:if>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none" /></div>
                            </g:else>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.QueryType"/></label>
                            <div>
                                <g:if test="${query.nonValidCases}">
                                    <span><g:message code="app.label.NonValidCases"/></span>
                                </g:if>
                                <g:if test="${query.deletedCases}">
                                    <span><g:message code="app.label.DeletedCases"/></span>
                                </g:if>
                                <g:if test="${query.icsrPadderAgencyCases}">
                                    <span><g:message code="app.label.icsrPadderAgencyCases"/></span>
                                </g:if>
                            </div>
                        </div>
                    </div>
                    <g:if test="${grailsApplication.config.pvsignal.url !=""}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.queryTarget"/></label>
                                <div class="word-wrapper">${query.queryTarget}</div>
                            </div>
                        </div>
                    </g:if>
                </div>

                <div class="col-xs-3">
                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.owner"/></label>
                                <div class="word-wrapper">${query?.owner?.fullName}</div>
                            </div>
                        </div>
                    </sec:ifAnyGranted>

                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.qualityChecked"/></label>
                            <div>
                                <g:formatBoolean boolean="${query.qualityChecked}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12 col-lg-12">
                    <g:render template="tabContent"
                              model="[isExecuted: isExecuted, query: query, currentUser: currentUser]"/>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <g:hiddenField name="viewJSONQuery" id="viewJSONQuery" value="${query.getJSONQuery()}"/>
                    <div id="viewStringQuery"></div>
                </div>
            </div>
            <g:if test="${isExecuted}">
                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <g:link controller="query" action="view" id="${currentQuery.id}">See current query</g:link>
                        </div>
                    </div>
                </div>
            </g:if>
            <g:else>
                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["configuration", "create", {"selectedQuery": ${query.id}}]}' id="runBtn">
                                ${message(code: "default.button.run.label")}
                            </button>
                            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["caseSeries", "preview", {"selectedQuery": ${query.id}}]}' id="previewBtn">
                                ${message(code: "default.button.preview.label")}
                            </button>
                            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["query", "edit", {"id": ${params.id}}]}' id="editBtn">
                                ${message(code: "default.button.edit.label")}
                            </button>
                            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["query", "copy", {"id": ${params.id}}]}' id="copyBtn">
                                ${message(code: "default.button.copy.label")}
                            </button>
                            <button url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="query"
                                    data-instanceid="${params.id}" data-instancename="${query.name}" class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></button>
                        </div>
                    </div>
                </div>
            </g:else>
            <sec:ifAllGranted roles="ROLE_DEV">
                <div>
                    <g:textArea name="queryExport" value="${queryAsJSON([query: query])}" style="width: 100%; height: 150px; margin-top:20px"/>
                </div>
            </sec:ifAllGranted>
        </div>
        <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${query}" var="theInstance"/>
    </rx:container>
    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
</div>


</body>
