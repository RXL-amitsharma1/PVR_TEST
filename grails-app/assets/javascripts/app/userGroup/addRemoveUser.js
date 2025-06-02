var table;
var usersSelected = [];
$(function () {

    $('#allowedUsers').pickList({
        afterRefreshControls: enableDisableAddAllBtn,
        afterRefresh: caseInsensitiveSortWrapper // don't use in-built sort option of pick-list, that is case sensitive.
    });

    $(document).on('keyup', '.fieldNameFilter', function () {
        var f = $(this).val().toLowerCase();
        if(_.isEmpty(f)) {
            $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", false);
        }else {
            $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", true);
        }
        $(".pickList_sourceList li").each(function () {
            var elem = $(this);
            if (elem.html().toLowerCase().indexOf(f) > -1)
                elem.show();
            else
                elem.hide();
        });
    });
    $(".pickList_list.pickList_targetList").css("height", "305px");
    $(".pickList_listLabel.pickList_sourceListLabel").text($.i18n._("picklist.available"));
    $(".pickList_listLabel.pickList_targetListLabel").text($.i18n._("picklist.selected"));
    $(".pickList_listLabel.pickList_sourceListLabel").html($(".pickList_listLabel.pickList_sourceListLabel").html() +
        '<br> <input class="fieldNameFilter" style="width:100%;" placeholder="' + $.i18n._("fieldprofile.search.label") + '" >');

    // update usersSelected from picklist
    var modalDiv = $('div#addRemoveUserModal');
    var itemIds = modalDiv.find("#allowedUsers").val();
    var managerIdsArray = JSON.parse(managerIdsString);
    updateUsersSelected(itemIds, managerIdsArray);

    // Update - usersSelected when switches are toggled
    $(document).on('change', 'input[name*="MANAGER_"]', function() {
        const userId = $(this).attr('name').match(/MANAGER_(\d+)/)[1];
        const state = $(this).prop('checked')
        const user = usersSelected.find(function(u) {
            return u.id === parseInt(userId, 10);
        });

        if (user) {
            user.manager = state;
            console.log('Switch for user ' + user.fullName + ' with userId ' + userId + ' is ' + (state ? 'ON' : 'OFF') + ', updated manager to ' + state);
        }
    });

    // non-visible input field are not passed in form (only visible page (not all in paginated screen), therefore done manually
    $('form').on('submit', function(e) {
        e.preventDefault();

        var $form = $(this);
        var selectedUsersInputs = '';
        var managerInputs = '';

        // Remove previously selected users and managers
        $form.find('[name="selectedUsers"]').remove();
        $form.find('[name^="MANAGER_"]').remove();

        // Construct input fields for selected users and managers
        usersSelected.forEach(function(rowData) {
            selectedUsersInputs += '<input type="hidden" name="selectedUsers" value="' + rowData.id + '" />';
            if (rowData.manager === true) {
                managerInputs += '<input type="hidden" name="' + GROUP_MANAGER + rowData.id + '" value="on" />';
            }
        });

        $form.append(selectedUsersInputs + managerInputs);
        $form.get(0).submit();
    });

    // for edit screen - too many bootstrap switch -> page breaking/unresponsive for user count > 400
    table = $('#rxUserTable').DataTable({
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
        initComplete: function () {

        },
        "customProcessing": true, //handled using processing.dt event
        "serverSide": true,
        //"stateSave": true,
        "ajax": {
            "url": USERGROUP.ajaxUserFetchUrl,
            "dataSrc": "data",
            "data": function (d) {
                d.searchString = d.search.value;
                if (d.order.length > 0) {
                    d.direction = d.order[0].dir;
                    d.sort = d.columns[d.order[0].column].data;
                }
            }
        },
        "aaSorting": [],
        "order": [[0, "asc"]],
        "columnDefs": [
            { "orderable": false, "targets": 1 } // Disable sorting for the Manager switch column
        ],
        "bLengthChange": true,
        "paging": true,
        "lengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
        "iDisplayLength": 10,

        drawCallback: function (settings) {
            $('.reloaderBtn').removeClass('glyphicon-refresh-animate');
            if (table.settings()[0].oFeatures.bServerSide === false){
                pageDictionary($('#rxUserTable_wrapper')[0], settings.aLengthMenu[0][0], usersSelected ? usersSelected.length : 0);
            }
        },
        "aoColumns": [
            {
                "mData": "fullName",
                "mRender": function (data, type, row) {
                    return formFullNameCell(row.id, row.fullName);
                }
            },
            {
                "mData": "isManager",
                "mRender": function (data, type, row) {
                    return formManagerCell(row.id, row.manager);
                }
            }],
        "createdRow": function (row, data, dataIndex) {
            var ele = $(row).find("input[name^=MANAGER_]");
            if (data.manager === true){
                ele.prop('checked', true);
            }
        }

    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
    });
   loadTableOption('#rxUserTable');

    $("[data-evt-clk]").on('click', function(e) {
        e.preventDefault();
        const eventData = JSON.parse($(this).attr("data-evt-clk"));
        const methodName = eventData.method;
        const params = eventData.params;

        if (methodName == "changeUser") {
            changeUser();
        }
    });
});

