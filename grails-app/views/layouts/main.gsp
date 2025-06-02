<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.user.User;com.rxlogix.user.Preference" %>
<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <META HTTP-EQUIV='Pragma' CONTENT='no-cache'>
    <META HTTP-EQUIV='Cache-Control' CONTENT='no-cache'>
    <title>
    <g:if test="${!(ViewHelper.isPvqModule(request))}">
        <g:layoutTitle default="PV Reports"/>
    </g:if>
    <g:else>
        <g:layoutTitle default="PV Quality"/>
    </g:else>
    </title>
    <style>

    .side-menu .slimScrollDiv {
        pointer-events: none;
    }

    .side-menu .slimscrollleft {
        pointer-events: none;
    }

    #sidebar-menu {
        pointer-events: auto;
        width: 190px;
    }
    #wrapper:not(.enlarged) #sidebar-menu li {
        max-width: 190px;
    }
    .side-menu.left {
        z-index: 99999;
    }

    .side-menu .slimscrollleft:hover {
        width: 500px !important;
    }

    .side-menu .slimScrollDiv:hover {
        width: 500px !important;
    }

    .enlarged #sidebar-menu {
        width: 70px;
    }

    .resizableModal {
        pointer-events: none;
    }

    .resizableModal .modal-dialog {
        position: fixed;
        width: 100%;
        margin: 0;
        padding: 10px;
        pointer-events: auto;
    }

    </style>

    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <asset:stylesheet src="application.css"/>
    <asset:javascript src="vendorUi/jquery/jquery-3.7.1.min.js"/>
    <asset:javascript src="vendorUi/jquery-ui/jquery-ui.min.js"/>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>
    <script>
        var LOAD_THEME_URL = "${createLink(controller: 'preference', action: 'loadTheme')}";
        var UPDATE_USER_PREFERENCES_URL = "${createLink(controller: 'preference', action: 'updateUserPreferences')}";
        var GET_USER_PREFERENCES_URL = "${createLink(controller: 'preference', action: 'getUserPreferences')}";
        <g:if test="${params.controller == "auditLogEvent"}"> //temp fix till auditlog will be updated to provide localization support
        var userLocale = "en"
        </g:if>
        <g:else>
        var userLocale = "${session.getAttribute('org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE')?.language?:'en'}";
        </g:else>
        var APP_PATH='${request.contextPath}';
        var appContext = "${request.contextPath}";
        var APP_ASSETS_PATH = APP_PATH + '/assets/';
    </script>
    <asset:javascript src="application.js"/>
    <asset:javascript src="handlebars.runtime.min.js"/>
    <asset:javascript src="vendorUi/fuelux/fuelux.min.js"/>
    <script>
        moment.locale(userLocale);
        userTimeZone = "${(session.getAttribute('user.preference.timeZone'))?.ID?:TimeZone.default.ID}";
        if (userLocale == JAPANESE_LOCALE) {
            DEFAULT_DATE_DISPLAY_FORMAT = "YYYY/MM/DD";
            DEFAULT_DATE_TIME_DISPLAY_FORMAT = "YYYY/MM/DD hh:mm A";
        }

        var DASHBOARD = "dashboard/index"

        if (location.href.indexOf(DASHBOARD) != -1) {
            sessionStorage.setItem("module", "pvr");
            localStorage.setItem("module", "pvr");
        }

        $.ajax({
            url: APP_ASSETS_PATH + 'i18n/' + userLocale + '.json',
            dataType: 'json',
            async: false
        })
            .done(function (data) {
                $.i18n.load(data);
            });
    </script>
    <% if (params.customCss) session.customCss = (params.hideMenu == "false" ? "" : params.customCss) %>
    <g:if test="${session.customCss}">
        <link rel="stylesheet" href="${session.customCss}"/>
    </g:if>

    <g:layoutHead/>
    <rx:isCsrfProtectionEnabled>
        <meta name="_csrf" content="${_csrf?.token}"/>
        <meta name="_csrf_header" content="${_csrf?.headerName}"/>
        <meta name="_csrf_parameter" content="${_csrf?.parameterName}"/>
    </rx:isCsrfProtectionEnabled>
</head>
<body class="fixed-left-void">
<asset:javascript src="UIConstants.js"/>
<asset:javascript src="common/change-theme.js"/>

