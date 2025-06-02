$(function () {
    $(".workflowState").each(function () {
        $(this).select2({
            placeholder: $.i18n._('Workflow States'),
            allowClear: true,
        });
    });

    $(document).on('click', "#rcaMandatory", function (e) {
        var editableByWidth = $($(document).find(".editableBy")[0]).parent().find(".select2-selection__rendered").width();
        $(document).find(".editableBy").each(function () {
            $(this).parent().find(".select2-container").addClass("form-control");
            $(this).parent().find(".select2-search__field").css("width", editableByWidth);
        });

        var workflowStateWidth = $($(document).find(".workflowState")[0]).parent().find(".select2-selection__rendered").width();
        $(document).find(".workflowState").each(function () {
            $(this).parent().find(".select2-container").addClass("form-control");
            $(this).parent().find(".select2-search__field").css("width", workflowStateWidth);
        });
    });

    $(document).on("change", ".mandatoryWorkflow, .editableWorkflow", function () {
        var field = $(this).closest('tr').data('field');
        var ownerApp = $(this).closest('tr').data('owner-app');
        var url = $(this).hasClass('mandatoryWorkflow') ? addWorkflowStateUrl + "?addMandatory=true" : addWorkflowStateUrl + "?addEditable=true";
        var successMessage = $(this).hasClass('mandatoryWorkflow') ? $.i18n._('success.rcaMandatory.workflow') : $.i18n._('success.rcaEditable.workflow');
        var errorMessage = $(this).hasClass('mandatoryWorkflow') ? $.i18n._('error.rcaMandatory.workflow') : $.i18n._('error.rcaEditable.workflow');
        $.ajax({
            url: url,
            type: 'POST',
            data: {
                field: field,
                ownerApp: ownerApp,
                workflowStateIds: JSON.stringify($(this).val())
            },
            dataType: 'html'
        })
            .done(function (data) {
                var successContent = successMessage + $.i18n._('app.rcaMandatory.' + ownerApp) + " " + $.i18n._('app.rcaMandatory.' + field);
                $('.successContent').html(successContent);
                $('.alert-danger').hide();
                $('.alert-success').show();
            })
            .fail(function (e) {
                var errorContent = errorMessage + $.i18n._('app.rcaMandatory.' + ownerApp) + " " + $.i18n._('app.rcaMandatory.' + field);
                $('.errorContent').html(errorContent);
                $('.alert-success').hide();
                $('.alert-danger').show();
            });
    });

    $('.editableBy').on('change', function () {
        var field = $(this).closest('tr').data('field');
        var ownerApp = $(this).closest('tr').data('owner-app');
        $.ajax({
            url: addEditableByUrl,
            type: 'POST',
            data: {
                field: field,
                ownerApp: ownerApp,
                editableBy: JSON.stringify($(this).val())
            },
            dataType: 'html'
        })
            .done(function (data) {
                $('.successContent').html($.i18n._('success.rcaEditableBy.user') + $.i18n._('app.rcaMandatory.' + ownerApp) + " " + $.i18n._('app.rcaMandatory.' + field));
                $('.alert-danger').hide();
                $('.alert-success').show();
            })
            .fail(function (e) {
                $('.errorContent').html($.i18n._('error.rcaEditableBy.user') + $.i18n._('app.rcaMandatory.' + ownerApp) + " " + $.i18n._('app.rcaMandatory.' + field));
                $('.alert-success').hide();
                $('.alert-danger').show();
            });
    });

    $(document).on("click", ".errorClose, .successClose", function () {
        var alertPanel = $(this).closest('.alert');
        alertPanel.hide();
    });

});