<%@ page import="com.rxlogix.config.Capa8D; com.rxlogix.config.Capa8DAttachment"%>
<div id="createQualityIssue" class="modal fade" role="dialog">
    <div class="modal-dialog modal-lg   ">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><g:message code="qualityModule.createIssue.label"/></h4>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="issueNumberDlgErrorDiv" style="display: none">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <p class="issueNumberBlankError"><i class="fa fa-check"></i> <g:message code="com.rxlogix.config.Capa8D.issueNumber.nullable"/></p>
                    <p class="issueNumberUniqueError"><i class="fa fa-check"></i> <g:message code="com.rxlogix.config.Capa8D.issueNumber.unique"/></p>
                </div>
                <div class="alert alert-danger alert-dismissible issueErrorDiv" role="alert" hidden="hidden">
                    <button type="button" class="close" id="issueErrorDiv">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <div id="errormessage"></div>
                </div>
                <div class="alert alert-danger alert-dismissible" role="alert" id="descSizeExceed">
                    <g:message code="rod.capa.description.size.warning" args="[Capa8D.constrainedProperties.description.maxSize]"/>
                </div>
                <div class="alert alert-danger alert-dismissible attachSizeExceed" role="alert" hidden="hidden">
                    <button type="button" class="close" id="attachSizeExceed">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <div id="message"></div>
                </div>
                <div class="alert alert-success alert-dismissible actionItemSuccess m-t-4" role="alert" hidden="hidden">
                    <button type="button" class="close">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <div id="successMessage"></div>
                </div>
                <div class="row form-group">
                    <div class="col-lg-3">
                        <label for="issueNumber"><g:message code="quality.capa.capaNumber.label"/></label><span class="required-indicator">*</span>
                        <g:textField name="issueNumber" value="${capaInstance?.issueNumber}"
                                     maxlength="${Capa8D.constrainedProperties.issueNumber.maxSize}"
                                     class="form-control seriesName caseSeriesField"/>
                    </div>

                    <div class="col-lg-3">
                        <label for="issueTypePopup"><g:message code="quality.capa.issueType.label"/></label>
                        <g:textField name="issueTypePopup" value="${capaInstance?.issueType}"
                                     maxlength="${Capa8D.constrainedProperties.issueType.maxSize}"
                                     class="form-control seriesName caseSeriesField"/>
                    </div>

                    <div class="col-lg-3">
                        <label for="categoryPopup"><g:message code="quality.capa.category.label"/></label>
                        <g:textField name="categoryPopup" value="${capaInstance?.category}"
                                     maxlength="${Capa8D.constrainedProperties.category.maxSize}"
                                     class="form-control seriesName caseSeriesField"/>
                    </div>

                    <div class="col-lg-3">
                        <label for="remarksPopup"><g:message code="quality.capa.remarks.label"/></label>
                        <g:textField name="remarksPopup" value="${capaInstance?.remarks?.join(",")}"
                                     class="form-control seriesName caseSeriesField"/>
                    </div>

                </div>

                <div class="row form-group">
                    <div class="col-lg-3">
                        <label for="approvedByPopup"><g:message code="quality.capa.approvedBy.label"/></label>
                        <select id="approvedByPopup" name="approvedByPopup" class="form-control select2-box">
                            <option value="">${message(code: 'select.one')}</option>
                            <optgroup class="capaUsers" label="${g.message(code: 'user.label')}">
                            </optgroup>
                        </select>
                    </div>

                    <div class="col-lg-3">
                        <label for="initiatorPopup"><g:message code="quality.capa.initiator.label"/></label>
                        <select id="initiatorPopup" name="initiator" class="form-control select2-box">
                            <option value="">${message(code: 'select.one')}</option>
                            <optgroup class="capaUsers" label="${g.message(code: 'user.label')}">
                            </optgroup>
                        </select>
                    </div>

                    <div class="col-lg-3">
                        <label for="teamLeadPopup"><g:message code="quality.capa.teamLead.label"/></label>
                        <select id="teamLeadPopup" name="teamLeadPopup" class="form-control select2-box">
                            <option value="">${message(code: 'select.one')}</option>
                            <optgroup class="capaUsers" label="${g.message(code: 'user.label')}">
                            </optgroup>
                        </select>
                    </div>

                    <div class="col-lg-3">
                        <label for="teamMembersPopup"><g:message code="quality.capa.teamMembers.label"/></label>
                        <select id="teamMembersPopup" name="teamMembersPopup" class="form-control select2-box" multiple="true" data-placeholder="${message(code: 'select.one')}">
                            <optgroup class="capaUsers" label="${g.message(code: 'user.label')}">
                            </optgroup>
                        </select>
                    </div>
                </div>

                <div class="row form-group">
                    <div class="col-lg-3">
                        <label for="descriptionPopup"><g:message code="app.label.case.series.description"/></label>
                        <g:textArea rows="5" cols="5" name="descriptionPopup" value="${capaInstance?.description}"
                                    maxlength="${Capa8D.constrainedProperties.description.maxSize}"
                                    class="form-control description withCharCounter"/>
                    </div>

                    <div class="col-lg-3">
                        <label for="rootCausePopup"><g:message code="quality.capa.rootCause.label"/></label>
                        <g:textArea rows="5" cols="5" name="rootCausePopup" value="${capaInstance?.rootCause}"
                                    maxlength="${Capa8D.constrainedProperties.rootCause.maxSize}"
                                    class="form-control description withCharCounter"/>
                    </div>

                    <div class="col-lg-3">
                        <label for="verificationResultsPopup"><g:message code="quality.capa.verificationResults.label"/></label>
                        <g:textArea rows="5" cols="5" name="verificationResultsPopup"
                                    value="${capaInstance?.verificationResults}"
                                    maxlength="${Capa8D.constrainedProperties.verificationResults.maxSize}"
                                    class="form-control description withCharCounter"/>
                    </div>

                    <div class="col-lg-3">
                        <label for="commentsPopup"><g:message code="quality.capa.comments.label"/></label>

                        <g:textArea rows="5" cols="5" name="commentsPopup"
                                    value="${capaInstance?.comments}"
                                    maxlength="${Capa8D.constrainedProperties.comments.maxSize}"
                                    class="form-control description withCharCounter"/>
                    </div>
                </div>
                <div class="rxmain-container rxmain-container-top">
                    <div class="rxmain-container-row rxmain-container-header">
                        <label class="rxmain-container-header-label">
                            <g:message code="quality.attachment.label"/>
                        </label>
                    </div>

                    <div class="rxmain-container-content attachmnetcontainer rxmain-container-show">
                        <div class="row">
                            <div class="col-md-3">
                                <label for="filename_attach">Name</label>
                                <g:textField name="filename_attach" value=""
                                    maxlength="${Capa8DAttachment.constrainedProperties.filename.maxSize}"
                                    class="form-control filename_attach"/>
                            </div>
                            <div class="col-md-5">
                                <label>Attach</label>
                                <span class="input-group" style="width: 100%">
                                    <input type="text" class="form-control" id="file_name1" readonly >
                                        <label class="input-group-btn">
                                            <span class="btn btn-primary browse-button">
                                                <g:message code="quality.browse.label"/>
                                                <input type="file" id="file_input_attach" name="file" multiple  class="browse-button" style="display: none;">
                                            </span>
                                        </label>
                                </span>
                            </div>
                            <div class="col-md-3  m-t-25">
                                <g:actionSubmit value="${message(code: "quality.attach.label")}" class="btn btn-sm btn-primary attachment-button" id="createAttach"/>
                                <g:actionSubmit value="${message(code: "quality.attach.label")}" class="btn btn-sm btn-primary attachment-button" id="updateAttach"/>
                            </div>
                            <div id="icons" class="col-md-1"></div>
                        </div>
                    </div>

                    </div>
                    <div class="rxmain-container-content rxmain-container-show">
                        <div class="pv-caselist">
                            <table class="table table-striped pv-list-table dataTable no-footer" width="100%">
                                <thead>
                                    <tr>
                                        <th style="vertical-align: middle;text-align: left; min-width: 30px"> <div>
                                            <input type="checkbox" name="selectAll1" class="selectAllCheckbox1"/><label for="selectAll1"></label>
                                        </div></th>
                                        <th><g:message code="quality.attachment.label"/></th>
                                        <th><g:message code="quality.added.by.label"/></th>
                                        <th><g:message code="quality.date.added.label"/></th>
                                        <th><g:message code="quality.actions.label"/></th>
                                    </tr>
                                </thead>
                                <tbody id="attachmentList">
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary createCapaIssue" id="createCapaIssue"><g:message code="default.button.create.label"/></button>
                    <button type="button" class="btn btn-primary updateCapaIssue" id="updateCapaIssue" ><g:message code="default.button.update.label"/></button>
                    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
                </div>
                <div style="overflow: auto; max-width: 100%; width: auto; margin: 7px">
                    <table id="issue-number" class="table table-striped pv-list-table dataTable no-footer modal-header" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.linkedIssues"/></th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>


        </div>

    </div>
</div>