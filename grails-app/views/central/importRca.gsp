<%@ page import="com.rxlogix.Constants;com.rxlogix.ReportExecutorService" %>
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
                        <li role="presentation" ><a href="${createLink(action: "importSubmissions")}" ><g:message code="app.pvc.import.offline.submission" /></a></li>
                        <li role="presentation" class="active"><a href="#"><g:message code="app.pvc.import.rca" /></a></li>
                     </ul>
                    <br>
                    <form action="importRca" id="fileForm" method="post" enctype="multipart/form-data">

                        <pre><g:message code="app.pvc.import.rca.comment"/> <g:message code="app.pvc.import.rca.usethis"/> <a href="${createLink(action: "downloadTemplate")}"><g:message code="app.pvc.import.rca.template"/></a>.
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
                        <form id="listForm" action="importRcaForm" method="post" >
                            <div style="margin: 10px;">
                        <g:message code="app.pvc.import.pleaseCheck"/></div>
                            <div style="margin: 10px;">
                                <div class="radio radio-primary radio-inline">
                                    <input type="radio" name="replace" id="replace2" ${params.replace=="append"?"checked":""} value="append" autocomplete="off">
                                    <label for="replace2">
                                        <g:message code="app.pvc.import.Append"/>
                                    </label>
                                </div>
                                <div class="radio radio-primary radio-inline">
                                    <input type="radio" name="replace" id="replace1" ${params.replace!="append"?"checked":""} value="replace" autocomplete="off">
                                    <label for="replace1">
                                        <g:message code="app.pvc.import.Replace"/>
                                    </label>
                                </div>
                            </div>
                            <table width="100%" class="table">
                                <tr>
                                    <th></th>
                                    <th><g:message code="app.pvc.import.CaseNumber"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.import.ReportAgencyName"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.import.CaseReceiptDate"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.import.ReportDueDate"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.import.SubmissionDate"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.late"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.rootcause"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.RootCauseClass"/></th>
                                    <th><g:message code="app.pvc.RootCauseSubCategory"/></th>
                                    <th><g:message code="app.pvc.ResponsibleParty"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.CorrectiveAction"/></th>
                                    <th><g:message code="app.pvc.PreventiveAction"/></th>
                                    <th><g:message code="app.pvc.CorrectiveDate"/></th>
                                    <th><g:message code="app.pvc.PreventiveDate"/></th>
                                    <th><g:message code="app.pvc.import.Primary"/><span class="required-indicator">*</span></th>
                                    <th><g:message code="app.pvc.version"/></th>
                                    <th><g:message code="app.pvc.investigation"/></th>
                                    <th><g:message code="app.pvc.summary"/></th>
                                    <th><g:message code="app.pvc.actions"/></th>
                                    <th><g:message code="app.pvc.import.Status"/></th>
                                </tr>
                                <g:each var="row" in="${rows}" status="i">
                                    <tr class="import-rca-data-row"><td><span class="table-remove glyphicon glyphicon-remove"></span></td>
                                        <g:each var="cell" in="${row}" status="j">
                                            <g:if test="${j == Constants.Central.RCA_COLUMN_NUMBER}">
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
                                                        <g:if test="${j in [0,14]}">
                                                            maxlength="255"
                                                        </g:if>
                                                        <g:elseif test="${j in [5,6,7,8,9,10,11,16,17,18]}">
                                                            maxlength="2000"
                                                        </g:elseif>
                                                        <g:elseif test="${j == 1}">
                                                            maxlength="4000"
                                                        </g:elseif>
                                                        <g:elseif test="${j == 15}">
                                                            maxlength="10"
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