<!-- Begin page -->
<div id="wrapper" class="enlarged forced">

    <% if (params.hideMenu) session.hideMenu = (params.hideMenu == "true") %>
    <g:if test="${!session.hideMenu && !params.boolean('iframe')}">
        <!-- Top Bar-->
    %{--        <g:render template="/common-ui/topBar" />--}%
    %{--<!-- Left Sidebar -->--}%
    %{--        <g:render template="/common-ui/side_bar"/>--}%
        <!-- Left Sidebar End -->


        <g:render template="/includes/layout/topNav"/>
        <!-- Top Bar End -->

        <!-- Left Sidebar Start -->
        <g:render template="/includes/layout/leftNav"/>
        <script>
            window.onfocus = function () {
                var module = sessionStorage.getItem("module");
                if (module)
                    localStorage.setItem("module", module);
            };
            var module;

            var isPvq = ${ViewHelper.isPvqModule(request)?'true':'false'};
            var isPvp = ${ViewHelper.isPvPModule(request)?'true':'false'};
            var isPvc = ${ViewHelper.isPvcModule(request)?'true':'false'};
            var isPvr = ${params.pvr?'true':'false'};
            var isActionItem = ${params.actionItem ? 'true' : 'false'};
            if (isPvq)
                module = "pvq";
            else if (isPvp)
                module = "pvp";
            else if (isPvr)
                module = "pvr";
            else if (isPvc)
                module = "pvc";
            else if (isActionItem) {
                module = "actionItem";
            }
            if ((!module || module == "null") && sessionStorage.getItem("module") != "actionItem")
                module = sessionStorage.getItem("module");
            if ((!module || module == "null") && localStorage.getItem("module") != "actionItem")
                module = localStorage.getItem("module");
            if (!module || module == "null")
                module = 'pvr';

            sessionStorage.setItem("module", module);
            localStorage.setItem("module", module);
            if (module == "pvp") {
                $(".pvpLogo").show();
                $(".pvpLeftNav").show();
                $(".pvpLink").hide();
                $(".pvqLeftNav, .pvcLeftNav, .pvrLeftNav").remove();
                $(function () {
                    $(".pvpOnly").show();
                    $(".notPvpOnly").hide();
                });
            } else if (module == "pvq") {
                $(".pvqLogo").show();
                $(".pvqLeftNav").show();
                $(".pvqLink").hide();
                $(".pvpLeftNav, .pvcLeftNav, .pvrLeftNav").remove();
            } else if (module == "pvc") {
                $(".pvcLogo").show();
                $(".pvcLeftNav").show();
                $(".pvcLink").hide();
                $(".pvpLeftNav, .pvqLeftNav, .pvrLeftNav").remove();
            } else {
                $(".pvrLogo").show();
                $(".pvrLeftNav").show();
                if (module == "actionItem") {
                    $(".actionItemLink").hide();
                }
                $(".pvrLink").hide();
                $(".pvpLeftNav, .pvqLeftNav, .pvcLeftNav").remove();
            }
            $(function () {
                if (module != "pvp") {
                    $(".nonPvpRemove").remove();
                    $(".notPvpOnly").show();
                }
            });
            $(document).on("click", ".dismissHelp", function () {
                $(".localizationHelpModalContent").empty();
                $("#localizationHelpModal").modal("hide");
            });
            $(document).on("click", ".localizationHelpIcon", function (e) {
                e.preventDefault();
                var id = $(this).attr("data-id");
                openLocalizationHelpModal(id);
            });

            $(document).on("click", ".runInteractiveHelp", function (e) {
                e.preventDefault();
                showLoader();
                $.ajax({
                    url: "${createLink(controller: 'localizationHelpMessage', action: 'fetchInteractiveHelp')}?page=${params.controller+"/"+params.action}",
                    type: 'get',
                    success: function (response) {
                        hideLoader();
                        var tour;
                        eval("tour=new Tour(" + response[0].description + ")");
                        tour.init();
                        tour.start(true);
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                        $("#localizationHelpModalContent").html("Unexpected Error");
                        hideLoader();
                        $("#localizationHelpModal").modal("show");

                    }
                });

            });

            function openLocalizationHelpModal(id, localizationId) {
                showLoader();
                $.ajax({
                    url: "${createLink(controller: 'localizationHelpMessage', action: 'showMessage')}?id=" + id + "&localizationId=" + localizationId,
                    type: 'get',
                    dataType: 'html'
                })
                    .done(function (response) {
                        $(".localizationHelpModalContent").html(response);
                        hideLoader();
                        showResizableModal('#localizationHelpModal', 1000);
                        $(".viewHelpInNewWindow").attr("href", "${createLink(controller: 'localizationHelpMessage', action: 'showMessage')}?id=" + id + "&localizationId=" + localizationId)
                        $('.localizationHelpModalContent .helpDataTable').DataTable();

                    })
                    .fail(function (XMLHttpRequest, textStatus, errorThrown) {
                        $("#localizationHelpModalContent").html("Unexpected Error");
                        hideLoader();
                        $("#localizationHelpModal").modal("show");
                    });

            }

            function showResizableModal(id, width) {
                if (!($('.modal.in').length)) {
                    var left = Math.max(0, (($(window).width() - width) / 2));
                    $(id + ' .modal-dialog').css({
                        top: 50,
                        left: left
                    });
                }

                $(id).modal({
                    backdrop: false,
                    show: true
                });
                $(id + ' .modal-content').resizable({
                    minHeight: 185,
                    handles: ' e, w, ne, sw, se, nw',
                });

                $(id + ' .modal-dialog').draggable({
                    handle: ".modal-header"
                });
            }
        </script>
    </g:if>
