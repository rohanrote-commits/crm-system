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
    window.location.href = "/Frontend/html/login.html";
    return;
  }

  const payload = parseJwt(token);
  const userRole = payload?.role?.trim();
  
      // Profile dropdown
    $("#profilePic").click(function () {
        $("#profileDropdown").toggle();
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

    $.ajax({
      url: `http://localhost:8080/crm/lead/import/${payload.sub}`, 
      type: 'POST',
      headers: { Authorization: "Bearer " + token },
      data: formData,
      contentType: false,
      processData: false,
      success: function(response) {
        showAlert('Leads imported successfully!',"success");
        $('#importLeadsModal').modal('hide');
        $('#importLeadsForm')[0].reset();
        $('#leadTable').DataTable().ajax.reload();
      },
      error: function(err) {
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
