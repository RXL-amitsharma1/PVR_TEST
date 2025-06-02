
$(function () {
    var table = $('#rxTableCognosReport').DataTable({
        //"sPaginationType": "bootstrap",
        "ajax": {
            "url": cognosUrl,
            "dataSrc": ""
        },
        "aaSorting": [[ 0, "asc" ]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [ [50, 100, 200, 1000, -1], [50, 100, 200, 1000, "All"] ],
        "aoColumns": [
            { "mData": "name",
                "aTargets": ["name"],
                "mRender": function(data, type, row) {
                    data = (data == null) ? data : data.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
                    return '<a href='+row.url+'>'+data+'</a>'
                }},
            { "mData": "description" }

        ]
    });
    actionButton( '#rxTableCognosReport' );
    loadTableOption('#rxTableCognosReport');
});

