<%@ page import="grails.util.Holders" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.field.profile.label")}"/>
    <title><g:message code="app.field.profile.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <g:javascript>
        var FIELDPROFILE = {
             listUrl: "${createLink(controller: 'fieldProfileRest', action: 'index')}",
             deleteUrl: "${createLink(controller: 'fieldProfile', action: 'delete')}",
             editUrl: "${createLink(controller: 'fieldProfile', action: 'edit')}",
             viewUrl: "${createLink(controller: 'fieldProfile', action: 'show')}"
        }
        var privacyProfileName = "${Holders.config.getProperty("pvadmin.privacy.field.profile")}"
        var pvAdminUrl = "${Holders.config.getProperty('app.pvadmin.url')}"
    </g:javascript>
    <asset:javascript src="app/fieldProfile/field-profile.js"/>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <g:render template="/includes/layout/flashErrorsDivs" bean="${fieldProfileInstanceList}"
                      var="theInstance"/>
            <rx:container title="${message(code: "app.field.profile.label")}" options="true"
                          customButtons="${g.render(template: "/fieldProfile/customHeaderButtons")}">
                <div class="body">
                <div id="action-list-conainter" class="list">
                    <div class="pull-right" style="cursor: pointer; text-align: right; position: relative;">
                    <a href="#" class="ic-sm pv-ic pv-ic-hover" data-evt-clk='{"method": "goToUrl", "params": ["fieldProfile", "create"]}'>
                        <i class="md-plus" data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.field.profile.create')}" style="color: #353d43;"></i>
                    </a>

                </div>
                <div class="pv-caselist">
                    <table id="rxTableFieldProfile" class="table table-striped pv-list-table dataTable no-footer"
                           width="100%">
                        <thead>
                        <tr>
                            <th class="fieldProfileNameColumn" style="width: 15%"><g:message code="field.profile.name.label"/></th>
                            <th class="fieldProfileDescriptionColumn" style="width: 45%"><g:message
                                    code="fieldProfile.description.label"/></th>
                            <th style="text-align: center;width: 10%"><g:message code="app.label.fieldProfile.lastUpdated"/></th>
                            <th style="text-align: center;width: 10%"><g:message code="fieldProfile.table.createdDate.label"/></th>
                            <th style="width: 10%"><g:message code="fieldProfile.table.createdBy.label"/></th>
                            <th style="width: 10%"><g:message code="app.label.action"/></th>
                        </tr>
                        </thead>
                    </table>
                </div>

                <g:form controller="${controller}" method="delete">
                    <g:render template="/includes/widgets/deleteRecord"/>
                </g:form>
                </div>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>
</html>
