<%@ page import="com.rxlogix.util.DateUtil; grails.util.Holders" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>PV Reports - Schema Comparison Tool</title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/schemaComparison.js"/>
    <g:set var="tab" value="${params.tab ?: "1"}"/>

    <g:if test="${isSuccess && (type=="0")}">
        <g:javascript>



        var dataDiffListURL = "${createLink(controller: 'schemaComparison', action: 'dataDiffList')}";
            <g:if test="${tab == '1'}">
                var fields=[
                    {name: "OWNER",isSortable:true,label: 'Owner'},
                    {name: "OBJECT_TYPE",isSortable:true,label: 'Object Type'},
                    {name: "OBJECT_NAME",isSortable:true,label: 'Object Name'},
                    {name: "COLUMN_NAME",isSortable:true,label: 'Column Name'},
                    {name: "DATA_TYPE",isSortable:true,label: 'Data Type'},
                    {name: "DATA_LENGTH",isSortable:true,label: 'Date Length'},
                    {name: "CHAR_LENGTH",isSortable:true,label: 'Char Length'},
                    {name: "CHAR_USED",isSortable:true,label: 'Char Used'},
                    {name: "POSITION",isSortable:true,label: 'Position'},
                    {name: "COLUMN_POSITION",isSortable:true,label: 'Column Position'},
                    {name: "CHANGE_TYPE",isSortable:true,label: 'Change Type'},
                    {name: "DIFFERENCE",isSortable:true,label: 'Difference'},

                ]
                createDataTableForDiffTable("objectList", "SCHVAL_DIFF", fields);
            </g:if>
            <g:if test="${tab == '2'}">
                fields=[
                    {name: "NAME",isSortable:true,label: 'Name'},
                    {name: "VALUE",isSortable:true,label: 'Value'},
                    {name: "DISPLAY_VALUE", isSortable:true, label: 'Display Value'},
                    {name: "DIFFERENCE",isSortable:true,label: 'Difference'},

                ]
                createDataTableForDiffTable("dbParamsList", "V$PARAMETER_DIFF", fields);
            </g:if>
            <g:if test="${tab == '3'}">
                fields=[
                    {name: "PRIVILEGE",isSortable:true,label: 'Privelege'},
                    {name: "ADMIN_OPTION",isSortable:true,label: 'Admin Option'},
                    {name: "COMMON", isSortable:true, label: 'Common'},
                    {name: "DIFFERENCE",isSortable:true,label: 'Difference'},

                ]
                createDataTableForDiffTable("dbPrivelegeList", "USER_SYS_PRIVS_DIFF", fields);
            </g:if>
            <g:if test="${tab == '4'}">
                fields=[
                    {name: "TABLE_NAME",isSortable:true,label: 'Table Name'},
                    {name: "PRIVILEGE",isSortable:true,label: 'Privelege'},
                    {name: "GRANTABLE",isSortable:true,label: 'Grantable'},
                    {name: "HIERARCHY",isSortable:true,label: 'Hierarchy'},
                    {name: "COMMON", isSortable:true, label: 'Common'},
                    {name: "TYPE", isSortable:true, label: 'Type'},
                    {name: "DIFFERENCE",isSortable:true,label: 'Difference'},

                ]
                createDataTableForDiffTable("dbAllTabList", "ALL_TAB_PRIVS_DIFF", fields);
            </g:if>
        </g:javascript>
    </g:if>
    <g:if test="${!isSuccess && !isRunning && (type=="0")}">
        <g:javascript>
        var dataDiffListURL = "${createLink(controller: 'schemaComparison', action: 'dataDiffList')}";
        var fields=[
                    {name: "SEQ_SCHEVAL_LOG",isSortable:true,label: 'N'},
                    {name: "DESCRIPTION",isSortable:true,label: 'Description'},
                    {name: "LOG_DT",isSortable:true,label: 'Log DT'},
                    {name: "STATUS",isSortable:true,label: 'Status'},
                    {name: "ROWCOUNT", isSortable:true, label: 'Row Count'},
                ]
                createDataTableForDiffTable("dbLogList", "SCHVAL_LOG", fields);

        </g:javascript>
    </g:if>
</head>


<body>
<div class="content">
    <div class="container ">
    <div>
        <rx:container title="Schema Comparison Tool">
            <div class="body">
            <g:if test="${isRunning}">
                Comparison script is running, please wait...
                <script>
                    setTimeout(function () {
                        location.reload()
                    }, 5000);
                </script>
            </g:if>
            <g:else>
                <g:render template="/includes/layout/flashErrorsDivs" var="theInstance"/>
                <ul class="nav nav-tabs" role="tablist">
                    <li role="presentation" class="active"><a href="index">Schema Objects Comparison</a>
                    </li>
                    <li role="presentation"><a href="data">Schema Data Comparison</a>
                    </li>
