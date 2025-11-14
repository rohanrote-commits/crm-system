$(document).ready(function() {

  $('#bulkUpload').on('click', function() {
      const modal = new bootstrap.Modal(document.getElementById('importUsersModal'));
      modal.show();
  });
  const token = sessionStorage.getItem("Authorization");

  $('#importUsersForm').on('submit', function(e) {
      e.preventDefault();

      const formData = new FormData(this);

      $.ajax({
          url: 'http://localhost:8080/crm/user/upload-user-file',
          type: 'POST',
          data: formData,
          contentType: false,
          processData: false,
          headers: { "Authorization": "Bearer " + token },
          success: function(response) {
              showAlert('Users imported successfully.',"info");

              const modalEl = document.getElementById('importUsersModal');
              const modal = bootstrap.Modal.getInstance(modalEl);
              modal.hide();

              $('#importUsersForm')[0].reset();
              $('#user-table').DataTable().ajax.reload();
          },
          error: function(err) {
              showAlert('Error importing User: ' + err.responseText,"danger");
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
