<div class="modal fade" id="sharedWithModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><g:message code="share.with.users" /></h4>
            </div>

            <div class="modal-body">
                <g:renderClosableInlineAlert id="share-with-required-alert" type="danger" message="${g.message(code: 'app.error.fill.all.required')}" />
                <div class="row">
                    <div class="col-sm-6">
                        <label style="padding-left: 5px;"><g:message code="user.group.label"/>:</label>
                        <div id="sharedWithGroupList" style="padding-left: 5px;"></div>
                    </div>
                    <div class="col-sm-6">
                        <label style="padding-left: 5px;"><g:message code="user.label"/>:</label>
                        <div id="sharedWithUserList" style="padding-left: 5px;"></div>
                    </div>
                </div>
                <div class="col-md-12">
                    <script>
                        sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
                        sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
                        $(function () {
                            bindShareWith($('.sharedWithControl'), sharedWithListUrl, sharedWithValuesUrl, "100%", true, $("#sharedWithModal"))
                        });
                    </script>
                    <label><g:message code="shared.with"/></label>
                    <select class="sharedWithControl form-control" id="sharedWith" name="sharedWith" value=""></select>
                </div>
            </div>
            <div class="modal-footer">
                <g:actionSubmit class="btn btn-primary" action="share" value="${message(code: 'default.button.add.label')}" />
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal">
                    <g:message code="default.button.cancel.label" />
                </button>
            </div>
        </div>
    </div>
</div>