<%@ page contentType="text/html;charset=UTF-8" import="com.rxlogix.enums.ReasonOfDelayAppEnum;" %>
<html>
<head>

    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <g:if test="${type == ReasonOfDelayAppEnum.PVC}">
        <title><g:message code="app.central.title.central.issue.view" /></title>
    </g:if>
    <g:else>
        <title><g:message code="app.quality.title.quality.issue.view"/></title>
    </g:else>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/quality/capa_edit.js"/>
    <g:javascript>
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var downloadAllAttachmentUrl="${createLink(controller: 'issue', action: 'downloadAllAttachment')}";
    </g:javascript>
    <style>
    #corrective-actions_wrapper > .dt-layout-row:first-child {
        margin-top : 0px;
        padding-right: 0px;
        .dt-layout-cell {
            float: none;
        }
    }
    #preventive-actions_wrapper > .dt-layout-row:first-child {
        margin-top : 0px;
        padding-right: 0px;
        .dt-layout-cell {
            float: none;
        }
    }
    </style>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${capaInstance}" var="theInstance"/>
<g:if test="${type == ReasonOfDelayAppEnum.PVC}">
    <g:set var="issueControlller" value="pvcIssue" />
</g:if>
<g:else>
    <g:set var="issueControlller" value="issue" />
