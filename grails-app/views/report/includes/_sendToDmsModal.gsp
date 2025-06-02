<%@ page import="com.rxlogix.enums.ReportFormatEnum" %>
<div class="modal fade" id="sendToDmsModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.dms.upload.label" /></h4>
            </div>
            <div class="modal-body">

                <g:each var="reportFormatEnum" in="${ReportFormatEnum.emailShareOptions}" status="i" style="margin-top: 10px;" >
                    <div class="radio radio-inline">
                        <input type="radio" id="format${reportFormatEnum}" ${i==0? "checked":""} name="dmsConfiguration.format" value="${reportFormatEnum}"/>
                        <label for="format${reportFormatEnum}">
                            ${message(code: reportFormatEnum.i18nKey)}
                        </label>
                    </div>
                </g:each>
                <span class="glyphicon glyphicon-edit showDmsConfigurationDlg" style="cursor: pointer;font-size: 25px;margin-left: 190px;"></span>
                <div id="formatError" hidden="hidden">
                    <div class="row">
                        <div class="col-xs-12" style="color: #ff0000">
                            <g:message code="select.at.least.one.attachment.format" />!
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <g:actionSubmit class="btn btn-primary" action="sendToDms" value="${message(code: 'default.button.send.label')}" />
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal">
                    <g:message code="default.button.cancel.label" />
                </button>
            </div>
        </div>
    </div>
</div>
<div id="dmsConfigContainer">
</div>