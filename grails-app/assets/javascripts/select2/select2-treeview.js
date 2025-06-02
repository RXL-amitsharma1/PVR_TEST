function getSelect2TreeView(selectElem) {
  let timeout;
  $.fn.select2.amd.require(['select2/diacritics'], function (DIACRITICS) {

        // stripDiacritics code copied from select2
        function stripDiacritics(text) {
            // Used 'uni range + named function' from http://jsperf.com/diacritics/18
            function match(a) {
                return DIACRITICS[a] || a;
            }

            return text.replace(/[^\u0000-\u007E]/g, match);
        }

        selectElem.select2({
            escapeMarkup: function (markup) {
                return markup;
            },
            templateResult: function (data) {
                if (data.loading) {
                    return data.text;
                }
                var markup = "";
                if (data.children) {
                    clearTimeout(timeout);
                    timeout = setTimeout(callback.bind($(data.element).parent()), 0);
                    markup =
                        "<div class='select2-treeview'><div class='select2-treeview-triangle select2-treeview-down'></div><span>" +
                        data.text +
                        "</span></div>";
                } else {
                    markup =
                        "<div class='select2-treeview-item'><span>" +
                        data.text +
                        "</span></div>";
                }
                return markup;
            },
            templateSelection: function (data) {
                return data.text;
            },
            sortResults: function (results, container, query) {
                if (query && query.term && query.term.length > 0) {
                    var term = query.term.toLowerCase();
                    for (var i = 0; i < results.length; i++) {
                        if (results[i].children) {
                            results[i].children = results[i].children.sort(function (a, b) {
                                return (
                                    a.text.toLowerCase().indexOf(term) -
                                    b.text.toLowerCase().indexOf(term)
                                );
                            });
                        }
                    }
                }
                return results;
            },
            formatSelection: function (data) {
                return data.text;
            },
            matcher: function (term, data) {
                if (data.children?.length) {
                    let result = {...data, children: []};
                    data.children.forEach(child => {
                        const searchChildText = child.text.replace(/&amp;/g, "&");
                        if (stripDiacritics("" + searchChildText)
                            .toUpperCase()
                            .indexOf(
                                stripDiacritics("" + (term.term ?? '')).toUpperCase()
                            ) >= 0) {
                            result.children.push(child);
                        }
                    });
                    return result.children.length ? result : null;
                }

                return null;
            },
            queryComplete: function (select2, term) {
                select2.results.children().click(function () {
                    var triangle = $(this).find(".select2-treeview-triangle");
                    if (triangle.hasClass("select2-treeview-down")) {
                        triangle
                            .removeClass("select2-treeview-down")
                            .addClass("select2-treeview-right");
                    } else {
                        triangle
                            .removeClass("select2-treeview-right")
                            .addClass("select2-treeview-down");
                    }

                    $(this).children("ul").toggle();
                });

                if (term == "") select2.results.children().click();

                var highlighted = select2.results.find(".select2-highlighted");

                highlighted.parent().show();

                if (
                    !(
                        highlighted.hasClass("select2-results-dept-0") &&
                        highlighted.hasClass("select2-result-selectable")
                    )
                ) {
                    var triangle = highlighted
                        .parent()
                        .parent()
                        .find(".select2-treeview-triangle");
                    triangle
                        .removeClass("select2-treeview-right")
                        .addClass("select2-treeview-down");
                }

                select2.results.scrollTop(highlighted.offsetTop - 35 - 29);
            },
            placeholder: $.i18n._("dataTabulation.select.field"),
            allowClear: true,
            minimumResultsForSearch: 15,
        });
    })
  return selectElem;
}

const callback = function () {
    const container = $(`#select2-${$(this).attr("id")}-results`);
    const searchField = container
        .parent()
        .parent()
        .find("input.select2-search__field");
    const term = searchField.val();
    const selector = container.find(
        "li.select2-results__option .select2-results__group"
    );
    selector.off("click").on("click", function () {
        const triangle = $(this).find(".select2-treeview-triangle");
        if (triangle.hasClass("select2-treeview-down")) {
            triangle
                .removeClass("select2-treeview-down")
                .addClass("select2-treeview-right");
        } else {
            triangle
                .removeClass("select2-treeview-right")
                .addClass("select2-treeview-down");
        }

        $(this).parent().children("ul").toggle();
    });

    if (term == "") container.find(".select2-results__group").trigger("click");

    if ($(this).val()) {
        const highlighted = container.find(".select2-results__option--highlighted");
        const triangle = highlighted
            .parent()
            .parent()
            .find(".select2-treeview-triangle");
        if (triangle.hasClass("select2-treeview-right") && highlighted.length) {
            highlighted
                .parent()
                .parent()
                .find(".select2-results__group")
                .trigger("click");
            container.scrollTop(highlighted[0].offsetTop - 35 - 29);
        }
    }
};
