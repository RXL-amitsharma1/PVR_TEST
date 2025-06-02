var table;
var selectedIds=[];
var selectAll=false;
var totalFilteredRecord;
var allCapaIds;

$(function (e) {
    var tableFilter = {};
    var advancedFilter = false;
    var init_table = function () {
        table = $('#capa-list').DataTable({
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
            "language": {
                "search": '',
                "searchPlaceholder": $.i18n._("fieldprofile.search.label")
            },

            "ajax": {
                "url": listCapaUrl,
                "dataSrc": function(res) {
                    totalFilteredRecord=res["recordsFiltered"];
                    allCapaIds=res["allCapaIds"];
                    return res["aaData"];
                },
                "data": function (d) {
                    d.tableFilter = JSON.stringify(tableFilter);
                    d.advancedFilter = advancedFilter;
                    d.searchString = d.search.value;
                    d.ownerType = $("input[name=ownerType]").val();
                    if (d.order.length > 0) {
                        d.direction = d.order[0].dir;
                        //Column header mData value extracting
                        d.sort = d.columns[d.order[0].column].data;
                    }
                }
            },
            initComplete: function () {
                $('#rxTableQualityReports').removeClass('hide');
                $('#rxTableQualityReports').show();

                actionButton('#capa-list');
                initCapa8dQualityButtons(table);
                initExpandHandler(table);

                $('#rxTableQualityReports tbody tr').each(function () {
                    $(this).find('td:eq(1)').attr('nowrap', 'nowrap');
                });
            },
            "stateSave": true,
            "stateDuration": -1,
            "customProcessing": true, //handled using processing.dt event
            "serverSide": true,

            "aaSorting": [[12, "desc"]],
            aoColumnDefs: [
                {
                    bSortable: false,
                    aTargets: [0,11,15]
                }
            ],

            "searching": true,

            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "pagination": true,

            drawCallback: function (settings) {
                pageDictionary($('#capa-list_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
            },

            "aoColumns": [
                {
                    "mData": "",
                    mRender : function(data, type, row, meta) {
                        var strPrm = "<div class='checkbox checkbox-primary'>";
                        strPrm = strPrm + "<input type='checkbox' class='selectCheckbox'  name='selected" + meta.row + "' _issueNumber='" + row.issueNumber + "' _id='" + row.id + "' ";

                        if(findInArray(selectedIds,row.id) || selectAll==true) {
                            strPrm += "checked=true ";
                        }

                        strPrm = strPrm + "/>";
                        strPrm = strPrm + "<label class= 'selected' for='selected" + meta.row + "'></label>"
                        strPrm = strPrm + "</div>";
                        return strPrm;
                    }
                },
                {
                    "mData": "issueNumber",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "issueType",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "category",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }

                },
                {
                    "mData": "approvedBy"
                },
                {
                    "mData": "initiator"
                },
                {
                    "mData": "teamLead"
                },
                {
                    "mData": "description",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "rootCause",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "verificationResults",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "comments",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "attachments",
                    mRender: function (data, type, row) {
                        for(var i=0;i<data.length;i++){
                            if(data[i].isDeleted == false)
                                return encodeToHTML("Yes");
                        }
                         return encodeToHTML("No");
                    }
                },
                {
                    "mData": "dateCreated",
                    "aTargets": ["dateCreated"],
                    "sClass": "dataTableColumnCenter forceLineWrapDate",
                    "mRender": function (data, type, full) {
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                },
                {
                    "mData": "lastUpdated",
                    "aTargets": ["lastUpdated"],
                    "sClass": "dataTableColumnCenter forceLineWrapDate",
                    "mRender": function (data, type, full) {
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    }
                },
                {"mData": "createdBy"},
                {
                    "sClass": "mw-100",
                    "mData": null,
                    "mRender": function (data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs btn-left-round" href="' + viewUrl + '/' + row.id + '" data-value="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs btn-right-round dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> ';
                        if (editRole) actionButton += '<li role="presentation"><a role="menuitem" href="" id="' + row.id + '" data-toggle="modal" data-target="#sharedWithModal">' + $.i18n._('labelShare') + '</a></li> \
                                                        <li role="presentation"><a role="menuitem" href="" id="' + row.id + '" data-toggle="modal" data-target="#emailToModal">' + $.i18n._('labelEmailTo') + '</a></li> \
                                                        <li role="presentation"><a class="work-flow-edit" role="menuitem" href="' + editUrl + '/' + row.id + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                                        <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="Issue Number" data-instanceid="' + row.id + '" data-instancename="' + row.issueNumber + '">' + $.i18n._('delete') + '</a></li> ';
                        actionButton += ' </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                $('#capa-list tbody tr').each(function () {
                    /*$(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                    $(this).find('td:eq(3)').attr('nowrap', 'nowrap');*/
                });
            }, 100);
        }).on('xhr.dt', function (e, settings, json, xhr) {
            checkIfSessionTimeOutThenReload(e, json)
        });
        actionButton('#capa-list');
        loadTableOption('#capa-list');
        init_table_filter();
    };

    function init_table_filter() {

        function initFilter(userListCache, ownerList) {
            var filter_data = [
                {
                    label: $.i18n._("app.advancedFilter.issueNumber"),
                    type: 'text',
                    name: 'issueNumber',
                    maxlength: 200
                },
                {
                    label: $.i18n._("app.advancedFilter.issueType"),
                    type: 'text',
                    name: 'issueType',
                    maxlength: 255
                },
                {
                    label: $.i18n._("app.advancedFilter.category"),
                    type: 'text',
                    name: 'category',
                    maxlength: 255
                },
                {
                    label: $.i18n._("app.advancedFilter.approvedBy"),
                    type: 'select2-id',
                    name: 'approvedBy',
                    data: userListCache
                },
                {
                    label: $.i18n._("app.advancedFilter.initiator"),
                    type: 'select2-id',
                    name: 'initiator',
                    data: userListCache
                },
                {
                    label: $.i18n._("app.advancedFilter.teamLead"),
                    type: 'select2-id',
                    name: 'teamLead',
                    data: userListCache
                },
                {
                    label: $.i18n._("app.advancedFilter.rootCause"),
                    type: 'text',
                    name: 'rootCause',
                    maxlength: 2000
                },
                {
                    label: $.i18n._("app.advancedFilter.verificationResult"),
                    type: 'text',
                    name: 'verificationResults',
                    maxlength: 2000
                },
                {
                    label: $.i18n._("app.advancedFilter.attachment"),
                    type: 'boolean',
                    name: 'attachmentChecked'
                },
                {
                    label: $.i18n._("app.advancedFilter.dateCreatedStart"),
                    type: 'date-range',
                    group: 'dateCreated',
                    group_order: 1
                },
                {
                    label: $.i18n._("app.advancedFilter.dateCreatedEnd"),
                    type: 'date-range',
                    group: 'dateCreated',
                    group_order: 2
                },
                {
                    label: $.i18n._("app.advancedFilter.dateModifiedStart"),
                    type: 'date-range',
                    group: 'lastUpdated',
                    group_order: 1
                },
                {
                    label: $.i18n._("app.advancedFilter.dateModifiedEnd"),
                    type: 'date-range',
                    group: 'lastUpdated',
                    group_order: 2
                },
                {
                    label: $.i18n._("app.advancedFilter.owner"),
                    type: 'select2',
                    name: 'createdBy',
                    data: ownerList
                }
            ];

            pvr.filter_util.construct_right_filter_panel({
                table_id: '#capa-list',
                container_id: 'config-filter-panel',
                filter_defs: filter_data,
                column_count: 1,
                done_func: function (filter) {
                    tableFilter = filter;
                    advancedFilter = true;
                    var dataTable = $('#capa-list').DataTable();
                    dataTable.ajax.reload(function (data) {
                    }, true);
                }
            });
        }

        $.ajax({
            url: userListUrl,
            success: function (data) {
                var userListCache = []
                var ownerList = []
                _.each(data, function (it) {
                    userListCache.push({key: it.id, value: it.fullName});
                    ownerList.push({key: it.username, value: it.fullName});
                });
                initFilter(userListCache, ownerList);
            },
            error: function (data) {
                console.log(data);
            }
        });
    }
    init_table();
});


function initCapa8dQualityButtons(table) {
    var buttonHtml ='<div class="row">' +
        '<div class="col-lg-10"><label style="float: right; margin-bottom: 0; font-weight: normal" id="lblSelectedRowCount">' + getSelectedRowsCount()  +'</label></div>' +
        '</div>';
    $("div.capa8d-datatable-toolbar").html(buttonHtml);
}

function getSelectedRowsCount() {
    var result ="";
    var recCount = 0;
    if(selectAll==true) {
        recCount=totalFilteredRecord;
    } else {
        recCount= selectedIds.length
    }
    if(recCount > 0) {
        result += recCount;
        if(result == 1) {
            result+= " " + $.i18n._('sng.row.count')
        } else {
            result+= " " + $.i18n._('multi.row.count')
        }
        result+= " " + $.i18n._('label.selected')
    }
    return result;
}

var initExpandHandler = function (table) {

    table.on('click', '#selectAll', function (evt) {
        if($(this).is(":checked")) {
            selectAll=true;
            $("#lblSelectedRowCount").html(getSelectedRowsCount());
            $(".selectCheckbox").prop("checked", true).trigger("change");
            selectedIds = Array.from(allCapaIds);
        } else {
            selectAll=false;
            $(".selectCheckbox").prop("checked", false).trigger("change");
            selectedIds = [];
            $("#lblSelectedRowCount").html("");
        }
    });


    table.on('click', 'label.selected', function () {
        var labelFor = ($(this).attr("for"));
        if(labelFor) {
            var itm = "input[name='" + labelFor +"']";
            var val = $(itm).prop("checked");
            $(itm).prop("checked", (!val)).trigger("change");
        }
    });

    table.on('change',"input.selectCheckbox",function () {
        var isChecked = $(this).prop("checked");
        var id = $(this).attr("_id");

        var idx = findIdx(selectedIds,id);
        if(isChecked) {
            if(idx <0  ) {
                if(id) {
                    selectedIds.push(id);
                }
            }
        } else {
            if(idx>=0) {
                selectedIds.splice(idx,1);
            }
            if(selectAll==true) {
                $("#selectAll").prop("checked",false);
                selectAll=false;
            }
        }
        if(selectAll==false) {
            $("#lblSelectedRowCount").html(getSelectedRowsCount());
        }
    })
};

function findIdx(arItems,searchValue) {
    var found = false;
    var return_idx = -1;
    for(var i=0;i<arItems.length && (!found);i++) {
        if(arItems[i] == searchValue) {
            found = true;
        }
    }
    if(found) {
        return_idx=i-1;
    } else {
        return_idx=-1;
    }
    return return_idx
}

function findInArray(arItems, searchValue) {
    var found = false;
    for(var i=0;i<arItems.length && (!found);i++) {
        if(arItems[i] == searchValue) {
            found = true;
        }
    }
    return found;
}

$(function () {
    //initSelect2ForEmailUsers("#emailUsers");
    $( ".summExport" ).mouseover(function() {
        $('.summarised').show();
        $( ".main" ).css({"border-top": "2px solid #ddd"});
    });

    $( ".summExport" ).mouseout(function() {
        $('.summarised').hide();
        $( ".main" ).css({"border-top": "none"});
    });

    $( ".detExport" ).mouseover(function() {
        $('.detailed').show();
        $( ".main" ).css({"border-bottom": "2px solid #ddd"});
    });

    $( ".detExport" ).mouseout(function() {
        $('.detailed').hide();
        $( ".main" ).css({"border-bottom": "none"});
    });

    $(document).on("click", '.excelWidgetExport', function (e) {
        if ($(this).hasClass("summarised"))
            exportFunc('XLSX', false);
        else if ($(this).hasClass("detailed"))
            exportFunc('XLSX', true);
    });

    $(document).on("click", '.pdfWidgetExport', function (e) {
        if ($(this).hasClass("summarised"))
            exportFunc('PDF', false);
        else if ($(this).hasClass("detailed"))
            exportFunc('PDF', true);
    });

    $(document).on("click", '.docxWidgetExport', function (e) {
        if ($(this).hasClass("summarised"))
            exportFunc('DOCX', false);
        else if ($(this).hasClass("detailed"))
            exportFunc('DOCX', true);
    });

    $(document).on("click", '.pptxWidgetExport', function (e) {
        if ($(this).hasClass("summarised"))
            exportFunc('PPTX', false);
        else if ($(this).hasClass("detailed"))
            exportFunc('PPTX', true);
    });

});

function exportFunc(outputFormat, detailed) {
    if(selectedIds.length > 0){
        if (detailed){
            $(".messageBody").html($.i18n._("qualityModule.capa.detailed.export.warning"));
            $("#warningModal").modal('show');
        }
        var data = createParametersExcelEmail(outputFormat, detailed);
        $("#data").val(JSON.stringify(data));
        $("#exportFormId").trigger('submit');
    }else{
        $(".messageBody").html($.i18n._("app.quality.empty.list.selected"));
        $("#warningModal").modal('show');
    }
}

function createParametersExcelEmail(outputFormat, detailed) {
    var data = {}
    data["selectAll"] = "false";
    data["outputFormat"]=outputFormat;
    data["selectedIds"] = selectAll ? allCapaIds : selectedIds;
    data["detailed"]= detailed;
    data["ownerType"]=$('input[name="ownerType"]').val();
    return data;
}