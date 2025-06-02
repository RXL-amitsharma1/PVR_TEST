$(function () {

    if($("#configurationForm").length == 1) //create or edit page
        init();

    function init() {
        $(document).find("select[name='globalDateRangeInformation.dateRangeEnum']").on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        }).on("change", function (e) {
            globalDateRangeChangedAction(document);
        }).select2().trigger('change');
        if(module=='pvp') {
            $('[name=file]').on('change', function (evt, numFiles, label) {
                $("#file_name").val($.map($('[name=file]')[0].files, function (val) {
                    return val.name;
                }).join(";"));
            });
            $(document).on("click", ".deleteAttachment", function () {
                $("#attachmentsToDelete").val($("#attachmentsToDelete").val() + "," + $(this).attr("data-id"));
                $(this).parent().css("text-decoration", "line-through");
                $(this).find(".btn").prop('disabled', true);
            });
            $(".queryTemplateUserGroupSelect").select2();
            $(".userSelectSelect").select2();
            $(".ganttSelect").select2();
            $("input[name=dueInPlanSwitcher]").on('change', function () {
                if ($("#dueInPlanSwitcher1").is(":checked")) {
                    $(".dueInDays").attr("disabled", false);
                    $(".ganttSelect").attr("disabled", true).val("");
                } else {
                    $(".dueInDays").attr("disabled", true).val("");
                    $(".ganttSelect").attr("disabled", false);
                }
            }).trigger("change");
            initInlineSelect(".updateQueryTemplateUserGroup", ".queryTemplateUserGroupLabel", "#userGroupEditDiv", '.queryTemplateUserGroupSelect');
            initInlineSelect(".author", ".authorLabel", "#userEditDiv", '.userSelectSelect');
            initInlineSelect(".reviewer", ".reviewerLabel", "#userEditDiv", '.userSelectSelect');
            initInlineSelect(".approver", ".approverLabel", "#userEditDiv", '.userSelectSelect');
            bindMultipleSelect2WithUrl($(".templateTableRow .publisherReportingDestinations"), reportingDestinationsUrl, false, false, null);
            bindSelect2WithUrl($(".templateTableRow .publisherTemplateSelect"), PVPTemplateSearchUrl, PVPTemplateNameUrl, false);
            bindSelect2WithUrl($(".templateTableRow  [name=publisherSectionTaskTemplate]"), PVPTaskTemplateSearchUrl, PVPTaskTemplateNameUrl, true);
        }
        function initInlineSelect(cell, label, editDiv, select) {

            $(document).on("click", cell, function (e) {
                var $this = $(this);
                $label = $this.parent().find(label);
                var $textEditDiv = $(editDiv);
                var enterField = $textEditDiv.find(select);
                var position = $this.offset();
                $textEditDiv.css("left", position.left);
                $textEditDiv.css("top", position.top);
                $textEditDiv.show();
                if (enterField) {
                    enterField.on("keydown", function (evt) {
                        evt = evt || window.event;
                        if (evt.keyCode == 13) {//27 is the code for Enter
                            $textEditDiv.find(".saveButton").trigger('click');
                        }
                    });
                }
                var $input = $this.parent().find("input");
                var oldVal = $input.val();

                enterField.focus();
                $textEditDiv.find(".saveButton").one('click', function (e) {
                    var newVal = enterField.select2("data").id;
                    var name = enterField.select2("data").text;
                    if (newVal !== oldVal)
                        if (newVal && newVal != 0) {
                            $input.val(newVal);
                            if (cell == '.updateQueryTemplateUserGroup')
                                $label.html("Visible For " + name + " Only");
                            else
                                $label.html(name)
                        } else {
                            $input.val("");
                            if (cell == '.updateQueryTemplateUserGroup')
                                $label.html("Visible For Any User Group");
                            else
                                $label.html("");
                        }
                    $(".popupBox").hide();

                });
                $textEditDiv.find(".cancelButton").one('click', function (e) {
                    $(".popupBox").hide();
                });
            });
        }
        bindQuerySelect2($("#globalQuery")).on("select2:select select2:unselect", function (e) {
            selectGlobalQueryOnChange(this);
        }).on("select2:open", function (e) {
            var searchField = $('.select2-dropdown .select2-search__field');
            if (searchField.length) {
                searchField[0].focus();
            }
        });

        $(".globalQueryWrapper").find(".expressionField,.expressionOp,.expressionValueSelect").select2();
        buildReportingDestinationsSelectBox($("select[name='reportingDestinations']"), reportingDestinationsUrl, $("input[name='primaryReportingDestination']"), true);
        buildReportingDestinationsSelectBox($("select[name='publisherContributors']"), publisherContributorsUrl, $("input[name='primaryPublisherContributor']"), true,userValuesUrl);
        $(document).find("select[name='periodReportType']").select2();
        // Add Report Name autofilling for new reports only (without report name)
        if (!$("input[name='reportName']").val()) {
            $(document).on('click', '.addAllProducts', function () {
                autoFillReportName();
            });

            $("#productSelection").on('change', function () {
                autoFillReportName();
            });

            $("select[name='periodicReportType']").on('change', function () {
                autoFillReportName();
            });

            $("input[name='reportName']").on('change', function () {
                $(this).data("changed", true);
            });

            autoFillReportName();
        }
    }

    $("#generateCaseSeries").on('click', function () {
        showGenerateDraftCheckbox()
    });

    function showGenerateDraftCheckbox() {
        if ($("#generateCaseSeries").is(':checked'))
            $(".generateDraft").show();
        else
            $(".generateDraft").hide();
    }

    showGenerateDraftCheckbox();
});


