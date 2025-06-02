<%@ page import="com.rxlogix.enums.BalanceQueryPeriodEnum; com.rxlogix.util.DateUtil; com.rxlogix.util.RelativeDateConverter; java.util.Date; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.user.UserGroup; com.rxlogix.config.SourceProfile;com.rxlogix.Constants"%>

<style type="text/css">
    .pickList_sourceListContainer {width : 44%; margin-left:10px;}
    .pickList_targetListContainer {width : 44%}
    .pickList_list {width :100%; height: 250px;}
    .pickList_sourceList{background-color: #f3f0f0; border: 1px solid #ccc; border-radius: 0px 0px 4px 4px;}
    .pickList_targetList{background-color: #f3f0f0;border: 1px solid #ccc; border-radius: 4px;}
    .pickList_list.pickList_targetList{height:272px!important;}
    .pickList_controlsContainer{width: 8%;}
    .fa-pencil-square-o.copy-n-paste{padding-top: 0px;}
    .width-45{width:44%;}
    .width-45-right{width:44%;float:right;}
    #caseList.columnSearchCll{min-height: 70px!important;}
    .fieldNameFilter {
        font-weight: 100;
        border: 1px solid #ccc;
        border-bottom: 0px;
        border-radius: 4px 4px 0px 0px;
    }
    .border-around-1{
        background: #cccccc82;
        padding: 4px 0px 0px 0px;
        border-radius: 4px 4px 0px 0px;
    }
    .border-around-2{
        border-radius: 0px 0px 4px 4px;
        border: 1px solid #ccc;
        padding-bottom: 5px;
    }
</style>
<script>
    $(function () {
        $("#sourceProfile").select2();
    });
</script>
<asset:javascript src="select2/select2-treeview.js"/>
<g:set var="centralSource" value="${centralSource}"/>
<g:set var="userService" bean="userService"/>
<g:set var="balanceMinusQueryService" bean="balanceMinusQueryService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>
<g:set var="entityName" value="${message(code: 'app.balanceMinusQuery.label')}"/>

<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">

        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label"><g:message code="app.balanceMinusQueryStatus.run.label" args="[entityName]"/></label>
        </div>

        <div class="rxmain-container-content rxmain-container-show" id="bmQuerySectionContainer">

            <div class="row m-b-10">
                <div class="col-md-12">
                    <div class="col-md-12 border-around-1">
                        <label><g:message code="app.label.odataSource.label"/></label>
                    </div>
                    <div class="clearfix"></div>
                    <div class="row border-around-2 m-0">
                        <div class="col-lg-4 m-t-4">
                            <g:select name="sourceProfile" id="sourceProfile" from="${sourceProfiles}" disabled="${bmQueryInstance?.isDisabled}"
                              optionValue="sourceName" optionKey="id" multiple="true" class="form-control m-t-5 " value="${bmQueryInstance?.bmQuerySections?.sourceProfile?.collect{it.id} ?: centralSource?.id}"/>
                        </div>
                    </div>
                </div>
            </div>

            <div id="bmQuerySectionList" data-counter="${bmQueryInstance?.bmQuerySections?.size() ?: 0}">
                <g:if test="${bmQueryInstance?.bmQuerySections?.size() > 0}">
                    <g:each var="bmQuerySection" in="${bmQueryInstance?.bmQuerySections}" status="i">
                        <g:render template='/balanceMinusQuery/include/bmQuerySection' model="['bmQueryInstance' : bmQueryInstance, 'bmQuerySection': bmQuerySection, 'i': i, 'hidden': false]"></g:render>
                    </g:each>
                </g:if>
            </div>
        </div>

    </div>
</div>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label"><g:message code="app.label.balanceMinusQueryScheduler"/></label>
        </div>
        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <g:render template="/balanceMinusQuery/include/balanceMinusQueryScheduler" model="[bmQueryInstance: bmQueryInstance]"/>
                <g:hiddenField id="isDisabled" name="isDisabled"  value="${bmQueryInstance?.isDisabled ? true : false}"/>
                <g:hiddenField name="schedulerTime" value="${RelativeDateConverter.getCurrentTimeWRTTimeZone(currentUser)}"/>
                <g:hiddenField id="startDateTime" name="startDateTime" value="${bmQueryInstance?.startDateTime}"/>
                <g:hiddenField id="repeatInterval" name="repeatInterval" value="${bmQueryInstance?.repeatInterval}" />
                <g:hiddenField id="configSelectedTimeZone" name="configSelectedTimeZone" value="${balanceMinusQueryService.getTimezone(bmQueryInstance?.configSelectedTimeZone)}"/>
                <g:hiddenField id="timezoneFromServer" name="timeZone" value="${DateUtil.getTimezone(currentUser)}"/>
            </div>
        </div>
    </div>
</div>
<g:render template="/query/copyPasteModal"/>
