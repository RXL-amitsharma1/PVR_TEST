<%@ page import="com.rxlogix.config.QueryRCA; com.rxlogix.Constants; com.rxlogix.enums.ReasonOfDelayAppEnum; org.springframework.context.MessageSource; com.rxlogix.util.ViewHelper;com.rxlogix.user.UserGroup;com.rxlogix.user.User;" %>

<g:set var="configurationService" bean="configurationService"/>
<g:set var="reportExecutorService" bean="reportExecutorService"/>
<div id="queryRCA${i}" class="templateQuery-div" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name="queriesRCA[${i}].version" value="${queryRCAInstance?.version}"/>
    <g:hiddenField name='queriesRCA[${i}].id' value="${queryRCAInstance?.id}"/>
    <g:hiddenField name='queriesRCA[${i}].dynamicFormEntryDeleted' value='false'/>
    <g:hiddenField name='queriesRCA[${i}].new' value="${queryRCAInstance?.id == null ? 'true' : 'false'}"/>
    <g:if test="${queryRCAIndex}">
        <g:hiddenField class="templateQueryFieldToUpdate" name="templateQueryFieldToUpdate" type-name='${queryRCAIndex.type}' value="${queryRCAIndex.index}"/>
    </g:if>
    <div>
        <span class="btn-group" style="float: right;">
            <a  class="btn btn-xs btn-default pv-btn-badge" > <i class="fa fa-arrows add-cursor grab-icon"  title="${message(code: 'tip.dragSection')}" ></i> </a>
            <a class="btn btn-xs btn-default pv-btn-badge"> <i class="fa fa-times add-cursor  templateQueryDeleteButton" id="queriesRCA[${i}].deleteButton"  data-toggle="tooltips" title="${message(code: 'tip.removeSection')}" data-id="${i}"></i> </a>
        </span>
    </div>
    <div class="row templateContainer" id="queriesRCA">

        <div class="col-md-12 queryWrapperRow" style="margin-bottom: 11px;">
            <div class="row">
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
                                        <g:select type="hidden" name="queriesRCA[${i}].query" from="${[]}"
                                                                          data-value="${queryRCAInstance?.query?.id}"
                                                                          class="form-control selectQuery"/></div>
                                    <div>
                                        <a href="${queryRCAInstance?.query?.id ?createLink(controller: 'query' , action: 'view', id: queryRCAInstance?.query?.id):'#'}"
                                            title="${message(code: 'app.label.viewQuery')}" target="_blank" class="templateQueryIcon pv-ic queryViewButton glyphicon glyphicon-info-sign ${queryRCAInstance?.query?.id ?'':'hide'}"></a>

                                        <g:selectController isForAutoReasonOfDelay="true"
                                                            templateQueryInstance="${queryRCAInstance?.query?.id ? 'hide' : ''}"/>

                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                %{--Date Range--}%
                <div class="col-lg-8">
                    <div class="col-md-3 sectionDateRange">
                        <g:render template="/autoReasonOfDelay/includes/dateRange"
                                  model="[queryRCAInstance: queryRCAInstance, isForPeriodicReport: false]"/>
                    </div>
                    <div class="col-md-3">
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message code="rod.capa.issueType.label"/></label>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <div>
                                    <g:select name="queriesRCA[${i}].lateId" noSelection="${['': message(code: 'select.one')]}"
                                              from="${reportExecutorService.getLateListForOwnerApp(ReasonOfDelayAppEnum.PVC,true)}"
                                              value="${queryRCAInstance?.lateId}" optionKey="id" optionValue="textDesc"
                                              class="form-control editLate selectLateId"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-2">
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message code="rod.capa.rootCause.label"/></label> <i style="cursor:pointer;" class="fa fa-edit pencilOptionSelectedInRootCause"></i>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <div style="display:flex">
                                    <g:select name="queriesRCA[${i}].rootCauseId" noSelection="${['': message(code: 'select.one')]}"
                                              from="${queryRCAInstance?.lateId ? reportExecutorService.getRootCauseListByLateId(queryRCAInstance?.lateId) : []}"
                                              value="${queryRCAInstance?.rootCauseId}" optionKey="id" optionValue="textDesc"
                                              class="form-control inputField editRootCause selectRootCauseId ${queryRCAInstance?.rcCustomExpression ? 'hidden' : ''}"/>
                                    <g:textField id="queriesRCA[${i}].rcCustomExpression" name="queriesRCA[${i}].rcCustomExpression" value="${queryRCAInstance?.rcCustomExpression}"
                                                 readonly="readonly" class="form-control customField hideShowRCField rcCustomExpression ${queryRCAInstance?.rcCustomExpression ? '' : 'hidden'}"/>
                                    <button type="button" class="close customField hideShowRCField changeRCTextToSelect ${queryRCAInstance?.rcCustomExpression ? '' : 'hidden'}">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-2">
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message code="app.pvc.RCSubCategory"/></label> <i style="cursor:pointer;" class="fa fa-edit pencilOptionSelectedInRCSubCat"></i>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <div style="display:flex">
                                    <g:select name="queriesRCA[${i}].rootCauseSubCategoryId" noSelection="${['': message(code: 'select.one')]}"
                                              from="${queryRCAInstance?.rootCauseId ? reportExecutorService.getRCSubCategoryListByRCId(queryRCAInstance?.rootCauseId) : []}"
                                              value="${queryRCAInstance?.rootCauseSubCategoryId}" optionKey="id" optionValue="textDesc"
                                              class="form-control inputField editRootCauseSubCategory selectRootCauseSubCategoryId ${queryRCAInstance?.rcSubCatCustomExp ? 'hidden' : ''}"/>
                                    <g:textField id="queriesRCA[${i}].rcSubCatCustomExp" name="queriesRCA[${i}].rcSubCatCustomExp" value="${queryRCAInstance?.rcSubCatCustomExp}"
                                                 readonly="readonly" class="form-control customField hideShowRCSCField rcSubCatCustomExp ${queryRCAInstance?.rcSubCatCustomExp ? '' : 'hidden'}"/>
                                    <button type="button" class="close customField hideShowRCSCField changeRCSCTextToSelect ${queryRCAInstance?.rcSubCatCustomExp ? '' : 'hidden'}">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-2">
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message default="Assigned To" code="app.label.action.item.assigned.to"/></label>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <div style="overflow: hidden">
                                    <div class="checkbox checkbox-primary sameAsRespPartyCheckBox">
                                        <input class="sameAsRespParty" type="checkbox" name="queriesRCA[${i}].sameAsRespParty" id="queriesRCA[${i}].sameAsRespParty"
                                                ${queryRCAInstance?.sameAsRespParty?"checked":""}>
                                        <label title="${message(code:'app.label.assignedToRespParty')}" class="add-cursor no-bold" for="queriesRCA[${i}].sameAsRespParty" style="white-space: nowrap;">
                                            <g:message code="app.label.assignedToRespParty"/>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                </div></div>

            <div class="row">
                %{--Blank values--}%
                <div class="col-md-8">
                    <div class="queryExpressionValues">
                        <g:if test="${queryRCAInstance?.queryValueLists?.size() > 0}">
                            <g:each var="qvl" in="${queryRCAInstance.queryValueLists}">
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
                    <g:hiddenField class="validQueries" name="queriesRCA[${i}].validQueries"
                                   value="${configurationService.getQueriesRCAId(queryRCAInstance)}" />
                </div>
                <div class="col-md-4">
                    <div class="col-md-4">
                        <label><g:message code="app.pvc.RCClassification"/></label> <i style="cursor:pointer;" class="fa fa-edit pencilOptionSelectedInRCClass"></i>
                        <div style="display:flex">
                            <g:select name="queriesRCA[${i}].rootCauseClassId" noSelection="${['': message(code: 'select.one')]}"
                                      from="${queryRCAInstance?.lateId ? reportExecutorService.getRCClassificationListByLateId(queryRCAInstance?.lateId) : []}"
                                      value="${queryRCAInstance?.rootCauseClassId}" optionKey="id" optionValue="textDesc"
                                      class="form-control inputField editRootCauseClass selectRootCauseClassId ${queryRCAInstance?.rcClassCustomExp ? 'hidden' : ''}"/>
                            <g:textField id="queriesRCA[${i}].rcClassCustomExp" name="queriesRCA[${i}].rcClassCustomExp" value="${queryRCAInstance?.rcClassCustomExp}"
                                         readonly="readonly" class="form-control customField hideShowRCCField rcClassCustomExp ${queryRCAInstance?.rcClassCustomExp ? '' : 'hidden'}"/>
                            <button type="button" class="close customField hideShowRCCField changeRCCTextToSelect ${queryRCAInstance?.rcClassCustomExp ? '' : 'hidden'}">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <label><g:message code="rod.capa.responsibleParty.label"/></label> <i style="cursor:pointer;" class="fa fa-edit pencilOptionSelectedInRespParty"></i>
                        <div style="display:flex">
                            <g:select name="queriesRCA[${i}].responsiblePartyId" noSelection="${['': message(code: 'select.one')]}"
                                      from="${queryRCAInstance?.rootCauseId ? reportExecutorService.getResponsiblePartyListByRootCauseId(queryRCAInstance?.rootCauseId) : []}"
                                      value="${queryRCAInstance?.responsiblePartyId}" optionKey="id" optionValue="textDesc"
                                      class="form-control inputField editResponsibleParty selectResponsiblePartyId ${queryRCAInstance?.rpCustomExpression ? 'hidden' : ''}"/>
                            <g:textField id="queriesRCA[${i}].rpCustomExpression" name="queriesRCA[${i}].rpCustomExpression" value="${queryRCAInstance?.rpCustomExpression}"
                                         readonly="readonly" class="form-control customField hideShowRPField rpCustomExpression ${queryRCAInstance?.rpCustomExpression ? '' : 'hidden'}"/>
                            <button type="button" class="close customField hideShowRPField changeRPTextToSelect ${queryRCAInstance?.rpCustomExpression ? '' : 'hidden'}">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="row">
                            <div class="col-md-12">
                                <div>
                                    <select name="queriesRCA[${i}].assignedToGroup" id="queriesRCA[${i}].assignedToGroup" class="form-control selectAssignedToGroup"
                                            data-value="${queryRCAInstance?.assignedToUserGroup?Constants.USER_GROUP_TOKEN + queryRCAInstance?.assignedToUserGroupId:""}">
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <div>
                            <select name="queriesRCA[${i}].assignedToUser" id="queriesRCA[${i}].assignedToUser" class="form-control selectAssignedToUser"
                                    data-value="${queryRCAInstance?.assignedToUser?Constants.USER_TOKEN+queryRCAInstance?.assignedToUserId:""}"
                            >
                            </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-4" >
                    <label><g:message code="app.pvc.investigation"/> <i style="cursor:pointer;" class="fa fa-edit editInvestigationCustomSq"></i></label>
                    <div style="display: flex;">
                    <textarea style="height: 73px;" class="form-control inputInvestigation inputField ${queryRCAInstance?.investigationSql?"hidden":""}"
                              name="queriesRCA[${i}].investigation"
                              id="queriesRCA[${i}].investigation"
                              maxlength="${QueryRCA.constrainedProperties.investigation.maxSize}">${queryRCAInstance?.investigation}</textarea>
                    <textarea style="height: 73px;" class="form-control hideShowFieldInvestigation customSqlInputInvestigation customField ${queryRCAInstance?.investigationSql?"":"hidden"}"
                              readonly name="queriesRCA[${i}].investigationSql" id="queriesRCA[${i}].investigationSql"
                              maxlength="${QueryRCA.constrainedProperties.investigation.maxSize}">${queryRCAInstance?.investigationSql}</textarea>
                    <button type="button" class="close customField hideShowFieldInvestigation changeTextToInputInvestigation ${queryRCAInstance?.investigationSql ? '' : 'hidden'}">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    </div>
                </div>
                <div class="col-md-4" >
                    <label><g:message code="app.pvc.summary"/> <i style="cursor:pointer;" class="fa fa-edit editSummaryCustomSq"></i></label>
                    <div style="display: flex;">
                        <textarea style="height: 73px;" class="form-control inputSummary inputField ${queryRCAInstance?.summarySql?"hidden":""}"
                                  name="queriesRCA[${i}].summary"
                                  id="queriesRCA[${i}].summary"
                                  maxlength="${QueryRCA.constrainedProperties.summary.maxSize}">${queryRCAInstance?.summary}</textarea>
                        <textarea style="height: 73px;" class="form-control hideShowFieldSummary customSqlInputSummary customField ${queryRCAInstance?.summarySql?"":"hidden"}"
                                  readonly name="queriesRCA[${i}].summarySql" id="queriesRCA[${i}].summarySql"
                                  maxlength="${QueryRCA.constrainedProperties.summary.maxSize}">${queryRCAInstance?.summarySql}</textarea>
                        <button type="button" class="close customField hideShowFieldSummary changeTextToInputSummary ${queryRCAInstance?.summarySql ? '' : 'hidden'}">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                </div>
                <div class="col-md-4" >
                    <label><g:message code="app.pvc.actions"/> <i style="cursor:pointer;" class="fa fa-edit editActionsCustomSq"></i></label>
                    <div style="display: flex;">
                        <textarea style="height: 73px;" class="form-control inputActions inputField ${queryRCAInstance?.actionsSql?"hidden":""}"
                                  name="queriesRCA[${i}].actions"
                                  id="queriesRCA[${i}].actions"
                                  maxlength="${QueryRCA.constrainedProperties.actions.maxSize}">${queryRCAInstance?.actions}</textarea>
                        <textarea style="height: 73px;" class="form-control hideShowFieldActions customSqlInputActions customField ${queryRCAInstance?.actionsSql?"":"hidden"}"
                                  readonly name="queriesRCA[${i}].actionsSql" id="queriesRCA[${i}].actionsSql"
                                  maxlength="${QueryRCA.constrainedProperties.actions.maxSize}">${queryRCAInstance?.actionsSql}</textarea>
                        <button type="button" class="close customField hideShowFieldActions changeTextToInputActions ${queryRCAInstance?.actionsSql ? '' : 'hidden'}">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                </div>
            </div>

        </div>
    </div>
</div>
<g:render template="/query/queryLevelHelp"/>
