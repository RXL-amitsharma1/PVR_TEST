<%@ page import="com.rxlogix.enums.QueryTarget; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.EditQuery.title"/></title>
%{--    <asset:javascript src="backbone/underscore.js"/>--}%
    <g:javascript>
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action: 'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var extraValuesUrl = "${createLink(controller: 'query', action: 'extraValues')}";
        var embaseOperatorsURL = "${createLink(controller: 'query', action: 'getEmbaseOperators')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var querySearchUrl = "${createLink(controller: 'queryRest', action: 'getNonSetQueries')}";
        var queryNameUrl = "${createLink(controller: 'queryRest', action: 'getQueryNameDescription')}";
        var listTagsUrl = "${createLink(controller: 'tag', action:'index')}";
        var createTagUrl = "${createLink(controller: 'query', action: 'addTag')}";
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var queryType = "${query.queryType}";
        var editable = ${editable};
        var disableInactiveTabs = true;
        var importExcel="${createLink(controller: 'query', action: 'importExcel')}";
        var validateValue="${createLink(controller: 'query', action: 'validateValue')}";
        var hasConfigTemplateCreatorRole=false;
        var FAERS_QUERY_TARGET_ENUM_VAL = "${QueryTarget.FAERS.name()}";
        var isTargetValueFaers = "${(grailsApplication.config.pvsignal.url !="") && (QueryTarget.getQueryTarget().size() > 1)
        && (query?.queryTarget?.name() == QueryTarget.FAERS.name())}";
    </g:javascript>
    <asset:javascript src="vendorUi/backbone/underscore-1.8.3.js"/>
    <asset:javascript src="vendorUi/backbone/backbone-min.js"/>
    <asset:javascript src="app/tags.js"/>
    <asset:javascript src="app/query/queryBuilder.js"/>
    <asset:javascript src="app/query/queryValueSelect2.js"/>
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:stylesheet src="query.css"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="select2-treeview.css" />
    <asset:javascript src="select2/select2-treeview.js"/>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5>${message(code: "app.query.title")} </h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div>
            <div style="margin-top: 27px;">



        <g:render template="/includes/layout/flashErrorsDivs" bean="${query}" var="theInstance"/>

        <div>
            <form name="queryForm" id="queryForm" action="${createLink(controller: 'query', action:'update')}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}" method="POST" enctype="multipart/form-data" data-evt-sbt='{"method": "finalizeForm", "params": []}'>

                <g:render template="form"
                          model="['query': query, 'editable': editable, 'fromEdit': true, users: users, userGroups: userGroups, currentUser: currentUser, sourceProfiles:sourceProfiles]"/>
                <g:hiddenField name="version" value="${query?.version}"/>
                <g:hiddenField name="id" value="${params.id}"/>
                <g:hiddenField name="edit" id="edit" value="${isAdmin}"/>

                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <g:if test="${usage && isAdmin}">
                                <button type="button" class="btn btn-primary"
                                        data-evt-clk='{"method": "showWarning", "params": ["${createLink(action: "update")}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}", true]}'
                                        id="updateButton">${message(code: 'default.button.update.label')}</button>
                            </g:if><g:else>
                            <g:submitButton class="btn btn-primary" name="${message(code:'default.button.update.label')}" value="${message(code:'default.button.update.label')}"/>
                        </g:else>
                            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["query", "index"]}'
                                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>

    <g:render template="/includes/widgets/warningTemplate"/>
</div>
        </div>
</div>
</div>

</body>
