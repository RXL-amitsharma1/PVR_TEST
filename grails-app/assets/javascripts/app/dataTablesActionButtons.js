window.actionButton = function(tablebody) {
    var selectedCell;
    var lastCell = 'td:last-child';
    var selector = $(tablebody).find('tbody');
    $(selector)
        .on('mouseover', 'tr', function () {
            var foundDiv = $(this).find(lastCell).find("div");
            foundDiv.parent().addClass('no-padding');
            foundDiv.removeClass('dataTableHideCellContent');
        })
        .on('mouseleave', 'tr', function () {
            var foundDiv = $(this).find(lastCell).find("div");
            if (!foundDiv.hasClass('open'))
                foundDiv.addClass('dataTableHideCellContent');
        })
        .on('click', 'td', function () {
            var foundDiv = $(this).find("div");
            if (!foundDiv.hasClass('open')) {
                selectedCell = $(this);
            }
            else {
//                $(selectedCell).find("div").addClass('dataTableHideCellContent');
                selectedCell = $(this);
            }
            $(this).focusout(function() {
                var foundDiv = $(this).find("div");
//                foundDiv.addClass('dataTableHideCellContent');
            })
        })
//        .focusout(function() {
//            var foundDiv = $(selectedCell).find("div");
//            foundDiv.addClass('dataTableHideCellContent');
//        })
//    $('.dropdown-menu').click(function() {
//
//    })
}