<!-- ============================================================== -->
<!-- Start right Content here -->
<!-- ============================================================== -->
    <div class="content-page">
        <div id="mainContent" style="${session.hideMenu ? 'padding: 0;margin-left: -60px;' : ''}">
            <g:layoutBody/>
        </div>
    </div>
    <!-- ============================================================== -->
    <!-- End Right content here -->
    <!-- ============================================================== -->

    %{--    Loader Spinner--}%
    <g:render template="/common-ui/pvLoader"/>
    %{--    Loader Spinner--}%
</div>
<g:if test="${!session.hideMenu && !params.boolean('iframe')}">
    <sec:ifLoggedIn>
        <g:render template="/session/sessiontimeout"/>
    </sec:ifLoggedIn>
</g:if>
<asset:javascript src="common/jquery.app.js"/>
<script>
    //overriding function from common/jquery.app.js to provide scrollling in left menu
    function toggle_slimscroll(item) {
    }

    function initscrolls() {
        if (jQuery.browser.mobile !== true) {
            //SLIM SCROLL
            $('.slimscroller').slimscroll({
                height: 'auto',
                size: "5px"
            });

            $('.slimscrollleft').slimScroll({
                height: 'auto',
                position: 'left',
                size: "5px",
                color: '#dcdcdc',
                wheelStep: 5
            });
        }
    }
</script>

<div class="modal fade resizableModal" id="localizationHelpModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg " style="width:1000px;" role="document">
        <div class="modal-content">
            <div class="modal-header" style="cursor: move">

                <button type="button" class="close dismissHelp" aria-label="Close"><span
                        aria-hidden="true">&times;</span>
                </button>
                <a href="" target="_blank" style="font-size: 18px;margin-right: 5px;"
                   class="close dismissHelp viewHelpInNewWindow" type="button" aria-label="View"><span
                        aria-hidden="true" class="md md-open-in-new"></span>
                </a>

                <h4 class="modal-title"><g:message code="app.label.localizationHelp.help"/></h4>
            </div>

            <div class="modal-body localizationHelpModalContent" style="width: 100%; height: calc(100% - 85px);">

            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey dismissHelp">
                    <g:message code="default.button.ok.label"/>
                </button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="TenantWarningModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="warningModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-evt-clk='{"method": "modalHide", "params": ["#TenantWarningModal"]}' aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="warningModalLabel">Warning</h4>
            </div>

            <div class="modal-body">
                <div id="warningType">Are you sure you want to leave? </div>

                <p></p>

                <div class="description" style="font-weight:bold;">Changes you made may not be saved </div>

                <div class="extramessage"></div>

            </div>

            <div class="modal-footer">
                <button id="continueTenantSwitch" class="btn btn-primary">
                    <g:message code="default.warningModal.message.continue.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "modalHide", "params": ["#TenantWarningModal"]}'><g:message
                        code="default.button.cancel.label"/></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div>
<asset:javascript src="bootstrap-tour/bootstrap-tour.js"/>
</body>
</html>
