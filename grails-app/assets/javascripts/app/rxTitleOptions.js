    function loadTableOption(tableID, columnPreferences, saveCallBack) {
        var table = $(tableID).DataTable();
        var tableColumns = $('#tableColumns');
        if (columnPreferences) {
            try {
                columnPreferences = JSON.parse(columnPreferences);
                for (var i in columnPreferences) {
                    if (i >= table.columns()[0].length - 1) continue;
                    var column = table.column(columnPreferences[i].index);
                    if (column) {
                        column.visible(columnPreferences[i].visible);
                        updateColumn(column, columnPreferences[i].index);
                    }
                }
                updateControl(false);
            } catch (e) {
                //nothing
            }
        }
        var columns = table.settings()[0].aoColumns;
        if(tableID.startsWith("#allIssuesTable")) {
            tableColumns = $('#tableColumns2');
            columns = $(tableID).dataTable().dataTableSettings[1].aoColumns;
        }
        var countVisible = 0;
        var trClass;
        var actionsButtons = 0;
        $.each(columns, function(i, v) {
            var innerTh = $(tableID + ' th:eq(' +i+')')[0];
            if(innerTh != undefined) {
                v.sTitle = innerTh.innerHTML;
            }
            if (table.column(i).visible()) {
                countVisible++;
            }
        });
        var addColumns = "";
        $.each(columns, function(i, v) {
            if (countVisible == 1) {
                var checked = 'checked="checked" disabled="true"';
                trClass = 'rxmain-dropdown-settings-table-disabled';
            } else {
                var checked = 'checked="checked"';
                trClass = 'rxmain-dropdown-settings-table-enabled';
            }
            if (!table.column(i).visible()) {
                checked = '';
                trClass = 'rxmain-dropdown-settings-table-enabled';
            }
            var colWidth = $(tableID + ' th:eq(' + i + ')').width();
            var columnName = v.sTitle;
            if (columnName != '') {
                var addColumn = '<tr class="' + trClass + '">' +
                    '<td>' + columnName + '</td>' +
                    '<td style="text-align: center;"><input type="checkbox" ' + checked + ' class="chk-table-columns" data-columns="' + i + '" data-table-id="' + tableID + '"/></td>' +
                    '</tr>';
                addColumns = addColumns + addColumn;
            } else {
                actionsButtons = 1;
            }
        });
        if(addColumns !== "") {
            var tbodyColumn = "<tbody>" +addColumns + "</tbody>";
            $(tbodyColumn).appendTo(tableColumns);
        }
        $('.dropdown-menu input, .dropdown-menu label, .dropdown-menu button, .dropdown-menu select, .dropdown-menu .rxmain-container-dropdown').on('click', function(e) {
            e.stopPropagation();
        });
        $('.chk-table-columns').on('change', function (e) {
            if ($(this).attr('data-table-id') === tableID) {
                e.preventDefault();
                var index = $(this).attr('data-columns');
                var column = table.column(index);
                column.visible(!column.visible());
                updateColumn(column, index)
                updateControl(true);
            }
        });

        function updateColumn(col, index) {
            if (col.visible) {
                $(tableID + 'thead tr:eq(1) th:eq(' + index + ')').hide();
            } else {
                $(tableID + 'thead tr:eq(1) th:eq(' + index + ')').show();
            }
        }

        function updateControl(updateSelection ) {
            var selection = []
            var countNotVis = 0;
            $.each(columns, function (i, v) {
                if (!table.column(i).visible()) {
                    selection.push({index: i, visible: false});
                    countNotVis++;
                } else {
                    selection.push({index: i, visible: true});
                }
            });
            if (saveCallBack && updateSelection) saveCallBack(JSON.stringify(selection))
            // $("#columnPreferences").val(JSON.stringify(selection));
            countNotVis = columns.length - countNotVis;
            if (countNotVis == (1 + actionsButtons)) {
                $('.chk-table-columns').each(function () {
                    if (this.checked) {
                        $(this).attr("disabled", true);
                        var findRow = $(this).closest('tr');
                        $(findRow).removeClass('rxmain-dropdown-settings-table-enabled');
                        $(findRow).addClass('rxmain-dropdown-settings-table-disabled');
                    }
                });
            } else {
                $('.chk-table-columns').each(function () {
                    $(this).removeAttr("disabled");
                    var findRow = $(this).closest('tr');
                    $(findRow).removeClass('rxmain-dropdown-settings-table-disabled');
                    $(findRow).addClass('rxmain-dropdown-settings-table-enabled');
                });
            }
        }
        $('.btn-change-width').click(function (e) {
            var getWith = $('#col' + $(this).attr('data-columns')).val();
            var colId = $(this).attr('data-columns');
            $(tableID + ' th:eq(' + colId + ')').width(getWith);
        });
        tableColumns.on('click', 'tr', function(event) {
            if (event.target.type !== 'checkbox') {
                $(':checkbox', this).trigger('click');
            }
        });
    }
