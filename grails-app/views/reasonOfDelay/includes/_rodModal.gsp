<div class="modal fade" id="rodModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby=""  aria-hidden="true">
    <%@ page contentType="text/html;charset=UTF-8" %>
    <g:javascript>
    </g:javascript>
    <div class="modal-dialog ">
        <rx:container title="${message(code: "app.label.edit.dashboard")}">

            <div class="alert alert-danger alert-dismissible modalError closeError forceLineWrap" role="alert" style="display:none">
                <button type="button" class="close">
                    <span aria-hidden="true" data-evt-clk='{"method" : "addClassHide", "params" : [".alert-danger"]}'>&times;</span>
                    <span class="sr-only"><g:message code="default.button.close.label"/></span>
                </button>
                <span ><g:message code="app.rod.label.error"/></span>
            </div>

            <div class="container-fluid">
                <g:render template="/reasonOfDelay/includes/form"/>

                <div class="row">
                    <div class="col-xs-12">
                        <div class="pull-right">
                            <button type="button" class="btn btn-primary" id="submitButton">${message(code: 'default.button.update.label')}</button>
                            <button type="button" class="btn pv-btn-grey" data-dismiss="modal"
                                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                            <button type="button" class="btn btn-primary" data-dismiss="modal"
                                    id="cancelButtonForView">${message(code: "default.button.close.label")}</button>
                        </div>
                    </div>
                </div>
            </div>
        </rx:container>
    </div>
</div>