</g:else>
<div style="border: 0px;" class="rxmain-container">
    <div class="rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.quality.title.issue.view"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <div class="col-md-3">
                    <label><g:message code="quality.capa.capaNumber.label"/></label>

                    <p>${capaInstance?.issueNumber}</p>
                </div>
                <div class="col-md-3">
                    <label><g:message code="quality.capa.issueType.label"/></label>

                    <p>${capaInstance?.issueType}</p>
                </div>
                <div class="col-md-3">
                    <label><g:message code="quality.capa.category.label"/></label>

                    <p>${capaInstance?.category}</p>
                </div>
                <div class="col-md-3">
                    <label><g:message code="quality.capa.remarks.label"/></label>
                    <g:each in="${capaInstance?.remarks}" var="it">
                        <p>${it?.encodeAsHTML()}</p>
                    </g:each>
                </div>
            </div>
            <div class="row">
                <div class="col-md-3">
                    <label><g:message code="quality.capa.approvedBy.label"/></label>

                    <p>${capaInstance?.approvedBy?.fullName}</p>
                </div>
                <div class="col-md-3">
                    <label><g:message code="quality.capa.initiator.label"/></label>

                    <p>${capaInstance?.initiator?.fullName}</p>
                </div>
                <div class="col-md-3">
                    <label><g:message code="quality.capa.teamLead.label"/></label>

                    <p>${capaInstance?.teamLead?.fullName}</p>
                </div>
                <div class="col-md-3">
                    <label><g:message code="quality.capa.teamMembers.label"/></label>

                    <p>${capaInstance?.teamMembers?.fullName?.join(", ")}</p>
                </div>
            </div>
            <div class="row">
                <div class="col-md-3">
                    <label><g:message code="app.label.case.series.description"/></label>

                    <p>${capaInstance?.description}</p>
                </div>
                <div class="col-md-3">
                    <label><g:message code="quality.capa.rootCause.label"/></label>

                    <p>${capaInstance?.rootCause}</p>
                </div>
                <div class="col-md-3">
                    <label><g:message code="quality.capa.verificationResults.label"/></label>

                    <p>${capaInstance?.verificationResults}</p>
                </div>
                <div class="col-md-3">
                    <label><g:message code="quality.capa.comments.label"/></label>

                    <p>${capaInstance?.comments}</p>
                </div>
            </div>
            <g:if test="${caseNumber!=""}">
                <div class="row">
                    <div class="col-md-3">
                        <label><g:message code="app.label.action.item.associated.caseNumber"/></label>
                        <p>${caseNumber}</p>
                    </div>
                </div>
            </g:if>
            <div class="rxmain-container rxmain-container-top">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        <g:message code="quality.attachment.label"/>
                    </label>
                </div>

                <div class="rxmain-container-content rxmain-container-show">
                    <div class="row">
                        <div class="col-md-3">
                            <label for="filename">Name</label>
                            <g:textField name="filename" value=""
                                class="form-control"/>
                        </div>
                        <div class="col-md-5">
                            <label>Attach</label>
                            <span class="input-group" >
                                <input type="text" class="form-control" id="file_name" readonly>
                                    <label class="input-group-btn">
                                        <input type="file" id="file_input" name="file" multiple  class="browse-button" style="display: none;">
                                        <button class="btn btn-primary" id="browse_button"><g:message code="quality.browse.label"/></button>
                                    </label>
                            </span>
                        </div>
                        <div class="col-md-3" style='margin-top: 20px;'>
                            <g:actionSubmit value="${message(code: "quality.attach.label")}" action="validateAndCreate" class="btn btn-sm btn-primary attachment-button" id="saveButton"/>
                        </div>
                        <g:if test='${capaInstance?.attachments}'>
                        <div class="col-md-1" style='margin-top: 20px;'>
                            <sec:ifAnyGranted roles="ROLE_PVC_EDIT, ROLE_PVQ_EDIT, ROLE_ADMIN, ROLE_DEV">
                          <a href="javascript:void(0);" class="attachments m-t-5 hidden">
                            <i class="glyphicon glyphicon-download-alt theme-color" style="font-size: 18px;"
                               data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.download')}">
                            </i>
                          </a>
                            </sec:ifAnyGranted>
                        </div>
                        </g:if>
                    </div>
                </div>

                </div>
                <div class="rxmain-container-content rxmain-container-show">
                    <div class="pv-caselist">
                        <table id="rxTableAttachment" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                            <thead>
                                <tr>
                                    <th style="vertical-align: middle;text-align: left; min-width: 30px"> <div>
                                                                            <input type="checkbox" name="selectAll" class="selectAllCheckbox"/><label for="selectAll"></label>
                                                                        </div></th>
                                    <th><g:message code="quality.attachment.label"/></th>
                                    <th><g:message code="quality.added.by.label"/></th>
                                    <th><g:message code="quality.date.added.label"/></th>
                                    <th><g:message code="quality.actions.label"/></th>
                                </tr>
                            </thead>
                            <g:each var="item" in="${capaInstance?.attachments}">
                                <g:if test="${item.isDeleted == false}" >
                                <tr class="actionTableRow">
                                     <td style="vertical-align: middle;text-align: left; min-width: 30px"> <div>
                                         <input type="checkbox" _id="${item.id}" class="selectCheckbox"  name="selected" />
                                     </div>
                                     <td>
                                         ${item.filename}
                                     </td>
                                     <td>
                                         ${item.createdBy}
                                     </td>
                                     <td>
                                         <g:renderShortFormattedDate date="${item.dateCreated}" timeZone="${userTimeZone}"/>
                                     </td>
                                     <td>
                                         <sec:ifAnyGranted roles="ROLE_PVC_EDIT, ROLE_PVC_EDIT, ROLE_ADMIN, ROLE_DEV">
                                             <g:link action="downloadAttachment" params="[id: item.id]">
                                                 <i id="saveButton" class="glyphicon glyphicon-download-alt theme-color" style="font-size: 18px;"
                                                    data-toggle="modal" data-target="#saveAsModal" data-action="viewMultiTemplateReport"
                                                    data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.download')}">
                                                 </i>
                                             </g:link>
                                         </sec:ifAnyGranted>
                                     </td>
                                </tr>
                                </g:if>
                            </g:each>

                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <g:set var="userTimeZone" value="${getCurrentUserTimezone()}"/>
    <div class="rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="quality.issue.actions.label"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="pv-caselist">

                <div class="col-lg-6">
                    <div class="row" >
                        <div class="col-lg-12">
                            <g:message code="quality.capa.corrective.label"/>
                        </div>
                    </div>
                    <table id="corrective-actions" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.description"/></th>
                            <th><g:message code="app.label.action.item.due.date"/></th>
                            <th><g:message code="app.label.action.item.priority"/></th>
                            <th><g:message code="app.label.action.item.status"/></th>
                        </tr>
                        </thead>
                        <g:each var="item" in="${capaInstance?.correctiveActions}">
                           <g:if test="${!item.isDeleted}">
                            <tr class="actionTableRow">
                                <td>
                                    <a href="javascript:void(0)" data-actionId="${item.id}" class="action-item-view">${item.description}</a>
                                </td>
                                <td>
                                    <g:renderShortFormattedDate date="${item.dueDate}" timeZone="${userTimeZone}"/>
                                </td>
                                <td>
                                    ${item.priority}
                                </td>
                                <td>
                                    <g:message code="${item.status?.getI18nKey()}"/>
                                </td>
                            </tr>
                           </g:if>
                        </g:each>
                    </table>
                </div>

                <div class="col-lg-6">
                    <div class="row" >
                        <div class="col-lg-12">
                            <g:message code="quality.capa.preventive.label"/>
                        </div>
                    </div>
                    <table id="preventive-actions" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.description"/></th>
                            <th><g:message code="app.label.action.item.due.date"/></th>
                            <th><g:message code="app.label.action.item.priority"/></th>
                            <th><g:message code="app.label.action.item.status"/></th>
                        </tr>
                        </thead>
                        <g:each var="item" in="${capaInstance?.preventiveActions}">
                            <g:if test="${!item.isDeleted}">
                            <tr class="actionTableRow">
                                <td>
                                    <a href="javascript:void(0)" data-actionId="${item.id}" class="action-item-view">${item.description}</a>
                                </td>
                                <td>
                                    <g:renderShortFormattedDate date="${item.dueDate}" timeZone="${userTimeZone}"/>
                                </td>
                                <td>
                                    ${item.priority}
                                </td>
                                <td>
                                    <g:message code="${item.status?.getI18nKey()}"/>
                                </td>
                            </tr>
                            </g:if>
                        </g:each>
                    </table>
                </div>

            </div>
            <div class="button" style="text-align: right">
                <div class="row">
                    <div class="col-lg-12" style="margin-top: 10px;">
                        <sec:ifAnyGranted roles="ROLE_PVQ_EDIT, ROLE_PVC_EDIT">
                            <g:link controller="${issueControlller}" action="edit" id="${params.id}"
                                    class="btn btn-primary"><g:message code="default.button.edit.label"/></g:link>
                            <button type="reset" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["${issueControlller}", "index"]}'
                                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                        </sec:ifAnyGranted>
                    </div>
                </div>
            </div>
        </div>

    </div>

</div>
</div>
<g:form name="attachForm" method="post">
    <input type="hidden" name="selectAll" id="selectAll">
    <input type="hidden" name="selectedIds" id="selectedIds">
    <input type="hidden" name="capaInstanceId" value="${capaInstance.id}">
</g:form>
<g:render template="/actionItem/includes/actionItemModal"/>
<g:render template="/includes/widgets/infoTemplate"/>
<g:javascript>
    $(function() {
        $("#viewScreenButton").hide();
        $("#filename").attr("disabled", true);
        $("textarea").attr("disabled", true);
        $("select").attr("disabled", true);
        $("#browse_button").attr("disabled", true);
    });
</g:javascript>
</body>
</html>