function selectGlobalQueryOnChange(selectContainer) {
    var queryContainer = $(".globalQueryWrapper");
    var expressionValues = getExpressionValues(queryContainer);
    var queryWrapperRow = getGlobalQueryWrapperRow(selectContainer);
    $(expressionValues).empty();
    if (getAJAXCount() == -1) {
        if($(selectContainer).val() != '') {
            $(queryWrapperRow).find('.queryViewButton').attr('href', queryViewUrl+'/'+$(selectContainer).val());
            $(queryWrapperRow).find('.queryViewButton').removeClass('hide');
           } else {
            $(queryWrapperRow).find('.queryViewButton').addClass('hide');
           }
        getBlankValuesForQueryAJAX($(selectContainer).val(), expressionValues, '');
        getCustomSQLValuesForQueryAJAX($(selectContainer).val(), expressionValues, '');
        getBlankValuesForQuerySetAJAX($(selectContainer).val(), expressionValues, '');
    } else {
        $(selectContainer).val(null).trigger('change');
    }
}


function globalDateRangeChangedAction(currentDocument) {
    var valueChanged = $(currentDocument).find("#globalDateRangeInformation\\.dateRangeEnum").val();
    if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
        $(currentDocument).find('#datePickerFromDiv').show();
        $(currentDocument).find('#datePickerToDiv').show();
        $(currentDocument).find('#globalDateRangeInformation\\.relativeDateRangeValue').hide();
        initializeGlobalDatePickersForEdit(currentDocument);

    } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
        $(currentDocument).find('#globalDateRangeInformation\\.relativeDateRangeValue').hide();

    } else {
        if (_.contains(X_OPERATOR_ENUMS, valueChanged)) {
            $(currentDocument).find('#globalDateRangeInformation\\.relativeDateRangeValue').show();

        } else {
            $(currentDocument).find('#globalDateRangeInformation\\.relativeDateRangeValue').hide();
        }
        $(currentDocument).find('#datePickerFromDiv').hide();
        $(currentDocument).find('#datePickerToDiv').hide();
    }
}

function initializeGlobalDatePickersForEdit(currentDocument) {
    var from = null;
    var to = null;
    if ($(currentDocument).find('#globalDateRangeInformation\\.dateRangeStartAbsolute').val()) {
        from = $(currentDocument).find('#globalDateRangeInformation\\.dateRangeStartAbsolute').val();
    }
    if ($(currentDocument).find('#globalDateRangeInformation\\.dateRangeEndAbsolute').val()) {
        to = $(currentDocument).find('#globalDateRangeInformation\\.dateRangeEndAbsolute').val();
    }

    $($(currentDocument).find('#datePickerFromDiv')).datepicker({
        allowPastDates: true,
        date: from,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $($(currentDocument).find('#datePickerToDiv')).datepicker({
        allowPastDates: true,
        date: to,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
}

function autoFillReportName() {
    if (($("input[name='editable']").val() != 'true') && (!$("input[name='reportName']").data("changed"))) {
        var reportNameLength = 200;
        var reportName = $("select[name='periodicReportType']").val();
        if ($("#productSelection").val()) {
            var dictionaryValues = JSON.parse($("#productSelection").val());
            for (var key in dictionaryValues) {
                if (dictionaryValues.hasOwnProperty(key)) {
                    _.each(dictionaryValues[key], function (obj) {
                        var appendedName;
                        if (key == 100)
                            appendedName = obj.id;
                        else
                            appendedName = obj.name;
                        reportName += " " + appendedName;
                    })
                }
            }
        }
        reportName = reportName.substr(0, reportNameLength);
        $("input[name='reportName']").val(reportName);
    }
}




