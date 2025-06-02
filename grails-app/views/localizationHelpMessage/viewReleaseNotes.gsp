<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.releaseNotes.view.title"/></title>

</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.label.localizationHelp.releaseNote")}">

                <div class="body">
                    <div>

                        <g:render template="/includes/layout/flashErrorsDivs" bean="${instance}" var="theInstance"/>
                        <div class="row">
                            <div class="col-md-12">
                                <h2>${instance.releaseNumber} ${instance.title ?: ""} <a href="${createLink(controller: 'localizationHelpMessage', action: 'editReleaseNotes')}?id=${instance.id}" class="btn btn-primary"><g:message code="app.edit.button.label"/></a>
                                </h2>
                                ${raw(instance.description)}
                                <table class="table table-hover" style="width: 100%;overflow: auto;display: block;">
                                    <thead>
                                    <tr>
                                        <th style="min-width: 100px; width: 5vw"><g:message code="app.label.releaseNotesItem.visible"/></th>
                                        <th style="min-width: 250px; width: 20vw"><g:message code="app.label.releaseNotesItem.title"/></th>
                                        <th><g:message code="app.label.releaseNotesItem.summary"/></th>
                                        <th ><g:message code="app.label.releaseNotesItem.shortDescription"/></th>
                                        <th style="width: 5vw;min-width: 100px"><g:message code="app.label.action"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <g:each var="item" in="${instance.notes.findAll { !it.isDeleted }?.sort { it.sortNumber }}">
                                        <tr>
                                            <td>${item.invisible ? message(code: "app.label.no") : message(code: "app.label.yes")}</td>
                                            <td>${item.title ?: ""}</td>
                                            <td><div style="overflow: auto;width: 30vw">${raw(item.summary ?: "")}</div></td>
                                            <td><div style="overflow: auto;width: 30vw">${raw(item.shortDescription ?: "")}
                                                <g:if test="${item.hasDescription}">
                                                    <a class="showWhatsNewDescription" style="cursor: pointer; text-decoration: underline" data-id="${item.id}">
                                                        <g:message code="app.label.releaseNotesItem.learnMore"/>
                                                    </a>
                                                </g:if></div>
                                            </td>
                                            <td>
                                                <div class="btn-group dropdown " align="center">

                                                    <a class="btn btn-success btn-xs" href="${createLink(controller: 'localizationHelpMessage', action: 'editReleaseNotesItem')}?id=${item.id}"><g:message code="app.edit.button.label"/></a>
                                                    <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">
                                                        <span class="caret"></span>
                                                        <span class="sr-only">Toggle Dropdown</span>
                                                    </button>
                                                    <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">

                                                        <li role="presentation">
                                                            <a data-toggle="modal" data-target="#deleteModal" data-instancetype="Release Note Item" data-action="deleteReleaseNotesItem" data-instanceid="${item.id}" data-instancename="${item.title}"><g:message code="default.button.delete.label"/></a>
                                                        </li>
                                                        <li role="presentation">
                                                            <a href="${createLink(controller: 'localizationHelpMessage', action: 'toggleVisability')}?id=${item.id}">
                                                                <g:if test="${item.invisible}">
                                                                    <g:message code="app.label.show"/>
                                                                </g:if>
                                                                <g:else>
                                                                    <g:message code="app.label.show.as.hide"/>
                                                                </g:else>
                                                            </a>
                                                        </li>

                                                    </ul>
                                                </div>
                                            </td>

                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>

                            </div>

                        </div>
                        <a href="${createLink(controller: 'localizationHelpMessage', action: 'createReleaseNotesItem')}?id=${instance.id}" class="btn btn-primary"><g:message code="app.label.releaseNotesItem.create"/></a>
                        <a class="btn pv-btn-grey" href="${createLink(controller: 'localizationHelpMessage', action: 'releaseNotes')}"><g:message code="default.button.close.label"/></a>
                    </div>
                </div>
            </rx:container>

        </div>
    </div>
</div>
<g:render template="includes/releaseNoteHelpModal"/>
<g:form controller="${controller}" method="delete">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
</body>
</html>