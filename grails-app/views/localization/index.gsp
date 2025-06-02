<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.localization")}"/>
    <title><g:message code="app.localizationList.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <g:javascript>
        var LOCALIZATION = {
             listUrl: "${createLink(controller: 'localization', action: 'list')}",
             editUrl: "${createLink(controller: 'localization', action: 'edit')}",
             viewUrl: "${createLink(controller: 'localization', action: 'show')}",
             preSearchString: "${preSearchString}"
        }
    </g:javascript>
    <asset:javascript src="app/localization.js"/>

</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${locInstance}" var="theInstance"/>

            <rx:container title="${message(code: "default.list.label", args:[entityName])}">
                <div class="body">
                    <div id="action-list-conainter" class="list">
                        <div class="pull-right" style="cursor: pointer; text-align: right; position: relative; margin-right:15px;">
                            <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["localization", "create"]}'>
                                <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: "default.create.label", args:[entityName])}" style="color: #353d43;"></i>
                            </a>

                        </div>
                        <div class="pv-caselist">
                            <table id="rxTableLocalizationList" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                                <thead>
                                <tr>
                                    <th class="localizationCodeColumn"><g:message code="app.label.localization.code"/></th>
                                    <th class="localizationLocaleColumn"><g:message code="app.label.localization.locale"/></th>
                                    <th class="localizationTextColumn"><g:message code="app.label.localization.text"/></th>
                                    <th style="width: 80px;"><g:message code="app.label.action"/></th>
                                </tr>
                                </thead>
                            </table>
                        </div>
                    </div>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>
</html>
