<asset:javascript src="/app/emailModal.js"/>
<div class="modal fade" data-keyboard="false" data-backdrop="static" id="emailToModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.emailTo" /></h4>
            </div>
            <div class="modal-body">

                <g:renderClosableInlineAlert id="email-to-required-alert" type="danger" message="${g.message(code: 'app.error.choose.email.required')}" />

                <div class="row m-b-10">
                    <div class="col-xs-10" style="padding-right: 27px;">
                        <g:renderClosableInlineAlert id="email-invalid-alert" type="danger" />
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
                          data-target="#emailConfiguration"><asset:image src="icons/email.png" title="${message(code: 'default.button.addEmailConfiguration.label')}"/></span>
                </div>

                <div id="attachmentCheckboxes">
                    <div class="row">
                        <div class="col-xs-12">
                            <g:each in="${com.rxlogix.enums.ReportFormatEnum.capa8dShareOptions}">
                                <div class="checkbox checkbox-primary checkbox-inline">
                                    <g:checkBox id="${it.key}" class="emailOption" name="attachmentFormats" value="${it}"/>
                                    <g:if test="${it.key != 'r3xml'}">
                                        <label for="${it.key}">
                                            ${message(code: it.i18nKey)}
                                        </label>
                                    </g:if>
                                </div>
                            </g:each>
                        </div>
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
                <g:actionSubmit id="sendEmailbtn" class="btn btn-primary" action="email" value="${message(code: 'default.button.send.label')}"/>
                <button type="button" id="cancelBtn" class="btn btn-default" data-dismiss="modal">
                    <g:message code="default.button.cancel.label" />
                </button>
            </div>
        </div>
    </div>
</div>
<div id="emailConfigContainer">
    <g:render template="/configuration/includes/emailConfiguration" />
</div>