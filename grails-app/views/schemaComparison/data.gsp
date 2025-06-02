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
    <g:if test="${isSuccess && !isRunning && (type != "0")}">
        <g:javascript>
            var schemaDiffListURL = "${createLink(controller: 'schemaComparison', action: 'schemaDiffList')}";

            initDataComparison();
        </g:javascript>
    </g:if>
    <g:if test="${!isSuccess && !isRunning && (type != "0")}">
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
                <g:if test="${isRunning}">
                    Comparison script is running, please wait...
                    <script>
                        setTimeout(function () {
                            location.reload()
                        }, 5000);
                    </script>
                </g:if>
                <g:else>
                    <div class="body">
                        <g:render template="/includes/layout/flashErrorsDivs" var="theInstance"/>
                        <ul class="nav nav-tabs" role="tablist">
                            <li role="presentation"><a href="index">Schema Objects Comparison</a>
                            </li>
                            <li role="presentation" class="active"><a href="data">Schema Data Comparison</a>
                            </li>
                            %{--                    <li role="presentation" ><a href="dataValidation">ETL Data Validation</a>--}%
                            %{--                        </li>--}%

                        </ul>

                    <!-- Tab panes -->

                        <g:form action="runCompare" autocomplete="off" data-evt-sbt='{"method": "showLoader", "params": []}'>
                            <div class="row" style="margin:5px; padding:5px; border: 1px solid #cccccc; border-radius: 10px ">

                                <div class="col-md-2">
                                    <label>Source DB</label>
                                    <input name="DB1" placeholder="192.168.1.1:1521/PVRDEMO" class="form-control" required>
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
                                <div class="col-md-2 m-t-5">
                                    <label>Profile</label>
                                    <select name="profile" class="form-control" required>
                                        <option value="1">PV Datahub Metadata Tables</option>
                                        <option value="2">PV Reports Metadata Tables</option>
                                        <option value="3">PV Signal Metadata Tables</option>
                                    </select>
                                </div>
                                    <button type="submit" class="btn btn-primary pull-right m-t-20 m-r-5">
                                        Compare
                                    </button>
                            </div>
                        </g:form>


                        <div class="row">
                            <g:if test="${!isSuccess && (type != "0")}">
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
                            <g:elseif test="${type != "0"}">
                                <rx:container title="Schema data comparison result (${label})" options="true" filterButton="true">
                                    <div class="list">

                                        <div class="pv-caselist">
                                            <table id="dataList" class="table table-striped pv-list-table dataTable no-footer">
                                                <thead>
                                                <tr>
                                                    <th>Object Name</th>
                                                    <th>Comparison Rsult</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <g:each in="${tables}" var="table">
                                                    <tr>
                                                        <td>${table.name}</td>
                                                        <td>
                                                            <g:if test="${table.count}">
                                                                <g:link action="dataDiff" class="red bold" params="[table: table.name]">Tables has ${table.count} different rows</g:link>
                                                            </g:if>
                                                            <g:else>
                                                                <span class="green bold">Data in tables are equal</span>
                                                            </g:else>
                                                        </td>
                                                    </tr>
                                                </g:each>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </rx:container>
                            </g:elseif>
                            <g:else><div style="margin-left:25px;width: 100%;" >No data</div>
                            </g:else>
                        </div>

                    </div>
                </g:else>
            </rx:container>

        </div>
    </div>
</div>
</body>
</html>
