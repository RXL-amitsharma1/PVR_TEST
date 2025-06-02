<%@ page import="com.rxlogix.enums.TemplateTypeEnum; grails.converters.JSON" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.View.Template" /></title>
    <g:javascript>
        var templateType = "${template.templateType}";
        var cllTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL')}";
        var specificCllTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL', params: [templateTypeEnum: TemplateTypeEnum.CASE_LINE.name()])}";
        var customSQLTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL', params: [templateTypeEnum: template.templateType.name()])}";
        var specificCllCSQLTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLLCSQL')}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var checkDrillDownUrl = "${createLink(controller: 'reportTemplateRest', action: 'checkDrillDown')}";
        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}?cllOnly=true";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var executedtmpltViewUrl = "${createLink(controller: 'template', action: 'viewExecutedTemplate')}";
    </g:javascript>

    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-10.3.3/solid-gauge.js"/>
    <asset:javascript src="vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"/>
    <asset:javascript src="vendorUi/easychart/ec.js"/>
    <asset:javascript src="vendorUi/easychart/ec_vapt.js"/>
    <asset:javascript src="app/template/editTemplate.js"/>
    <asset:javascript src="app/template/editColumns.js"/>
    <asset:javascript src="app/template/editMeasures.js"/>
    <asset:javascript src="app/template/editColumnMeasure.js"/>
    <asset:javascript src="app/template/editTemplateSet.js"/>
    <asset:javascript src="app/template/editChartTemplate.js"/>
    <asset:stylesheet src="template.css" />
    <asset:javascript src="app/expandingTextarea.js"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:stylesheet src="select2-treeview.css" />
    <asset:javascript src="select2/select2-treeview.js"/>
</head>

<body>
    <rx:container title="${title}" bean="${template}">

        <g:render template="/includes/layout/flashErrorsDivs" bean="${template}" var="theInstance"/>

        <div class="container-fluid">

            <div class="row">
                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.templateName" /></label>
                            <div class="word-wrapper">${template.name}</div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.tag"/></label>
                            <g:if test="${template.tags?.name}">
                                <g:each in="${template.tags.name}">
                                    <div>${it}</div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none" /></div>
                            </g:else>
                        </div>
                    </div>
                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-5">
                            <label><g:message code="app.label.category" /></label>
                            <g:if test="${template.category?.name}">
                                <div>${template.category?.name}</div>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none" /></div>
                            </g:else>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.qualityChecked"/></label>
                            <div>
                                <g:formatBoolean boolean="${template.qualityChecked}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" />
                            </div>
                        </div>
                    </div>

                </div>

                <div class="col-xs-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="shared.with"/></label>
                            <g:if test="${template.shareWithUsers || template.shareWithGroups}">
                                <g:if test="${template.shareWithUsers}">
                                    <g:each in="${template.shareWithUsers}">
                                        <div>${it.reportRequestorValue}</div>
                                    </g:each>
                                </g:if>
                                <g:if test="${template.shareWithGroups}">
                                    <g:each in="${template.shareWithGroups}">
                                        <div>${it.name}</div>
                                    </g:each>
                                </g:if>
                            </g:if>
                            <g:else>
                                <div><g:message code="app.label.none" /></div>
                            </g:else>
                        </div>
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.outputInteractive"/></label>
                                <div>
                                    <g:formatBoolean boolean="${template.interactiveOutput}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                    <div class="col-xs-3">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.description" /></label>
                                <g:if test="${template.description}">
                                    <div class="word-wrapper">${template.description}</div>
                                </g:if>
                                <g:else>
                                    <div><g:message code="app.label.none.parends"/></div>
                                </g:else>
                            </div>
                        </div>
                    </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <g:render template="includes/templateType" model="[currentUser: currentUser, 'reportTemplateInstance': template, 'editable': editable, isExecuted: isExecuted]" />
                    <g:hiddenField name="templateId" id="templateId" value="${template.id}" />
                    <g:hiddenField name="editable" id="editable" value="false" />
                </div>
            </div>

            <g:if test="${isExecuted}">
                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <g:link controller="template" action="view" id="${currentTemplate.id}"><g:message code="template.see.current.template" /></g:link>
                        </div>
                    </div>
                </div>
            </g:if>
            <g:else>
                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <button type="button" class="btn btn-primary viewButtons" data-evt-clk='{"method": "goToUrl", "params": ["configuration", "create", {"selectedTemplate": ${template.id}}]}' id="runBtn">
                                ${message(code: "default.button.run.label")}
                            </button>
                            <button type="button" class="btn pv-btn-grey viewButtons" data-evt-clk='{"method": "goToUrl", "params": ["template", "edit", {"id": ${params.id}}]}' id="editBtn">
                                ${message(code: "default.button.edit.label")}
                            </button>
                            <button type="button" class="btn pv-btn-grey viewButtons" data-evt-clk='{"method": "goToUrl", "params": ["template", "copy", {"id": ${params.id}}]}' id="copyBtn">
                                ${message(code: "default.button.copy.label")}
                            </button>
                            <button url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="template"
                                    data-instanceid="${params.id}" data-instancename="${template.name}" class="btn pv-btn-grey viewButtons"><g:message code="default.button.delete.label"/></button>
                        </div>
                    </div>
                </div>
            </g:else>
            <sec:ifAllGranted roles="ROLE_DEV">
            <div>
                <g:textArea name="templateExport" value="${templateAsJSON([reportTemplate: template])}"
                            style="width: 100%; height: 150px; margin-top: 20px"/>
            </div>
            </sec:ifAllGranted>
        </div>
        <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${template}" var="theInstance"/>
    </rx:container>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>

</body>
