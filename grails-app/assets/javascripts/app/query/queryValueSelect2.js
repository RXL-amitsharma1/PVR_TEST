function upgradeMultiSelect2(select2Selector, url, fieldControl) {
    var selectedItems = []
    var allData = []
    var startSelection = null;
    var select = $(select2Selector);
    select.addClass('select2-selection__expandable');
    select.attr("data-select-all-preparing", 'N');
    var term = "";
    var container = select.parent();
    container.find(".select2-container").css("width", "100%").addClass("form-control");
    var dropdown = select.data().select2.$results.parent();
    var buttons = dropdown.find(".select2ClearAll")
    if (buttons.length != 0) buttons.detach();
    dropdown.append("<p class='select2DropdownBottom' style='height: 25px'><span style='width: 100%'> </span><button class='btn pv-btn-grey add-clear-button select2ClearAll pull-right m-r-5 m-b-5'>Clear All</button> <button class='btn btn-primary select2SelectAll add-clear-button pull-right m-r-5 m-b-5'>Add All</button></p>");
    var expandSelect = select.parent().find(".expandSelect")
    if (expandSelect.length != 0) expandSelect.detach()
    select.before("<span class=\"md md-arrow-expand-vertical toggle-button expandSelect\"></span>");
    var searchInput = container.find("input.select2-input")
    container.find(".expandSelect").on("click", function () {
        if ($(this).hasClass("md-arrow-expand-vertical")) {
            $(this).removeClass("md-arrow-expand-vertical");
            $(this).addClass("md-arrow-collapse-vertical");
            container.find("ul").attr("style", "max-height: 150px!important");

        } else {
            container.find("ul").attr("style", "");
            $(this).addClass("md-arrow-expand-vertical");
            $(this).removeClass("md-arrow-collapse-vertical");
        }
    });
    dropdown.find(".select2ClearAll").on("click", function () {
        select.val([]).trigger('change')
        select.select2("close");
    });
    searchInput.on("keyup", function (e) {
        if ((e.keyCode != 16) && (e.keyCode != 17)) {
            if (url) select2SelectAll.prop("disabled", true);
            allData = [];
        }
    });
    var doScrollingDown = false
    dropdown.on("mouseover", ".select2DropdownBottom", function () {
        if (window.event && window.event.shiftKey) {
            doScrollingDown = true
            scrollDown()
        }
    });
    dropdown.on("mouseout", ".select2DropdownBottom", function () {
        doScrollingDown = false
    });
    var scrollDown;
    scrollDown = function () {
        if (doScrollingDown) {
            dropdown.find("ul")[0].scrollTop += 20;
            setTimeout(scrollDown, 100)
        }
    }

    function isEventSupported(eventName) {
        var el = document.createElement('div');
        eventName = 'on' + eventName;
        var isSupported = (eventName in el);
        if (!isSupported) {
            el.setAttribute(eventName, 'return;');
            isSupported = typeof el[eventName] == 'function';
        }
        el = null;
        return isSupported;
    }


    // Check which wheel event is supported. Don't use both as it would fire each event
    // in browsers where both events are supported.
    var wheelEvent = isEventSupported('mousewheel') ? 'mousewheel' : 'wheel';

    dropdown.on(wheelEvent, function (e) {
        if (window.event.shiftKey) {
            var oEvent = e.originalEvent,
                delta = oEvent.deltaY || oEvent.wheelDelta;
            dropdown.find("ul")[0].scrollTop += delta;
        }
    });

    $(document).on("keyup", function (e) {
        if (e.keyCode == 16) {
            dropdown.find("ul li").removeClass("select2-highlighted-multi");
            doScrollingDown = false;
        }
    });

    select.data().select2.listeners['results:focus'].push(function (e) {
        e.element.trigger("select2-highlight", [e.data]);
    })
    dropdown.on('select2-highlight', function (e, data) {
        if (window.event?.shiftKey) {
            if (startSelection) {
                var startIndex = -1;
                var endIndex = -1;
                dropdown.find("ul li").removeClass("select2-highlighted-multi");
                for (var i in allData) {
                    var item = allData[i];
                    if (item.id == startSelection) startIndex = i;
                    if (item.id == data.id) {
                        endIndex = i;
                        break;
                    }
                }

                if ((endIndex > -1) && (startIndex > -1)) {
                    dropdown.find("ul li").each(function (index) {
                        if ((index >= startIndex) && (index <= endIndex)) {
                            $(this).addClass("select2-highlighted-multi");
                        }
                    });
                }
            }
        } else {
            startSelection = data.id;
            dropdown.find("ul li").removeClass("select2-highlighted-multi");
        }
    });
    var select2SelectAll = dropdown.find(".select2SelectAll");
    select.data().select2.listeners['results:all'].push(function({data}) {
        term = searchInput.val();
        if (data.results) {
            allData = [...new Set([...allData.concat(data.results)])];
            selectedItems = data.results;
            data.results.forEach(function ({text, id}) {
                if (!select.find(`option[value="${id}"]`).length) {
                    select.append(new Option(text, id))
                }
            })
            select.trigger('change').trigger('select2-loaded')
        } else selectedItems = [];
        var disabled = (selectedItems.length == 0)
        if (url && !disabled) disabled = (dropdown.find("ul li").length == 0)
        select2SelectAll.prop("disabled", disabled);
    })

    select.on("select2:opening", function (data) {
        allData = []
        startSelection = null;
        select2SelectAll.prop("disabled", true);
    })
        .on("select2:close", function (data) {
            allData = [];
            startSelection = null;
            if (select.attr("data-select-all-preparing") !== 'Y') {
                select.trigger("change");
            }
        });
    select2SelectAll.on("click", function () {
        select.attr("data-select-all-preparing", 'Y');
        select.select2("close");
        if (url) {
            showLoader();
            $.ajax({
                type: "GET",
                url: url,
                data: {
                    field: fieldControl.select2("val"),
                    term: term,
                    max: 1000000,
                    lang: userLocale
                },
                dataType: 'json'
            })
                .done(function (result) {
                    const loadedOptions = select.find('option').map((_, i) => ({
                        id: i.value,
                        text: i.text
                    }));
                    select.html('');
                    [...new Set([...result, ...loadedOptions])].forEach(({id, text}) => {
                        if (!select.find(`option[value="${id}"]`).length) {
                            select.append(new Option(text, id, true, true))
                        }
                    })
                    select.attr("data-select-all-preparing", 'N');
                    select.trigger("change");
                    hideLoader();
                })
                .fail(function (err) {
                    console.log(err);
                    alert('Unexpected Error!');
                    hideLoader();
                });
        } else {
            var oldSelection = select.select2("data");
            const value = [...new Set([...oldSelection, ...(selectedItems ? selectedItems : [])])]
            select.val(value.map(_ => _.id));
            select.attr("data-select-all-preparing", 'N');
            select.trigger("change");
        }
    });

    select.on("select2:selecting", function (e) {
        e.preventDefault();
        var target = $(this);
        if (window.event?.shiftKey && $(".select2-highlighted-multi").length) {
            $(".select2-highlighted-multi").removeClass("select2-highlighted-multi").trigger('mouseup')
        } else {
            handleSelection(e, target);
        }
        searchInput.val(term);
        if ((dropdown.find("ul li").not(".select2-selected").length < 5) && (dropdown.find(".select2-more-results").length > 0)) {
            dropdown.find("ul").trigger("scroll-debounced")
        }
    });

    function handleSelection(e, target) {

        e.preventDefault();

        const text = e.params.args.data.text;
        const id = e.params.args.data.id;
        if (!target.find(`option[value="${id}"]`).length) {
            target.append(new Option(text, id)).trigger('change')
        }
        if (e.params.args.originalEvent) {
            const opt = $(e.params.args.originalEvent?.target)
            opt.attr("aria-selected", true);
        } else {
            dropdown.find("ul .select2-results__option--highlighted").attr("aria-selected", true)
        }
        target.val([...target.val(), id]).trigger('change')
        target.select2("open");
    }
}