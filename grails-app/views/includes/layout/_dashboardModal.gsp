<div class="modal fade" id="dashboardModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby=""  aria-hidden="true">
<%@ page contentType="text/html;charset=UTF-8" %>
<g:javascript>
    var editDashboardUrl= "${createLink(controller: 'dashboardDictionary', action: 'editModal')}";
    var updateDashboardUrl= "${createLink(controller: 'dashboardDictionary', action: 'updateModal')}";
</g:javascript>
<asset:javascript src="app/dashboardModal.js"/>
<div class="modal-dialog modal-sm">
<rx:container title="${message(code: "app.label.edit.dashboard")}">

    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="alert alert-danger alert-dismissible dictionaryErrorDiv" role="alert" hidden="hidden">
        <button type="button" class="close" id="dictionaryErrorDiv">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <div id="errormessage"></div>
    </div>

    <div class="container-fluid">
        <g:render template="/includes/layout/form"/>

        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <button type="button" class="btn btn-primary" id="submitButton">${message(code: 'default.button.update.label')}</button>
                    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"
                            id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                </div>
            </div>
        </div>
    </div>
</rx:container>
</div>
</div>