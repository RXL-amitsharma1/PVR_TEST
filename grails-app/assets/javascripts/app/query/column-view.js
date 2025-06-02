var DIC_EVENT = "EVENT";
var DIC_PRODUCT = "PRODUCT";
var DIC_STUDY = "STUDY";
$(function() {
    $(document).on('click', '.addEventValues', function () {
        // check levels of selectedDictionaryValue and report field
        var reportFieldLevel = getReportFieldLevel();
        if (selectedDictionaryValue.level == reportFieldLevel) {
            $('.errorMessage').hide();
            addDictionary(eventValues, EVENT_DICTIONARY);
        } else {
            $('.errorMessage').show();
        }

    });

    $(document).on('click', '.addProductValues', function () {
        var reportFieldLevel = getReportFieldLevel();
        if (selectedDictionaryValue.level == reportFieldLevel) {
            $('.errorMessage').hide();
            addDictionary(productValues, PRODUCT_DICTIONARY);
        } else {
            $('.errorMessage').show();
        }
    });

    $(document).on('click', '.addStudyValues', function () {
        var reportFieldLevel = getReportFieldLevel();
        if (selectedDictionaryValue.level == reportFieldLevel) {
            $('.errorMessage').hide();
            addDictionary(studyValues, STUDY_DICTIONARY);
        } else {
            $('.errorMessage').show();
        }
    });

    $(document).on('click', '.addAllEvents', function () {
        var reportFieldLevel = getReportFieldLevel();
        $("#selectValue").val(getNames(eventValues[reportFieldLevel]));
    });

    $(document).on('click', '.addAllProducts', function () {
        var reportFieldLevel = getReportFieldLevel();
        $("#selectValue").val(getNames(productValues[reportFieldLevel]));
    });

    $(document).on('click', '.addAllStudies', function () {
        var reportFieldLevel = getReportFieldLevel();
        $("#selectValue").val(getNames(studyValues[reportFieldLevel]));
    });

    function getNames(values) {
        var result = '';
        $.each(values, function () {
            result += this.name + ';';
        });
        return result.substring(0, result.length-1);
    }

    //Prevent Users from submitting form by hitting enter
    $(window).keydown(function(event){
        if((event.which== 13) && ($(event.target)[0]!=$("textarea")[0])) {
            event.preventDefault();
            return false;
        }
    });

});

function getReportFieldLevel() {
    return $('#selectField option:selected').attr('data-level');
}
function getReportFieldDicType() {
    return $('#selectField option:selected').attr('data-dictionary');
}

function showEventDicIcon(isEvent) {
    if (isEvent) {
        $('#searchEvents').show();
    } else {
        $('#searchEvents').hide();
    }
}
function showProductDicIcon(isProduct) {
    if (isProduct) {
        $('#searchProducts').show();
    } else {
        $('#searchProducts').hide();
    }
}

function showStudyDicIcon(isStudy) {
    if (isStudy) {
        $('#searchStudies').show();
    } else {
        $('#searchStudies').hide();
    }
}