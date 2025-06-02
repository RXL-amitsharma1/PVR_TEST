/**
 * Created by prashantsahi on 30/05/18.
 */

var icsrReport = icsrReport || {};

icsrReport.icsrCaseList = (function () {
    //ICSR Case List table.
    var icsr_case_list_table;

    //The function for initializing the ICSR Case List data tables.
    var init_icsr_case_list_table = function () {
        var columns = [
            {
                "data": "",
                "orderable": false,
                "width": "6%",
                "render": function (data, type, row) {
                    return ""
                }
            },
            {
                "data": "caseNumber",
                "width": "12%",
                "render": function (data, type, row) {
                    return '<span class="caseNumberElement"><a href="' + icsrReportConfig.caseDataLinkR2Url + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&processReportId=' +row.processedReportId +'&reportLang=' + userLocale + '" target="_blank">' + row.caseNumber + '</a></span>';
                }
            },
            {
                "data": "versionNumber",
                "width": "12%"
            },
//            {
//                "data": "profileName",
//                "width": "15%",
//                mRender: function (data, type, row) {
//                    return encodeToHTML(data);
//                }
//            },
            {
                "data": "productName",
                "width": "20%"
            },
            {
                "data": "eventPreferredTerm",
                "width": "15%"
            },
            {
                "data": "susar",
                "width": "10%"
            },
            {
                "data": "downgrade",
                "width": "10%"
            }
        ];

        //Initialize the datatable
        icsr_case_list_table = $("#icsrCaseList").DataTable({
            language: { info: 'Showing _START_ to _END_ of _TOTAL_ entries'},
            ajax: {
                url: icsrReportConfig.caseListUrl,
                dataSrc: "data",
                data: function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            buttons: [
//                {
//                    extend: "selected",
//                    text: "Add to ICSR Tracking",
//                    action: function (e, dt, node, config) {
//                        var caseNumbersWithVersion = getAddToIcsrCases(dt);
//                        $('#addToIcsrTracking input#caseNumbersWithVersion').val(caseNumbersWithVersion);
//                        $('#addToIcsrTracking div.caseNumbers').text(caseNumbersWithVersion);
//                        $('#addToIcsrTracking').modal('show');
//                    }
//                }
            ],
            columnDefs: [ {
                orderable: false,
                className: "select-checkbox",
                targets:   0
            } ],
            columns: columns,
            dom: "lBfrtip",
            "pagingType": "full_numbers",
            processing: true,
            rowId: "id",
            select: {
                style:    "multi",
                selector: "td:first-child"
            },
            serverSide: true,
            stateDuration: -1,
            stateSave: true
        });
    };

    var getAddToIcsrCases = function (table) {
        var rows = table.rows({selected: true}).data();
        var caseNumberWithVersion = [];
        for (var i = 0; i < rows.length; i++) {
            caseNumberWithVersion.push(rows[i].caseNumber+':'+rows[i].versionNumber);
        }
        return caseNumberWithVersion.join()
    };

    return {
        init_icsr_case_list_table: init_icsr_case_list_table
    }
})();
