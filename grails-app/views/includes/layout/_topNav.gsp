<%@ page import="com.rxlogix.localization.InteractiveHelp;grails.util.Holders; com.rxlogix.util.ViewHelper; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.user.User;com.rxlogix.config.SourceProfile;com.rxlogix.RxCodec;" %>
<g:set var="userService" bean="userService"/>
<g:set var="user" value="${userService.user}"/>
<g:set var="isPVCM" value="${SourceProfile.findByIsCentral(true)?.sourceName?.equals("PVCM")}"/>
<asset:javascript src="app/pushNotification.js"/>
<g:javascript>
    $(function () {
        $("[data-evt-clk]").on('click', function() {
          const eventData = JSON.parse($(this).attr("data-evt-clk"));
          const methodName = eventData.method;
          const params = eventData.params;

          if (methodName == "logoutRxSession") {
            // Call the method from the eventHandlers object with the params
            logoutRxSession();
          }
        });
    })
    function logoutRxSession() {
      sessionStorage.clear();
      localStorage.clear();
      clearFormInputsChangeFlag($(document));
      $.fn.idleTimeout().logout();
    }
    var oneDriveEnabled = ${grailsApplication.config.oneDrive.enabled};
    var officeOnlineEnabled = ${grailsApplication.config.officeOnline.enabled};
    var notificationURL = "${createLink(controller: 'notificationRest', action: 'forUser', params: [id: user?.id])}";
    var notificationChannel ="${user?.notificationChannel}";
    var notificationWSURL = "ws${createLink(uri: '/stomp', absolute: true).replaceFirst('(?i)http', '')}";
    var viewNotificationErrorURL = "${createLink(controller: 'executionStatus', action: 'viewNotificationError')}";
    var notificationDeleteURL = "${createLink(controller: 'notificationRest', action: 'deleteNotificationById')}";
    var notificationDeleteByUserURL = "${createLink(controller: 'notificationRest', action: 'deleteNotificationsForUserId')}";
    var notificationBulkDownloadIcsrURL = "${createLink(controller: 'icsrCaseTrackingRest', action: 'bulkDownLoadIcsrReports')}"
    var reportRedirectURL = "${createLink(controller: 'report', action: 'showFirstSection')}";
    <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
    var hasAccessOnActionItem = true;
    </sec:ifAnyGranted>
    <sec:ifNotGranted roles="ROLE_ACTION_ITEM">
    var hasAccessOnActionItem = false;
    </sec:ifNotGranted>
    window.onfocus = function() {
        var module = sessionStorage.getItem("module")
        if(module)
            localStorage.setItem("module",module);
    };
</g:javascript>

