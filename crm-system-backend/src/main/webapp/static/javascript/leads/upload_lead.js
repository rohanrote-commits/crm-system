$(document).ready(function() {

  function parseJwt(token) {
    try {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join("")
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  }

  // Get token from sessionStorage
  const token = sessionStorage.getItem("Authorization");
  if (!token) {
    showAlert("⚠ Unauthorized. Please login.","danger");
    window.location.href = "/crm/login";
    return;
  }

  const payload = parseJwt(token);
  const userRole = payload?.role?.trim();
  
    $(".profile-pic").click(function () {
    $("#profileDropdown").toggleClass("show");
  });

    // Close profile dropdown when clicked outside
    $(document).click(function (event) {
        if (!$(event.target).closest(".profile-menu").length) {
            $("#profileDropdown").hide();
        }
    });

  // Open Modal on Button Click
  $('#uploadTemplateBtn').on('click', function() {
    $('#uploadLeadsModal').modal('show');
  });

  // Handle Excel File Upload
  $('#uploadLeadsForm').on('submit', function(e) {
    e.preventDefault();

    const formData = new FormData(this);
      formData.append("userId", payload.sub);
    $.ajax({
      url: LEAD_API.BULK_IMPORT,
      type: 'POST',
      headers: { Authorization: "Bearer " + token },
      data: formData,
      contentType: false,
      processData: false,
      success: function(response) {
        showPopup("Success","Leads imported successfully!", "success");
        //showAlert('Leads imported successfully!',"success");
        $('#importLeadsForm')[0].reset();
        $('#leadTable').DataTable().ajax.reload();
        $("#upload-table").DataTable().ajax.reload();
          $('#uploadLeadsModal').modal('hide');
      },
      error: function(err) {
         showPopup("Error","Error while importing the leads", "error");
        showAlert('Error importing leads: ' + err.responseText,"danger");
      }
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
    
     function showPopup(title, message, iconType) {
    Swal.fire({
        title: title,
        text: message,
        icon: iconType, // success, error, warning, info
        confirmButtonText: 'OK'
    });
  }