$(function () {
      $('[name=file]').on('change', function (evt, numFiles, label) {
        $("#file_name").val($.map($('[name=file]')[0].files, function (val) {
            return val.name;
        }).join(";"));
    });

     $(document).on('click', '.table-remove', function () {
         var tr = $(this).parents('tr');
         if (tr.hasClass('import-rca-data-row')){
            var table = tr.closest('table');
            tr.detach();
            disableActionButtonsIfTableIsEmpty(table);
         } else {
             tr.detach();
             updateButtonStatus();
         }
     });

    function disableActionButtonsIfTableIsEmpty(table) {
        if (table.find('tr.import-rca-data-row').length === 0) {
            $('.validateButton').attr('disabled', true);
            $('.applyButton').attr('disabled', true);
        }
    }

    function updateButtonStatus() {
        var tableRowCount = $('table tbody tr').length;
        $('.validateButton').prop('disabled', tableRowCount === 1);
        $('.applyButton').prop('disabled', tableRowCount === 1);
    }

    $(document).on('click', '.previewButton', function () {
        setTimeout(function () {
            $("button").attr("disabled", true);
        }, 50);
    });

    $(document).on('click', '.validateButton', function () {
        setTimeout(function () {
            $("button").attr("disabled", true);
        }, 50);
    });
    $(document).on('click', '.applyButton', function () {
        $("#submitInput").val(true);
        setTimeout(function () {
            $("button").attr("disabled", true);
        }, 50);
    });
    if ($(".rowErrorMessage").length == 0) {
        $("button").attr("disabled", false);
    }
});
