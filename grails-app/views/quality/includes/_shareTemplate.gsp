<%@ page import="com.rxlogix.config.EmailConfiguration" %>
<div class="pull-right" style="cursor: pointer; position: relative">
    <i class="glyphicon glyphicon-copy pull-right" style="margin-right: 5px; color: #353d43;" id="caseNumCopyBt" data-toggle="modal" data-target="#copyCaseNumModal" title="<g:message code="qualityModule.copyCaseNumber.label"/>" name="Copy"></i>
    <i class="fa fa-envelope pull-right" style="margin-right:10px; margin-top: 2px; color: #353d43;" data-toggle="modal" name="Email" data-target="#emailToModal" title="<g:message code="quality.shareDropDown.email.menu"/>" name="Email"></i>
    <i class="glyphicon glyphicon-download-alt  excelExport pull-right" style="margin-right:10px; color: #353d43;" title="<g:message code="quality.shareDropDown.excexcel.menu"/>" name="Excel"></i>
    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <i class="glyphicon glyphicon-trash pull-right deleteErrors" style="margin-right:10px; color: #353d43;" title="<g:message code="default.button.delete.label"/>"></i>
    </sec:ifAnyGranted>
      <div id="assignedToFilterDiv" class="d-inline"  style="margin-right: 10px;">
          <label><g:message code="app.pvc.assignedTo.filter.label"/></label>
          <select class="sharedWithControl form-control" id="assignedToFilter" name="assignedToFilter" value=""></select>
      </div>

</div>
%{--<div style="bottom: 44px; cursor: pointer; text-align: right; position: relative">--}%
%{--    <i class="glyphicon glyphicon-copy pull-right" style="margin-right: 45px; color: #353d43;" id="caseNumCopyBt" data-toggle="modal" data-target="#copyCaseNumModal" title="<g:message code="qualityModule.copyCaseNumber.label"/>" name="Copy"></i>--}%
%{--    <i class="fa fa-envelope pull-right" style="margin-right:10px; margin-top: 2px; color: #353d43;" data-toggle="modal" name="Email" data-target="#emailToModal" title="<g:message code="quality.shareDropDown.email.menu"/>" name="Email"></i>--}%
%{--    <i class="glyphicon glyphicon-download-alt  excelExport pull-right" style="margin-right:10px; color: #353d43;" title="<g:message code="quality.shareDropDown.excexcel.menu"/>" name="Excel"></i>--}%

%{--</div>--}%

<div class="modal fade" id="emailToModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <g:form controller="quality" action="sendEmail" name="emailToModalModal" data-evt-sbt='{"method": "createCaseData", "params": []}'>
            <g:hiddenField name="executedConfigId"/>
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title"><g:message code="app.label.emailTo"/></h4>
                </div>
                <div class="modal-body">
                    <div class="alert alert-danger hide">
                        <a href="#" class="close" aria-label="close">&times;</a>
                        <strong><g:message code="app.add.case.error.label" /> !</strong> <span class="errorMessageSpan"></span>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <g:renderClosableInlineAlert id="email-required-alert" type="danger" message="${g.message(code: 'app.error.choose.email.required')}" />
                            <g:select placeholder="${g.message(code: 'app.label.emailTo')}*"
                                      id="emailUsers"
                                      name="emailToUsers"
                                      from="${[]}"
                                      data-value=""
                                      class="form-control emailUsers" multiple="true" data-options-url="${createLink(controller: 'email', action: 'allEmails')}" data-width="100%"/>
                            <g:textField name="subject" maxlength="${EmailConfiguration.constrainedProperties.subject.maxSize}" class="form-control add-margin-bottom"
                                         value="${emailConfiguration?.subject}" style="margin-top: 5px;"
                                         placeholder="${g.message(code: 'app.label.emailConfiguration.subject', default: 'Subject')}*"/>
                            <g:textArea name="body" class="form-control add-margin-bottom" value=""
                                        placeholder="${g.message(code: 'app.label.emailConfiguration.message', default: 'Message')}*"/>
                            <input type="hidden" name="data" id="casesData">
                            <input type="hidden" name="dataType" value="${dataType}">
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <g:submitButton name="Submit" class="btn primaryButton btn-primary"
                                    value="${message(code: 'default.button.send.label')}"/>
                    <button type="button" class="btn pv-btn-grey" data-dismiss="modal">
                        <g:message code="default.button.cancel.label"/>
                    </button>
                </div>
            </div>
        </g:form>
    </div>
</div>

<div id="copyCaseNumModal" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><g:message code="app.caseList.caseNumber"/></h4>
            </div>

            <div class="modal-body">
                <div class="text-wrapper">
                    <textarea id="caseNumberContainer" style="width: 100%; height: 150px"></textarea>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
            </div>
        </div>

    </div>
</div>
<g:render template="/includes/widgets/deleteRecord"/>
<div id="tableWrap" style="display: none">
    <table id="export">
        <tr><td></td><td></td></tr>
    </table>
</div>