%{--                    <li role="presentation"><a href="dataValidation">ETL Data Validation</a>--}%
%{--                    </li>--}%
                </ul>


                <!-- Tab panes -->

                <g:form action="runCompare" autocomplete="off" data-evt-sbt='{"method": "showLoader", "params": []}'>
                    <div class="row" style="margin:5px; padding:5px; border: 1px solid #cccccc; border-radius: 10px ">
                        <input type="hidden" name="profile" class="form-control" value="0">
                        <div class="col-md-2">
                            <label>Source DB</label>
                            <input name="DB1" placeholder="192.168.1.1:1521/PVRDEMO" class="form-control" required autocomplete="off">
                        </div>
                        <div class="col-md-2">
                            <label>Source Schema</label>
                            <input name="schema1" class="form-control" required autocomplete="off">
                        </div>
                        <div class="col-md-2">
                            <label>Source Password</label>
                            <input name="pass1" type="password" class="form-control" required autocomplete="new-password">
                        </div>
                        <div class="col-md-2">
                            <label>Target DB</label>
                            <input name="DB2" placeholder="192.168.1.1:1521/PVRDEMO" class="form-control" required>
                        </div>
                        <div class="col-md-2">
                            <label>Target Schema</label>
                            <input name="schema2" class="form-control" required>
                        </div>
                        <div class="col-md-2">
                            <label>Target Password</label>
                            <input name="pass2" type="password" class="form-control" required>
                        </div>
                            <button type="submit" class="btn btn-primary pull-right m-t-5 m-r-5">
                                Compare
                            </button>
                    </div>
                </g:form>
                <g:if test="${!isSuccess  && (type=="0")}">
                    <h3 style="color: red">
                        Comparison script has finished with errors, please see  execution log below for details
                    </h3>

                    <rx:container title="Execution Log (${label})" options="true" filterButton="true">
                        <div class="list">

                            <div class="pv-caselist">
                                <table id="dbLogList" class="table table-striped pv-list-table dataTable no-footer">
                                </table>
                            </div>
                        </div>
                    </rx:container>
                </g:if>
                <g:else>
                <ul class="nav nav-tabs" role="tablist">
                    <li role="presentation" class="${tab == "1" ? "active" : ""}"><a href="${createLink(controller: 'schemaComparison', action: 'index')}?tab=1">Objects Difference</a>
                    </li>
                    <li role="presentation" class="${tab == "2" ? "active" : ""}"><a href="${createLink(controller: 'schemaComparison', action: 'index')}?tab=2">DB Params Difference</a>
                    </li>
                    <li role="presentation" class="${tab == "3" ? "active" : ""}"><a href="${createLink(controller: 'schemaComparison', action: 'index')}?tab=3">User Sys Privilege Difference</a>
                    </li>
                    <li role="presentation" class="${tab == "4" ? "active" : ""}"><a href="${createLink(controller: 'schemaComparison', action: 'index')}?tab=4">All Tab Privilege Difference</a>
                    </li>
                </ul>

                <!-- Tab panes -->
                <div class="tab-content">
                    <g:if test="${type=="0"}">
                    <div role="tabpanel" class="tab-pane ${tab == "1" ? "active" : ""}" id="objects">
                        <g:if test="${tab == '1'}">
                            <rx:container id="objectListCnt" title="Schema objects comparison result (${label})" options="true" filterButton="true">
                                <div class="list">

                                    <div class="pv-caselist">
                                        <table id="objectList" class="table table-striped pv-list-table dataTable no-footer"></table>
                                    </div>
                                </div>
                            </rx:container>
                        </g:if>
                    </div>
                    <div role="tabpanel" class="tab-pane ${tab == "2" ? "active" : ""}" id="db">
                        <g:if test="${tab == '2'}">
                            <rx:container id="dbParamsListCtr" title="DB Parameters Comparison Result (${label})" options="true" filterButton="true">
                                <div class="list">

                                    <div class="pv-caselist">
                                        <table id="dbParamsList" class="table table-striped pv-list-table dataTable no-footer"></table>
                                    </div>
                                </div>
                            </rx:container>
                        </g:if>
                    </div>
                    <div role="tabpanel" class="tab-pane ${tab == "3" ? "active" : ""}" id="privilege">
                        <g:if test="${tab == '3'}">
                            <rx:container id="privListCnt" title="User Sys Privilege Comparison Result (${label})" options="true" filterButton="true">
                                <div class="list">

                                    <div class="pv-caselist">
                                        <table id="dbPrivelegeList" class="table table-striped pv-list-table dataTable no-footer">
                                        </table>
                                    </div>
                                </div>
                            </rx:container>
                        </g:if>
                    </div>
                    <div role="tabpanel" class="tab-pane ${tab == "4" ? "active" : ""}" id="privilege2">
                        <g:if test="${tab == '4'}">
                            <rx:container title="All Tab Privilege Comparison Result (${label})" options="true" filterButton="true">
                                <div class="list">

                                    <div class="pv-caselist">
                                        <table id="dbAllTabList" class="table table-striped pv-list-table dataTable no-footer">
                                        </table>
                                    </div>
                                </div>
                            </rx:container>
                        </g:if>

                    </div>
                    </g:if><g:else>
                    No data
                    </g:else>
                </div>
                </g:else>
                </div>
            </g:else>
            </div>
        </rx:container>

    </div>
</div>
<form id="exportForm" method="post" action="${createLink(controller: "schemaComparison", action: "exportToExcel")}">
    <input type="hidden" name="table" id="table" >
    <input type="hidden" name="advancedFilter" id="advancedFilter" >
    <input type="hidden" name="searchString" id="searchString" >
    <input type="hidden" name="tableFilter" id="tableFilter" >
    <input type="hidden" name="direction" id="direction" >
    <input type="hidden" name="sort" id="sort" >
</form>
</body>
</html>
