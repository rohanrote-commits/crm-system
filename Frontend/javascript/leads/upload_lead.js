$(document).ready(function() {

  // Open Modal on Button Click
  $('#importLeadBtn').on('click', function() {
    $('#importLeadsModal').modal('show');
  });

  // Handle Excel File Upload
  $('#importLeadsForm').on('submit', function(e) {
    e.preventDefault();

    const formData = new FormData(this);

    $.ajax({
      url: 'http://localhost:8080/api/leads/file/{id}', // your backend endpoint for importing leads
      type: 'POST',
      data: formData,
      contentType: false,
      processData: false,
      success: function(response) {
        alert('Leads imported successfully!');
        $('#importLeadsModal').modal('hide');
        $('#importLeadsForm')[0].reset();
        $('#leadTable').DataTable().ajax.reload(); // reload your DataTable
      },
      error: function(err) {
        alert('Error importing leads: ' + err.responseText);
      }
    });
  });

});
