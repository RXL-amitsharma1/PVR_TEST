<%@ page import="com.rxlogix.enums.UnitTypeEnum; com.rxlogix.util.ViewHelper;com.rxlogix.config.UnitConfiguration;com.rxlogix.Constants; com.rxlogix.util.DateUtil" %>
<g:set var="userService" bean="userService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>
<asset:javascript src="app/scheduler.js"/>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner basicInformation" style="margin-bottom: 5px">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.icsr.profile.conf.basic.information"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row form-group">
                <div class="col-md-2">
                    <label for="unitName"><g:message
                            code="app.unitConfiguration.label.unitName"/></label><span class="required-indicator">*</span>
                    <g:textField name="unitName" id="unitName" value="${unitConfigurationInstance?.unitName}"
                                 class="form-control" maxlength="255"/>
                </div>

                <div class="col-md-2">
                    <label for="unitType"><g:message
                            code="app.unitConfiguration.label.unitType"/></label><span class="required-indicator">*</span>
                    <g:select name="unitType" id='unitType' from="${ViewHelper.getUnitTypeEnumI18n()}"
                              class="form-control select2-box" optionKey="name" optionValue="display"
                              noSelection="['': message(code: 'select.one')]" value="${unitConfigurationInstance?.unitType}"
                              data-evt-change='{"method": "onchangeUnitType", "params": []}' />
                </div>

                <div class="col-md-2">
                    <label for=unitOrganizationName"><g:message
                            code="app.unitConfiguration.label.organizationName"/></label>
                    <g:textField name="unitOrganizationName" id="unitOrganizationName" value="${unitConfigurationInstance?.unitOrganizationName}" class="form-control" maxlength="100"/>
                </div>

                <div class="col-md-2">
                    <label for="organizationType.id"><g:message
                            code="app.unitConfiguration.label.organizationType"/></label>
                    <span class="required-indicator">*</span>
                    <g:select name="organizationType.id"
                              from="${icsrOrganizationTypeList}"
                              optionKey="id" optionValue="name"
                              noSelection="['':  message(code: 'select.one')]"
                              value="${ViewHelper.getOrganizationTypeIdByPreference(unitConfigurationInstance?.organizationType?.id)}"
                              class="form-control select2-box"/>
                </div>

                <div class="col-md-2">
                    <label for="organizationCountry"><g:message
                            code="app.unitConfiguration.label.organizationCountry"/></label>
                    <g:select name="organizationCountry" id="organizationCountry" from="${organizationCountry}"
                              class="form-control select2-box"
                              noSelection="['':  message(code: 'select.one')]" value="${ViewHelper.getOrganizationCountryNameByPreference(unitConfigurationInstance?.organizationCountry)}"/>
                </div>

            </div>

            <div class="row form-group">
                <div class="col-md-2">
                    <label for="address1"><g:message code="app.unitConfiguration.label.address1"/></label>
                    <g:textArea name="address1" id="address1" value="${unitConfigurationInstance?.address1}"
                                class="form-control" maxlength="${UnitConfiguration.constrainedProperties.address1.maxSize}"
                                style="height: 95px;"/>
                </div>

                <div class="col-md-2">
                    <label for="address2"><g:message code="app.unitConfiguration.label.address2"/></label>
                    <g:textArea name="address2" id="address2" value="${unitConfigurationInstance?.address2}"
                                class="form-control" maxlength="${UnitConfiguration.constrainedProperties.address2.maxSize}"
                                style="height: 95px;"/>
                </div>

                <div class="col-md-2">
                    <label for="preferredLanguage"><g:message
                            code="app.unitConfiguration.label.preferredLanguage"/></label>
                    <span class="required-indicator">*</span>
                    <g:select name="preferredLanguage" from="${ViewHelper.preferredLanguageList()}"
                              optionValue="display" optionKey="name"
                              noSelection="['':  message(code: 'select.one')]"
                              value="${unitConfigurationInstance?.preferredLanguage}"
                              class="form-control select2-box"/>
                </div>

                <div class="col-md-2">
                    <label for="city"><g:message code="app.unitConfiguration.label.city"/></label>
                    <g:textField name="city" id="city" value="${unitConfigurationInstance?.city}"
                                 maxlength="${UnitConfiguration.constrainedProperties.city.maxSize}" class="form-control"/>
                </div>

                <div class="col-md-2">
                    <label for="state"><g:message code="app.unitConfiguration.label.state"/></label>
                    <g:textField name="state" id="state" value="${unitConfigurationInstance?.state}" class="form-control" maxlength="255"/>
                </div>

                <div>&nbsp;</div>
                <div class="col-md-2">
                    <label for="postalCode"><g:message code="app.unitConfiguration.label.postalCode"/></label>
                    <g:textField name="postalCode" id="postalCode" value="${unitConfigurationInstance?.postalCode}"
                                 class="form-control" maxlength="255"/>
                </div>

                <div class="col-md-2">
                    <label for="postalCode"><g:message code="app.unitConfiguration.label.postalCodeExt"/></label>
                    <g:textField name="postalCodeExt" id="postalCodeExt" value="${unitConfigurationInstance?.postalCodeExt}"
                                 class="form-control" maxlength="255"/>
                </div>

                <div class="col-md-2">
                    <label for="phone"><g:message code="app.unitConfiguration.label.phone"/></label>
                    <g:textField name="phone" id="phone" value="${unitConfigurationInstance?.phone}" class="form-control" maxlength="255"/>
                </div>

            </div>

            <div class="row form-group" style="display: block">
                <div class="col-md-2">
                    <label for="email"><g:message code="app.unitConfiguration.label.email"/></label>
                    <input type="email" name="email" id="email" value="${unitConfigurationInstance?.email}" class="form-control" maxlength="200"/>
                </div>

                <div class="col-md-2" style="display: block">

                    <label for="emailTemplate"><g:message
                            code="app.label.emailTemplate.appName"/></label>
                    <g:select name="emailTemplate" id='emailTemplate' from="${com.rxlogix.config.EmailTemplate.findAllByType(com.rxlogix.enums.EmailTemplateTypeEnum.PUBLIC)}"
                              class="form-control select2-box" optionKey="id" optionValue="name"
                              noSelection="['': message(code: 'select.one')]" value="${unitConfigurationInstance?.emailTemplateId}" disabled="true"/>
                </div>

                <div class="col-md-2">
                    <label for="fax"><g:message code="app.unitConfiguration.label.fax"/></label>
                    <g:textField name="fax" id="fax" value="${unitConfigurationInstance?.fax}" class="form-control " maxlength="255"/>
                </div>

                %{--            <div class="col-md-2">--}%
                %{--                <label for="registeredWith" style="font-size: 13px;"><g:message--}%
                %{--                        code="app.unitConfiguration.label.registeredWith"/></label>--}%
                %{--                <g:select name="registeredWith"--}%
                %{--                          from="${recipientWithList}"--}%
                %{--                          optionKey="id" optionValue="unitName"--}%
                %{--                          noSelection="['':  message(code: 'select.one')]"--}%
                %{--                          value="${unitConfigurationInstance?.registeredWith?.id}"--}%
                %{--                          class="form-control select2-box"/>--}%

                %{--            </div>--}%

                <div class="col-md-2 attachment" id="attachment"
                     style="display:${(unitConfigurationInstance?.unitType in [UnitTypeEnum.RECIPIENT, UnitTypeEnum.BOTH]) ? 'block' : 'none'};">
                    <label class="add-margin-bottom"><g:message code="app.unitConfiguration.label.attachment"/></label>
                    <select class="attachmentControl form-control" id="attachmentControl" name="attachmentControl"
                           data-value="${allowedAttachments}" value="${allowedAttachments ? 'dummy' : ''}"
                            style="min-width: 100px;"></select>
                </div>

                <div class="col-md-2 reportSubDateDiv"
                     style="display:${(unitConfigurationInstance?.unitType in [UnitTypeEnum.RECIPIENT, UnitTypeEnum.BOTH]) ? 'block' : 'none'};">
                    <label><g:message code="app.unitConfiguration.label.timeZone"/></label>
                    <g:select name="preferredTimeZone" class="form-control" from="${ViewHelper.getTimeZoneValues()}"
                              value="${unitConfigurationInstance?.preferredTimeZone ?: 'UTC'}" optionKey="name"
                              optionValue="display"/>
                </div>

                <div class="col-md-2">
                    <label for="unitRetired"><g:message
                            code="app.unitConfiguration.label.unitRetired"/></label>
                    <div>
                        <g:checkBox id="unitRetired" name="unitRetired"
                                    value="${unitConfigurationInstance?.unitRetired}"
                                    checked="${unitConfigurationInstance?.unitRetired}"/>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="rxmain-container-inner e2bDistributionSetting" style="margin-bottom: 5px">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.icsr.profile.conf.e2b.distribution.settings"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row form-group" style="display:block">
                <div class="col-md-2">
                    <label for="unitRegisteredId"><g:message
                            code="app.unitConfiguration.label.unitRegisteredID"/></label><span
                        class="required-indicator">*</span>
                    <g:textField name="unitRegisteredId" id="unitRegisteredId"
                                 value="${unitConfigurationInstance?.unitRegisteredId}" class="form-control" maxlength="255"/>
                </div>

                <div class="col-md-2">
                    <label for="organizationName"><g:message
                            code="app.unitConfiguration.label.companyName"/></label>
                    <g:textField name="organizationName" id="organizationName" value="${unitConfigurationInstance?.organizationName}" class="form-control" maxlength="255"/>
                </div>

                <div class="col-md-2">
                    <label for="xsltName"><g:message code="app.unitConfiguration.label.xsltName"/><span
                            class="required-indicator hide xsltNameIndicator"><g:message code="required.indicator"/></span></label>
                    <g:selectXsltsName name="xsltName" id="xsltName" noSelection="['': message(code: 'select.one')]"
                                       value="${unitConfigurationInstance?.xsltName}" class="form-control select2-box"/>
                </div>

                <div class="col-md-2">
                    <label for="holderId"><g:message code="app.unitConfiguration.label.holderId"/></label>
                    <g:textField name="holderId" id="holderId" value="${unitConfigurationInstance?.holderId}" maxlength="200"
                                 class="form-control"/>
                </div>

                <div class="col-md-2 unitAttachmentRegIdDiv"
                     style="display:${(unitConfigurationInstance?.unitType in [UnitTypeEnum.RECIPIENT, UnitTypeEnum.BOTH]) ? 'block' : 'none'};">
                    <label for="unitAttachmentRegId"><g:message
                            code="app.unitConfiguration.label.unitAttachmentRegId"/></label>
                    <g:textField name="unitAttachmentRegId" id="unitAttachmentRegId"
                                 value="${unitConfigurationInstance?.unitAttachmentRegId}" class="form-control emptyValue" maxlength="255"/>
                </div>
            </div>

            <div class="row form-group">

                <div class="col-md-2 xmlFields">
                    <label for="xmlVersion"><g:message
                            code="app.unitConfiguration.label.xmlVersion"/>
                    </label>
                    <g:textField name="xmlVersion" id="xmlVersion" value="${unitConfigurationInstance?.xmlVersion}" maxlength="2000"
                                 class="form-control emptyValue"/>
                </div>

                <div class="col-md-2 xmlFields">
                    <label for="xmlEncoding"><g:message
                            code="app.unitConfiguration.label.xmlEncoding"/>
                    </label>
                    <g:textField name="xmlEncoding" id="xmlEncoding" value="${unitConfigurationInstance?.xmlEncoding}" maxlength="2000"
                                 class="form-control emptyValue"/>
                </div>

                <div class="col-md-2 xmlFields">
                    <label for="xmlDoctype"><g:message
                            code="app.unitConfiguration.label.xmlDoctype"/>
                    </label>
                    <g:textField name="xmlDoctype" id="xmlDoctype" value="${unitConfigurationInstance?.xmlDoctype}" maxlength="2000"
                                 class="form-control emptyValue"/>
                </div>
            </div>
        </div>
    </div>

    <div class="rxmain-container-inner contactInformation" style="margin-bottom: 5px">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.icsr.profile.conf.contact.information"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row form-group" style="display:block">
                <div class="col-md-2">
                    <label for="title" style="width: 100%;"><g:message code="app.unitConfiguration.label.title"/></label>
                    <g:select name="title" id="title" from="${ViewHelper.getTitleEnumI18n(currentUser.preference.locale.toString())}"
                              optionKey="name" optionValue="display" noSelection="['': message(code: 'select.one')]"
                              value="${unitConfigurationInstance?.title}"
                              class="form-control select2-box"/>
                </div>

                <div class="col-md-2">
                    <label for="firstName"><g:message code="app.unitConfiguration.label.firstName"/></label>
                    <g:textField name="firstName" id="firstName" value="${unitConfigurationInstance?.firstName}"
                                 class="form-control emptyValue" maxlength="255"/>
                </div>

                <div class="col-md-2">
                    <label for="middleName"><g:message code="app.unitConfiguration.label.middleName"/></label>
                    <g:textField name="middleName" id="middleName" value="${unitConfigurationInstance?.middleName}"
                                 class="form-control emptyValue" maxlength="255"/>
                </div>

                <div class="col-md-2">
                    <label for="lastName"><g:message code="app.unitConfiguration.label.lastName"/></label>
                    <g:textField name="lastName" id="lastName" value="${unitConfigurationInstance?.lastName}"
                                 class="form-control emptyValue" maxlength="255"/>
                </div>

                <div class="col-md-2">
                    <label for="department"><g:message code="app.unitConfiguration.label.department"/></label>
                    <g:textField name="department" id="department" value="${unitConfigurationInstance?.department}"
                                 class="form-control emptyValue" maxlength="255"/>
                </div>
            </div>
        </div>
    </div>
</div>
