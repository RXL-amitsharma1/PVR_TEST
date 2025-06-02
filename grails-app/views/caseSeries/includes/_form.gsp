<%@ page import="com.rxlogix.config.CaseSeries; com.rxlogix.user.User; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.util.DateUtil"%>

<g:if test="${'mode' != 'create'}">
    <g:hiddenField name="id" value="${seriesInstance?.id}" />
</g:if>
<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable'}" >
    <g:set var="editMode" value="${true}"/>
</g:if>
<div style="border: 0px;" class="rxmain-container">

    <g:render template="includes/reportConfigurationSection" model="[seriesInstance: seriesInstance, editMode: editMode]"/>

    <div class="rxmain-container rxmain-container-top">

        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.basic.information"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">

            <div class="row form-group">
                <div class="col-lg-3">
                    <label for="seriesName"><g:message code="app.label.case.series.name" /><span class="required-indicator">*</span></label>
                    <g:textField name="seriesName" value="${seriesInstance?.seriesName}" class="form-control seriesName caseSeriesField" maxlength="${CaseSeries.constrainedProperties.seriesName.maxSize}"/>
                </div>

                <div class="col-lg-3">
                    <label for="description"><g:message code="app.label.case.series.description"/></label>
                    <g:render template="/includes/widgets/descriptionControl" model="[ value:seriesInstance?.description, maxlength: 4000]"/>
                </div>
                <div class="col-lg-2">
                    <g:render template="/includes/widgets/tagsSelect2" model="['domainInstance': seriesInstance]" />
                </div>
                <div class="col-lg-2">
                    <sec:ifAnyGranted roles="ROLE_QUALITY_CHECK">
                        <div class="row">
                            <div class="col-lg-12 configurationForm-basic-checkbox">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="qualityChecked" value="${seriesInstance?.qualityChecked}" checked=""/>
                                    <label for="qualityChecked">
                                        <g:message code="app.label.qualityChecked"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </sec:ifAnyGranted>
                </div>
                <div class="col-lg-2">

                            <g:render template="/includes/widgets/tenantDropDownTemplate"
                                      model="[configurationInstance: seriesInstance]"/>

                </div>
            </div>

        </div>

    </div>

    <g:render template="/configuration/includes/deliveryOptionsSection" model="[configurationInstance: seriesInstance]"/>

<sec:ifAnyGranted roles="ROLE_DATA_ANALYSIS">
    <div class="rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label"><g:message code="app.label.dataAnalysis"/></label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <g:render template="includes/spotfire" model="[seriesInstance: seriesInstance]"/>
            </div>
        </div>
    </div>
</sec:ifAnyGranted>
</div>

