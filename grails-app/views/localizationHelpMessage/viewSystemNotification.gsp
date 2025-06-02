<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.systemNotification.view.title"/></title>

</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.label.systemNotification.view")}">

                <div class="body">
                    <div>

                        <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
                        <div class="row">
                            <div class="col-md-12">
                                <b>${instance.title ?: ""}</b>
                                <br><br>
                                <b><g:message code="app.label.systemNotification.groupsOnly"/>:</b><br>
                                ${instance.userGroups?.collect{it.name}?.join(";")?: "All"}
                                <br><br>
                                ${raw(instance.description)}
                                <br>
                                ${raw(instance.details)}
                            </div>

                        </div>
                        <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                        %{--                        <a href="${createLink(controller: 'localizationHelpMessage', action: 'createReleaseNotesItem')}?id=${instance.id}" class="btn btn-primary"><g:message code="app.label.releaseNotesItem.create"/></a>--}%

                            <g:if test="${instance.published}">
                                <a class="btn btn-primary" disabled=""><g:message code="app.label.systemNotification.published"/></a>
                            </g:if>
                            <g:else>
                                <button class="btn btn-primary" data-toggle="modal" data-target="#versionNotificationModalTest"><g:message code="app.label.systemNotification.test"/></button>
                                <a class="btn btn-primary" href="${createLink(controller: 'localizationHelpMessage', action: 'publishSystemNotification')}?id=${instance.id}"><g:message code="app.label.systemNotification.publish"/></a>
                                <a class="btn btn-primary" href="${createLink(controller: 'localizationHelpMessage', action: 'editSystemNotification')}?id=${instance.id}"><g:message code="default.button.edit.label"/></a>
                            </g:else>
                            <a class="btn pv-btn-grey" href="${createLink(controller: 'localizationHelpMessage', action: 'systemNotification')}"><g:message code="default.button.close.label"/></a>
                        </sec:ifAnyGranted>
                    </div>
                </div>
            </rx:container>

        </div>
    </div>
</div>

<div class="modal fade" id="versionNotificationModalTest" tabindex="-1" role="dialog">
    <div class="modal-dialog " role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.label.systemNotification.systemNotifications"/></h4>
            </div>
            <div class="modal-body ">
                <div class="panel panel-default">
                    <div class="panel-heading"><b>${instance.title}</b></div>
                    <div class="panel-body" style="border: 1px solid #ccc; border-radius: 0 0 10px 10px;">${raw(instance.description)}

                        <g:if test="${instance.details}">
                            <br>
                            <a href="${createLink(controller: 'localizationHelpMessage', action: 'viewSystemNotification')}?id=${instance.id}" class="btn btn-primary "><g:message code="app.label.systemNotification.viewDetails"/></a>
                        </g:if>
                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" class=" btn btn-primary remindLater" data-dismiss="modal"><g:message code="app.button.close"/></button>

            </div>
        </div>
    </div>
</div>
</body>
</html>