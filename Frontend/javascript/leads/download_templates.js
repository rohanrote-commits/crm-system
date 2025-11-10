$(document).ready(function () {
  const token = sessionStorage.getItem("Authorization");

  $(document).ready(function () {
    $("#templateMenuToggle").on("click", function (e) {
      e.preventDefault();
      $("#templateDropdown").slideToggle(150);
    });

    // Optional: close dropdown when clicking outside
    $(document).on("click", function (e) {
      if (!$(e.target).closest(".template-menu").length) {
        $("#templateDropdown").slideUp(150);
      }
    });
  });
  $(".drop-item-template").on("click", function () {
  const token = sessionStorage.getItem("Authorization");
  const templateName = $(this).text().trim();
  let downloadUrl = "";

  // Choose correct backend URL based on button
  if (templateName === "User Template") {
    downloadUrl = "http://localhost:8080/crm/files/user-template";
  } else if (templateName === "Lead Template") {
    downloadUrl = "http://localhost:8080/crm/files/lead-template";
  } else if (templateName === "Report Template") {
    downloadUrl = "http://localhost:8080/crm/files/report-template";
  }

  $.ajax({
    url: downloadUrl,
    type: "GET",
    headers: {
      Authorization: "Bearer " + token,
    },
    xhrFields: {
      responseType: "blob", 
    },
    success: function (data, status, xhr) {
      const filename = `${templateName.replace(" ", "_")}.xlsx`;
      const blob = new Blob([data], {
        type: xhr.getResponseHeader("Content-Type"),
      });

      // Create a download link dynamically
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

      $("#templateDropdown").slideUp(150);
    },
    error: function (xhr) {
      if (xhr.status === 401) {
        alert("Session expired. Please login again.");
        sessionStorage.clear();
        window.location.href = "/Frontend/html/login.html";
      } else {
        console.error("Token used:", token);
        alert("Error while downloading the template.");
      }
    },
  });
});

});
