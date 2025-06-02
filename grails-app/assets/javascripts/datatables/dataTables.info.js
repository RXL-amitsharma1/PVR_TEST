$(document).on("tableSelectedRowsCountChanged", "input, table", function (event, rowsCount) {
    const $mainContainer = $(event.target).closest('.rxmain-container');
    if ($mainContainer.length > 0) {
        const $tableSelectedRowsCountLabel = $mainContainer.find('.rxmain-container-header .table-selected-rows-count-label');
        const rowsLabel = rowsCount > 1 ? $.i18n._('multi.row.count') : $.i18n._('sng.row.count');
        const labelText = (rowsCount > 0 ? ' (' + rowsCount + ' ' + rowsLabel + ' ' + $.i18n._('label.selected') + ')' : '');
        if ($tableSelectedRowsCountLabel.length > 0) {
            $tableSelectedRowsCountLabel.text(labelText);
        } else {
            $mainContainer.find('.rxmain-container-header .rxmain-container-header-label').after('<span class="table-selected-rows-count-label">' + labelText + '</span>');
        }
    }
});