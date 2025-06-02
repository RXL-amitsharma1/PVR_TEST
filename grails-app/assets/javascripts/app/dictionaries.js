var EVENT_DICTIONARY = "event";
var PRODUCT_DICTIONARY = "product";
var STUDY_DICTIONARY = "study";
var eventValues = {"1": [], "2": [], "3": [], "4": [], "5": [], "6": [], "7": [], "8": []};
var productValues = {"1": [], "2": [], "3": [], "4": []};
var studyValues = {"1": [], "2": []};
var selectedDictionaryValue = [];
var currentSelectedLevel = 0;

function source(ID, cb, level, dictionaryType, isClick, isCtrlPressed, lang) {
    // show child events in next column
    if (ID) {
        // get "level" from li -> 1, 2, 3, 4, 5, 6
        var getSelectedUrl = getSelectedEventUrl + "?eventId=" + ID + "&dictionaryLevel=" + level;
        if (dictionaryType == PRODUCT_DICTIONARY) {
            getSelectedUrl = getSelectedProductUrl + "?productId=" + ID + "&dictionaryLevel=" + level + addLanguageToUrl(lang);
        } else if (dictionaryType == STUDY_DICTIONARY) {
            getSelectedUrl = getSelectedStudyUrl + "?studyId=" + ID + "&dictionaryLevel=" + level + addLanguageToUrl(lang);
        }
        disableDictionaryButtons();
        $.ajax({
            type: "POST",
            url: getSelectedUrl,
            dataType: "json"
        })
            .done(function (result) {
                if (isClick) {
                    selectedDictionaryValue.id = result.id;
                    selectedDictionaryValue.name = result.name;
                    selectedDictionaryValue.level = level;
                    currentSelectedLevel = level;
                    // If PT was clicked, add primary SOC to data
                    if (level == 4) {
                        if (result.primarySOC) {
                            selectedDictionaryValue.primarySOC = result.primarySOC;
                            selectedDictionaryValue.primaryHLT = result.primaryHLT;
                            selectedDictionaryValue.primaryHLGT = result.primaryHLGT;
                        } else {
                            selectedDictionaryValue.primarySOC = null;
                            selectedDictionaryValue.primaryHLT = null;
                            selectedDictionaryValue.primaryHLGT = null;
                        }
                    }
                }
                if (result.nextLevelItems.length) {
                    cb({items: result.nextLevelItems, lang: result.lang}, level);
                } else {
                    // PVR-3016: Synonyms returned on level 6 require a callback to fire to showParents.
                    cb({items: [], lang: result.lang}, level);
                }
                enableDictionaryButtons();
            });
    } else if (level) {
        // get "level" from ul -> SOC, HLGT, HLT, PT, LLT

    }
}

function sourceParents(IDs, level, cb, dictionaryType, lang) {
    var getParentsUrl = getPreLevelEventParentsUrl + "?eventIds=" + IDs + "&dictionaryLevel=" + level;
    if (dictionaryType == PRODUCT_DICTIONARY) {
        getParentsUrl = getPreLevelProductParentsUrl + "?productIds=" + IDs + "&dictionaryLevel=" + level + addLanguageToUrl(lang);
    } else if (dictionaryType == STUDY_DICTIONARY) {
        getParentsUrl = getPreLevelStudyParentsUrl + "?studyIds=" + IDs + "&dictionaryLevel=" + level + addLanguageToUrl(lang);
        ;
    }

    if (IDs) {
        $.ajax({
            type: "POST",
            url: getParentsUrl,
            dataType: "json"
        })
            .done(function (result) {
                if (result.length > 0) {
                    cb({items: result, lang: lang}, level);
                }
            });
    }
}

function addLanguageToUrl(lang) {
    return lang ? ("&lang_code=" + lang) : ""
}

function showLoading(element) {
    if (element) {
        element.disabled = true;
        var st = element.style;
        var pathToImage = document.location.href.split();
        st.backgroundImage = "url('" + APP_ASSETS_PATH + "select2-spinner.gif')";
        st.backgroundPosition = "right center";
        st.backgroundRepeat = "no-repeat";
    }
}

function hideLoading(element) {
    if (element) {
        element.disabled = false;
        var st = element.style;
        st.backgroundImage = "none";
    }
}

function disableButtons() {

}

