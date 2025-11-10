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
              alert('Users imported successfully.');

              const modalEl = document.getElementById('importUsersModal');
              const modal = bootstrap.Modal.getInstance(modalEl);
              modal.hide();

              $('#importUsersForm')[0].reset();
              $('#user-table').DataTable().ajax.reload();
          },
          error: function(err) {
              alert('Error importing User: ' + err.responseText);
          }
      });
  });

});