<div class="topbar">
    <!-- LOGO -->

    <!-- Navbar -->
    <div class="navbar navbar-default" role="navigation">
        <div class="pull-left">
            <button class="button-menu-mobile open-left waves-effect">
                <i class="md md-menu"></i>
            </button>
            <span class="clearfix"></span>
        </div>

    <div class="topbar-left">
        <div class="pull-left">
            <span class="pvqLogo"  style="display: none;">
                <g:link controller="quality" action="index"><asset:image src="pvq_logo.png" class="pvLogo"/></g:link>
            </span>
            <span class="pvpLogo"  style="display: none;">
                <g:link controller="pvp" action="index"><asset:image src="PVPublisher.png" class="pvLogo"/></g:link>
            </span>
             <span class="pvcLogo"  style="display: none;">
                <g:link controller="central" action="index"><asset:image src="pvc_logo.png" class="pvLogo"/></g:link>
            </span>
            <span class="pvrLogo" style="display: none;">
                <g:link controller="dashboard" action="index"><asset:image src="pvr_logo.png" class="pvLogo"/></g:link>
            </span>
        </div>
    </div>
        <div class="container">
            <div class="">
               <span class="dropdown hidden-lg">
                   <button class="btn dropdown-toggle m-t-20 ml-arrow" type="button" data-toggle="dropdown" style="background-color:transparent;color:#fff">
                   <span class="caret"></span></button>
                   <ul class="dropdown-menu dd-arrow m-t-10">
                                           <li class="pvrLi topNameItems">
                                               <g:link controller="dashboard" action="index" class="waves-effect pvrLink" params="[pvr:true]">
                                               <g:message code="app.label.pv.reports" default="PV Reports"/></g:link>
                                           </li>
                                           <g:if test="${grailsApplication.config.app.pvintake.url}">
                                               <li><g:link uri="${grailsApplication.config.app.pvintake.url}" class="waves-effect"><g:message code="app.label.pvintake"/></g:link></li>
                                           </g:if>
                                            <rx:showPVCModule>
                                               <sec:ifAnyGranted roles="ROLE_PVC_VIEW">
                                               <li class="pvcLi topNameItems">
                                                   <g:link uri="${grailsApplication.config.app.pvcentral.url}" class="waves-effect pvcLink" >
                                                   <g:message code="app.label.pvcentral" default="PV Central"/></g:link>
                                               </li>
                                               </sec:ifAnyGranted>
                                            </rx:showPVCModule>
                                           <rx:showPVQModule>
                                               <sec:ifAnyGranted roles="ROLE_PVQ_VIEW">
                                                   <li class="pvqLi topNameItems">
                                                       <g:link controller="quality" action="index" class="waves-effect pvqLink">
                                                       <g:message code="app.label.pv.quality" default="PV Quality"/></g:link>
                                                   </li>
                                               </sec:ifAnyGranted>
                                           </rx:showPVQModule>
                                            <rx:showPVPModule>
                                               <li class="pvpLi topNameItems">
                                                   <g:link uri="${grailsApplication.config.app.pvpublisher.url}" class="waves-effect pvpLink" >
                                                   <g:message code="app.label.pvpublisher" default="PV Publisher"/></g:link>
                                               </li>
                                           </rx:showPVPModule>
                                           <g:if test="${grailsApplication.config.app.pvsignal.url}">
                                               <li><g:link uri="${grailsApplication.config.app.pvsignal.url}" class="waves-effect"><g:message code="app.label.pvsignal"/></g:link></li>
                                           </g:if>
                   </ul>


               </span>



                <ul class="nav navbar-nav pv-prod-items-list hidden-xs pull-left hidden-sm hidden-md">
                        <li class="pvrLi topNameItems">
                            <g:link controller="dashboard" action="index" class="waves-effect pvrLink" params="[pvr:true]">
                            <g:message code="app.label.pv.reports" default="PV Reports"/></g:link>
                        </li>
                        <g:if test="${grailsApplication.config.app.pvintake.url}">
                            <li><g:link uri="${grailsApplication.config.app.pvintake.url}" class="waves-effect"><g:message code="app.label.pvintake"/></g:link></li>
                        </g:if>
                        <rx:showPVCModule>
                            <sec:ifAnyGranted roles="ROLE_PVC_VIEW">
                            <li class="pvcLi topNameItems">
                                <g:link uri="${grailsApplication.config.app.pvcentral.url}" class="waves-effect pvcLink" >
                                <g:message code="app.label.pvcentral" default="PV Central"/></g:link>
                            </li>
                            </sec:ifAnyGranted>
                        </rx:showPVCModule>
                        <rx:showPVQModule>
                            <sec:ifAnyGranted roles="ROLE_PVQ_VIEW">
                                <li class="pvqLi topNameItems">
                                    <g:link controller="quality" action="index" class="waves-effect pvqLink">
                                    <g:message code="app.label.pv.quality" default="PV Quality"/></g:link>
                                </li>
                            </sec:ifAnyGranted>
                        </rx:showPVQModule>


                    <rx:showPVPModule>
                        <li class="pvpLi topNameItems">
                            <g:link uri="${grailsApplication.config.app.pvpublisher.url}" class="waves-effect pvpLink" >
                            <g:message code="app.label.pvpublisher" default="PV Publisher"/></g:link>
                        </li>
                    </rx:showPVPModule>
                    <g:if test="${grailsApplication.config.app.pvsignal.url}">
                        <li><g:link uri="${grailsApplication.config.app.pvsignal.url}" class="waves-effect"><g:message code="app.label.pvsignal"/></g:link></li>
                    </g:if>
                </ul>
                <ul class="nav navbar-nav pv-navbar-right navbar-right pull-right">
                    <sec:ifLoggedIn>
                        <li class="hidden-xs m-t-10">
                            <h5 class="text-white login-user-fullname member-login"><g:message code="app.label.topnav.welcome" /> ${sec.loggedInUserInfo(field: "fullName")}!</h5>
                                <small class="text-white last-login-date-time-box f12"><b><g:message code="user.lastLogin.label"/></b> :  <g:renderUserLastLoginDate/></small>
                        </li>
                        <g:if test="${Holders.config.get('pvreports.multiTenancy.enabled')}">
                            <li class="hidden-xs m-t-10">
                                <div class="tenants text-white mt-6">
                                    <strong>|</strong>
                                    <g:selectCurrentTenant name="currentUserTenantSelect" optionKey="id" optionValue="displayName" area-expend="true" class="selected-tenant"/>
                                </div>
                            </li>
                        </g:if>
                        <li id="menuNotification" class="hidden-xs">
                                <a href="#" data-target="#" class="dropdown-toggle waves-effect waves-light"
                                   data-toggle="dropdown" aria-expanded="true">
                                    <i class="md md-bell"></i> <span
                                        id="notificationBadge"  class="badge badge-xs badge-pink"></span>
                                </a>
                                <ul class="dropdown-menu dropdown-menu-lg">
                                    <li id="notificationHeader" class="text-center notifi-title"></li>
                                    <li id="notificationRows" class="list-group nicescroll notification-list" style="max-height: 360px">
                                    </li>
                                    <li>
                                        <a href="javascript:void(0);" id="clearNotifications" class=" text-right" userId="${user?.id}">
                                            <small><b><g:message code="app.notification.clearAll"/></b></small>
                                        </a>
                                    </li>
                                </ul>
                        </li>
                        <li class="hidden-xs nav-item dropdown ">

                            <a href="#" data-target="#" id="settingIcon" class="dropdown-toggle waves-effect waves-light" data-toggle="dropdown" aria-expanded="true">
                                <i class="md md-settings"></i>
                            </a>
                            <ul class="dropdown-menu " style="    width: 250px;">
                                <li id="executionStatus"><g:link controller="executionStatus" action="list" class="mega-dd-font"><i class="md md-alarm"></i> <g:message code="app.label.executionStatus"/></g:link>
                                </li>
                                <li id="menuPreference"><g:link controller="preference" action="index" class="mega-dd-font"><i class="md md-compare"></i>  <g:message code="app.label.preference"/></g:link>
                                </li>
                                <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                                    <li id="auditLog"><g:link controller="auditLogEvent" action="list" class="mega-dd-font"><i class="md md-list" style="padding:0"></i>  <g:message code="auditLog.label"/></g:link>
                                    </li>
                                </sec:ifAnyGranted>
                                <form class="panel-group settings-accordion" id="accordion" role="tablist" aria-multiselectable="true">
                                    <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                                        <g:if test="${!isPVCM}">
                                            <div class="panel panel-default">
                                                <div role="tab" id="heading1">
                                                    <div><a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseMenu1" aria-expanded="false" aria-controls="collapseMenu1" class="mega-dd-font">
                                                        <i class="md md-database "></i> ETL</a></div>
                                                </div>
                                                <div id="collapseMenu1" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading1">
                                                    <div class="mega-dd-font-item"><g:link controller="etlSchedule" action="index"><g:message code="app.label.etlScheduler"/></g:link></div>
                                                    <div class="mega-dd-font-item"><g:link controller="balanceMinusQuery" action="index"><g:message code="app.balanceMinusQuery.label"/></g:link></div>
                                                </div>
                                            </div>
                                        </g:if>
                                    </sec:ifAnyGranted>

                                    <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION,ROLE_USER_MANAGER,ROLE_CUSTOM_FIELD,ROLE_ADMIN">

                                        <div class="panel panel-default">
                                            <div role="tab" id="heading2">
                                                <div><a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseMenu2" aria-expanded="false" aria-controls="collapseMenu2" class="mega-dd-font">
                                                    <i class="md md-account-circle"></i> <g:message code="app.label.accessManagement"/>
                                                </a></div>
                                            </div>
                                            <div id="collapseMenu2" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading2">

                                                <sec:ifAnyGranted roles="ROLE_USER_MANAGER">
                                                    <div class="mega-dd-font-item" id="userManagement"><g:link controller="user" action="index"><g:message code="app.label.userManagement"/></g:link></div>
                                                    <div class="mega-dd-font-item" id="userManagement"><g:link controller="userGroup" action="index"><g:message code="app.label.group.management"/></g:link></div>
                                                    <div class="mega-dd-font-item" id="roleManagement"><g:link controller="role" action="index"><g:message code="app.label.roleManagement"/></g:link></div>
                                                </sec:ifAnyGranted>
                                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                                    <div class="mega-dd-font-item" id="fieldManagement"><g:link controller="reportField" action="index"><g:message code="app.label.field.management"/></g:link></div>
                                                </sec:ifAnyGranted>
                                                <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION,ROLE_USER_MANAGER">
                                                    <div class="mega-dd-font-item" id="userManagement"><g:link controller="dashboardDictionary" action="index"><g:message code="app.label.dashBoard.nenuItem"/></g:link></div>
                                                </sec:ifAnyGranted>
                                                <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                                                    <div class="mega-dd-font-item" id="userManagement"><g:link controller="fieldProfile" action="index"><g:message code="app.label.field.profile"/></g:link></div>
                                                    <div class="mega-dd-font-item" id="sourceProfileManagement"><g:link controller="sourceProfile" action="index"><g:message code="app.sourceProfile.label"/></g:link></div>
                                                </sec:ifAnyGranted>
                                            </div>
                                        </div>

                                    </sec:ifAnyGranted>



                                    <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION, ROLE_PVQ_EDIT">

                                        <div class="panel panel-default">
                                            <div role="tab" id="heading3">
                                                <div><a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseMenu3" aria-expanded="false" aria-controls="collapseMenu3" class="mega-dd-font">
                                                    <i class="md md-image"></i> <g:message code="app.label.settings.business"/>
                                                </a></div>
                                            </div>
                                            <div id="collapseMenu3" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading3">
                                                <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                                                <div class="mega-dd-font-item" id="workflowState"><g:link controller="workflowState" action="index"><g:message code="app.label.workflow.appName"/></g:link></div>
                                                <div class="mega-dd-font-item" id="workflowRule"><g:link controller="workflowRule" action="index"><g:message code="app.label.workflow.rule.appName"/></g:link></div>
                                                <g:if test="${(grailsApplication.config.show.pvc.module && grailsApplication.config.pv.app.pvcentral.enabled) || (grailsApplication.config.show.pvq.module && grailsApplication.config.pv.app.pvquality.enabled)}">
                                                    <div class="mega-dd-font-item" id="advancedAssignment"><g:link controller="advancedAssignment" action="index"><g:message code="app.label.advanced.assignment.appName"/></g:link></div>
                                                </g:if>
                                                <g:if test="${grailsApplication.config.show.pvc.module && grailsApplication.config.pv.app.pvcentral.enabled}">
                                                    <div class="mega-dd-font-item" id="autoReasonOfDelay"><g:link controller="autoReasonOfDelay" action="index"><g:message code="app.label.auto.rod.appName"/></g:link></div>
                                                </g:if>
                                                <g:if test="${(grailsApplication.config.show.pvc.module && grailsApplication.config.pv.app.pvcentral.enabled) || (grailsApplication.config.show.pvq.module && grailsApplication.config.pv.app.pvquality.enabled)}">
                                                    <div class="mega-dd-font-item" id="capaConfig"><g:link controller="capa" action="capaList"><g:message code="app.capa.label"/></g:link></div>
                                                </g:if>
                                                <g:if test="${(grailsApplication.config.show.pvc.module && grailsApplication.config.pv.app.pvcentral.enabled) || (grailsApplication.config.show.pvq.module && grailsApplication.config.pv.app.pvquality.enabled)}">
                                                    <div class="mega-dd-font-item" id="rodMapping"><g:link controller="reasonOfDelay" action="rodMapping"><g:message code="app.label.rodMapping.appName"/></g:link></div>
                                                </g:if>
                                                </sec:ifAnyGranted>
                                                <g:if test="${(grailsApplication.config.show.pvq.module && grailsApplication.config.pv.app.pvquality.enabled)}">
                                                    <div class="mega-dd-font-item" id="autoRCA"><g:link controller="configuration" action="listPvqCfg"><g:message code="app.configuration.autorca.name"/></g:link></div>
                                                </g:if>

                                                <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                                                <rx:showXMLOption>
                                                    <div class="mega-dd-font-item"><g:link controller="unitConfiguration" action="index"><g:message code="app.label.unit.configuration.menuItem"/></g:link></div>
                                                </rx:showXMLOption>
                                                <div class="mega-dd-font-item" id="taskTemplate"><g:link controller="taskTemplate" action="index"><g:message code="app.label.task.template.appName"/></g:link></div>
                                                </sec:ifAnyGranted>
                                            </div>
                                        </div>

                                    </sec:ifAnyGranted>
                                    <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION,ROLE_CUSTOM_FIELD">
                                        <div class="panel panel-default">
                                            <div role="tab" id="heading4">
                                                <div><a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseMenu4" aria-expanded="false" aria-controls="collapseMenu4" class="mega-dd-font">
                                                    <i class="md md-view-list"></i> <g:message code="app.label.settings.system"/>
                                                </a></div>
                                            </div>
                                            <div id="collapseMenu4" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading4">

                                                <sec:ifAnyGranted roles="ROLE_CUSTOM_FIELD">
                                                    <div class="mega-dd-font-item" id="customField"><g:link controller="customField" action="index"><g:message code="app.label.customField.title"/></g:link></div>
                                                </sec:ifAnyGranted>
                                                <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                                                    <div class="mega-dd-font-item" id="reportFooter"><g:link controller="emailTemplate" action="index"><g:message code="app.label.emailTemplate.appName"/></g:link></div>
                                                    <div class="mega-dd-font-item" id="reportFooter"><g:link controller="email" action="index"><g:message code="app.label.email.menuItem"/></g:link></div>
                                                    <div class="mega-dd-font-item" id="reportFooter"><g:link controller="reportFooter" action="index"><g:message code="app.label.reportFooter.menuItem"/></g:link></div>
                                                    <div class="mega-dd-font-item" id="reportRequestType"><g:link controller="reportRequestType" action="index"><g:message code="app.label.reportRequest.settings"/></g:link></div>
                                                    <div class="mega-dd-font-item"><g:link controller="localizationHelpMessage" action="index"><g:message code="app.label.localizationHelp.menu" default="Configure Help Content"/></g:link></div>
                                                </sec:ifAnyGranted>
                                            </div>
                                        </div>
                                    </sec:ifAnyGranted>
                                    <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION,ROLE_ADMIN">
                                        <div class="panel panel-default">
                                            <div role="tab" id="heading5">
                                                <div><a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseMenu5" aria-expanded="false" aria-controls="collapseMenu5" class="mega-dd-font">
                                                    <i class="md md-settings"></i> <g:message code="app.label.settings.util"/>
                                                </a></div>
                                            </div>
                                            <div id="collapseMenu5" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading5">

                                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                                    <div class="mega-dd-font-item" id="compare"><g:link controller="comparison" action="index"><g:message code="app.comparison.reportsComparison"/></g:link></div>
                                                </sec:ifAnyGranted>
                                                <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                                                    <div class="mega-dd-font-item" id="jsonDownloaded"><g:link controller="queryTemplateJSON" action="index"><g:message code="app.label.download.json"/></g:link></div>
                                                    <div class="mega-dd-font-item" id="controlPanel"><g:link controller="controlPanel" action="index"><g:message code="app.label.controlPanel"/></g:link></div>
                                                    <g:if test="${grailsApplication.config.show.pvc.module && grailsApplication.config.pv.app.pvcentral.enabled}">
                                                        <div class="mega-dd-font-item" id="rodMapping"><g:link controller="central" action="importSubmissions">
                                                            <g:message code="app.pvc.import.submission"/></g:link>
                                                        </div>
                                                    </g:if>
                                                </sec:ifAnyGranted>
                                            </div>
                                        </div>
                                    </sec:ifAnyGranted>


                                    <sec:ifAnyGranted roles="ROLE_DEV">
                                        <div class="panel panel-default">
                                            <div role="tab" id="heading6">
                                                <div><a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseMenu6" aria-expanded="false" aria-controls="collapseMenu6" class="mega-dd-font">
                                                    <i class="md md-engine"></i> <g:message code="app.label.settings.tools"/>
                                                </a></div>
                                            </div>
                                            <div id="collapseMenu6" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading6">

                                                <div class="mega-dd-font-item"><g:link controller="schemaComparison" action="index"><g:message code="app.label.settings.schemaComparison"/></g:link></div>
                                                <div class="mega-dd-font-item"><a href="${grailsApplication.config.pvreports.monitoring.url}"><g:message code="app.label.monitoring"/></a>
                                                </div>
                                                <div class="mega-dd-font-item"><a href="${createLink(uri: "/quartz")}"><g:message code="app.label.quartz.monitoring"/></a>
                                                </div>
                                            </div>
                                        </div>
                                    </sec:ifAnyGranted>
                                        <g:if test="${grailsApplication.config.getProperty('app.pvadmin.url')}">
                                        <g:if test="${Holders.config.getProperty('safety.source') == "pvcm"}">
                                            <li><g:link url="${grailsApplication.config.getProperty('app.pvadmin.url')}" target="_blank" class="mega-dd-font"><i class="md md-apps"></i>  <g:message code="app.label.pvadmin"/></g:link>
                                            </li>
                                        </g:if>
                                        <g:else>
                                            <li><g:link controller="PvAdmin" target="_blank" class="mega-dd-font"><i class="md md-apps"></i>  <g:message code="app.label.pvadmin"/></g:link>
                                            </li>
                                        </g:else>
                                        </g:if>

                                    <div class="panel panel-default" style="margin-bottom: 2px;">
                                        <div role="tab" id="heading7">
                                            <div><a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseMenu7" aria-expanded="false" aria-controls="collapseMenu7" class="mega-dd-font">
                                                <i class="md md-help"></i> <g:message code="app.label.help"/></a></div>
                                        </div>
                                        <div id="collapseMenu7" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading7">

                                            <div class="mega-dd-font-item"><g:link url="${com.rxlogix.localization.HelpLink.getDefaultHelpLink()?:grailsApplication.config.getProperty('helpUrl','/help')}" target="_blank"><g:message code="app.label.help"/></g:link></div>
                                            <div class="mega-dd-font-item"><g:link controller="localizationHelpMessage" action="readReleaseNotes"><g:message code="app.label.releaseNotesItem.menu"/></g:link></div>
                                            <g:if test="${InteractiveHelp.fetchByPage(params.controller+"/"+params.action).count()}">
                                                <div class="mega-dd-font-item"><a style="cursor: pointer" class="runInteractiveHelp"><g:message code="app.label.interactiveHelp.interactiveHelp"/></a></div>
                                            </g:if>
                                        </div>
                                    </div>

                                </form>
                                <li><a href="#" data-evt-clk='{"method": "logoutRxSession", "params": []}' type="button" class="mega-dd-font"><i class="md md-exit-to-app"></i> <g:message code="app.label.logout"/>
                                </a></li>
                            </ul>

                        </li>

                    </sec:ifLoggedIn>
                </ul>
            </div>
        </div>
    </div>
</div>
