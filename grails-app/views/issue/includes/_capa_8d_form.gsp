<%@ page import="com.rxlogix.config.ReportSubmission; com.rxlogix.config.ExecutedReportConfiguration; com.rxlogix.user.User; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.Capa8D; com.rxlogix.config.Capa8DAttachment" %>

<div style="border: 0px;" class="rxmain-container">
    <div class="rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header">
            <g:if test='${editmode == true}'>
                <label class="rxmain-container-header-label">
                    Edit Issue
                </label>
            </g:if>
            <g:else>
                <label class="rxmain-container-header-label">
                    Basic Issue Information
                </label>
            </g:else>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row form-group">
                <div class="col-lg-3">
                    <label for="issueNumber"><g:message code="quality.capa.capaNumber.label"/></label><span class="required-indicator">*</span>
                    <g:textField name="issueNumber" value="${capaInstance?.issueNumber}"
                                 maxlength="${Capa8D.constrainedProperties.issueNumber.maxSize}"
                                 class="form-control seriesName caseSeriesField"/>
                </div>
                <input type="hidden" name="oldIssueNumber" value="${capaInstance?.issueNumber}">
                <div class="col-lg-3">
                    <label for="issueType"><g:message code="quality.capa.issueType.label"/></label>
                    <g:textField name="issueType" value="${capaInstance?.issueType}"
                                 class="form-control seriesName caseSeriesField" maxlength="${Capa8D.constrainedProperties.issueType.maxSize}"/>
                </div>

                <div class="col-lg-3">
                    <label for="category"><g:message code="quality.capa.category.label"/></label>
                    <g:textField name="category" value="${capaInstance?.category}"
                                 class="form-control seriesName caseSeriesField" maxlength="${Capa8D.constrainedProperties.category.maxSize}"/>
                </div>

                <div class="col-lg-3">
                    <label for="remarks"><g:message code="quality.capa.remarks.label"/></label>
                    <g:textField name="remarks" value="${capaInstance?.remarks?.join(",")}"
                                 class="form-control seriesName caseSeriesField" maxlength="${Capa8D.REMARKS_MAX_LENGTH}"/>
                </div>

            </div>

            <div class="row form-group">
                <div class="col-lg-3">
                    <label for="approvedBy"><g:message code="quality.capa.approvedBy.label"/></label>
                    <select id="approvedBy" name="approvedBy" class="form-control select2-box">
                        <option value="">${message(code: 'select.one')}</option>
                        <optgroup label="${g.message(code: 'user.label')}">
                            <g:each in="${users}" var="user">
                                <option value="${user.id}" ${capaInstance?.approvedBy?.id==user.id ?'selected="selected"':''}>${user.getReportRequestorValue()}</option>
                            </g:each>
                        </optgroup>
                    </select>
                </div>

                <div class="col-lg-3">
                    <label for="initiator"><g:message code="quality.capa.initiator.label"/></label>
                    <select id="initiator" name="initiator" class="form-control select2-box">
                        <option value="">${message(code: 'select.one')}</option>
                        <optgroup label="${g.message(code: 'user.label')}">
                            <g:each in="${users}" var="user">
                                <option value="${user.id}" ${capaInstance?.initiator?.id==user.id ?'selected="selected"':''}>${user.getReportRequestorValue()}</option>
                            </g:each>
                        </optgroup>
                    </select>
                </div>

                <div class="col-lg-3">
                    <label for="teamLead"><g:message code="quality.capa.teamLead.label"/></label>
                    <select id="teamLead" name="teamLead" class="form-control select2-box">
                        <option value="">${message(code: 'select.one')}</option>
                        <optgroup label="${g.message(code: 'user.label')}">
                            <g:each in="${users}" var="user">
                                <option value="${user.id}"  ${capaInstance?.teamLead?.id==user.id ?'selected="selected"':''}>${user.getReportRequestorValue()}</option>
                            </g:each>
                        </optgroup>
                    </select>
                </div>

                <div class="col-lg-3">
                    <label for="teamMembers"><g:message code="quality.capa.teamMembers.label"/></label>
                    <select id="teamMembers" name="teamMembers" class="form-control select2-box" multiple="true" >
                        <option value="">${message(code: 'select.one')}</option>
                        <optgroup label="${g.message(code: 'user.label')}">
                            <g:each in="${users}" var="user">
                                <option value="${user.id}"  ${capaInstance?.teamMembers?.find{it.id==user.id} ?'selected="selected"':''}>${user.getReportRequestorValue()}</option>
                            </g:each>
                        </optgroup>
                    </select>
                </div>
            </div>

            <div class="row form-group">
                <div class="col-lg-3">
                    <label for="description"><g:message code="app.label.case.series.description"/></label>
                    <g:textArea rows="5" cols="5" name="description" value="${capaInstance?.description?:(params.description?:"")}"
                                class="form-control description withCharCounter" maxlength="${Capa8D.constrainedProperties.description.maxSize}"/>
                </div>

                <div class="col-lg-3">
                    <label for="rootCause"><g:message code="quality.capa.rootCause.label"/></label>
                    <g:textArea rows="5" cols="5" name="rootCause" value="${capaInstance?.rootCause}"
                                class="form-control description withCharCounter" maxlength="${Capa8D.constrainedProperties.rootCause.maxSize}"/>
                </div>

                <div class="col-lg-3">
                    <label for="verificationResults"><g:message code="quality.capa.verificationResults.label"/></label>
                    <g:textArea rows="5" cols="5" name="verificationResults"
                                value="${capaInstance?.verificationResults}"
                                class="form-control description withCharCounter" maxlength="${Capa8D.constrainedProperties.verificationResults.maxSize}"/>
                </div>

                <div class="col-lg-3">
                    <label for="comments"><g:message code="quality.capa.comments.label"/></label>

                    <g:textArea rows="5" cols="5" name="comments"
                                value="${capaInstance?.comments}"
                                class="form-control description withCharCounter" maxlength="${Capa8D.constrainedProperties.comments.maxSize}"/>
                </div>
            </div>
            <div class="rxmain-container rxmain-container-top">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        <g:message code="quality.attachment.label"/>
                    </label>
                </div>

                <div class="rxmain-container-content rxmain-container-show attachment-container">
                    <div class="row">
                        <div class="col-md-3">
                            <label for="filename_attach">Name</label>
                            <g:textField name="filename_attach" value=""
                                class="form-control filename_attach" maxlength="${Capa8DAttachment.constrainedProperties.filename.maxSize}"/>
                        </div>
                        <div class="col-md-5">
                            <label>Attach</label>
                            <span class="input-group" >
                                <input type="text" class="form-control" id="attach_file_name" readonly>
                                    <label class="input-group-btn">
                                        <span class="btn btn-primary browse-button">
                                            <g:message code="quality.browse.label"/>
                                            <input type="file" id="attach_file_input" name="file" multiple style="display: none;">
                                        </span>
                                    </label>
                            </span>
                        </div>
                        <div class="col-md-3" style='margin-top: 20px;'>
                            <g:actionSubmit value="${message(code: "quality.attach.label")}" id="attachment-capa" class="btn btn-sm btn-primary attachment-button"/>
                        </div>
                        <g:if test='${editmode == true && capaInstance?.attachments}'>
                            <div class="col-md-1 m-t-25">
                                <sec:ifAnyGranted roles="ROLE_PVC_EDIT, ROLE_PVQ_EDIT, ROLE_ADMIN, ROLE_DEV">
                                    <a href="javascript:void(0);" class="attachments m-t-5 hidden">
                                        <i class="glyphicon glyphicon-download-alt theme-color"
                                            title="${message(code: 'app.label.download')}">
                                        </i>
                                    </a>
                                    <a href="javascript:void(0);" class="removes m-t-5 hidden">
                                        <i data-toggle="popover" data-trigger="hover" data-placement="top" data-container="body"
                                           data-content="<g:message code="issue.delete.multi.attachment"/> " class="md-lg md-close theme-color v-a-initial"
                                           ></i>
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
                                    <g:if test='${editmode == true}'>
                                        <th style="vertical-align: middle;text-align: left; min-width: 30px">
                                             <div><input type="checkbox" name="selectAll" class="selectAllCheckbox"/><label for="selectAll"></label></div>
                                        </th>
                                    </g:if>
                                    <g:else>
                                        <th style="vertical-align: middle;text-align: left; min-width: 30px">
                                             <div><input type="checkbox" name="selectAll" class="selectAllCheckbox" disabled/><label for="selectAll"></label></div>
                                        </th>
                                    </g:else>
                                    <th><g:message code="quality.attachment.label"/></th>
                                    <th><g:message code="quality.added.by.label"/></th>
                                    <th><g:message code="quality.date.added.label"/></th>
                                    <th><g:message code="quality.actions.label"/></th>
                                </tr>
                            </thead>
                            <tbody id="attachmentListcapa">
                                <g:each var="item" in="${capaInstance?.attachments}">
                                   <g:if test="${item.isDeleted == false}">
                                    <tr class="actionTableRow">
                                         <g:if test='${editmode == true}'>
                                            <td style="vertical-align: middle;text-align: left; min-width: 30px">
                                            <div><input type="checkbox" _id="${item.id}" class="selectCheckbox"  name="selected"/></div>
                                            </td>
                                         </g:if>
                                         <g:else>
                                            <td style="vertical-align: middle;text-align: left; min-width: 30px">
                                            <div><input type="checkbox" _id="${item.id}" class="selectCheckbox"  name="selected" disabled/></div>
                                            </td>
                                         </g:else>
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
                                             <g:link action="downloadAttachment" params="[id: item.id]" target="_blank">
                                                <i id="saveButton" class="glyphicon glyphicon-download-alt theme-color"
                                                   data-toggle="modal" data-target="#saveAsModal" data-action="viewMultiTemplateReport"
                                                   data-tooltip="tooltip" data-placement="bottom" title="${message(code: 'app.label.download')}">
                                                </i>
                                             </g:link>
                                             <a href="javascript:void(0);" class="remove" id="${item.id}" capaId="${capaInstance.id}" >
                                                 <i data-toggle="popover" data-trigger="hover" data-container="body"
                                                    data-content="<g:message code="issue.delete.attachment"/> " class="md-lg md-close theme-color v-a-initial" ></i>
                                             </a>
                                             </sec:ifAnyGranted>
                                         </td>
                                    </tr>
                                    </g:if>
                                </g:each>
                            </tbody>

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
    <g:if test="${!capaInstance?.id}">
        <div class="rxmain-container-content rxmain-container-show border_radius_0">
      <div class="alert alert-info report_request_alert" role="alert" >
            <g:message code="quality.capa.validateAndCreate.left.label"/> <g:actionSubmit value="${message(code: "quality.capa.validateAndCreate.label")}" action="validateAndCreate" class="btn btn-sm btn-success report-create-button" id="saveButton"/> <g:message code="quality.issue.validateAndCreate.right.label"/>
        </div>
        </div>
    </g:if>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="pv-caselist">
