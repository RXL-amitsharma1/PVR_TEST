    <%@ page import="com.rxlogix.Constants; grails.util.Holders; com.rxlogix.config.Tenant; com.rxlogix.user.User; com.rxlogix.helper.LocaleHelper; com.rxlogix.config.Tag; com.rxlogix.user.UserRole; com.rxlogix.user.Role; org.joda.time.DateTimeZone;com.rxlogix.util.ViewHelper;com.rxlogix.enums.UserType" %>
    <g:set var="column1Width" value="4"/>
    <g:set var="column2Width" value="8"/>

    <g:if test="${actionName == 'create' || actionName == 'save'}">
        <g:set var="createMode" value="${true}"/>
    </g:if>

    <g:if test="${actionName == 'edit' || actionName == 'update'}">
        <g:set var="editMode" value="${true}"/>
    </g:if>

    <sec:ifAnyGranted roles="ROLE_DEV">
        <g:set var="sadmin" value="${true}"/>
    </sec:ifAnyGranted>

    <div class="row">

        %{--User Details--}%
        <div class="col-md-12">

            <h3 class="sectionHeader"><g:message code="user.details"/></h3>

            <div class="row">

                <div class="col-md-6">
                %{--Create Mode--}%
                    %{--<div class="form-group">--}%
                     %{--<g:if test="${sadmin}">--}%
                        %{--<label class="col-md-4 control-label" for="type">--}%
                            %{--<g:message code="user.userType.label"/><span class="required-indicator">*</span>--}%
                        %{--</label>--}%
                        %{--<g:if test="${createMode}">--}%

                                %{--<div class="col-md-8">--}%
                                %{--<g:select id="type" name="type"--}%
                                          %{--from="${UserType.values().findAll {--}%
                                                    %{--it.name--}%
                                          %{--}}"--}%
                                          %{--noSelection="${['': message(code: 'select.one')]}"--}%
                                          %{--value="${userInstance.type}"--}%
                                          %{--class="form-control type"/>--}%
                                %{--</div>--}%
                            %{--</g:if>--}%
                            %{--<g:else>--}%

                            %{--</g:else>--}%
                        %{--</g:if>--}%
                    %{--Edit Mode--}%
                        %{--<g:if test="${editMode}">--}%
                            %{--<div class="col-md-${column2Width}">--}%
                                %{--<g:textField id="type" name="type" class="form-control"--}%
                                             %{--value="${userInstance?.type}" disabled="disabled"/>--}%
                            %{--</div>--}%
                        %{--</g:if>--}%
                    %{--</div>--}%
                    <input type="hidden" id="type" name="type" class="form-control"
                           value="${UserType.LDAP}">

                    <div class="form-group ldapUser">
                        <g:if test="${createMode}">
                        <label class="col-md-4 control-label" for="username">
                            <g:message code="user.username.label"/><span class="required-indicator">*</span>
                        </label>
                            <div class="col-md-8">
                            <select name="username" id="username"
                                         value="${userInstance?.username}"
                                    class="form-control"></select>
                                <div class="bs-callout bs-callout-info">
                                    <h5><g:message code="search.for.ldap.user"/>:</h5>

                                    <div class="text-muted">- <g:message code="search.by.username.fullname.or.email"/></div>

                                    <div class="text-muted">- <g:message
                                            code="users.previously.created.do.not.appear.in.search.results"/></div>
                                </div>

                            </div>
                        </g:if>
                    %{--Edit Mode--}%
                    </div>

                    <g:if test="${editMode}">
                        <div class="form-group">
                            <label class="col-md-4 control-label" for="username">
                                <g:message code="user.username.label"/><span class="required-indicator">*</span>
                            </label>
                            <div class="col-md-8">
                            <g:textField name="anyNameHereWillDoAsLongAsItsNotUsername"
                                         value="${userInstance?.username}"
                                         placeholder="${message(code: 'user.username.label')}"
                                         disabled="disabled"
                                         class="form-control"/>
                            </div>
                        </div>
                    </g:if>



                        %{--<g:if test="${createMode}">--}%
                        %{--<div class="nonLdapUser">--}%
                            %{--<div class="form-group">--}%
                             %{--<label class="col-md-${column1Width} control-label" for="username"><g:message--}%
                                    %{--code="user.username.label"/></label>--}%

                                %{--<div class="col-md-${column2Width}">--}%
                                %{--<g:textField id='username' name="username"--}%
                                             %{--placeholder="${message(code: 'user.username.label')}"--}%
                                             %{--class="form-control" onInput="checklength(this)" />--}%
                                %{--</div>--}%
                            %{--</div>--}%

                            %{--<div class="form-group">--}%
                                    %{--<label class="col-md-${column1Width} control-label" for="email"><g:message--}%
                                            %{--code="user.email.label"/></label>--}%

                                %{--<div class="col-md-${column2Width}">--}%
                                        %{--<g:textField id='email' name="email"--}%
                                                     %{--value="${userInstance?.email}"--}%
                                                     %{--placeholder="${message(code: 'user.email.label')}"--}%
                                                     %{--class="form-control"/>--}%
                                %{--</div>--}%

                            %{--</div>--}%

                            %{--<div class="form-group">--}%
                                    %{--<label class="col-md-${column1Width} control-label" for="fullName"><g:message--}%
                                            %{--code="user.fullName.label"/></label>--}%

                                %{--<div class="col-md-${column2Width}">--}%
                                        %{--<g:textField id='fullName' name="fullName"--}%
                                                     %{--value="${userInstance?.fullName}"--}%
                                                     %{--placeholder="${message(code: 'user.fullName.label')}"--}%
                                                     %{--class="form-control"/>--}%
                                %{--</div>--}%
                            %{--</div>--}%
                %{--</div>--}%
                        %{--</g:if>--}%



                %{--Edit Mode--}%
                    <g:if test="${editMode}">
                        <div class="form-group">
                            <label class="col-md-${column1Width} control-label" for="fullName">
                                <g:message code="user.fullName.label"/>
                            </label>

                            <div class="col-md-${column2Width}">
                                <g:textField name="fullName"
                                             value="${userInstance?.fullName}"
                                             placeholder="${message(code: 'user.fullName.label')}"
                                             disabled="disabled"
                                             class="form-control"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="col-md-${column1Width} control-label" for="email"><g:message
                                    code="user.email.label"/></label>

                            <div class="col-md-${column2Width}">
                                <g:textField name="email"
                                             value="${userInstance?.email}"
                                             placeholder="${message(code: 'user.email.label')}"
                                             disabled="disabled"
                                             class="form-control"/>
                            </div>
                        </div>

                    </g:if>

                    <g:if test="${Holders.config.get('pvreports.multiTenancy.enabled')}">
                        <div class="form-group">
                            <label class="col-md-${column1Width} control-label" for="tenantsSelectBox"><g:message
                                    code="user.tenants.label"/><span class="required-indicator">*</span></label>
                            <div class="col-md-${column2Width}">
    %{--                            Actual values are submitting from header javascript code--}%
                                <g:hiddenField id="tenantsSelectBox" name="tenantsValueString" class="form-control"
                                               value="${userInstance?.tenants*.id?.join(com.rxlogix.Constants.MULTIPLE_AJAX_SEPARATOR)}"/>
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <g:hiddenField name="tenantsValueString"
                                       value="${userInstance?.tenants*.id?.join(com.rxlogix.Constants.MULTIPLE_AJAX_SEPARATOR) ?: Holders.config.get('pvreports.multiTenancy.defaultTenant')}"/>
                    </g:else>
                </div>


                <div class="col-md-6">

                <div class="form-group" style="padding-bottom: 8px">
                    <label class="col-md-${column1Width} control-label" for="enabled">
                        <g:message code="user.enabled.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <label class="pv-switch">
                            <input id="enabled"
                                   name="enabled"
                                   type="checkbox"
                                   class="form-element"
                                   data-value="${userInstance.enabled}"
                                   data-on="${message(code: "default.button.yes.label")}"
                                   data-off="${message(code: "default.button.no.label")}"
                            />
                            <span class="switch-slider" data-on="${message(code: "default.button.yes.label")}" data-off="${message(code: "default.button.no.label")}"></span>
                        </label>
                    </div>
                </div>

                <div class="form-group" style="padding-bottom: 8px">
                    <label class="col-md-${column1Width} control-label" for="accountLocked">
                        <g:message code="user.accountLocked.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <label class="pv-switch">
                            <input id="accountLocked"
                                   name="accountLocked"
                                   type="checkbox"
                                   class="form-element"
                                   data-value="${userInstance.accountLocked}"
                                   data-on="${message(code: "default.button.yes.label")}"
                                   data-off="${message(code: "default.button.no.label")}"
                            />
                            <span class="switch-slider" data-on="${message(code: "default.button.yes.label")}" data-off="${message(code: "default.button.no.label")}"></span>
                        </label>
                    </div>
                </div>

                <div class="form-group" style="padding-bottom: 8px">
                    <label class="col-md-${column1Width} control-label">
                        <g:message code="user.badPasswordAttempts.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        ${userInstance.badPasswordAttempts}
                    </div>
                </div>

                <div class="form-group" style="padding-bottom: 8px">
                    <label class="col-md-${column1Width} control-label" for="accountExpired">
                        <g:message code="user.accountExpired.label"/>
                    </label>

                    <div class="col-md-${column2Width}">
                        <label class="pv-switch">
                            <input id="accountExpired"
                                   name="accountExpired"
                                   type="checkbox"
                                   class="form-element"
                                   data-value="${userInstance.accountExpired}"
                                   data-on="${message(code: "default.button.yes.label")}"
                                   data-off="${message(code: "default.button.no.label")}"
                            />
                            <span class="switch-slider" data-on="${message(code: "default.button.yes.label")}" data-off="${message(code: "default.button.no.label")}"></span>
                        </label>
                    </div>
                </div>

                </div>

               </div>
            </div>

        %{--Roles--}%
        <div class="col-md-12">

            <h3 class="sectionHeader"><g:message code="roles.label"/></h3>

            <div class="row">
                <div class="col-md-7">

                %{--Create Mode--}%
                    <g:if test="${actionName == 'create' || actionName == 'save'}">

                        <g:each var="role" in="${Role.list().sort { it.authority }}" status="i">
                            <div class="row">
                                <div class="col-md-${column1Width}">
                                    <g:message code="app.role.${role.authority}" default="${role.authority}"/>
                                </div>

                                <div class="col-md-${column1Width}">
                                    <label class="pv-switch">
                                        <input  type="checkbox"
                                                class="form-element"
                                                data-value="${userInstance?.roles?.contains(role.authority)}"
                                                name="${role.authority}"
                                                id="${role.authority}"
                                                data-on="${message(code: "default.button.yes.label")}"
                                                data-off="${message(code: "default.button.no.label")}"
                                        />
                                        <span class="switch-slider" data-on="${message(code: "default.button.yes.label")}" data-off="${message(code: "default.button.no.label")}"></span>
                                    </label>
                                </div>
                            </div>
                        </g:each>

                    </g:if>

                %{--Edit Mode--}%
                    <g:if test="${actionName == 'edit' || actionName == 'update'}">
                        <g:each var="entry" in="${roleMap}">

                            <div class="row">
                                <div class="col-md-${column1Width}">
                                    <g:message code="app.role.${entry.key.authority}" default="${entry.key.authority}"/>
                                </div>

                                <div class="col-md-${column1Width}">
                                    <label class="pv-switch">
                                        <input name="${entry.key.authority}"
                                               id="${entry.key.authority}"
                                               type="checkbox"
                                               class="form-element"
                                               data-value="${entry.value}"
                                               data-on="${message(code: "default.button.yes.label")}"
                                               data-off="${message(code: "default.button.no.label")}"
                                        />
                                        <span class="switch-slider" data-on="${message(code: "default.button.yes.label")}" data-off="${message(code: "default.button.no.label")}"></span>
                                    </label>
                                </div>
                            </div>
                        </g:each>
                    </g:if>

                </div>
            </div>

        </div>

        %{--Preferences--}%
        <div class="col-md-6">

            <h3 class="sectionHeader"><g:message code="app.label.preference"/></h3>

            <div class="form-group">
                <label class="col-md-${column1Width} control-label" for="email"><g:message
                        code="app.label.language"/></label>

                <div class="col-md-${column2Width}">
                    <g:select id="locale" class="form-control" name="preference.locale"
                              from="${LocaleHelper.buildLocaleSelectList()}"
                              value="${userInstance.preference.locale}" optionKey="lang_code" optionValue="display"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-md-${column1Width} control-label" for="email"><g:message
                        code="app.label.timezone"/></label>

                <div class="col-md-${column2Width}">
                    <g:select id="timeZone"
                              name="preference.timeZone"
                              from="${ViewHelper.getTimezoneValues()}"
                              optionKey="name"
                              optionValue="display"
                              class="form-control"
                              value="${userInstance.preference.timeZone}"/>
                </div>
            </div>
        </div>


        <div class="col-md-12">
            <h3 class="sectionHeader"><g:message code="app.label.api.token"/></h3>

            <div class="form-group">
                <label class="col-md-2 control-label" for="email"><g:message
                        code="app.label.api.token"/></label>

                <div class="col-md-6">
                    <g:textField id='api-token-field' name="apiToken"
                                 value="${userInstance?.apiToken}"
                                 placeholder="${message(code: 'app.label.api.token')}"
                                 class="form-control"/>

                </div>

                <div class="col-md-2">
                    <a id='token-gen-bt' href="#" class="btn btn-inverse">${message(code: "app.label.generate.api.token")}</a>
                </div>
            </div>
        </div>

    </div>
    %{--<script>--}%
        %{--var isCreateMode = "${createMode}";--}%
        %{--function checklength(element){--}%
            %{--var fieldLength = element.value.length;--}%
            %{--if(fieldLength <= 100){--}%
                %{--return true;--}%
            %{--}--}%
            %{--else--}%
            %{--{--}%
                %{--var str = element.value;--}%
                %{--str = str.substring(0, str.length - 1);--}%
                %{--element.value = str;--}%
            %{--}--}%
        %{--}--}%
    %{--</script>--}%

