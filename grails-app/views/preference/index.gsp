<%@ page import="org.joda.time.DateTimeZone;com.rxlogix.util.ViewHelper" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.Preference.title"/></title>
    <script>
        $(function () {
            $("#language").select2();
            $("#timeZone").select2();

            $(".themeRadio").on('click', function() {
                var id = $(this).attr("id");
                $(".theme").val(id);
            });

            var elem = $("#" + $('.theme').val());
            if (elem.length) {
                elem.attr('checked', true);
            } else {
                $("#gradient_blue").attr('checked', true);
            }
            //Update session storage on form submit to update theme for PV-UI plugin use
            $("input[type='submit']").on('click', function(e){
                var uiTheme = UI_THEME[$("input[name='theme_val']:checked").attr('id')];
                sessionStorage.setItem("theme", uiTheme);

            });

            $('.cancel').on('click', function (){
                $("form").detach();
                location.reload();
            });

            $("[data-evt-clk]").on('click', function() {
                const eventData = JSON.parse($(this).attr("data-evt-clk"));
                const methodName = eventData.method;
                const params = eventData.params;

                if (methodName == "hideShowContent") {
                    // Call the method from the eventHandlers object with the params
                    hideShowContent($(this));
                }
            });
        });

        function hideShowContent(e) {
            var getContent = $(e).parent().parent().find('.rxmain-container-content');
            var display = true;
            if ($(getContent).hasClass('rxmain-container-hide')) {
                display = false;
            }

            var getIcon;
            if (display) {
                getIcon = $(e).parent().find('i');
                $(getIcon).removeClass('fa-caret-down').addClass('fa-caret-right').trigger("classAdded");
                $(getContent).removeClass('rxmain-container-show').addClass('rxmain-container-hide');
            } else {
                getIcon = $(e).parent().find('i');
                $(getIcon).removeClass('fa-caret-right').addClass('fa-caret-down').trigger("classAdded");
                $(getContent).removeClass('rxmain-container-hide').addClass('rxmain-container-show');
            }
        }
    </script>
    <style>


    .defaultTheme {
        background-image: url('../images/background.jpg');
    }

    .rxmain-container-inner {
        margin: 10px;
    }

    </style>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs"/>
