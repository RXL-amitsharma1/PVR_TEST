<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReasonOfDelayAppEnum; org.springframework.context.MessageSource; com.rxlogix.util.ViewHelper;com.rxlogix.user.UserGroup;com.rxlogix.user.User;com.rxlogix.config.QueryCompliance;" %>

<g:set var="configurationService" bean="configurationService"/>
<g:set var="reportExecutorService" bean="reportExecutorService"/>
<div id="queryCompliance${i}" class="templateQuery-div" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name="queriesCompliance[${i}].version" value="${queryComplianceInstance?.version}"/>
    <g:hiddenField name='queriesCompliance[${i}].id' value="${queryComplianceInstance?.id}"/>
    <g:hiddenField name='queriesCompliance[${i}].dynamicFormEntryDeleted' value='false'/>
    <g:hiddenField name='queriesCompliance[${i}].new' value="${queryComplianceInstance?.id == null ? 'true' : 'false'}"/>
    <g:if test="${queryComplianceIndex}">
        <g:hiddenField class="templateQueryFieldToUpdate" name="templateQueryFieldToUpdate" type-name='${queryComplianceIndex.type}' value="${queryComplianceIndex.index}"/>
    </g:if>
    <div>
        <span class="btn-group" style="float: right;">
            <a  class="btn btn-xs btn-default pv-btn-badge" > <i class="fa fa-arrows add-cursor grab-icon"  title="${message(code: 'tip.dragSection')}" ></i> </a>
            <a class="btn btn-xs btn-default pv-btn-badge"> <i class="fa fa-times add-cursor templateQueryDeleteButton" id="queriesCompliance[${i}].deleteButton"  data-toggle="tooltips" title="${message(code: 'tip.removeSection')}" data-id="${i}"></i> </a>
        </span>
    </div>
    <div class="row templateContainer" id="queriesCompliance">

        <div class="col-md-12 queryWrapperRow" style="margin-bottom: 11px;">
            <div class="row">
                <div class="col-md-4">
                    <div class="row">
                        <div class="col-md-12">
                            <label><g:message code="app.label.criteria.name"/><span class="required-indicator">*</span></label>
                        </div>
                    </div>
                    <div class="row">
                        %{--                        maxlength is set according to the database limit --}%
                        <g:textField name="queriesCompliance[${i}].criteriaName" value="${queryComplianceInstance?.criteriaName}" maxlength="${QueryCompliance.constrainedProperties.criteriaName.maxSize}"
                                     class="form-control"/>
                    </div>
                </div>

                %{--Query--}%
                <div class="col-md-4">
                    <div class="row">
                        <div class="col-md-12 ${hasErrors(bean: error, field: 'query', 'has-error')}">
                            <label><g:message code="app.label.chooseAQuery"/><span class="required-indicator">*</span></label>
                        </div>
                    </div>

                    <div class="row queryContainer">
                        <div>
                            <i class="fa fa-refresh fa-spin loading"></i>
                        </div>
                        <div class="col-md-12">
                            <div class="doneLoading" style="padding-bottom: 5px;">
                                <div>
                                    <div class="col-md-11">
                                        <g:select type="hidden" name="queriesCompliance[${i}].query" from="${[]}"
                                                       data-value="${queryComplianceInstance?.query?.id}"
                                                  class="form-control selectQuery"></g:select></div>
                                    <div>
                                        <a href="${queryComplianceInstance?.query?.id ?createLink(controller: 'query' , action: 'view', id: queryComplianceInstance?.query?.id):'#'}"
                                           title="${message(code: 'app.label.viewQuery')}" target="_blank" class="templateQueryIcon pv-ic queryViewButton glyphicon glyphicon-info-sign ${queryComplianceInstance?.query?.id ?'':'hide'}"></a>

                                        <g:selectController isForAutoReasonOfDelay="true"
                                                            templateQueryInstance="${queryComplianceInstance?.query?.id ? 'hide' : ''}"/>

                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>


                <div class="col-md-2">
                    <div class="row">
                        <div class="col-md-12">
                            <label><g:message code="app.label.allow.timeframe"/></label>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6">
                            <input class="form-control natural-number" type="number" min="1" max="999" name="queriesCompliance[${i}].allowedTimeframe" id="queriesCompliance[${i}].allowedTimeframe" value="${queryComplianceInstance?.allowedTimeframe}">                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                %{--Blank values--}%
                <div class="col-md-4"></div>
                <div class="col-md-8">
                    <div class="queryExpressionValues">
                        <g:if test="${queryComplianceInstance?.queryValueLists?.size() > 0}">
                            <g:each var="qvl" in="${queryComplianceInstance.queryValueLists}">
                                <g:each var="qev" in="${qvl.parameterValues}" status="j">
                                    <g:if test="${qev?.hasProperty('reportField')}">
                                        <g:render template='/query/toAddContainerQEV' model="['qev': qev, 'i':i, 'j':j]"/>
                                    </g:if>
                                    <g:else>
                                        <g:render template='/query/customSQLValue' model="['qev': qev, 'i':i, 'j':j]"/>
                                    </g:else>
                                </g:each>
                            </g:each>
                        </g:if>
                    </div>
                    <g:hiddenField class="validQueries" name="queriesCompliance[${i}].validQueries"
                                   value="${configurationService.getQueriesComplianceId(queryComplianceInstance)}" />
                </div>
            </div>
        </div>
    </div>
</div>
<g:render template="/query/queryLevelHelp"/>