function forSearch(searchTerm, level, ref_level, additionalCriteriaData, cb, dictionaryType, selectedInput) {
    currentSelectedLevel = level;
    showLoading(selectedInput);
    var searchUrl = searchEventsUrl;
    var exact_search = $("#event_exactSearch").is(':checked');
    var imp = false;
    if (dictionaryType == PRODUCT_DICTIONARY) {
        exact_search = $("#product_exactSearch").is(':checked');
        searchUrl = searchProductsUrl;
    } else if (dictionaryType == STUDY_DICTIONARY) {
        exact_search = $("#study_exactSearch").is(':checked');
        searchUrl = searchStudiesUrl;
        imp = $("#study_imp").is(':checked');
    }

    var data;
    if (delimiter != null && typeof delimiter == "string") {
        data = {
            contains: searchTerm,
            dictionaryLevel: level,
            delimiter: delimiter,
            ref_level: ref_level,
            exact_search: exact_search,
            imp: imp
        }
    } else {
        data = {
            contains: searchTerm,
            dictionaryLevel: level,
            ref_level: ref_level,
            exact_search: exact_search,
            imp: imp
        }
    }
    if (additionalCriteriaData) {
        $.each(additionalCriteriaData, function (i, item) {
            data[item.key] = item.val;
        })
    }
    // show result in correct column
    $.ajax({
        type: "POST",
        url: searchUrl,
        data: data,
        dataType: "json"
    })
        .done(function (result) {
            if (result) {
                addAllStudyValues();
                addAllProductValues();
            }
            cb({items: result}, level);
            hideLoading(selectedInput);
        });
}

function addAllStudyValues() {
    var currentSelectedLevel = 2;
    $(document).on('click', '.addAllStudyValues', function () {
        var studyOptions = $(this).parents("#studyModal").find('[class*="dicLi"][dictionarylevel="' + currentSelectedLevel + '"]');
        addAllOptions(studyOptions, STUDY_DICTIONARY);
    });
}

function addAllProductValues() {
    $(document).on('click', '.addAllProductValues', function () {
        var productOptions = $(this).parents("#productModal").find('[class*="dicLi"][dictionarylevel="' + currentSelectedLevel + '"]');
        addAllOptions(productOptions, PRODUCT_DICTIONARY);
    });
}

function clearSearchInputs(exceptIndex, dictionaryType) {
    var inputClass = "input.searchEvents";
    if (dictionaryType == PRODUCT_DICTIONARY) {
        inputClass = "input.searchProducts";
    } else if (dictionaryType == STUDY_DICTIONARY) {
        inputClass = "input.searchStudies";
    }
    _.each($(inputClass), function (input, index) {
        if (index != exceptIndex && !$(input).hasClass("additionalCriteria")) {
            input.value = "";
        }
    });
}

function clearAdditionalProductFilter() {
    _.each($('#productModal').find('.additionalCriteria'), function (input, index) {
        $(input).val(null).trigger('change');
    });
}

function clearAdditionalFilters(dictionaryType) {
    var exactSearch = $('#event_exactSearch');
    if (dictionaryType == PRODUCT_DICTIONARY) {
        exactSearch = $('#product_exactSearch');
        clearAdditionalProductFilter();
    } else if (dictionaryType == STUDY_DICTIONARY) {
        exactSearch = $('#study_exactSearch');
        $("#studyDrugs").val(null).trigger('change');
    } else if (dictionaryType == EVENT_DICTIONARY) {
        $("#eventSmqSelect").val(null).trigger('change');
    }
    exactSearch.prop('checked', false)
}

function clearAllText(dictionaryType) {
    var dictionaryValues = $(".eventDictionaryValue");
    if (dictionaryType == PRODUCT_DICTIONARY) {
        dictionaryValues = $(".productDictionaryValue");
    } else if (dictionaryType == STUDY_DICTIONARY) {
        dictionaryValues = $(".studyDictionaryValue");
    }
    _.each(dictionaryValues, function (div) {
        div.innerHTML = "";
    });
}

function resetDictionaryList(dictionaryType) {
    if (dictionaryType == EVENT_DICTIONARY) {
        eventValues = {"1": [], "2": [], "3": [], "4": [], "5": [], "6": [], "7": [], "8": []};
    } else if (dictionaryType == PRODUCT_DICTIONARY) {
        productValues = {"1": [], "2": [], "3": [], "4": []};
    } else if (dictionaryType == STUDY_DICTIONARY) {
        studyValues = {"1": [], "2": []};
    }
}