<div class="content ">
<g:form name="selectedLocale" action="update">
    <div class="rxmain-container-inner">

            <div class="rxmain-container-row rxmain-container-header">
                <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    <g:message code="app.preference.languageAndTimezone"/>
                </label>
            </div>
            <div class="rxmain-container-content rxmain-container-show">
                <div class="row">
                    <div class="col-xs-6">
                        <g:message code="app.label.language"/>
                        <div>
                            <select id="language" class="form-control" name="language">
                                <g:each in="${locales}" var="locale">
                                    <option value="${locale.key}"
                                        ${locale.key == currentLocale.lang_code ? 'selected' : ''}>
                                        ${locale.value}
                                    </option>
                                </g:each>
                            </select>
                        </div>
                    </div>

                    <div class="col-xs-6">
                        <g:message code="app.label.timezone"/>
                        <div>
                            <g:select id="timeZone"
                                      name="timeZone"
                                      from="${ViewHelper.getTimezoneValues()}"
                                      optionKey="name"
                                      optionValue="display"
                                      noSelection="${['': message(code: 'select.one')]}"
                                      class="form-control"
                                      value="${theUserTimezone}"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    <div class="rxmain-container-inner">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.preference.appearance"/>
            </label>
        </div>
        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <div class="col-xs-3">
                    <div class="radio radio-primary">
                        <input class="themeRadio" id="gradient_blue" type="radio" name="theme_val">
                        <label for="gradient_blue"><span style="width: 15px;height: 15px;float:left; background:  linear-gradient(to right, #2a6ca3, #2a8fbb)"></span><span style="padding-left: 5px"><g:message code="app.preference.theme.gradientBlue"/></span></label>
                    </div>

                    <div class="radio radio-primary">
                        <input class="themeRadio" id="solid_orange" type="radio" name="theme_val">
                        <label for="solid_orange"><span style="width: 15px;height: 15px; float:left; background-color: #eea320"></span><span style="padding-left: 5px"><g:message code="app.preference.theme.solidOrange"/></span></label>
                    </div>

                    <div class="radio radio-primary">
                        <input class="themeRadio" id="solid_blue" type="radio" name="theme_val">
                        <label for="solid_blue"><span style="width: 15px;height: 15px; float:left;  background-color: #2a6ca3"></span><span style="padding-left: 5px"><g:message code="app.preference.theme.solidBlue"/></span></label>
                    </div>

                    <div class="radio radio-primary">
                        <input class="themeRadio" id="solid_golden_grey" type="radio" name="theme_val">
                        <label for="solid_golden_grey"><span style="width: 15px;height: 15px; float:left;  background:  linear-gradient(to right, #eea320, #cccccc)"></span><span style="padding-left: 5px"><g:message code="app.preference.theme.goldenGrey"/></span></label>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="rxmain-container-inner">

        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.preference.notifications"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <div class="col-xs-3">
                    <b><g:message code="app.preference.actionItem.emails"/>:</b>

                    <div class="emailOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="createAI"
                                    class="AIEmail"
                                    name="createAI"
                                    value="${true}"
                                    checked="${actionItemEmail?.creationEmails}"/>
                        <label for="createAI">
                            <g:message code="app.preference.create.emails"/>
                        </label>
                    </div>
                    <div class="emailOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="updateAI"
                                    class="AIEmail"
                                    name="updateAI"
                                    value="${true}"
                                    checked="${actionItemEmail?.updateEmails}"/>
                        <label for="updateAI">
                            <g:message code="app.preference.update.emails"/>
                        </label>
                    </div>
                    <div class="emailOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="AIJobEmails"
                                    class="AIEmail"
                                    name="AIJobEmails"
                                    value="${true}"
                                    checked="${actionItemEmail?.jobEmails}"/>
                        <label for="AIJobEmails">
                            <g:message code="app.preference.actionItem.reminder.emails"/>
                        </label>
                    </div>
                </div>


                <div class="col-xs-3">
                    <b><g:message code="app.preference.reportRequest.emails"/>:</b>

                    <div class="reportRequestOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="createReportRequest"
                                    class="RREmail"
                                    name="createReportRequest"
                                    value="${true}"
                                    checked="${reportRequestEmail?.creationEmails}"/>
                        <label for="createReportRequest">
                            <g:message code="app.preference.create.emails"/>
                        </label>
                    </div>
                    <div class="reportRequestOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="updateReportRequest"
                                    class="RREmail"
                                    name="updateReportRequest"
                                    value="${true}"
                                    checked="${reportRequestEmail?.updateEmails}"/>
                        <label for="updateReportRequest">
                            <g:message code="app.preference.update.emails"/>
                        </label>
                    </div>
                    <div class="reportRequestOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="deleteReportRequest"
                                    class="RREmail"
                                    name="deleteReportRequest"
                                    value="${true}"
                                    checked="${reportRequestEmail?.deleteEmails}"/>
                        <label for="deleteReportRequest">
                            <g:message code="app.preference.delete.emails"/>
                        </label>
                    </div>
                    <div class="reportRequestOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="reportRequestWorkflow"
                                    class="RREmail"
                                    name="reportRequestWorkflow"
                                    value="${true}"
                                    checked="${reportRequestEmail?.workflowUpdate}"/>
                        <label for="reportRequestWorkflow">
                            <g:message code="app.preference.workflow.update.emails"/>
                        </label>
                    </div>
                </div>

                <div class="col-xs-3">
                    <b><g:message code="app.preference.pvc.emails"/>:</b>

                    <div class="emailOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="pvcAssignedToMe"
                                    class="PVCEmail"
                                    name="pvcAssignedToMe"
                                    value="${true}"
                                    checked="${pvcEmail?.assignedToMe}"/>
                        <label for="pvcAssignedToMe">
                            <g:message code="app.preference.assigned.emails"/>
                        </label>
                    </div>
                    <div class="emailOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="pvcAssignedToMyGroup"
                                    class="PVCEmail"
                                    name="pvcAssignedToMyGroup"
                                    value="${true}"
                                    checked="${pvcEmail?.assignedToMyGroup}"/>
                        <label for="pvcAssignedToMyGroup">
                             <g:message code="app.preference.assignedGroup.emails"/>
                        </label>
                    </div>
                    <div class="emailOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="pvcWorkflowStateChange"
                                    class="PVCEmail"
                                    name="pvcWorkflowStateChange"
                                    value="${true}"
                                    checked="${pvcEmail?.workflowStateChange}"/>
                        <label for="pvcWorkflowStateChange">
                              <g:message code="app.preference.workflow.emails"/>
                        </label>
                    </div>
                </div>


                <div class="col-xs-3">
                     <b><g:message code="app.preference.pvq.emails"/>:</b>
                    <div class="emailOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="pvqAssignedToMe"
                                    class="PVQEmail"
                                    name="pvqAssignedToMe"
                                    value="${true}"
                                   checked="${pvqEmail?.assignedToMe}"/>
                        <label for="pvqAssignedToMe">
                            <g:message code="app.preference.assigned.emails"/>
                        </label>
                    </div>
                    <div class="emailOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="pvqAssignedToMyGroup"
                                    class="PVQEmail"
                                    name="pvqAssignedToMyGroup"
                                    value="${true}"
                                    checked="${pvqEmail?.assignedToMyGroup}"/>
                        <label for="pvqAssignedToMyGroup">
                            <g:message code="app.preference.assignedGroup.emails"/>
                        </label>
                    </div>
                    <div class="emailOptions checkbox checkbox-primary" style="margin-top: 10px !important;">
                        <g:checkBox id="pvqWorkflowStateChange"
                                    class="PVQEmail"
                                    name="pvqWorkflowStateChange"
                                    value="${true}"
                                    checked="${pvqEmail?.workflowStateChange}"/>
                        <label for="pvqWorkflowStateChange">
                              <g:message code="app.preference.workflow.emails"/>
                        </label>
                    </div>
                </div>


            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-1" >
            <input class="btn btn-primary" type="submit" style="margin-left: 20px" value="${message(code: 'default.button.update.label')}"/>
        </div>

        <div class="col-xs-1">
            <button type="button" class="btn pv-btn-grey cancel">${message(code: "default.button.cancel.label")}</button>
        </div>
    </div>
    <g:hiddenField name="theme" class="theme" value="${theme}"/>
</g:form>
</div>
</body>