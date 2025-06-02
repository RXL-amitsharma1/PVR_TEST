$(function () {

    $(".select2-box").select2();
    $("select[name=reportRequestType]").select2({allowClear:true});
    $("#fieldType").on("change", function () {
        var val = $(this).val();
        if ((val === "STRING") || (val === "TEXTAREA") || (val === "LONG") || (val === "DATE")) {
            $("#disabled").attr("disabled",false);
        }else{
            $("#disabled").attr("disabled",true);
        }
        if ((val === "SELECT") || (val === "LIST") || (val === "CASCADE")) {
            $("#allowedValues").attr("disabled", false);
            $(".allowedValuesIndicator").show();
        } else {
            $("#allowedValues").attr("disabled", true)
                .attr("required", false);
            $(".allowedValuesIndicator").hide();
        }

        if (val === "CASCADE") {
            $(".cascade").show();
        } else {
            $(".cascade").hide();
        }
    }).trigger("change");

    $(document).on("change", ".cascadeValue", function () {
        var json = {}
        $(".cascadeDiv").each(function () {
            var key = $(this).find("#cascadeKey").val();
            var value = $(this).find("#cascadeValue").val();
            json[key] = value
        });
        $("#secondaryAllowedValues").val(JSON.stringify(json))
    });

    $(document).on("click", ".formCascadeInputs", function () {
        var rootVal = $("#allowedValues").val().split(";");
        var secondaryAllowedValues = $("#secondaryAllowedValues");
        var valStr = secondaryAllowedValues.val()
        var secondaryAllowedValuesJson = valStr ? JSON.parse(valStr) : {}
        var cascadeContent = $(".cascadeContent")
        cascadeContent.empty();
        if (rootVal) {
            rootVal = rootVal.filter(function (x, i, a) {
                return a.indexOf(x) == i;
            });
            for (var i in rootVal) {
                var val = secondaryAllowedValuesJson[rootVal[i]]
                cascadeContent.append(createCascadeRow(rootVal[i], val ? val : ""));
            }
        }
    });

    function init() {
        var valStr = $("#secondaryAllowedValues").val();
        var secondaryJson = valStr ? JSON.parse(valStr) : {}
        var cascadeContent = $(".cascadeContent")
        if (secondaryJson) {
            for (var x in secondaryJson) {
                cascadeContent.append(createCascadeRow(x, secondaryJson[x]));
            }
        }
    }

    init();

    function createCascadeRow(key, value) {
        if (!key) return "";
        return "<div class='cascadeDiv'>" + key + ":<input type='hidden' id='cascadeKey' value='" + key + "'><input id='cascadeValue' value='" + value + "' class='form-control cascadeValue'></div>"
    }
});