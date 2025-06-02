var selectedGroupId;
var AuditLogCategoryEnum = Object.freeze({ "CREATED":'CREATED'});


$(function () {

    var table = $('#auditSearchResultsTable').DataTable({
        "layout": {
            topStart: null,
            topEnd: {search: {placeholder: 'Search'}},
            bottomStart: ['pageLength','info', {
                paging: {
                    type: 'full_numbers'
                }
            }],
            bottomEnd: null,
        },
        language: { search: ''},
        // //"sPaginationType": "bootstrap",
        "bFilter": false,
        "stateSave": true,
        "stateDuration": -1,
        "customProcessing": true, //handled using processing.dt event
        "serverSide": true,
        "ajax": {
            "url": AUDITSEARCH.auditLogListUrl,
            "dataSrc": "data",
            "dataType": 'json',
            "data": function (d) {
                var params = $('#auditLogSearchForm').serializeArray()
                for (var i = 0; i < params.length; i++) {
                    d[params[i].name] = params[i].value;
                }
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    //Column header mData value extracting
                    d.sort = d.columns[d.order[0].column].data;
                }
            },
            always: function(){
                $('#auditLogSearchButton').attr("disabled", false);
            }
        },
        rowId: "id",
        "aaSorting": [],
        "order": [[0, "desc"]],
        "bLengthChange": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "pagination": true,
        "iDisplayLength": 50,

        drawCallback: function (settings) {
            showTotalPage(settings.json.recordsFiltered);
            pageDictionary($('#auditSearchResultsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
        },
        "aoColumns": [
            //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
            {
                "mData": "category",
                "bSortable": false,
                mRender: function (data, type, row) {
                    return '<span class="label label-'+row.categoryBtnIndicator+'">'+$.i18n._(data) + '</span>';
                }
            },
            {
                "mData": "username",
                mRender: function (data , type , row) {
                    return encodeToHTML(data);
                }

            },

            {
                "mData": "description",
                "bSortable": false,
                mRender: function (data, type, row) {

                    var htmlText = '<div class="comment">';
                    if(row.auditLogFieldChanges){
                        if(row.category.substr(row.category.lastIndexOf(".")+1) == AuditLogCategoryEnum.CREATED){
                            htmlText+='<a href="'+AUDITSEARCH.fieldChangeUrl+'/'+row.id+'">'+$.i18n._('auditLog.created.label')+' '+encodeToHTML(row.domainObjectDescription)+'</a>';
                        }else{
                            htmlText+='<a href="'+AUDITSEARCH.fieldChangeUrl+'/'+row.id+'">'+$.i18n._('auditLog.ViewChangesFor.label')+' '+encodeToHTML(row.domainObjectDescription)+'</a>';
                        }
                        if (row.justification != null && row.justification != '') {
                            htmlText += '(' + $.i18n._('app.label.justification') + ':' + encodeToHTML(row.justification) + ')';
                        }
                    }else{
                        htmlText+=encodeToHTML(row.description);
                        if(row.justification!=null && row.justification!=''){
                            htmlText+='('+ $.i18n._('app.label.justification')+':'+encodeToHTML(row.justification)+')';

                        }
                        if(row.userIPAddress!=null && row.userIPAddress!='') {
                            htmlText += "&nbsp&nbsp<span class='glyphicon glyphicon-info-sign popoverMessage'+'/n'+" +
                                " title data-content='"+$.i18n._('auditLog.userIPAddress.label') + row.userIPAddress +'</br>'+$.i18n._('auditLog.timeZone.label')+' :'+row.timeZone+'</br>'+ $.i18n._('auditLog.browser.label')+' :'+row.browser+'</br>'+ $.i18n._('auditLog.device.label')+' :' +row.device+ "' data-original-title=''></span>";
                        }

                    }

                    htmlText+='</div>';
                    return htmlText;
                }
            },
            {
                "mData": "lastUpdated",
                "bSortable": true,
                "aTargets": ["lastUpdated"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    if (data)
                        return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                    else
                        return $.i18n._("app.user.neverLoggedIn.label");
                }
            }
        ]
    }).on('draw.dt', function () {
    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json)
    });
    actionButton('#auditSearchResultsTable');
    loadTableOption('#auditSearchResultsTable');
    $('#auditLogSearchButton').on('click', function () {
        $('#auditLogSearchButton').attr("disabled", true);
        table.ajax.reload();
    });
});