<%@ page import="com.rxlogix.config.PvrTags" %>
<div class="modal fade" id="caseListTagModal" data-backdrop="static" tabindex="-1" role="dialog" aria-hidden="true">
   <div class="vertical-alignment-helper">
        <!-- Modal Dialog starts -->
        <div class="modal-dialog vertical-align-center">

        <div class="modal-content">
            <div class="modal-header dropdown">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.label.tags" /></h4>
            </div>

            <div class="modal-body">

                <div class="row">
                    <div class="col-md-12">
                        <span><b><g:message code="app.label.case.level.tags"/></b></span>
                        <select  id="caseLevelTags" multiple="true" class="form-control"></select>
                    </div>
                </div>
                <br/>
                <div class="row">
                    <div class="col-md-12">
                        <span><b><g:message code="app.label.global.level.tags"/></b></span>
                        <select  id="globalTags" class="form-control"  multiple="true"></select>
                    </div>
                </div>
                <br/>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary addTags"><g:message
                        code="default.button.update.label"/></button>
                <button type="button" class="btn pv-btn-grey closeTagModal">
                    <g:message code="default.button.cancel.label" />
                </button>

            </div>
        </div>
    </div>
</div>
</div>