          <%@ page import="com.rxlogix.enums.ReasonOfDelayAppEnum;" %>
           <g:if test="${type == ReasonOfDelayAppEnum.PVQ}">
           <div class="pv-caselist basicDataTable">
           </g:if>
           <g:else>
            <div class="pv-caselist">
            </g:else>
            <table id="capa-list" class="table table-striped pv-list-table dataTable no-footer" width="100%" style="overflow: visible! important">
                <thead>
                <tr>
                    <th>
                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="selectAll" checked="false"/>
                            <label for="selectAll"></label>
                        </div>
                    </th>
                    <th><g:message code="quality.capa.capaNumber.label"/></th>
                    <th><g:message code="quality.capa.issueType.label"/></th>
                    <th><g:message code="quality.capa.category.label"/></th>
                    <th><g:message code="quality.capa.approvedBy.label"/></th>
                    <th><g:message code="quality.capa.initiator.label"/></th>
                    <th><g:message code="quality.capa.teamLead.label"/></th>
                    <th><g:message code="app.label.description"/></th>
                    <th><g:message code="quality.capa.rootCause.label"/></th>
                    <th><g:message code="quality.capa.verificationResults.label"/></th>
                    <th><g:message code="quality.capa.comments.label"/></th>
                    <th><g:message code="quality.attachment.label"/></th>
                    <th><g:message code="app.label.dateCreated"/></th>
                    <th><g:message code="app.label.dateModified"/></th>
                    <th><g:message code="quality.capa.owner.label"/></th>
                    <th><g:message code="app.label.action"/></th>
                </tr>
                </thead>
            </table>
</div>
<div class="modal fade" id="warningModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="warningModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel">${title ?: 'Warning'}</h4>
            </div>

            <div class="modal-body">
                <div class="messageBody"></div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<script type="text/javascript">
    $(function () {
        $('#warningModal').on('show.bs.modal', function (event) {
            //Make sure cancel and continue buttons are enabled
            $(".btn").removeAttr("disabled", "disabled");
        });
    });
</script>