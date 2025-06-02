var spotfire = {
    fromDate: moment("1900-01-01 00:00:01"),
    endDate: new Date(),
    asOfDate: new Date(),
    productFamilyNames: null,
    fileName: null,
    serverUrl: null,
    type: 'drug',
    config: {
        server: null,
        path: null,
        domainName: null,
        version: null,
        templateFileName: null,
        user: null,
        libraryRoot: null,
        protocol: null,
        interval: null
    },
    file: null,
    init: function () {
        $.ajax({
            url: getDetailsUrl,
            async: false,
            dataType: 'json'
        })
            .done(function (data) {
                spotfire.config.server = data.server;
                spotfire.config.path = data.path;
                spotfire.config.port = data.port;
                spotfire.config.domainName = data.domainName;
                spotfire.config.version = data.version;
                spotfire.config.templateFileName = data.file_name;
                spotfire.config.user = data.user_name;
                spotfire.config.libraryRoot = data.libraryRoot;
                spotfire.config.protocol = data.protocol;
                spotfire.config.interval = data.interval;
                spotfire.config.token = data.auth_token;
                spotfire.config.callbackServer = data.callback_server;
            });
    },

    generateReport: function () {
        // This is not generate report, this is actually load spotfire report

        document.cookie = "auth_token=" + "a_fake_token";

        var url = spotfire.serverUrl;
        var iframe = document.createElement('iframe');
        var windowHeight = $(window).height() - 133;
        $(window).on('resize', function (evt) {
            var h = $(window).height();
            $("#spotfire-report-iframe").height(h - 133);
        });

        iframe.setAttribute('id', 'spotfire-report-iframe');
        iframe.setAttribute('width', '100%');
        iframe.setAttribute('height', windowHeight + 'px');

        $(iframe).css({
            border: '0',
        });

        iframe.src = url; //spotfire.serverUrl;
        document.getElementById('spotfirePanel').appendChild(iframe);

        // For PVR-17534, Inside Iframe activity cannot be tracked, hence applying these events to keep session alive, if user is active on screen.
        $('#spotfire-report-iframe').on('mouseenter, mouseleave, mouseover', function () {
            $('.rxmain-container-header').trigger('click');
        });
    },

    bindUiInputs: function () {
        var $useCaseSeries = $("#caseSeriesId");
        if ($useCaseSeries.length > 0)
            bindSelect2WithUrl($useCaseSeries, executedCaseSeriesListUrl, executedCaseSeriesItemUrl, true).on('change', function () {
                spotfire.disableEnableDateOptions($(this).val() != '')
            });
        $("#tag").on('input', function () {
            spotfire.setFullFileName()
        });
        spotfire.initDatepickers();
        $("#spotfireDrug").on('click', function () {
            spotfire.setFullFileName();
        });

        $("#spotfireVaccine").on('click', function () {
            spotfire.setFullFileName();
        });
        $("#spotfirePMPR").on('click', function () {
            spotfire.setFullFileName();
        });
    },

    keepLive: function () {
        window.setInterval(function () {
            $.ajax({
                url: 'keepAlive',
                dataType: 'json'
            })
                .done(function (data) {
                })
        }, spotfire.config.interval);
    },

    initDatepickers: function () {
        $("#spotfireFromDate").datepicker({
            allowPastDates: true,
            date: spotfire.fromDate,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
            spotfire.fromDate = date;
            spotfire.setFullFileName();
        });
        $("#spotfireEndDate").datepicker({
            allowPastDates: true,
            date: spotfire.endDate,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
            spotfire.endDate = date;
            spotfire.setFullFileName();
        });
        $("#spotfireAsOfDate").datepicker({
            allowPastDates: true,
            date: spotfire.asOfDate,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
            spotfire.asOfDate = date;
            spotfire.setFullFileName();
        });

        $("input[name='spotfireType']").on('change', function (evt) {
            spotfire.type = $("input[name='spotfireType']:checked").val();
        });
        $("#spotfireDrug").attr('checked', true);
    },

    composeParams: function () {
        return "&pc=" + spotfire.config.protocol + "&sr=" + spotfire.config.server + "&p=" + spotfire.config.path +
            "&v=" + spotfire.config.version + "&dn=" + spotfire.config.domainName + "&f=" +
            spotfire.file + "&u=" + spotfire.config.user;
    },

    composefileName: function () {
        spotfire.productFamilyNames = _.map($("#productFamilyIdsSelect").select2('data'), function (x) {
            return x.text.replace(/\(|\)/g, '_')
        });
        var startDate = moment($('#fromDate').val(), "DD-MMM-YYYY").format("DD-MMM-YYYY");
        var endDate = moment($('#endDate').val(), "DD-MMM-YYYY").format("DD-MMM-YYYY");
        var asofDate = moment($('#asOfDate').val(), "DD-MMM-YYYY").format("DD-MMM-YYYY");
        var radioButtonId = $('input:radio[name=type]:checked').attr("id");
        var radioButtonLabel = $('input[name="type"]:checked').val();
        var caseSeries = $('#caseSeriesId').select2("val");
        var caseSeriesName = "";
        if (caseSeries !== "" && caseSeries !== null) {
            caseSeriesName = ("_CaseSeries_" + replaceAll($('#caseSeriesId').select2('data').text, " ", "-"));
        }

        if (typeof spotfire.productFamilyNames !== 'undefined' && spotfire.productFamilyNames && spotfire.productFamilyNames.length > 0) {
            if (caseSeriesName !== "") {
                return (_.reduce(spotfire.productFamilyNames, function (str, name) {
                        return str + "_" + name
                    }, "") + caseSeriesName + "_" +
                    capitalizeFirstLetter(radioButtonLabel)).substring(1);
            }
            return (_.reduce(spotfire.productFamilyNames, function (str, name) {
                    return str + "_" + name
                }, "") +
                "_" + startDate + "_" + endDate + "_AoD_" + asofDate + "_" +
                capitalizeFirstLetter(radioButtonLabel)).substring(1);
        } else
            return "";
    },

    log: function () {
        console.log("fromDate:" + formatDate(spotfire.fromDate));
        console.log('endDate:' + formatDate(spotfire.endDate));
        console.log('asOfDate:' + formatDate(spotfire.asOfDate));
        console.log('server:' + spotfire.config.server);
        console.log('version:' + spotfire.config.version);
        console.log('path:' + spotfire.config.path);
        console.log('productFamilyNames:' + spotfire.productFamilyNames);
        console.log('fileName:' + spotfire.fileName);
        console.log('file:' + spotfire.file);
        console.log('type:' + spotfire.type);
    },

    openSpotfireReport: function (fileName) {
        spotfire.file = spotfire.config.libraryRoot + "/" + fileName;
        document.cookie = 'ticket=' + spotfire.config.user + '; path=/' + spotfire.config.path;

        spotfire.serverUrl = spotfire.config.protocol + "://" + spotfire.config.server;

        if (spotfire.config.port !== null && spotfire.config.port !== "") {
            spotfire.serverUrl += ":" + spotfire.config.port;
        }

        spotfire.serverUrl += "/" + spotfire.config.path + encodeURIComponent(fileName) +
            "&auth_token=" + encodeURIComponent(spotfire.config.token) +
            "&cbs=" + encodeURIComponent(spotfire.config.callbackServer);
        spotfire.generateReport();

        document.cookie = "auth_token=" + spotfire.config.token;

        return spotfire;
    },

    setFullFileName: function () {
        spotfire.fileName = spotfire.composefileName();
        $('#fullFileName').val(spotfire.fileName);
    },

    disableEnableDateOptions: function (disable) {
        if (disable) {
            $("#spotfireFromDate").datepicker('disable');
            $("#spotfireEndDate").datepicker('disable');
            $("#spotfireAsOfDate").datepicker('disable');
            $('#fromDate').val('');
            $('#endDate').val('');
            $('#asOfDate').val('');
        } else {
            $("#spotfireFromDate").datepicker('enable').datepicker('setDate', spotfire.fromDate);
            $("#spotfireEndDate").datepicker('enable').datepicker('setDate', spotfire.endDate);
            $("#spotfireAsOfDate").datepicker('enable').datepicker('setDate', spotfire.asOfDate);
        }
        spotfire.setFullFileName()
    }
};

function formatDate(date) {
    return moment(date).format("DD-MM-YYYY");
}

function replaceAll(str, find, replace) {
    return str.replace(new RegExp(find, 'g'), replace);
}

$(function () {
    $('body').find('.content').addClass('spotfire');
    $('body').find('.rxmain-container-content').addClass('spotfire-container-content');
    var selectProductFamily = bindMultipleSelect2WithUrl($("#productFamilyIdsSelect"), ajaxProductFamilySearchUrl, true);
    selectProductFamily.on('change', function () {
        spotfire.setFullFileName();
        var selectedValues = $(this).val();
        $("#productFamilyIds").val(selectedValues ? selectedValues.join(MULTIPLE_AJAX_SEPARATOR) : "");
    });
});