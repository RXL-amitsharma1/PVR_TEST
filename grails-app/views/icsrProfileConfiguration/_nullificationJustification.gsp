<%@ page import="grails.util.Holders" %>
<div class="modal fade nullificationModal" data-backdrop="static" style="margin-left: 5px" id="nullificationModal"
     tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.label.icsr.view.case.nullification"/></h4>
            </div>

            <div class="modal-body">
                <div>
                    <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="nullificationErrorDiv" style="display: none">
                        %{--<button type="button" class="close" data-dismiss="alert">--}%
                        <button type="button" class="close nullificationErrorDivclose">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <i class="fa fa-check"></i> <g:message code="app.label.justification.cannotbeblank"/>
                    </div>
                    <g:form autocomplete="off" name="nullificationJustificationForm" id="nullificationJustificationForm">
                        %{-- Putting approval section as comment because currently not required. --}%
                        %{--<div id="needApprovalDiv"
                             class="${Holders.config.getProperty('icsr.case.submission.user.revalidate', Boolean) ? '' : 'hidden'}">

                            <label>
                                <g:message code="app.label.workflow.rule.needApproval"/>
                            </label>

                            <div>
                                <label>
                                    <g:message code="user.username.label"/>
                                    <span class="required-indicator">*</span>
                                </label>
                            </div>

                            <div>
                                <g:set var="userService" bean="userService"/>
                                <input autocomplete="autocomplete_off_xfr4!k1" name="${new Date().getTime()}"
                                       disabled="true" id="login-input" value="${userService.currentUser.fullName}"
                                       class="form-control login-input">
                            </div>

                            <div>
                                <label>
                                    <g:message code="user.password.label"/>
                                    <span class="required-indicator">*</span>
                                </label>
                            </div>

                            <div id="password-input-div">
                                <input autocomplete="autocomplete_off_xfr4!k" name="${new Date().getTime()}"
                                       id="password-input" class="form-control password-input" type="text">
                            </div>
                        </div>--}%

                        <div class="description-wrapper">
                            <label><g:message code="report.submission.comment"/>*
                            </label>
                            <g:textArea rows="5" cols="3" name="notificationComments" id="notificationComments" maxlength="2000"
                                        style="height: 110px;" value="" class="form-control "/>
                            <g:message code="app.label.max.characters" args="${['2000']}"/>
                        </div>
                    </g:form>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="nullifyButton" class="btn btn-success confirm-paste">
                    <g:message code="default.button.confirm.label"/>
                </button>
                <button type="button" class="btn btn-default cancel" data-dismiss="modal">
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div>
        </div>
    </div>
</div>