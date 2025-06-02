<div class="modal fade customExpressionModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="customExpressionModalLabel"
     aria-hidden="true" style="z-index: 9999;">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title"><g:message code="app.custom.expression.sql.modal.title"/></h4>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger alert-dismissible forceLineWrap customExpressionErrorDiv" role="alert" style="display: none">
                    <button type="button" class="close closeError" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <i class="fa fa-check"></i> <g:message code="com.rxlogix.config.query.customSQLQuery.invalid"/>
                </div>
                <div>
                    <p>Note: </p>
                    <span><g:message code="app.custom.expression.first.note.statement"/></span></br>
                    <span style="background-color: #f4f8fb">  [select <i>value</i> from <i>tablename</i> where case_id=&lt;CASE_ID&gt;]  </span></br></br>
                    <span><g:message code="app.custom.expression.second.note.statement"/></span></br>
                    <span style="background-color: #f4f8fb">  [select <i>value</i> from <i>tablename</i> where case_id=&lt;CASE_ID&gt; and processed_report_id=&lt;PROCESSED_REPORT_ID&gt;]  </span>
                </div>
                <label style="margin-top: 25px"><g:message code="app.label.sql.statement"/>:</label>
                <input type="hidden" class="containerName"/>
                <input type="hidden" class="fieldName"/>
                <textarea placeholder="<g:message code="app.placeholder.sql.statement"/>" class="form-control customExpressionQuery" rows="6"></textarea>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary saveModal">
                    ${message(code: 'default.button.save.label')}
                </button>
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->