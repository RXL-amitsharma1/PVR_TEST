$(function () {

    var iframe = document.createElement('iframe');
    var windowHeight = $(window).height() - 75;
    $(window).on('resize', function (evt) {
        var h = $(window).height();
        $("#office_frame").height(h - 75);
    });
    iframe.setAttribute('height', windowHeight + 'px');
    $("#mainContent").css("margin-bottom", 0);
    $("#mainContent").css("padding", 0);
    $("[type=submit]").attr("name", "");

});