function resetMultiSearchModal() {
    var container = $("#copyAndPasteDicModal");
    container.find('.copyPasteContent').val("");
    container.find('.c_n_p_other_delimiter').val("");
    container.find(":radio[value=none]").prop("checked", true);
    if ($("#study_imp").is(':checked'))
        $("#study_imp").prop('checked', false);
}

function generateIdList(data) {
    return _.map(data.items, function (item) {
        return item.id.toString();
    });
}

function generateColList(data) {
    return _.map(data, function (item) {
        return item.getAttribute("data-value");
    });
}

function collectNextCol(list) {
    var data = {};
    var dataList = [];

    _.each(list, function (li) {
        var obj = {id: li.getAttribute("data-value"), name: li.innerText, level: li.getAttribute("dictionaryLevel")};
        dataList.push(obj);
    });
    data.items = dataList;
    return data;
}

function generateNewDataForCol(data, col) {
    var childrenIdList = generateIdList(data);
    var nextColList = generateColList(col.querySelectorAll("li"));
    var existingChildren = _.intersection(childrenIdList, nextColList);
    var newData = collectNextCol(col.querySelectorAll("li"));
    if (!existingChildren) {
        newData = data; // use same data
        newData.selectedPath = col.querySelector(".highlighted").getAttribute("data-value");
    } else {
        newData.selectedPath = existingChildren[0];
    }
    newData.highlightedValue = col.querySelector(".highlighted").getAttribute("data-value");
    return newData;
}

function setProductEventDictionary() {
    // Check if we need to give either of two option or not.
    if ($('input[name=optradio]').length > 0) {
        var checkedDic = $('input[name=optradio]:checked')[0];
        if ($(checkedDic).hasClass("productRadio")) {
            $("#studySelection").val("");
        } else {
            $("#productSelection").val("").trigger("change");
        }
    }
}

function addAllOptions(objs, dicType) {
    objs.each(function () {
        var name = $(this).text();
        var level = $(this).attr("dictionarylevel");
        var value = $(this).attr("data-value");
        var selectedObj = {"name": name, "id": value, "level": level};

        if (dicType == PRODUCT_DICTIONARY) dicTypeValues = productValues;
        if (dicType == STUDY_DICTIONARY) dicTypeValues = studyValues;
        if (dicType == EVENT_DICTIONARY) dicTypeValues = eventValues;

        if (!isDuplicate(dicTypeValues, selectedObj)) {
            setDictionaryValues(dicType, selectedObj);
            setDictionaryLevelText(dicType, selectedObj.level, selectedObj);
        }
    });
}

function isDuplicate(dictionaryValues, checkedDictionaryValue) {
    var key = checkedDictionaryValue.level;
    for (var i = 0; i < dictionaryValues[key].length; i++)
        if (dictionaryValues[key][i].id == checkedDictionaryValue.id)
            return true;
    return false;
}

function setDictionaryValues(dictionaryType, value) {
    var selectedObj = {"name": value.name, "id": value.id};
    console.log("Selected object", selectedObj);
    switch (dictionaryType) {
        case EVENT_DICTIONARY:
            eventValues[value.level].push(selectedObj);
            break;
        case PRODUCT_DICTIONARY:
            productValues[value.level].push(selectedObj);
            break;
        case STUDY_DICTIONARY:
            studyValues[value.level].push(selectedObj);
            break;
    }
}

function setDictionaryLevelText(dictionaryType, level, selectedObj) {
    if (level > 0) {
        var valueArea;
        switch (dictionaryType) {
            case EVENT_DICTIONARY:
                valueArea = $(".selectedEventDictionaryValue")[0];
                break;
            case PRODUCT_DICTIONARY:
                valueArea = $(".selectedProductDictionaryValue")[0];
                break;
            case STUDY_DICTIONARY:
                valueArea = $(".selectedStudyDictionaryValue")[0];
                break;
        }
        var textDiv = $(valueArea).find(".level" + level)[0];
        var label = (selectedObj.id == " " ? selectedObj.name : (selectedObj.name + " (" + selectedObj.id + ")"));
        var $elem = $("<span class='dictionaryItem'>" + label + "<span class='closebtn removeSingleDictionaryValue'>&times;</span></span>");
        var $closeButton = $elem.find(".closebtn");
        $closeButton.attr("data-type", dictionaryType);
        $closeButton.attr("data-level", level);
        $closeButton.attr("data-element", JSON.stringify(selectedObj));
        $(textDiv).append($elem);
    }
}