<html>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/helpMessageList.js"/>

    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <title><g:message code="app.label.localizationHelp.releaseNotes.pageTitle"/></title>
    <g:javascript>
       var listUrl="${createLink(controller: 'localizationHelpMessage', action: 'listReleaseNotes')}";
    </g:javascript>
    <style>
    .dt-layout-row:first-child {
        margin-top: 3px;padding-right: 0px;
    }
    .dt-layout-row:last-child {
        margin-top: 5px;
    }
    </style>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.label.localizationHelp.list")}">
                <ul class="nav nav-tabs" role="tablist">
                    <li role="presentation"><a href="${createLink(action: "index")}"><g:message code="app.label.localizationHelp.helpMessages"/></a>
                    </li>
                    <li role="presentation" class="active"><a href="${createLink(action: "releaseNotes")}"><g:message code="app.label.localizationHelp.releaseNotes"/></a>
                    </li>
                    <li role="presentation" ><a href="${createLink(action: "interactiveHelp")}"><g:message code="app.label.interactiveHelp.interactiveHelp"/></a>
                    </li>
                    <li role="presentation" ><a href="${createLink(action: "systemNotification")}"><g:message code="app.label.systemNotification.systemNotifications"/></a>
                    </li>
                    <li role="presentation" ><a href="${createLink(action: "helpLink")}"><g:message code="app.label.localizationHelp.helpLink"/></a>
                    </li>
                </ul>
                <br>
                <div class="body">
                    <div id="action-list-conainter" class="list pv-caselist">

                        <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
                        <div class="row">
                            <div class="col-md-4">
                                <div class="navScaffold">
                                    <g:link class="btn btn-primary" action="createReleaseNotes">
                                        <span class="glyphicon glyphicon-plus icon-white"></span>
                                        <g:message code="default.button.create.label"/>
                                    </g:link>
                                    <button class="btn btn-primary exportReleaseNotes"  data-toggle="modal"  data-target="#exportReleaseNotesModal">
                                        <span class="glyphicon glyphicon-send icon-white"></span>
                                        <g:message code="app.label.whatsNewReminder.button"/>
                                    </button>
                                    <g:link class="btn btn-primary" action="exportReleaseNotes">
                                        <span class="glyphicon glyphicon-export icon-white"></span>
                                        <g:message code="controlPanel.exportToExcel.button"/>
                                    </g:link>
                                </div>
                            </div>
                            <div class="col-xs-4">
                                <form action="importJsonReleaseNotes" id="fileForm" method="post" enctype="multipart/form-data">
                                    <div class="input-group">
                                        <input type="text" class="form-control" id="file_name" readonly>
                                        <label class="input-group-btn">
                                            <span class="btn btn-default" style="height: 24px">
                                                <g:message code="app.label.PublisherTemplate.chooseFile"/>
                                                <input type="file" id="execlFile" name="file" accept=".json" style="display: none;">
                                            </span>
                                        </label>
                                        <span class="input-group-btn">
                                            <button type="submit" class="btn btn-primary" data-evt-clk='{"method": "showLoader", "params": []}'><g:message code="default.button.import.label"/></button>
                                        </span>
                                    </div>

                                </form>

                            </div>
                            <div class="col-md-1"></div>
                            <div class="col-md-4">
                                <div>
                                    <form action="updateFileReferencesForReleaseNotes">
                                        <input type="hidden" id="oneDriveFolderId" name="oneDriveFolderId" value="">
                                        <input type="hidden" id="oneDriveSiteId" name="oneDriveSiteId" value="">
                                        <input type="hidden" id="oneDriveUserSettings" name="oneDriveUserSettings" value="">
                                        <div class="input-group">
                                            <input id="oneDriveFolderName" class="form-control" readonly name="deliveryOption.oneDriveFolderName" value="">
                                            <span class="input-group-btn">
                                                <button class="btn btn-default selectOneDrive" type="button"><g:message code="scheduler.select"/></button>
                                            </span>
                                            <span class="input-group-btn">
                                                <button class="btn btn-primary updateFileReferences" type="submit"><g:message code="app.label.localizationHelp.updateReference"/></button>
                                            </span>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                        <div class="pv-caselist">
                            <table id="releaseNotesList" class="table table-striped pv-list-table dataTable no-footer">
                                <thead>
                                <tr>
                                    <th><g:message default="Release Number" code="app.label.localizationHelp.releaseNotes.releaseNumber"/></th>
                                    <th><g:message default="Title" code="app.label.localizationHelp.title"/></th>
                                    <th style="min-width: 70px"><g:message default="Action" code="app.label.action"/></th>
                                </tr>
                                </thead>
                            </table>
                        </div>
                    </div>
                </div>
            </rx:container>
            <g:form controller="${controller}" method="delete">
                <g:render template="/includes/widgets/deleteRecord"/>
            </g:form>
        </div>
    </div>
</div>

<g:render template="/oneDrive/downloadModal" model="[select: true]"/>
<div class="modal fade" id="exportReleaseNotesModal" tabindex="-1" role="dialog">
    <div class="modal-dialog " role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close dismissNoteHelp" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="modal.owner.change.title.label"/></h4>
            </div>
            <div class="modal-body ">
                <g:message code="app.label.localizationHelp.confirmation"/>
            </div>
            <div class="modal-footer">
                <a class="btn btn-primary" href="${createLink(controller: 'localizationHelpMessage', action: 'sendNotifications')}"><g:message code="default.button.confirm.label"/></a>
                <button type="button" class="btn pv-btn-grey " data-dismiss="modal">
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div>
        </div>
    </div>
</div>
</body>
</html>