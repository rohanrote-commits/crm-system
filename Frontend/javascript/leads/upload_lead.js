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
    alert("⚠ Unauthorized. Please login.");
    window.location.href = "/Frontend/html/login.html";
    return;
  }

  const payload = parseJwt(token);
  const userRole = payload?.role?.trim();

  // Open Modal on Button Click
  $('#importLeadBtn').on('click', function() {
    $('#importLeadsModal').modal('show');
  });

  // Handle Excel File Upload
  $('#importLeadsForm').on('submit', function(e) {
    e.preventDefault();

    const formData = new FormData(this);

    $.ajax({
      url: `http://localhost:8080/crm/lead/file/${payload.sub}`, 
      type: 'POST',
      data: formData,
      contentType: false,
      processData: false,
      success: function(response) {
        alert('Leads imported successfully!');
        $('#importLeadsModal').modal('hide');
        $('#importLeadsForm')[0].reset();
        $('#leadTable').DataTable().ajax.reload();
      },
      error: function(err) {
        alert('Error importing leads: ' + err.responseText);
      }
    });
  });

});
