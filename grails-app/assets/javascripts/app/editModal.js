function loadEditConfig() {
    showLoader();
    $.ajax({
        url: editConfigUrl,
        beforeSend: function () {
            $(".editConfigLink").off("click");
        }
    })
        .done(function (result) {
            var element = $(".editConfigLink");
            $(result).insertAfter(element);
        })
        .fail(function (jqXHR) {
            console.log("Failed with Status Code:", jqXHR.status);
            if (jqXHR.status === 500) {
                alert('Internal error');
            } else {
                alert('Unexpected Error');
            }
        })
        .always(function () {
            var modal = $("#editConfigModal");
            hideLoader();
            modal.modal('show');
        });
}

$(function () {
    $(".editConfigLink").on("click", function (e) {
        e.stopPropagation();
        loadEditConfig();
    });
});