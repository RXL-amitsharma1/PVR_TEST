<%@ page import="com.rxlogix.localization.ReleaseNotes" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.localizationHelp.releaseNotes.pageTitle"/></title>

</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "app.label.localizationHelp.releaseNotes")}">
                <g:if test="${releaseNotes && instance}">
                <g:each var="release" in="${releaseNotes}">
                    <g:if test="${instance.id == release?.id}">
                        <a class="btn btn-primary" disabled="" href="#">
                            ${release.releaseNumber}
                        </a>
                    </g:if>
                    <g:else>
                        <a class="btn btn-primary " href="${createLink(controller: 'localizationHelpMessage', action: 'readReleaseNotes')}?id=${release.id}${params.fromList?"&fromList=true":""}">
                            ${release.releaseNumber}
                        </a>
                    </g:else>
                </g:each>

                <div class="body">
                    <div>

                        <div class="row">
                            <div class="col-md-12" >
                                <h2>${instance.releaseNumber} ${instance.title ?: ""}
                                </h2>
                                ${raw(instance.description)}
                                <g:if test="${instance?.notes.findAll { !it.isDeleted && !it.invisible }?.sort { it.sortNumber }}">
                                    <table class="table table-hover" style="width: 100%;overflow: auto;display: block;">
                                        <thead>
                                        <tr>
                                            <th style="min-width: 250px; width: 20vw"><g:message code="app.label.releaseNotesItem.title"/></th>
                                            <th><g:message code="app.label.releaseNotesItem.summary"/></th>
                                            <th ><g:message code="app.label.releaseNotesItem.shortDescription"/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <g:each var="item" in="${instance?.notes.findAll { !it.isDeleted && !it.invisible }?.sort { it.sortNumber }}">
                                            <tr>
                                                <td>${item.title ?: ""}</td>
                                                <td><div style="overflow: auto;width: 35vw">${raw(item.summary ?: "")}</div></td>
                                                <td><div style="overflow: auto;width: 35vw">${raw(item.shortDescription ?: "")}
                                                    <g:if test="${item.hasDescription}">
                                                        <a class="showWhatsNewDescription" style="cursor: pointer; text-decoration: underline" data-id="${item.id}">
                                                            <g:message code="app.label.releaseNotesItem.learnMore"/>
                                                        </a>
                                                    </g:if></div>
                                                </td>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </g:if>
                            </div>
                        </div>

                        <g:if test="${params.fromList}">
                            <a class="btn pv-btn-grey"
                               href="${createLink(controller: 'localizationHelpMessage', action: 'releaseNotes')}"><g:message
                                    code="default.button.close.label"/></a>
                        </g:if>
                    </div>
                </div>
                </g:if>
                <g:else>
                    <div style="width: 100%; text-align: center">
                    <g:message code="app.label.localizationHelp.releaseNotes.no"/>
                    </div>
                </g:else>
            </rx:container>

        </div>
    </div>
</div>

<g:render template="includes/releaseNoteHelpModal"/>

</body>
</html>