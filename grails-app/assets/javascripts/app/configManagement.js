$(function () {

    $("#exportTechConfig").on('click', function () {
        $('#exportStatusLoading').removeClass('hidden');

        $.ajax({
            url: "/reports/configManagement/generateConfigurationFile",
            dataType: 'json'
        })
            .done(function (result) {
                if (result.status) {
                    $('#exportStatusLoading').addClass('hidden');
                    $('#downloadExportFile').removeClass('hidden');
                    $('#downloadExportFile').attr('href', 'downloadFile?isExport=true&filepath=' + result.data["file"]);
                    successNotification($.i18n._('app.notification.config.generation.success'));
                } else {
                    $('#exportStatusLoading').addClass('hidden');
                    errorNotification($.i18n._('app.notification.config.generation.fail'));
                }
            })
            .fail(function (err) {
                $('#exportStatusLoading').addClass('hidden');
                errorNotification($.i18n._('app.notification.config.generation.fail'));
            })
    });


    $("#compareConfigurations").on('click', function () {
        $('#compareStatusLoading').removeClass('hidden');


        var form = $('#compareConfigForm')[0];
        var data = new FormData()
        data.append("configFileFirst", $("#config_first_file")[0].files[0]);
        data.append("configFileSecond", $("#config_second_file")[0].files[0]);
        $.ajax({
            url: "/reports/configManagement/compareConfigurations",
            data: data,
            type: "POST",
            contentType: false,
            processData: false,
            dataType: 'json'
        })
            .done(function (result) {
                $('#compareStatusLoading').addClass('hidden');
                if (result.status) {
                    $('#downloadDiffFile').removeClass('hidden');
                    $('#downloadDiffFile').attr('href', 'downloadFile?filepath=' + result.data["file"]);
                    successNotification($.i18n._('app.notification.config.difference.file.success'));
                } else {
                    errorNotification($.i18n._('app.notification.config.difference.file.fail'));
                }
                $("#config_first_file").val(null);
                $("#config_second_file").val(null);
                $("#config1_file_name").val('');
                $("#config2_file_name").val('');
            })
            .fail(function (err) {
                $("#config_first_file").val(null);
                $("#config_second_file").val(null);
                $("#config1_file_name").val('');
                $("#config2_file_name").val('');
                $('#compareStatusLoading').addClass('hidden');
                errorNotification($.i18n._('app.notification.config.difference.file.fail'));
            })
    });


    $("#ImportConfiguration").on('click', function () {
        $('#importStatusLoading').removeClass('hidden');

        var form = $('#importConfigForm')[0];
        var data = new FormData()
        if ($("#busConfig").is(":checked")) {
            data.append("configType", $("#busConfig").val());
        } else {
            data.append("configType", $("#techConfig").val());
        }
        data.append("appName", $("#appName").val());
        data.append("configFile", $("#config_file_input")[0].files[0]);
        $.ajax({
            url: "/reports/configManagement/importDataFromFile",
            data: data,
            type: "POST",
            contentType: false,
            processData: false,
            dataType: 'json'
        })
            .done(function (result) {
                $('#importStatusLoading').addClass('hidden');
                if (result.status) {
                    successNotification($.i18n._('app.notification.config.import.success'));
                } else {
                    errorNotification($.i18n._('app.notification.config.import.fail'));
                }
                $("#config_file_input").val(null);
                $("#config_file_name").val('');
            })
            .fail(function (err) {
                $('#importStatusLoading').addClass('hidden');
                $("#config_file_input").val(null);
                $("#config_file_name").val('');
                errorNotification($.i18n._('app.notification.config.import.fail'));
            })
    });


    $("#refreshTechnicalConf").on('click', function () {
        $(this).addClass('glyphicon-refresh-animate');
        $('#refreshTechnicalConf').off('click');
        $('#refreshTechnicalConf').css("cursor", "not-allowed");
        $.ajax({
            url: "/signal/configManagement/refreshTechConfig",
            dataType: 'json'
        })
            .done(function (result) {
                $('#refreshTechnicalConf').removeClass('glyphicon-refresh-animate');
                if (result.status) {
                    $.Notification.notify('success', 'top right', "Success", "Technical Configuration refresh triggered.", {autoHideDelay: 10000});
                    $('#lastRefreshTime').val(result.data);

                } else {
                    $.Notification.notify('warning', 'top right', "Warning", "Unable to fetch status. Data refresh maybe taking more time than expected. Please check again later.", {autoHideDelay: 2000});
                }
            })
            .fail(function (err) {
                $.Notification.notify('error', 'top right', "Error", "An Error occurred while refreshing technical configurations.", {autoHideDelay: 10000});
                $('#refreshTechnicalConf').removeClass('glyphicon-refresh-animate');
            })
    });
});