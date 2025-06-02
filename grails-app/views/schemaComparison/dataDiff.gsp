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


    <g:javascript>
        var dataDiffListURL = "${createLink(controller: 'schemaComparison', action: 'dataDiffList')}";
        var fields=[]
        <g:each in="${fields}" var="field">
            fields.push({
               label: "${field.columnName}",
               isSortable:${field.isSortable},
               name: '${field.columnName}'
           });
        </g:each>
        createDataTableForDiffTable("objectList", "${params.table}", fields);
    </g:javascript>
</head>


<body>
<div class="content">
    <div class="container ">
        <div class="row">

            <rx:container title="Data Difference" id="objectListCnt" options="true" filterButton="false">
                <div class="list">

                    <div class="pv-caselist"><g:link action="data"><-Back</g:link>
                        <table id="objectList" class="table table-striped pv-list-table dataTable no-footer">
                            <thead>
                            <tr>
                                <g:each in="${fields}" var="field">
                                    <th>${field.columnName}</th>
                                </g:each>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>
            </rx:container>
        </div>
    </div>
</div>
<form id="exportForm" method="post" action="${createLink(controller: "schemaComparison", action: "exportToExcel")}">
    <input type="hidden" name="table" id="table">
    <input type="hidden" name="advancedFilter" id="advancedFilter" >
    <input type="hidden" name="searchString" id="searchString" >
    <input type="hidden" name="tableFilter" id="tableFilter" >
    <input type="hidden" name="direction" id="direction" >
    <input type="hidden" name="sort" id="sort" >
</form>
</body>
</html>
