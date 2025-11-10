$(document).ready(function (params) {
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

  $("#addLeadBtn").click(function () {
    $("#leadModalLabel").text("Add Lead");
    $("#saveLeadBtn").text("Add");
    $("#leadForm")[0].reset();
    $("#leadId").val("");
    $("#leadModal").modal("show");
  });

  $("#lead-table").on("click", ".edit-lead", function () {
    const rowData = $("#lead-table")
      .DataTable()
      .row($(this).parents("tr"))
      .data();

    $("#leadModalLabel").text("Edit Lead");
    $("#saveLeadBtn").text("Update Lead");

    // Fill data
    $("#leadId").val(rowData.id);
    $("#firstName").val(rowData.firstName);
    $("#lastName").val(rowData.lastName);
    $("#email").val(rowData.email);
    $("#mobileNumber").val(rowData.mobileNumber);
    $("#gstin").val(rowData.gstin);
    $("#leadStatus").val(rowData.leadStatus);
    $("#businessAddress").val(rowData.businessAddress);
    $("#description").val(rowData.description);

    $("#leadModal").modal("show");
  });

  $('#saveLeadBtn').click(function () {
  const leadId = $('#leadId').val();
  const leadData = {
    firstName: $('#firstName').val(),
    lastName: $('#lastName').val(),
    email: $('#email').val(),
    mobileNumber: $('#mobileNumber').val(),
    gstin: $('#gstin').val(),
    leadStatus: $('#leadStatus').val(),
    businessAddress: $('#businessAddress').val(),
    description: $('#description').val()
  };

  const method = leadId ? 'PUT' : 'POST';
  const url = leadId
    ? `http://localhost:8080/api/crm/lead/${leadId}`
    : `http://localhost:8080/api/crm/lead`;

  $.ajax({
    url,
    type: method,
    contentType: 'application/json',
    headers: { "Authorization": "Bearer " + token },
    data: JSON.stringify(leadData),
    success: function () {
      alert(leadId ? 'Lead updated successfully!' : 'Lead added successfully!');
      $('#leadModal').modal('hide');
      $('#lead-table').DataTable().ajax.reload();
    },
    error: function () {
      alert('Something went wrong. Please try again.');
    }
  });
});


});
