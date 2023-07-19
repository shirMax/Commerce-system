$(document).on('click', '.star-rating .bx', function() {
  var $star_rating = $('.star-rating .bx');
  var clicked_rating = parseInt($(this).data('rating'));
  $star_rating.each(function() {
    var star_rating = parseInt($(this).data('rating'));
    if (star_rating <= clicked_rating) {
      $(this).removeClass('bx-star').addClass('bxs-star');
    } else {
      $(this).removeClass('bxs-star').addClass('bx-star');
    }
  });
});

$(document).ready(function() {
  // Code to be executed when the document is ready
});
