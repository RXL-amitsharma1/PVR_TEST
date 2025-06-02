<g:set var="dashboardService" bean="dashboardService"/>
<div class="left side-menu">
    <div class="sidebar-inner slimscrollleft">
        <div id="sidebar-menu">
            <ul>
                <li><g:link controller="pvp" action="index" class="waves-effect waves-primary">
                        <i class="md md-dashboard"></i><span style="overflow-wrap: anywhere">Dashboard</span>
                    </g:link>
                </li>
                 <sec:ifAnyGranted roles="ROLE_PERIODIC_CONFIGURATION_VIEW">
                <li class="has_sub">
                    <a href="javascript:void(0);" class="waves-effect waves-primary">
                        <i class="fa fa-list-alt"></i><span style="overflow-wrap: anywhere"><g:message code="app.label.reports"/></span><span class="menu-arrow"></span>
                    </a>
                        <ul class="list-unstyled">
                            <sec:ifAnyGranted roles="ROLE_PERIODIC_CONFIGURATION_CRUD">
                                <li><g:link controller="periodicReport" action="create" params="[pvp:true]"><g:message code="app.newReport.menu"/></g:link></li>
                            </sec:ifAnyGranted>
                            <li><g:link controller="periodicReport" action="index" params="[pvp:true]"><g:message code="app.label.library"/></g:link></li>
                            <li><g:link controller="pvp" action="reports" ><g:message code="app.label.generated.reports"/></g:link></li>
                            <g:if test="${grailsApplication.config.pv.app.pvpublisher.gantt.enabled}">
                                <li><g:link controller="gantt" action="gantt" params="[module:'pvp']"><g:message code="app.label.submissionScheduler.title"/></g:link></li>
                            </g:if>
                            <li><g:link controller="reportSubmission" action="index" params="[pvp:true]"><g:message code="app.viewReportsSubmission.menu"/></g:link></li>
                            <li><g:link controller="periodicReport" action="bulkUpdate" params="[module:'pvp']"><g:message code="app.menu.bulkImportConfiguration" default="Bulk Update"/></g:link></li>
                        </ul>
                    </li>
                </sec:ifAnyGranted>
                <li class="has_sub">
                    <a href="javascript:void(0);" class="waves-effect waves-primary">
                        <i class="fa fa-columns"></i><span style="overflow-wrap: anywhere"><g:message code="app.label.PublisherTemplate.mainMenu"/></span><span class="menu-arrow"></span>
                    </a>
                    <ul class="list-unstyled">
                        <li><g:link controller="publisherTemplate" action="index" ><g:message code="app.label.PublisherTemplate.templateLeftMenu"/></g:link></li>
                        <g:if test="${grailsApplication.config.pv.app.pvpublisher.gantt.enabled}">
                            <li><g:link controller="gantt" action="index" params="[pvp: true]" ><g:message code="app.label.gantt.menu"/></g:link></li>
                        </g:if>
                        <sec:ifAnyGranted roles="ROLE_TEMPLATE_LIBRARY_ACCESS">
                            <li><g:link controller="periodicReport" action="sources" ><g:message code="app.label.publisher.sources"/></g:link></li>
                        </sec:ifAnyGranted>
                        <li><g:link controller="publisherCommonParameter" action="index" ><g:message code="app.label.PublisherCommonParameter.menuItem"/></g:link></li>
                    </ul>
                </li>
                <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_VIEW, ROLE_PERIODIC_CONFIGURATION_VIEW">
                    <li class="has_sub">
                        <!-- Tasks -->
                        <a href="javascript:void(0);" class="waves-effect waves-primary"><i
                                class="md md-clipboard-text"></i><span style="overflow-wrap: anywhere"> <g:message code="app.label.tasks"/> </span> <span
                                class="menu-arrow"></span></a>
                        <ul class="list-unstyled">
                        <!-- Report Requests -->
                            <sec:ifAnyGranted roles="ROLE_REPORT_REQUEST_VIEW">
                                <li><g:link controller="reportRequest" action="index"><g:message code="app.label.topnav.report.request" /></g:link></li>
                                <li><g:link controller="reportRequest" action="plan" params="[pvp:true]"><g:message code="app.label.topnav.report.request.plan" /></g:link></li>
                            </sec:ifAnyGranted>
                            <sec:ifAnyGranted roles="ROLE_PERIODIC_CONFIGURATION_VIEW">
                                <li><g:link controller="calendar" action="reports"><g:message code="app.aggregate.report.calendar" /></g:link></li>
                            </sec:ifAnyGranted>
                        </ul>
                    </li>
                </sec:ifAnyGranted>
            </ul>
        </div>
        <div class="clearfix"></div>
    </div>
</div>
