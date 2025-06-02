<%@ page import="com.rxlogix.ReportExecutorService" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.pvc.import.submission.title"/></title>
    <asset:javascript src="app/importRca.js"/>
    <asset:stylesheet src="expandingTextarea.css"/>
</head>

<body>
<div class="content">
    <div class="container ">
        <div class="row">
            <rx:container title="${message(code: "app.pvc.import.submission")}">
                <div class="container-fluid">
                    <g:render template="/includes/layout/flashErrorsDivs" bean="${error}" var="theInstance"/>
                    <ul class="nav nav-tabs" role="tablist">
                        <li role="presentation" class="active"><a href="${createLink(action: "importRca")}" ><g:message code="app.pvc.import.offline.submission" /></a></li>
                        <li role="presentation" ><a href="${createLink(action: "importRca")}"><g:message code="app.pvc.import.rca" /></a></li>
                     </ul>
                    <br>
                    <form action="importSubmissions" id="fileForm" method="post" enctype="multipart/form-data">

                        <pre><g:message code="app.pvc.import.submissions.comment"/> <g:message code="app.pvc.import.rca.usethis"/> <a href="${createLink(action: "downloadSubmissionsTemplate")}"><g:message code="app.pvc.import.rca.template"/></a>.
                        </pre>
                        <div class="row">
                            <div class="col-xs-12">
                                <div class="input-group" style="width: 50%; float: left">
                                    <input type="text" class="form-control" id="file_name" readonly>
                                    <label class="input-group-btn">
                                        <span class="btn btn-primary">
                                            <g:message code="app.pvc.import.chooseFile"/>
                                            <input type="file" id="execlFile" name="file" accept=".xlsx" style="display: none;">
                                        </span>
                                    </label>
                                </div>

                            </div>
                        </div>


                        <div class="row" style="margin-top: 15px">
                            <div class="col-xs-12">

                                <button type="submit" class="btn btn-primary previewButton" >${message(code: 'app.pvc.import.preview')}</button>

                            </div>
                        </div>
                    </form>
                    <g:if test="${rows}">
                        <hr>
                        <form id="listForm" action="importSubmissionsForm" method="post" >
                            <div style="margin: 10px;">
                        <g:message code="app.pvc.import.pleaseCheck"/></div>
                            <table width="100%" class="table">
                                <tr>
                                    <th></th>
                                    <th><g:message code="app.pvc.import.CaseNumber"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.import.CaseReceiptDate"/></th>
                                    <th><g:message code="app.pvc.import.destination"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.import.ReportDueDate"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.import.SubmissionDate"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.import.ExpPeriodicSubmission"/></th>
                                    <th><g:message code="app.pvc.import.Timeframe"/></th>
                                    <th><g:message code="app.pvc.import.ReportForm"/></th>
                                </tr>
                                <g:each var="row" in="${rows}" status="i">
                                    <tr class="import-rca-data-row"><td><span class="table-remove glyphicon glyphicon-remove"></span></td>
                                        <g:each var="cell" in="${row}" status="j">
                                            <g:if test="${j == 8}">
                                                <td>
                                                    <g:if test="${!cell}">
                                                        <span style="color:green"><g:message code="app.pvc.import.success"/></span>
                                                    </g:if>
                                                    <g:else>
                                                        <span class="rowErrorMessage" style="color:red">${cell}</span>
                                                    </g:else>
                                                </td>
                                            </g:if>
                                            <g:else>
                                                <td>
                                                    <input name="cell_${i}_${j}" value="${cell}"
                                                        <g:if test="${j in [0,2,7]}">
                                                            maxlength="255"
                                                        </g:if>
                                                        <g:elseif test="${j == 5}">
                                                            maxlength="${ReportExecutorService.ALLOWED_SUBMISSION_TYPES.max{it.length()}.length()}"
                                                        </g:elseif>
                                                        <g:elseif test="${j == 6}">
                                                            maxlength="${Integer.MAX_VALUE.toString().length()}"
                                                        </g:elseif>
                                                    class="form-control">
                                                </td>
                                            </g:else>
                                        </g:each>
                                    </tr>
                                </g:each>
                            </table>

                            <div>
                                <input type="hidden" name="submit" id="submitInput" value="false">
                                <button type="submit" class="btn btn-primary validateButton"><g:message code="app.pvc.import.Validate"/></button>
                                <button type="submit" disabled class="btn btn-primary applyButton"><g:message code="app.pvc.import.Apply"/></button>
                            </div>
                            <input type="hidden" name="total" id="total" value="${rows.size()}">
                        </form>
                    </g:if>
                </div>
            </rx:container>
        </div>
    </div>
</div>
</body>
