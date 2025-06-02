// This script requires HandlebarJS
var pvr = pvr || {};

pvr.common_util = (function () {
    var base_url = function () {
        return '/reports'
    };

    var base_assets_url = function () {
        return base_url() + "/assets/app"
    };

    // And this is the definition of the custom function ​
    var render = function (tmpl_name, tmpl_data) {
        var templateObj = Handlebars.templates[tmpl_name+ '.hbs']
        var htmlElement = templateObj(tmpl_data);
        return htmlElement
    };

    var hbs_partial = function (tmpl_name) {
        var htmlElement = Handlebars.templates[tmpl_name+ '.hbs']
        return htmlElement
    };

    var preserve_line_break = function (value) {
        return value.replace(new RegExp("\n", 'g'), "<br>").replace(new RegExp("  ", 'g'), '&nbsp; ');
    };

    var downloadFile = function (url) {
        var link = document.createElement('a');
        document.body.appendChild(link);
        link.href = url;
        link.trigger('click');
    };

    var setCalanderPosition = function () {
        $('.datepicker').on('shown.bs.dropdown', function () {
            if ($(window).height() - ($(this).offset().top + $(this).find('.datepicker-calendar-wrapper').outerHeight()) < 0 && !($('.datepicker').closest('.modal'))) { //bottom position of calander display below
                $(this).find('.input-group-btn').addClass("dropup");
            }
        })
    };

    var sortByTextField = function (array, fieldName, ignoreCase) {
        if (!array || !fieldName) return;
        array.sort(function (lObject, rObject){
            var lValue = (lObject || {})[fieldName] || '';
            var rValue = (rObject || {})[fieldName] || '';
            if (ignoreCase !== false) {
                lValue = lValue.toLowerCase();
                rValue = rValue.toLowerCase();
            }
            return ((lValue < rValue) ? -1 : ((lValue > rValue) ? 1 : 0));
        });
    };

    const isJapaneseLocal = function () {
        return userLocale === JAPANESE_LOCALE;
    }

    return {
        base_url: base_url,
        base_assets_url: base_assets_url,
        render_tmpl: render,
        hbs_partial: hbs_partial,
        preserve_line_break: preserve_line_break,
        downloadFile: downloadFile,
        setCalanderPosition: setCalanderPosition,
        isJapaneseLocal: isJapaneseLocal,
        sortByTextField: sortByTextField
    };
})();
