$(function () {
  $(".comment").shorten({
    showChars: 120,
  });
  addReadMoreButton(".comment", 120);
});
