<g:set var="dashboardService" bean="dashboardService"/>
    <div class="left side-menu">
    <div class="sidebar-inner slimscrollleft">
        <div id="sidebar-menu">
            <ul>
                <li class="has_sub"><g:link controller="quality" action="index" class="waves-effect waves-primary"><i
                        class="md md-dashboard"></i><span style="overflow-wrap: anywhere"><g:message code="app.label.dashBoard" /></span><span class="menu-arrow"></span></g:link>
                    <ul class="list-unstyled">
                        <g:each in="${dashboardService.listPvqDashboards()}" var="dashboard">
                            <li>
                                <g:renderNameLinkInList label="${dashboard.label}" controller="quality" action="index" params="[id: dashboard.id]"/>
                            </li>
                        </g:each>
                        <li><g:link controller="quality" action="newDashboard"><i class="fa fa-plus"></i><g:message code="app.label.dashboard.new"/></g:link></li>
                    </ul>
                </li>
                <g:each in="${grailsApplication.config.pv.app.settings["PVQuality"]}" var="setting" status="i">
                    <sec:ifAnyGranted roles="${setting.role}">
                        <g:if test="${setting.caseDynamic}">
                            %{--Logic inside if condition is executed when caseDynamic is set to true and CaseEntry templates list is set dynamically--}%
                            <li class="has_sub">
                                <pvi:customNewCaseMenu values="${setting}" locale="${(userLocale?:Locale.default)}"/>
                            </li>
                        </g:if>
                        <g:else>
                            <li class="has_sub">
                                <a href="${createMenuLink(link:  setting.link)}" class="waves-effect waves-primary side-icon ${setting.children?'has-sub':''}">
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
                                                    <li>
                                                        <a href="${createMenuLink(link:  child.link)}" class="waves-effect waves-primary side-icon  ${child.customclass?:''}">
                                                            <span>${g.message(code: child.name)}</span>
                                                        </a>
                                                    </li>
                                                </sec:ifAnyGranted>
                                            </g:each>
                                        </ul>
                                </g:if>
                            </li>
                        </g:else>
                    </sec:ifAnyGranted>
                </g:each>
                <g:if test="${grailsApplication.config.applications.current == 'PVAdmin'}">
                    <g:if test="${grailsApplication.config.applications.installed.app.size() > 1}">
                        <li class="has_sub hidden">
                            <a href="#" class="waves-effect waves-primary active side-icon">
                                <i class="md md-apps" aria-hidden="true"></i>
                                <span style="overflow-wrap: anywhere">${g.message(code: 'application.name')}</span>
                                <span class="menu-arrow"></span>
                            </a>
                            <ul class="list-unstyled" style="width:203px;">
                                <g:each in="${grailsApplication.config.applications.installed.app}" var="app" status="i">
                                    <li class="has_sub">
                                        <a href="#" class="waves-effect waves-primary active side-icon">
                                            <span style="overflow-wrap: anywhere">${app}</span>
                                            <span class="menu-arrow"></span>
                                        </a>
                                        <ul class="list-unstyled">
                                            <g:each in="${grailsApplication.config.pv.admin.app.specific.settings[app]}" var="setting"
            status="j">
                                                <li>
                                                    <a href="${setting.link}" class="waves-effect waves-primary side-icon">
                                                        <span>${g.message(code: setting.name, locale:(userLocale?:Locale.default))}</span>
                                                    </a>
                                                </li>
                                            </g:each>
                                        </ul>
                                    </li>
                                </g:each>
                            </ul>
                        </li>
                </g:if>
                <g:else>
                    <g:each in="${grailsApplication.config.applications.installed.app}" var="app" status="i">
                        <ul>
                            <li class="has_sub hidden">
                                <a href="#" class="waves-effect waves-primary side-icon">
                                    <i class="md md-apps" aria-hidden="true"></i>
                                    <span style="overflow-wrap: anywhere">${app}</span>
                                    <span class="menu-arrow"></span>
                                </a>
                                <ul class="list-unstyled">
                                    <g:each in="${grailsApplication.config.pv.admin.app.specific.settings[app]}" var="setting"
            status="j">
                                        <li>
                                            <a href="${setting.link}" class="waves-effect waves-primary side-icon">
                                                <span>${g.message(code: setting.name, locale:(userLocale?:Locale.default))}</span>
                                            </a>
                                        </li>
                                    </g:each>
                                </ul>
                            </li>
                        </g:each>
                    </g:else>
                </g:if>
            </ul>
        </div>

        <div class="clearfix"></div>
    </div>
</div>
