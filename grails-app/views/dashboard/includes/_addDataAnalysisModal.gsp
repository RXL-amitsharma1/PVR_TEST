<div class="modal fade" id="addDataAnalysisModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="addDataAnalysisLabel"
         aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content pv-caselist">
            <rx:container title="${message(code: "app.label.dataAnalysis")}" closeButton="true">
                <table id="dataAnalysisTable" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                    <thead class="filter-head">
                    <tr>
                        <th><g:message code="app.file.name"/></th>
                        <th><g:message code="app.label.dateCreated"/></th>
                    </tr>
                    </thead>
                </table>
            </rx:container>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->