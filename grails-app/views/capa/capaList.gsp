<html>
<head>
    <asset:javascript src="app/capa/capa.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <g:javascript>
        var getCorrectiveDataList = "${createLink(controller: 'capa', action: 'getCorrectiveMapping')}";
        var getPreventativeDataList = "${createLink(controller: 'capa', action: 'getPreventativeMapping')}";
        var saveCAPA = "${createLink(controller: 'capa', action: 'saveCAPA')}";
    </g:javascript>
    <meta name="layout" content="main"/>
    <title><g:message code="app.capaAction.title"/></title>
    <style>
    .dt-layout-row:first-child {
        margin-top: 0px;
        padding-right:0px;
    }
    </style>
</head>
<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.capa.label")}">

            <g:render template="/includes/layout/flashErrorsDivs" bean="${workflowStateInstance}" var="theInstance"/>

                <button type="button" class="btn btn-primary" id="createButton" data-toggle='modal' data-target='#capaModal'>
                    <span class="glyphicon glyphicon-plus icon-white"></span>
                    <g:message code="default.button.create.label" />
                </button>

            <div class="pv-caselist">
                <div class="case-quality-datatable-toolbar"></div>
                <table id="capaTable" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th></th>
                            <th><g:message default="Name" code="app.label.name"/></th>
                            <th><g:message default="Owner App" code="label.owner.app"/></th>
                            <th><g:message default="Action" code="app.label.action"/></th>
                        </tr>
                    </thead>
                </table>
            </div>
         </rx:container>
            </div>
<g:form controller="capa" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
        </div>
    </div>
</div>
<g:render template="/capa/includes/capaModal"/>
</body>
</html>