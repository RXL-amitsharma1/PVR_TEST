<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.user.User" %>
<g:set var="dashboardService" bean="dashboardService"/>
<g:set var="mainDashboard" value="${dashboardService.mainPvcDashboard()}"/>
<div class="left side-menu">
    <div class="sidebar-inner slimscrollleft">
        <div id="sidebar-menu">
            <ul>
                <li class="has_sub"><g:link controller="central" action="index" class="waves-effect waves-primary"><i
                        class="md md-dashboard"></i><span style="overflow-wrap: anywhere"><g:message code="app.label.dashBoard" /></span><span class="menu-arrow"></span></g:link>
                    <ul class="list-unstyled">
                        <g:each in="${dashboardService.listPvcDashboardsForId(mainDashboard.id, true)}" var="dashboard">
                            <li>
                                <g:renderNameLinkInList label="${dashboard.label}" controller="central" action="index" params="[id: dashboard.id]"/>
                            </li>
                        </g:each>
                        <li><g:link controller="central" action="newDashboard" params="[id:mainDashboard.id]"><i class="fa fa-plus"></i><g:message code="app.label.dashboard.new"/></g:link></li>
                    </ul>
                </li>
                <g:each in="${dashboardService.listPvcDashboards()}" var="dashboard">
                    <li class="has_sub">
                        <a href="${createMenuLink(link: "/central/index/${dashboard.id}")}" class="waves-effect waves-primary">
                            <g:if test="${dashboard.icon!=null}">
                                <i class="${dashboard.icon}"></i>
                            </g:if>
                            <g:else><i class="md md-dashboard"></i></g:else>
                            <span style="overflow-wrap: anywhere">${dashboard.label}</span>
                            <span class="menu-arrow"></span>
                        </a>
                        <ul class="list-unstyled">
                            <g:each in="${dashboardService.listPvcDashboardsForId(dashboard.id)}"  var="subDashboard">
                                <li>
                                    <g:renderNameLinkInList label="${subDashboard.label}" controller="central" action="index" params="[id: subDashboard.id]"/>
                                </li>
                            </g:each>
                            <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                            <li><g:link controller="central" action="newDashboard" params="[id:dashboard.id]"><i class="fa fa-plus"></i><g:message code="app.label.dashboard.new"/></g:link></li>
                            </sec:ifAnyGranted>
                        </ul>
                    </li>
                </g:each>
                <sec:ifAnyGranted roles="ROLE_CONFIGURATION_VIEW, ROLE_COGNOS_VIEW">
                    <li class="has_sub">
                        <!-- Adhoc Reports -->
                        <a href="javascript:void(0);" class="waves-effect waves-primary">
                            <i class="md md-grid"></i> <span style="overflow-wrap: anywhere"><g:message code="app.label.adhoc.report"/></span>
                            <span class="menu-arrow"></span></a>
                        <ul class="list-unstyled">
                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_VIEW">
                                <!-- Library -->
                                <li><g:link controller="configuration" action="index"><g:message
                                        code="app.viewReports.menu"/></g:link></li>
                            </sec:ifAnyGranted>
                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                                <li><a href="#" class="createAdhocFromTemplate"><g:message
                                        code="app.menu.createFromTemplate"/></a></li>
                            </sec:ifAnyGranted>
                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                                <!-- New Report -->
                                <li><g:link controller="configuration" action="create"><g:message
                                        code="app.newReport.menu"/></g:link></li>
                            </sec:ifAnyGranted>
                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_VIEW">
                                <!-- Generated Reports -->
                                <li><g:link controller="report" action="index"><g:message
                                        code="app.label.generated.reports"/></g:link></li>
                            </sec:ifAnyGranted>
                        </ul>
                    </li>
                </sec:ifAnyGranted>
                <g:each in="${grailsApplication.config.pv.app.settings["PVReports"].findAll { it.name == 'app.label.templates' || it.name == 'app.label.queries' }}" var="setting" status="i">
                    <sec:ifAnyGranted roles="${setting.role}">
                        <g:if test="${!setting.hide}">
                            <li class="has_sub">
                                <a href="${createMenuLink(link: setting.link)}" class="waves-effect waves-primary side-icon ${setting.children?'has-sub':''}">
                                    <i class="${setting.icon}" aria-hidden="true"></i>
                                    <span style="overflow-wrap: anywhere">${g.message(code: setting.name)}</span>
                                    <g:if test="${setting.children}">
                                        <span class="menu-arrow"></span>
                                    </g:if>
                                </a>
                                <g:if test="${setting.children}">
                                    <ul class="list-unstyled">
                                        <g:each in="${setting.children}" var="child" status="j">
                                            <sec:ifAnyGranted roles="${child.role}">
                                                <g:if test="${!child.hide}">
                                                    <li>
                                                        <a href="${createMenuLink(link:  child.link)}${child.link?.indexOf("?")>-1?"&":"?"}module=pvc" class="waves-effect waves-primary side-icon ${child.customclass?:''}">
                                                            <span>${g.message(code: child.name)}</span>
                                                        </a>
                                                    </li>
                                                </g:if>
                                            </sec:ifAnyGranted>
                                        </g:each>
                                    </ul>
                                </g:if>
                            </li>
                        </g:if>
                    </sec:ifAnyGranted>
                </g:each>
                <sec:ifAnyGranted roles="ROLE_PVC_VIEW">
                    <li class="has_sub">
                        <!-- Issue Management  -->
                        <a href="javascript:void(0);" class="waves-effect waves-primary">
                            <i class="md md-calendar"></i> <span style="overflow-wrap: anywhere"><g:message code="app.label.quality.issue.management"/></span>
                            <span class="menu-arrow"></span></a>
                        <ul class="list-unstyled">
                            <li><g:link controller="central" action="actionPlan"><g:message
                                    code="app.actionPlan.actionPlan"/></g:link></li>
                            <sec:ifAnyGranted roles="ROLE_PVC_EDIT">
                                <li><g:link controller="pvcIssue" action="create"><g:message
                                        code="app.label.quality.create.report.issue"/></g:link></li>
                            </sec:ifAnyGranted>
                            <li><g:link controller="pvcIssue" action="index"><g:message
                                    code="app.label.quality.libraries.issue"/></g:link></li>
                        </ul>
                    </li>
                </sec:ifAnyGranted>

                <sec:ifAnyGranted roles="ROLE_PVC_INBOUND_VIEW">
                    <li class="has_sub">
                        <!-- Issue Management  -->
                        <a href="javascript:void(0);" class="waves-effect waves-primary">
                            <i class="md md-domain"></i> <span style="overflow-wrap: anywhere"><g:message code="app.label.pvc.inbound.compliance"/></span>
                            <span class="menu-arrow"></span></a>
                        <ul class="list-unstyled">
