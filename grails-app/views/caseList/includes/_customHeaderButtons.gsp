<i class="pull-right md-lg md-filter rxmain-dropdown-settings column-filter-toggle"
   title="Column Level Filters"></i>
<i class="pull-right md-lg md-refresh rxmain-dropdown-settings" id="resetTable"
   title="Reset Preferences"></i>

<g:javascript>
    $(".column-filter-toggle").on('click', function () {
        $(".column_filter_input").toggle("slow", function(){
            // check paragraph once toggle effect is completed
            if(!$(".column_filter_input").is(":visible")){
                $(".column_filter_input").trigger('removeSearch');
            }
        });
    });
</g:javascript>