<%@ page import="grails.util.Holders; com.rxlogix.user.UserGroup; com.rxlogix.user.UserGroupUser; com.rxlogix.enums.TimeZoneEnum;com.rxlogix.util.ViewHelper; com.rxlogix.user.Role; com.rxlogix.helper.LocaleHelper" %>

<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<div>
    <g:form name="userSearchForm" action="search" class="form-horizontal" method="post">

        <div class="row">

            <div class="col-md-4">

                <div class="form-group" style="white-space: nowrap">
                    <label class="col-md-${column1Width} control-label" for="usernameFullname">
                        <g:message code="user.usernameFullname.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:textField id="usernameFullname" name="usernameFullname" class="form-control"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-md-${column1Width} control-label" for="email">
                        <g:message code="user.email.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:textField id="email" name="email" class="form-control"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-md-${column1Width} control-label" for="enabled">
                        <g:message code="user.enabled.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:select id="enabled" name="enabled"
                                  from="${[['key': 'Yes', 'display': message(code: "app.label.yes")], ['key': 'No', 'display': message(code: "app.label.no")]]}"
                                  noSelection="${['': message(code: 'select.one')]}"
                                  value="Yes"
                                  optionKey="key"
                                  optionValue="display"
                                  class="form-control"/>
                    </div>
                </div>

            </div>

            <div class="col-md-4">

                <div class="form-group">
                    <label class="col-md-${column1Width} control-label" for="roles">
                        <g:message code="role.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:select id="roles" name="roles"
                                  from="${Role.findAll().sort()}"
                                  optionKey="id"
                                  optionValue="i18nAuthority"
                                  multiple="true"
                                  class="form-control"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-md-${column1Width} control-label" for="userGroups">
                        <g:message code="user.group.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:select id="userGroups" name="userGroups"
                                  from="${UserGroup.findAllByIsDeleted(false).sort()}"
                                  optionKey="id"
                                  optionValue="name"
                                  multiple="true"
                                  class="form-control"/>
                    </div>
                </div>
            </div>

            <div class="col-md-4">
                <div class="form-group">
                    <label class="col-md-${column1Width} control-label" for="locale">
                        <g:message code="app.label.language"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:select id="locale"
                                  name="locale"
                                  from="${LocaleHelper.buildLocaleSelectList()}"
                                  optionKey="lang_code"
                                  optionValue="display"
                                  noSelection="${['': message(code: 'select.one')]}"
                                  class="form-control"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-md-${column1Width} control-label" for="timeZone">
                        <g:message code="app.label.timezone"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <g:select id="timeZone"
                                  name="timeZone"
                                  from="${ViewHelper.getTimezoneValues()}"
                                  optionKey="name"
                                  optionValue="display"
                                  noSelection="${['': message(code: 'select.one')]}"
                                  class="form-control"/>
                    </div>
                </div>

            </div>
        </div>
    </g:form>
    <div class="margin20Top">
        <button id="userSearchButton" class="btn btn-primary">
            <span class="glyphicon glyphicon-search icon-white"></span>
            <g:message code="default.button.search.label" />
        </button>
        <g:if test="${Holders.config.getProperty('grails.plugin.springsecurity.ldap.active', Boolean)}">
            <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["user", "create"]}' id="createButton">
                <span class="glyphicon glyphicon-plus icon-white"></span>
                <g:message code="default.new.label" args="[entityName]"/>
            </button>
        </g:if>

    </div>

</div>
