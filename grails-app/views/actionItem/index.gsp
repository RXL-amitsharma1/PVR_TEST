<%@ page import="com.rxlogix.enums.AppTypeEnum; com.rxlogix.config.ActionItem; com.rxlogix.util.FilterUtil; com.rxlogix.enums.StatusEnum;" %>

<head>

    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:stylesheet src="quality.css"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:javascript src="app/actionItem/actionItemList.js"/>
    <asset:javascript src="app/actionItem/actionItem.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.task.actionitems.title"/></title>

<g:javascript>
        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var actionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}";
        var deleteActionItemUrl = "${createLink(controller: 'actionItem', action: 'delete')}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}";
        var appTypeUrl = "${createLink(controller: "actionItem", action: "fetchAppType")}";
        var statusUrl = "${createLink(controller: "actionItem", action: "fetchStatus")}";
        var priorityUrl = "${createLink(controller: "actionItem", action: "fetchPriority")}";
        var sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithFilterList', params: [clazz:'com.rxlogix.config.ActionItem'])}";
        var sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
        var actionItemCategoryListUrl = "${createLink(controller: "actionItem", action: "fetchActionItemCategoryList")}";
        var statusTypes = JSON.parse("${FilterUtil.buildEnumOptions(StatusEnum)}");
        var appType = JSON.parse("${FilterUtil.buildEnumOptions(AppTypeEnum)}");
        var baseUrlAdhoc = "${createLink(controller: 'configuration', action: 'view')}";
        var baseUrlAggregate = "${createLink(controller: 'periodicReport', action: 'view')}";
</g:javascript>
<style>
    .input-group{width:210px;}
    label {padding-bottom: 3px;}
    .export-icon {margin-right: 30px;margin-top: -6px;float: right;font-size: 21px;font-weight: 700;line-height: 1;}
    #actionItemList_wrapper{
        margin-top: -38px !important;
    }
    .dt-search{
        margin-left: 10px;
    }
    .top{
        padding-right: 90px;
    }
    #newTabContent  a.active {
        color: #fff!important;
        background: #4c5667!important;
        border: 0 solid #ccc!important;
        margin-top: 5px!important;
        border-radius: 18px!important;
        line-height: 25px!important;
        font-weight: 300!important;
        margin-right: 5px !important;
        margin-left: 5px !important;
    }

    #newTabContent  a {
        font-size: 12px!important;
        padding-top: 5px;
        padding-left: 10px;
        padding-right: 10px;
        padding-bottom: 4px;
        color: #414658;
        line-height: 23px!important;
        font-weight: 600!important;
        border: 1px solid #b9b5b5!important;
        background: 0 0!important;
        margin-top: 5px!important;
        border-radius: 18px!important;
        margin-right: 5px !important;
        margin-left: 5px !important;
    }

    #actionItemList_wrapper .dt-layout-cell.dt-end{
        display: flex;
        margin-top: -26px;
        margin-right: 100px;
    }
    #actionItemList_wrapper .assignedToFilterAIControl {
        min-width: initial !important;
        width: 48% !important;
    }
    #actionItemList_wrapper input[type='search'] {
        border: 1px solid #d2d2d2;
        border-radius: 4px;
        background-color: #fff;
        box-shadow: none;
        color: #444444;
        font-size: 14px;
        padding-top: 0px;
        padding-right: 5px;
        padding-bottom: 0px;
        padding-left: 5px;
        height: 24px;
    }
    #actionItemList_wrapper .dt-layout-cell.dt-start {
        width: 100%;
        display: flex;
        justify-content: left;
        align-items: baseline;
        label {
            font-weight: 200;
        }
        div {
            padding-right: 0.5em;
        }

        div.dt-paging {
            display: flex;
            margin-left: auto;
        }
    }
</style>

</head>
<body>
<div class="content">
    <div class="container ">
<g:if test="${params.id}">
<script>
    $(function() {
        actionItem.actionItemModal.view_action_item(${params.id});
      })
</script>
</g:if>
        <div class="alert alert-danger hide">
            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
            <strong><g:message code="app.label.icsr.error"/> !</strong> <span id="errorNotification"></span>
        </div>

        <div class="alert alert-success hide">
            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
            <strong><g:message code="app.label.success"/> !</strong> <span id="successNotification"></span>
        </div>
        <g:render template="/includes/layout/flashErrorsDivs" />
<rx:container title="${message(code: "actionItem.label")}" customButtons="${g.render(template: "/actionItem/includes/exportTemplate")}" options="true" filterButton="true">
    <div class="topControls" style="float: right;text-align: right;display: none">
    </div>
               <div class="pv-caselist">
                <table id="actionItemList" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                    <tr>
                        <th></th>
                        <th><g:message default="Action Category" code="app.label.action.item.action.category"/></th>
                        <th><g:message default="Assigned To" code="app.label.action.item.assigned.to"/></th>
                        <th><g:message default="Description" code="app.label.action.item.description"/></th>
                        <th><g:message default="Due Date" code="app.label.action.item.due.date"/></th>
                        <th><g:message default="Completion Date" code="app.label.action.item.completion.date"/></th>
                        <th><g:message default="Priority" code="app.label.action.item.priority"/></th>
                        <th><g:message default="Status" code="app.label.action.item.status"/></th>
                        <th><g:message default="Status" code="app.label.application"/></th>
                        <th class="col-min-70"><g:message default="Action" code="app.label.action.item.action"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
            <g:render template="includes/actionItemModal" model="[]" ></g:render>

</rx:container>
</div>
</div>
<g:render template="/includes/widgets/deleteRecord"/>
<form style="display: none" id="excelExport" action="${createLink(controller: 'actionItem', action: 'exportToExcelForAI')}">
    <input name="singleActionItemId" id="excelData" >
</form>
<form style="display: none" id="excelExportForm" action="${createLink(controller: 'actionItem', action: 'exportToExcelForAI')}">
</form>

</body>
