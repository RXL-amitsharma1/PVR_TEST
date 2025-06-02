<div class="modal scrollable-content-modal fade" id="createFromTemplateModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby=""  aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <rx:container title="${message(code: "app.menu.createFromTemplate")}" closeButton="true">
                <div class="pv-caselist">
                    <table id="rxTableTemplateConfiguration" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                        <thead>
                        <tr>
                            <th class="reportNameColumn" width="500"><g:message code="app.label.reportName"/></th>
                            <th class="reportDescriptionColumn"><g:message code="app.label.description"/></th>
                            <th width="150"><g:message code="app.label.dateCreated" /></th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </rx:container>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->