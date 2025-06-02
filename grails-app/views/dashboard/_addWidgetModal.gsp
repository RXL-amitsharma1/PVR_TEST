<div class="modal fade" id="addWidgetModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="addWidgetModalLabel"
         aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content pv-caselist">
            <div class="modal-header"><button type="button" class="close" data-dismiss="modal">&times;</button>
                ${message(code: "app.widget.reportLibrary.title.label")}</div>
            <div class="modal-body">
                <table id="rxTableConfiguration" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                    <thead class="filter-head">
                    <tr>
                        <th><g:message code="app.label.reportType"/></th>
                        <th class="reportNameColumn"><g:message code="app.label.reportName"/></th>
                        <th class="reportNameColumn"><g:message code="app.label.sectionTitle"/></th>
                        <th class="reportDescriptionColumn"><g:message code="app.label.description"/></th>
                        <th><g:message code="app.label.runTimes"/></th>
                        <th><g:message code="app.label.owner"/></th>
                        <th><g:message code="app.label.dateCreated"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->