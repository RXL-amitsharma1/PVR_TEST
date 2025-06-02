$(function () {
    var numCLL = 1;
    var counter = {
        totalSelects: 1,
        initializedSelects: 1
    };

    if ($('.templateSetCLL').length == 1) { // For create page
        addCLL(0);
        initCLLSelect2(0, counter);
        $('.removeCLL').hide();
    } else {
        $.each($('select.selectCLL:visible'), function (index) {
            initCLLSelect2(index, counter);
            counter.totalSelects++;
        });
        if (counter.totalSelects == 2) {
            $('.removeCLL').hide();
        }
    }

    $('#sectionBreakByEachTemplate').on("change", function () {
            if (!viewOnly) {
                if ($(this).is(":checked")) {
                    $("#excludeEmptySections").prop("checked", false).attr("disabled", true);
                } else {
                    $("#excludeEmptySections").attr("disabled", false);
                }
            }
        }
    ).trigger("change");

    if (viewOnly) {
        $('.removeCLL').hide();
        $('#addCLL').hide();
        $('select.selectCLL:visible').attr('disabled', true);
        $('#excludeEmptySections').attr('disabled', true);
        $('#excludeEmptySections').parent().removeClass('add-cursor');
        $('#linkSectionsByGrouping').attr('disabled', true);
        $('#linkSectionsByGrouping').parent().removeClass('add-cursor');
        $('#sectionBreakByEachTemplate').attr('disabled', true);
        $('#sectionBreakByEachTemplate').parent().removeClass('add-cursor');
    }

    $('#addCLL').on('click', function () {
        addCLL(counter.totalSelects);
        initCLLSelect2(counter.totalSelects, counter);
        counter.totalSelects++;
        numCLL++;

        $('.removeCLL').show();
    });

    $(document).on('click', '.removeCLL', function () {
        var container = $(this).closest('.templateSetCLL');
        $(container).remove();

        if ($('.templateSetCLL').length == 2) { // including the hidden gsp template
            $('.templateSetCLL').find('.removeCLL').hide();
        }
        handleLinkSectionGrouping();
    });

    $(document).on('change', '#sectionBreakByEachTemplate', function () {
        handleLinkSectionGrouping();
    });
});

function addCLL(index) {
    var cloned = $('#templateSetCLL_').clone();
    cloned.removeAttr('id');
    cloned.find('#selectCLL_').attr('id', 'selectCLL_' + index);

    $('#templateSetContainer').append(cloned);
}

function initCLLSelect2(index, counter) {
    bindCllTemplateSelect2($('#selectCLL_' + index))
        .on("change", function (e) {
            handleLinkSectionGrouping();
        });
}

function setCLLIdsForTemplateSet() {
    var cllList = [];
    $.each($('select.selectCLL:visible'), function () {
        cllList.push($(this).select2('val'));
    });
    $('#templateSetNestedIds').val(cllList);
}

function bindCllTemplateSelect2(selector) {
    return bindSelect2WithUrl(selector, cllTemplateSearchUrl, templateNameUrl, false);
}

function handleLinkSectionGrouping() {
    if (viewOnly) return;
    $('#linkSectionsByGrouping').attr('disabled', true);
    $('#linkSectionsByGrouping').parent().removeClass('add-cursor');
    var sectionBreakByEachTemplate = $("#sectionBreakByEachTemplate").is(":checked");
    var groupingColumns;
    var isLinkSecByGrpDisabled = false;
    $.each($('select.selectCLL:visible'), function () {
        var data = $(this).select2('data')[0];
        if (data && !isLinkSecByGrpDisabled) {
            var currentGroupingColumns = data.groupingColumns;
            if(!currentGroupingColumns){
                isLinkSecByGrpDisabled = true;
            }else if (!groupingColumns) {
                groupingColumns = currentGroupingColumns;
            } else if (!arrayEquals(currentGroupingColumns, groupingColumns)) {
                groupingColumns = null;
                isLinkSecByGrpDisabled = true;
            }
        }
    });
    if (groupingColumns && !isLinkSecByGrpDisabled && !sectionBreakByEachTemplate) {
    // if (groupingColumns && !isLinkSecByGrpDisabled) {
        $('#linkSectionsByGrouping').removeAttr('disabled');
        $('#linkSectionsByGrouping').parent().addClass('add-cursor');
    } else {
        $('#linkSectionsByGrouping').prop('checked', false);
        $('#linkSectionsByGrouping').attr('disabled', true);
        $('#linkSectionsByGrouping').parent().removeClass('add-cursor');
    }
}

function arrayEquals(a, b) {
    return Array.isArray(a) &&
        Array.isArray(b) &&
        a.length === b.length &&
        _.isEqual(a.sort(), b.sort());
}