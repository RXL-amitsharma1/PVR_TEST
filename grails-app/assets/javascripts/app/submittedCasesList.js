var submittedCases = submittedCases || {};

submittedCases.submittedCasesList = (function () {

    //Action item table.
    var submitted_cases_table;

    //The function for initializing the action item data tables.
    var init_submitted_cases_table = function (url) {

        //Initialize the datatable
        submitted_cases_table = $("#submittedCasesList").DataTable({
            //"sPaginationType": "bootstrap",
            "layout": {
                topStart: null,
                topEnd: {search: {placeholder: 'Search'}},
                bottomStart: ['pageLength', 'info', {
                    paging: {
                        type: 'full_numbers'
                    }
                }],
                bottomEnd: null,
            },
            language: { search: ''},
            "stateSave": true,
            "stateDuration": -1,

            initComplete: function () {
            },

            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,
            "ajax": {
                "url": url,
                "dataSrc": "data",
                "data": function (d) {
                    d.searchString = d.search.value;
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        //Column header mData value extracting
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            "aaSorting": [],
            "order": [[1, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aoColumns": [
                {
                    "mData": "caseType"
                },
                {
                    "mData": "caseNumber"
                },
                {
                    "mData": "versionNumber"
                },
                {
                    "mData": "eventSequenceNumber",
                    "visible" : dateRangeType == $.i18n._('app.label.eventReceiptDate') ? true : false
                },
                {
                    "mData": "eventReceiptDate",
                    "visible" : dateRangeType == $.i18n._('app.label.eventReceiptDate') ? true : false,
                    "mRender": function (mdata, type, full) {
                        if (mdata) {
                            return moment(mdata).format(DEFAULT_DATE_DISPLAY_FORMAT);
                        } else {
                            return ""
                        }
                    }
                },
                {
                    "mData": "eventPreferredTerm",
                    "visible" : dateRangeType == $.i18n._('app.label.eventReceiptDate') ? true : false
                },
                {
                    "mData": "eventSeriousness",
                    "visible" : dateRangeType == $.i18n._('app.label.eventReceiptDate') ? true : false
                }
            ]
        });
        return submitted_cases_table;
    };

    return {
        init_submitted_cases_table: init_submitted_cases_table
    }
})();