%{--                            <li><g:link controller="inboundCompliance" action="index"><g:message--}%
%{--                                    code="app.label.pvc.inbound.compliance.overview"/></g:link></li>--}%
                            <sec:ifAnyGranted roles="ROLE_PVC_INBOUND_EDIT">
                                <li><g:link controller="inboundCompliance" action="create"><g:message
                                        code="app.label.pvc.inbound.compliance.configuration"/></g:link></li>
                            </sec:ifAnyGranted>
                            <li><g:link controller="inboundCompliance" action="list"><g:message
                                    code="app.label.pvc.inbound.configuration.library"/></g:link></li>
                            <li><g:link controller="executedInbound" action="index"><g:message
                                    code="app.label.generated.reports"/></g:link></li>
                        </ul>
                    </li>
                </sec:ifAnyGranted>

                <sec:ifAnyGranted roles="ROLE_ACTION_ITEM">
                    <li class="has_sub">
                        <!-- Issue Management  -->
                        <a href="javascript:void(0);" class="waves-effect waves-primary">
                            <i class="md md-clipboard-text"></i> <span style="overflow-wrap: anywhere"><g:message code="app.label.tasks"/></span>
                            <span class="menu-arrow"></span></a>
                        <ul class="list-unstyled">
                            <li><g:link controller="actionItem" action="index"><g:message
                                    code="app.label.topnav.action.items"/></g:link></li>
                        </ul>
                    </li>
                </sec:ifAnyGranted>

                <sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
                    <li class="has_sub"><g:link controller="central" action="newDashboard" params="[id:0]" class="waves-effect waves-primary"><i class="fa fa-plus"></i><span style="overflow-wrap: anywhere"><g:message code="app.label.dashboard.new"/></span><span class="menu-arrow"></span></g:link></li>
                </sec:ifAnyGranted>
            </ul>
        </div>
        <div class="clearfix"></div>
    </div>
</div>
<g:render template="/includes/layout/templateModal"  model="[:]"/>