function enableDisableAddAllBtn() {
    var f = $(".fieldNameFilter").val();
    if(_.isEmpty(f)) {
        $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", false);
    }else {
        $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", true);
    }
}

function formFullNameCell(id, text) {
    return "<label>" + encodeToHTML(text) + "</label><input type='hidden' name='selectedUsers' value=" + id + ">";
}

function formManagerCell(id, data) {
    return `<label class="pv-switch">
        <input type="checkbox"
               name="${GROUP_MANAGER + id}"
               id="${GROUP_MANAGER + id}"
               class="form-element"
               data-on="${$.i18n._('yes')}"
               data-off="${$.i18n._('no')}"
        />
        <span class="switch-slider" data-on="${$.i18n._('yes')}" data-off="${$.i18n._('no')}"></span>
    </label>`
}

function caseInsensitiveSortWrapper() {
    var pickList_sourceList = $('.pickList_sourceList');
    var pickList_targetList = $('.pickList_targetList');
    caseInsensitiveSort(pickList_sourceList, 'label');
    caseInsensitiveSort(pickList_targetList, 'label');
}

function caseInsensitiveSort(list, sortItem) {
    var items = [];

    list.children().each(function () {
        items.push($(this));
    });

    items.sort(function (a, b) {
        var t1 = a.attr(sortItem).toLowerCase();
        var t2 = b.attr(sortItem).toLowerCase();
        return t1 > t2 ? 1 : t1 < t2 ? -1 : 0;
    });

    list.empty();

    for(var i = 0; i < items.length; i++) {
        list.append(items[i]);
    }
}

function changeUser() {
    var modalDiv = $('div#addRemoveUserModal');
    var itemIds = modalDiv.find("#allowedUsers").val();
    var $table = $('table.userTable');
    var managerIds = [];
    $table.find("input[name^=MANAGER_]:checked").each(function (e) {
        managerIds.push(parseInt($(this).attr("name").split('_')[1], 10));
    });

    updateUsersSelected(itemIds, managerIds);

    pushNewDataFromTargetList(usersSelected);
    modalDiv.modal('hide');
}

// Function to update data dynamically on the client side, not saving in backend (when clicked on select)
function pushNewDataFromTargetList(newData) {
    table.settings()[0].oFeatures.bServerSide = false;
    table.clear().rows.add(newData).draw();
}

//Function to update usersSelected when page loaded and of changeUser is called
function updateUsersSelected(itemIds, managerIds){
    var modalDiv = $('div#addRemoveUserModal');
    if (itemIds !== undefined) {
        var items = modalDiv.find('ul.pickList_targetList li.pickList_listItem');
        usersSelected = [];
        for(var i = 0; i < items.length; i++){
            var item = $(items[i]);
            var userId = parseInt(item.attr("data-value"), 10);
            usersSelected.push({
                id: userId,
                fullName: item.attr("label"),
                manager: ($.inArray(userId, managerIds) !== -1)
            });
        }
    }
}

