function showTagWidget(isAdmin) {
    $("#tags").select2({
        placeholder: $.i18n._('placeholder.tags'),
        allowClear: true,
        width: "100%",
        containerCssClass: 'tags-select2-container',
        language: {
           noResults: function() {return '';}
        },
        escapeMarkup: function (markup) {
            return markup; // Prevent Select2 from escaping HTML
        }
    }).one('select2:open', function (e) {
        let create = "";
        if (isAdmin) {
            create = "<a href=createTagUrl id='addNewTag' class='btn btn-success' style='float: right; margin-bottom: 2px' data-evt-clk='{\"method\": \"preventDefault\", \"params\": []}'> "+$.i18n._('create')+"</a>";
        } else {
            create = editMessage;
        }
        $('#select2-tags-results').after("<div id='addNewTagContainer' style='padding: 2px'><input readonly='readonly' class='form-control' id='newTagTerm' style='margin-bottom: 2px' value='' maxlength='255'>" + create + "</div>");
        showHideAddNewTagContainer(false);
    }).on('select2:open', function (e) {
        $('#newTagTerm').val('');
        showHideAddNewTagContainer(false);
    });
    $(document).on("click", "#addNewTag", function(){
        var newTerm = $("#newTagTerm").val();
        newTerm = encodeToHTML(newTerm);
        if ($.trim(newTerm) != "" && isAdmin) {
            $("<option>" + newTerm + "</option>").appendTo("#tags");
            var selectedItems = $("#tags").select2("val");
            selectedItems.push(newTerm);
            $("#tags").val(selectedItems);
            $("#tags").select2("close");
        } else {
            $("#newTagTerm").val('');
            $("#tags").select2("close");
        }
    });

    const $searchField = $('#tags').parent().find('.select2-search__field');

    $searchField.attr("maxlength", 255);

     $(document).on('input', '.tags-select2-container .select2-search__field', function (event) {
        const $target = $(event.target);
        const $tags = $("#tags");

         if($tags.inputTimer) {
             clearTimeout($tags.inputTimer);
         }
         $tags.inputTimer = setTimeout(function (){
             const upperCaseTerm = $target.val().toString().trim().toUpperCase();
             let hide = null;
             $tags.find('option').each(function () {
                 const upperCaseText = $(this).val().toString().trim().toUpperCase();
                 if (upperCaseText === upperCaseTerm) {
                     hide = true;
                     return false;
                 } else {
                     if (upperCaseText.indexOf(upperCaseTerm) > -1 || upperCaseTerm.startsWith(upperCaseText)) {
                         hide = false;
                     }
                 }
             });
             if (!upperCaseTerm) {
                 showHideAddNewTagContainer(false);
             } else {
                 showHideAddNewTagContainer(!hide);
             }
         }, 500);

        $('#newTagTerm').val($target.val());
    });

     function showHideAddNewTagContainer(show) {
         const $addNewTagContainer = $('#addNewTagContainer');
         const $select2TagsContainer = $('#select2-tags-results').closest('.select2-container');
         const $searchField = $('.tags-select2-container .select2-search__field');
         if (show) {
             if (!$addNewTagContainer.is(':visible')) {
                 $addNewTagContainer.show();
             }
         } else {
             if ($addNewTagContainer.is(':visible')) {
                 $addNewTagContainer.hide();
             }
         }
         if ($select2TagsContainer.find('.select2-dropdown--above').length > 0) {
             $select2TagsContainer.css('top', ($searchField.offset().top - $select2TagsContainer.find('.select2-dropdown').first().outerHeight() - 6) + 'px');
         } else {
             $select2TagsContainer.css('top', ($searchField.offset().top + $searchField.outerHeight() + 1) + 'px');
         }
     }

    //TODO: This is activating on other select2 fields, but should not be
    //$("#s2id_autogen1").keyup(function(event){
    //    if(event.keyCode == 13){
    //        $("#addNew").click();
    //    }
    //});
}