$(function () {

        function initIssueTypesValues(issueTypeSelect) {
            issueTypeSelect.empty();
            issueTypeSelect.append("<option value=''></option>")
            for (var i = 0; i < issueTypeList.length; i++) {
                if (!issueTypeList[i].hidden) {
                    issueTypeSelect.append("<option value='" + issueTypeList[i].id + "' >" + issueTypeList[i].name + "</option>")
                }

            }
        }

        function initRootCauseValues(issueTypeValue, rootCauseSelect) {
            var selectedIssueType = _.find(issueTypeList, function (e) {
                return e.id == issueTypeValue
            });
            if (selectedIssueType) {
                var rootCauseIds = selectedIssueType.rootCauseIds ? selectedIssueType.rootCauseIds.split(";") : [];
                var rootCauses = _.filter(rootCauseList, function (rc) {
                    return _.find(rootCauseIds, function (e) {
                        return e == rc.id
                    });
                });
                rootCauseSelect.empty();
                rootCauseSelect.append("<option value=''></option>")
                for (var i = 0; i < rootCauses.length; i++) {
                    if (!rootCauses[i].hidden) {
                        rootCauseSelect.append("<option value='" + rootCauses[i].id + "' >" + rootCauses[i].name + "</option>");
                    }
                }
            } else {
                rootCauseSelect.empty();
            }
        }

        function initResponsiblePartyValues(rootCauseValue, responsiblePartySelect) {
            var selectedRootCause = _.find(rootCauseList, function (e) {
                return e.id == rootCauseValue
            });
            if (selectedRootCause) {
                var responsiblePartyIds = selectedRootCause.responsiblePartyIds ? selectedRootCause.responsiblePartyIds.split(";") : [];
                var responsiblePartys = _.filter(responsiblePartyList, function (rc) {
                    return _.find(responsiblePartyIds, function (e) {
                        return e == rc.id
                    });
                });
                responsiblePartySelect.empty();
                responsiblePartySelect.append("<option value=''></option>")
                for (var i = 0; i < responsiblePartys.length; i++) {
                    if (!responsiblePartys[i].hidden) {
                        responsiblePartySelect.append("<option value='" + responsiblePartys[i].id + "'>" + responsiblePartys[i].name + "</option>");
                    }
                }
            } else {
                responsiblePartySelect.empty();
            }
        }

        $(document).on("change", ".issueType", function () {
            var val = $(this).val();
            var section = $(this).closest(".rcaSection");
            section.find(".issueTypeValue").val(val);
            section.find(".rootCauseValue").val("");
            section.find(".responsiblePartyValue").val("");
            var rootCauseSelect = section.find(".rootCause");
            initRootCauseValues(val, rootCauseSelect);
            rootCauseSelect.trigger("change");
        });

        $(document).on("change", ".rootCause", function () {
            var val = $(this).val();
            var section = $(this).closest(".rcaSection");
            section.find(".rootCauseValue").val(val);
            section.find(".responsiblePartyValue").val("");
            var responsiblePartySelect = section.find(".responsibleParty");
            initResponsiblePartyValues(val, responsiblePartySelect);

        });

        $(document).on("change", ".responsibleParty", function () {
            var val = $(this).val();
            var section = $(this).closest(".rcaSection");
            section.find(".responsiblePartyValue").val(val);
        });

        $(document).on("update", ".rcaSection", function () {
            initSectionRca($(this));
            initTextAreas($(this));
        });

        function initAllSectionRca() {
            $("#templateQueryList .rcaSection").each(function () {
                initSectionRca($(this));
            });
        }

        function initAssignedToValues(container) {

            let userSelect = $(container).find(".assignedToUserValue")
            let groupSelect = $(container).find(".assignedToGroupValue")
            if (userSelect.hasClass('select2-hidden-accessible')) userSelect.select2("destroy");
            if (groupSelect.hasClass('select2-hidden-accessible')) groupSelect.select2("destroy");

            bindShareWith(userSelect, sharedWithUserListUrl, sharedWithValuesUrl, "100%", true, $('body'), "placeholder.selectUsers"
            ).on("change", function () {
                groupSelect.attr("data-extraParam", JSON.stringify({user: $(this).val()}));
                groupSelect.data('select2').results.clear()
            });
            bindShareWith(groupSelect, sharedWithGroupListUrl, sharedWithValuesUrl, "100%", true, $('body'), "placeholder.selectGroup"
            ).on("change", function () {
                userSelect.attr("data-extraParam", JSON.stringify({userGroup: $(this).val()}));
                userSelect.data('select2').results.clear()
            });

            userSelect.attr("data-extraParam", JSON.stringify({userGroup: groupSelect.attr("data-value")}));
            groupSelect.attr("data-extraParam", JSON.stringify({user: userSelect.attr("data-value")}));
        }

        function initSectionRca(rcaSection) {
            var issueTypeValue = rcaSection.find(".issueTypeValue").val();
            var issueTypeSelect = rcaSection.find(".issueType");
            initIssueTypesValues(issueTypeSelect);
            issueTypeSelect.val(issueTypeValue);

            var rootCauseValue = rcaSection.find(".rootCauseValue").val();
            var rootCauseSelect = rcaSection.find(".rootCause");
            initRootCauseValues(issueTypeValue, rootCauseSelect);
            rootCauseSelect.val(rootCauseValue);

            var responsiblePartyValue = rcaSection.find(".responsiblePartyValue").val();
            var responsiblePartySelect = rcaSection.find(".responsibleParty");
            initResponsiblePartyValues(rootCauseValue, responsiblePartySelect);
            responsiblePartySelect.val(responsiblePartyValue);
            initAssignedToValues(rcaSection);
        }

        initAllSectionRca();
        $(document).on("click", ".editSql", function () {
            var modal = $(".customExpressionModal");
            var sqlField = $(this).closest("div").find(".sqlInput");
            modal.find("#sqlInput").val(sqlField.attr("id"));
            modal.find("#textInput").val($(this).closest("div").find(".textInput").attr("id"));
            modal.find(".customExpressionQuery").val(sqlField.val() ? sqlField.val() : "");
            modal.modal("show");
        });

        $(document).on("click", ".removeSql", function () {
            var parent = $(this).parent();
            parent.find(".sqlInput")
            sqlInput
            parent.find(".sqlInput").addClass("hidden").val("");
            $(this).addClass("hidden");
            parent.find(".textInput").removeClass("hidden");
        });
        $(document).on("click", ".saveCustomExpressionModal", function () {
            var modal = $(".customExpressionModal");
            var val = modal.find(".customExpressionQuery").val();
            var sqlInput = $(document.getElementById($("#sqlInput").val()));
            var textInput = $(document.getElementById($("#textInput").val()));
            var removeSqlSpan = textInput.parent().find(".removeSql");
            if (val) {
                sqlInput.val(val);
                textInput.val("");
                sqlInput.removeClass("hidden");
                removeSqlSpan.removeClass("hidden");
                textInput.addClass("hidden");
            } else {
                sqlInput.val("");
                sqlInput.addClass("hidden");
                removeSqlSpan.addClass("hidden");
                textInput.removeClass("hidden");
            }
            modal.modal("hide");
        });

        function initTextAreas(rcaSection) {
            $(rcaSection).find(".sqlInput").each(function () {
                if ($(this).val()) {
                    $(this).parent().find(".textInput").addClass("hidden");
                    $(this).parent().find(".removeSql").removeClass("hidden");
                    $(this).removeClass("hidden");

                }
            });
        }
        $("#pvqType").select2().on("select2-removing", function (e) {
                if(!$(this).select2("val") || $(this).select2("val").length==1) e.preventDefault();
        });
    }
);

