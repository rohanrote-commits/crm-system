$(document).ready(function() {

  // Open modal
  $("#openModalBtn").on("click", function() {
    $("#leadModal").fadeIn(200).css("display", "flex");
  });

  // Close modal
  $("#closeModalBtn").on("click", function() {
    $("#leadModal").fadeOut(200);
  });

  // Close when clicking outside modal
  $(window).on("click", function(e) {
    if ($(e.target).is("#leadModal")) {
      $("#leadModal").fadeOut(200);
    }
  });

  // Handle form submission
  $("#leadForm").on("submit", function(e) {
    e.preventDefault();

    // Collect form data
    const leadData = {
      firstName: $("#firstName").val(),
      lastName: $("#lastName").val(),
      email: $("#email").val(),
      mobileNumber: $("#mobileNumber").val(),
      gstin: $("#gstin").val(),
      leadStatus: $("#leadStatus").val(),
      description: $("#description").val(),
      businessAddress: $("#businessAddress").val(),
      interestedModules: $("#modules").val()
    };

    console.log("Lead Added:", leadData);

    // Close modal after saving
    $("#leadModal").fadeOut(200);

    // Optional: Clear form
    $("#leadForm")[0].reset();

    // ✅ You can now send `leadData` to your backend using AJAX
    // $.post("/api/leads", leadData, function(response) { ... });
  });

});
