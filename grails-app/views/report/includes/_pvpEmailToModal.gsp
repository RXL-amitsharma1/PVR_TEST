<asset:javascript src="/app/emailModal.js"/>
<div class="modal fade" data-keyboard="false" data-backdrop="static" id="emailToModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.emailTo" /></h4>
            </div>
            <div class="modal-body">

                <div class="alert alert-danger hide">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                    <span><g:message code="app.error.choose.email.required"/></span>
                </div>

                <div class="row m-b-10">
                    <div class="col-xs-10" style="padding-right: 27px;">
                        <g:select id="emailUsers"
                                  name="emailToUsers"
                                  from="${[]}"
                                  data-value=""
                                  class="form-control emailUsers" multiple="true"
                                  data-options-url="${createLink(controller: 'email', action: 'allEmails')}"/><i
                            class="fa fa-pencil-square-o copyPasteEmailButton paste-icon"></i>
                    </div>
                    <g:if test="${!isIcsrViewTracking}">
                        <span class="showEmailConfigurationLoading"><asset:image src="select2-spinner.gif" /></span>
                    </g:if>
                    <span class="showEmailConfiguration" style="display: none; cursor: pointer;" data-toggle="modal"
                          data-target="#emailConfiguration"><asset:image
                            src="icons/email.png" title="${message(code: 'default.button.addEmailConfiguration.label')}"/></span>
                </div>

                <div id="attachmentCheckboxes">
                    <div class="row">
                        <div class="col-xs-12">
                            <label>PVR Generated Report</label><br>
                            <g:each in="${com.rxlogix.enums.ReportFormatEnum.getEmailShareOptions(forClass ?: null)}">
                                <div class="checkbox checkbox-primary checkbox-inline">
                                    <g:checkBox id="${it.key}" class="emailOption" name="attachmentFormats" value="${it}"/>
                                    <label for="${it.key}">
                                        ${message(code: it.i18nKey)}
                                    </label>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-xs-12">
                        <label>PVP Sections</label>
                        <input id="pvpSectionsSelect" name="pvpSections" multiple/>
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <g:checkBox id="mergeSections" class="mergeSections" name="mergeSections"/>
                            <label for="mergeSections">
                                Merge Sections
                            </label>
                        </div>
                    </div>
                </div>
                 <div class="row">
                    <div class="col-xs-12">
                        <label>PVP Full Documents</label>
                        <input id="pvpFullDocumentSelect" name="pvpFullDocuments" multiple/>
                    </div>
                </div>
                <div id="formatError" hidden="hidden">
                    <div class="row">
                        <div class="col-xs-12" style="color: #ff0000">
                            <g:message code="select.at.least.one.attachment.format" />!
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <g:actionSubmit id="sendEmailbtn" class="btn btn-primary" action="emailPvp" value="${message(code: 'default.button.send.label')}"/>
                <button type="button" id="cancelBtn" class="btn pv-btn-grey" data-dismiss="modal">
                    <g:message code="default.button.cancel.label" />
                </button>
            </div>
        </div>
    </div>
</div>
<div id="emailConfigContainer">
    <g:render template="/configuration/includes/emailConfiguration" />
</div>