<div class="row">
    <div class="col-lg-6">
        <div class="row" >
            <div class="col-lg-11">
                <g:message code="quality.capa.corrective.label"/>
            </div>
            <div class="col-lg-1" >
                <a href="javascript:void(0)" class="btn btn-success btn-xs createActionItem ${capaInstance?.id?"":"disabled"}" data-actionType="QUALITY_MODULE_CORRECTIVE" style="margin-left: -10px;" data-ownerType="${type}"><g:message code="default.button.add.label"/></a>
            </div>
        </div>
        <table id="corrective-actions" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.description"/></th>
                <th><g:message code="app.label.action.item.due.date"/></th>
                <th><g:message code="app.label.action.item.priority"/></th>
                <th><g:message code="app.label.action.item.status"/></th>
                <th></th>
            </tr>
            </thead>
            <g:each var="item" in="${capaInstance?.correctiveActions}">
                <g:if test="${!item.isDeleted}">
                <tr class="actionTableRow">
                    <td>
                        <a href="javascript:void(0)" data-actionId="${item.id}" class="action-item-edit" data-ownerType="${type}">${item.description}</a>
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
                    <td>
                        <a href="javascript:void(0)" data-actionId="${item.id}" class="btn btn-success btn-xs action-item-remove" data-ownerType="${type}"><g:message code="default.button.remove.label"/></a>
                    </td>
                </tr>
                </g:if>
            </g:each>
        </table>
    </div>

    <div class="col-lg-6">
        <div class="row" >
            <div class="col-lg-11">
                <g:message code="quality.capa.preventive.label"/>
            </div>
            <div class="col-lg-1" >
                <a href="javascript:void(0)" class="btn btn-success btn-xs createActionItem ${capaInstance?.id?"":"disabled"}" data-actionType="QUALITY_MODULE_PREVENTIVE" style="margin-left: -10px;" data-ownerType="${type}"><g:message code="default.button.add.label"/></a>
            </div>
        </div>
        <table id="preventive-actions" class="table table-striped pv-list-table dataTable no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.description"/></th>
                <th><g:message code="app.label.action.item.due.date"/></th>
                <th><g:message code="app.label.action.item.priority"/></th>
                <th><g:message code="app.label.action.item.status"/></th>
                <th></th>
            </tr>
            </thead>
            <g:each var="item" in="${capaInstance?.preventiveActions}">
                <g:if test="${!item.isDeleted}">
                <tr class="actionTableRow">
                    <td>
                        <a href="javascript:void(0)" data-actionId="${item.id}" class="action-item-edit" data-ownerType="${type}">${item.description}</a>
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
                    <td>
                        <a href="javascript:void(0)" data-actionId="${item.id}" class="btn btn-success btn-xs action-item-remove" data-ownerType="${type}"><g:message code="default.button.remove.label"/></a>
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
    <g:set var="configuration" value="${capaInstance?.configuration?:(params.configurationId?ExecutedReportConfiguration.get(params.configurationId):null)}"/>
    <input type="hidden" name="configuration" value="${configuration?.id}">
   <g:if test="${configuration}">
    <div class="rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                Linked Report
            </label>
        </div>
        <div class="rxmain-container-content rxmain-container-show">
        <g:set var="configuration" value="${capaInstance?.configuration?:(params.configurationId?ExecutedReportConfiguration.get(params.configurationId):null)}"/>
        <g:set var="submission" value="${capaInstance?.submission?:(params.submissionId?ReportSubmission.get(params.submissionId):null)}"/>
        <input type="hidden" name="submission" value="${submission?.id}">
        <g:if test="${configuration}">
            <label>Report:</label>  <a href="${createLink(controller: 'report', action: 'showFirstSection')}?id=${configuration.id}">${configuration.reportName}</a><br>
        </g:if>
        <g:if test="${submission}">
            <label>Submission details:</label><br>
            <b>Status:</b> <g:message code="${submission.reportSubmissionStatus.i18nKey}"/><br>
            <b>Comment:</b> ${submission.comment}<br>
            <b>Destination:</b> ${submission.reportingDestination}<br>
            <b>Submission Date:</b> ${submission.submissionDate.format("dd-MMM-yyyy HH:mm")}<br>
            <b>Due Date:</b> ${submission.dueDate.format("dd-MMM-yyyy")}<br>
            <b>Late:</b>  <g:message code="${submission.late}"/> <br>
            <g:each in="${submission.lateReasons}" var="reason">
                <b>Responsible Party:</b> ${reason.responsible};
                <b>Reason:</b> ${reason.reason}<br>
            </g:each>

        </g:if>
        </div>
    </div>
   </g:if>
</div>



