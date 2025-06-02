<div id="saveCaseSeries" class="modal fade" role="dialog" aria-hidden="true" style="display: none;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">×</button>
                <h5 class="modal-title"><g:message code="app.label.save.caseSeries"/></h5>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger alert-dismissible forceLineWrap" id="errorDiv" role="alert"
                     hidden="hidden">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>

                    <p></p>
                </div>

                <div class="row" name="saveCaseSeriesContainer">
                    <label for="seriesName"><g:message code="app.label.case.series.name"/></label>
                    <g:textArea name="seriesName" class="form-control" row="2"/>
                </div>
            </div>

            <div>
                <div class="modal-footer">
                    <div class="add-comment-component">
                        <button type="button" class="btn btn-primary" id="saveCaseSeriesButton"
                                data-evt-clk='{"method": "saveCaseSeries", "params": []}'><g:message
                                code="default.button.save.label"/></button>
                        <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
                                code="default.button.cancel.label"/></button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="application/javascript">
    var saveCaseSeriesModal = $('#saveCaseSeries');
    var executedTemplateQueryId;
    var cid;
    saveCaseSeriesModal.on('show.bs.modal', function (e) {
        var csButton = e.relatedTarget;
        executedTemplateQueryId = $(csButton).attr('data-id');
        cid = $(csButton).attr('data-cid');
        saveCaseSeriesModal.find('#seriesName').val($(csButton).attr('data-title'));
    }).on('hide.bs.modal', function (e) {
        saveCaseSeriesModal.find('#seriesName').val('');
        saveCaseSeriesModal.find('#seriesName').parent().removeClass('has-error');
        $('#errorDiv').attr('hidden', 'hidden');
    });

    function saveCaseSeries() {
        if (saveCaseSeriesModal.find('#seriesName').val().trim() == '') {
            saveCaseSeriesModal.find('#seriesName').parent().addClass('has-error');
            return
        }
        saveCaseSeriesModal.find('#saveCaseSeriesButton').attr('disabled', 'disabled');
        showModalLoader(saveCaseSeriesModal);
        $.ajax({
            url: "${createLink(controller: 'executedCaseSeries', action: 'saveExecutedCaseSeries')}",
            method: 'POST',
            dataType: "json",
            data: {
                cid: cid,
                seriesName: saveCaseSeriesModal.find('#seriesName').val().trim(),
                executedTemplateQueryId: executedTemplateQueryId
            }
        })
            .done(function (result) {
                if (result.success == false) {
                    $('#errorDiv p').text(result.message);
                    $('#errorDiv').removeAttr('hidden');
                    saveCaseSeriesModal.find('#seriesName').parent().addClass('has-error');
                    saveCaseSeriesModal.find('#saveCaseSeriesButton').removeAttr('disabled');
                } else {
                    saveCaseSeriesModal.modal('hide').data('bs.modal', null);
                    showResponseAlerts(result);
                    saveCaseSeriesModal.find('#saveCaseSeriesButton').removeAttr('disabled');
                }
            })
            .fail(function () {
                saveCaseSeriesModal.find('#saveCaseSeriesButton').removeAttr('disabled');
                alert("Sorry! System level error");
            })
            .always(function () {
                hideModalLoader(saveCaseSeriesModal);
            });
    }

    $(function () {
        $("[data-evt-clk]").on('click', function() {
            const eventData = JSON.parse($(this).attr("data-evt-clk"));
            const methodName = eventData.method;
            const params = eventData.params;
            if (methodName == "saveCaseSeries") {
                saveCaseSeries();
            }
        });
    })
</script>