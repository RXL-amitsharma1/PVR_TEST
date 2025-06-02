<%@ page import="grails.converters.JSON; groovy.json.JsonOutput; com.rxlogix.user.FieldProfileFields; grails.util.Holders; com.rxlogix.user.User; com.rxlogix.user.UserRole" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'app.field.profile.label')}"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
    <asset:stylesheet href="field-profile.css"/>
    <asset:javascript src="app/fieldProfile/field-profile.js"/>
    <style>
    input[type="radio"] {
        pointer-events: none;
        opacity: 0.5;
        cursor: not-allowed;
    }
    </style>
</head>
<body>
<g:javascript>
    var blindedLabel = "${g.message(code: 'app.template.blinded')}";
    var protectedLabel = "${g.message(code: 'app.template.protected')}";
    var hiddenLabel = "${g.message(code: 'app.template.hidden')}";
    var disabled = true;
    var loadGroupDataUrl = '${createLink(controller: 'fieldProfile', action: 'loadFieldProfileData')}';
    var method = '${actionName}';
</g:javascript>

<div class="content">
    <div class="container ">
        <g:set var="isPrivacy"
               value="${(fieldProfileInstance.name == Holders.config.getProperty("pvadmin.privacy.field.profile"))}"/>
        <g:hiddenField name="id" class="fieldProfileId" value="${fieldProfileInstance?.id}"/>
        <rx:container title="${message(code: "app.view.field.profile.label")}">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${fieldProfileInstance}" var="theInstance"/>
            <div class="row">
                <div class="col-md-12">
                    <h3 class="sectionHeader"><g:message code="field.profile.details.label"/></h3>
                    <div class="row col-md-4">
                        <div><label><g:message code="field.profile.name.label"/></label></div>
                        <div>${fieldProfileInstance?.name}</div>
                    </div>
                    <div class="row col-md-8">
                        <div><label><g:message code="fieldProfile.description.label"/></label></div>
                        <div>${fieldProfileInstance?.description}</div>
                    </div>
                </div>
            </div>
            <div style="margin-top: 40px"></div>
            <h3 class="sectionHeader"><span style="font-size: 20px;"><g:message code="app.field.group.label"/></span></h3>

            <div class="row">
                <div class="col-md-12 groupNameList panel-group">
                    <g:if test="${!fieldGroupNames}">
                        <div class="col-md-12"><g:message code="app.label.none.parends"/></div>
                    </g:if>
                    <g:each in="${fieldGroupNames}" var="groupName" status="i">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h4 class="panel-title">
                                    <a class="accordion-toggle ${i == 0 ? "" : "collapsed"}" data-toggle="collapse" data-parent="#accordion" href="#collapse${i}" data-groupname="${groupName.key}">
                                        <label class="rxmain-container-header-label">${groupName.value}</label>
                                    </a>
                                </h4>
                            </div>

                            <div id="collapse${i}" class="panel-collapse collapse ${i == 0 ? "in" : ""} p-10">
                                <div class="horizontalRuleFull"></div>
                                <div class="col-xs-3" id="fieldGroup-${groupName.key}-loader" style="margin-top: -10px">
                                    <i class="fa fa-refresh fa-spin"></i>
                                </div>
                                <div class="chip-grid"></div>
                            </div>
                        </div>
                    </g:each>
                </div>
            </div>

            <div class="margin20Top"></div>

            <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${fieldProfileInstance}" var="theInstance"/>
            <div class="buttonBar text-right">
                <div class="text-right">
                    <g:if test="${!isPrivacy}">
                        <g:link action="edit" id="${fieldProfileInstance?.id ?: params.id}" class="btn btn-primary updateButton"><g:message code='default.button.edit.label'/></g:link>
                    </g:if>
                    <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["fieldProfile", "index"]}'
                            id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                </div>
            </div>
        </rx:container>
    </div>
</div>
</body>
</html>
