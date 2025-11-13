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
 $("#downloadTemplate").on("click", function () {
  const token = sessionStorage.getItem("Authorization");
  const templateName = "Lead_Teamplate";
  $.ajax({
    url: "http://localhost:8080/crm/files/lead-template",
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
       showAlert("File downloded successfully","success")
    },
    error: function (xhr) {
      if (xhr.status === 401) {
        showAlert("Session expired. Please login again.","warning");
        sessionStorage.clear();
        window.location.href = "/Frontend/html/login.html";
      } else {
        console.error("Token used:", token);
        showAlert("Error while downloading the template.","danger");
      }
    },
  });
});

});

    // Function to show bootstrap alert dynamically
    function showAlert(message, type) {
      const alertContainer = $("#alert-container");
      const alert = $(`
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
          ${message}
          <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
      `);
      alertContainer.append(alert);

      // Auto remove after 5 seconds
      setTimeout(() => {
        alert.alert('close');
      }, 5000);
    }
