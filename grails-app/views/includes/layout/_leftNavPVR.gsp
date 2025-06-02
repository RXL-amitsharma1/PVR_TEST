<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.user.User" %>
<g:set var="dashboardService" bean="dashboardService"/>

<div class="left side-menu">
    <div class="sidebar-inner slimscrollleft">
        <div id="sidebar-menu">
            <ul>
                <li class="has_sub"><g:link controller="dashboard" action="index" class="waves-effect waves-primary" params="[module:'pvr']"><i
                        class="md md-dashboard"></i><span style="overflow-wrap: anywhere"><g:message code="app.label.dashBoard" /></span><span class="menu-arrow"></span></g:link>
                    <ul class="list-unstyled">
                        <g:each in="${dashboardService.listPvrDashboards()}" var="dashboard">
                            <li>
                                <g:renderNameLinkInList label="${dashboard.label}" controller="dashboard" action="index" params="[id: dashboard.id, module: 'pvr']"/>
                            </li>
                        </g:each>
                        <li><g:link controller="dashboard" action="newDashboard"  params="[module:'pvr']"><i class="fa fa-plus"></i><g:message code="app.label.dashboard.new"/></g:link></li>
                    </ul>
                </li>
                <g:each in="${grailsApplication.config.pv.app.settings["PVReports"]}" var="setting" status="i">
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
                                                    <a href="${createMenuLink(link:  child.link)}${child.link?.indexOf("?")>-1?"&":"?"}module=pvr" class="waves-effect waves-primary side-icon ${child.customclass?:''}">
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
            </ul>
        </div>

        <div class="clearfix"></div>
    </div>
</div>
