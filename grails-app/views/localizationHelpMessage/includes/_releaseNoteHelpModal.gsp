<div class="modal fade" id="releaseNoteHelpModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg " role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close dismissNoteHelp" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.label.localizationHelp.help"/></h4>
            </div>

            <div class="modal-body releaseNoteHelpModalContent">

            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey dismissNoteHelp" data-dismiss="modal">
                    <g:message code="default.button.ok.label"/>
                </button>
            </div>
        </div>
    </div>
</div>
<script>
    $(document).on("click", ".dismissNoteHelp", function () {
        $(".releaseNoteHelpModalContent").empty();
    });
    $(document).on("click", ".showWhatsNewDescription", function () {
        var id = $(this).attr("data-id");
        openReleaseNoteHelpModal(id);
    });

    function openReleaseNoteHelpModal(id) {
        showLoader();
        $.ajax({
            url: "${createLink(controller: 'localizationHelpMessage', action: 'showReleaseItemHelp')}?id=" + id,
            type: 'get',
            dataType: 'html'
        })
            .done(function (response) {
                $(".releaseNoteHelpModalContent").html(response);
                hideLoader();
                $("#releaseNoteHelpModal").modal("show");
            })
            .fail(function (XMLHttpRequest, textStatus, errorThrown) {
                $("#releaseNoteHelpModal").html("Unexpected Error");
                hideLoader();
                $("#releaseNoteHelpModal").modal("show");
            });

    }
</script>