jQuery(function () {
    $("#header").load("/Frontend/html/components/header.html");


       // Toggle dropdown when profile image is clicked
  $("#profilePic").on("click", function (e) {
    $("#profileDropdown").toggleClass("show"); // Toggle visibility
  });
  
  // Hide dropdown when clicking anywhere outside
  $(document).on("click", function (e) {
    if (!$(e.target).closest(".profile-menu").length) {
      $("#profileDropdown").removeClass("show");
    }
  });
});