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

  var isEdit = false;

  //Add Lead
  $("#addSingleLeadBtn").click(function () {
    isEdit = false;
    $("#leadModalLabel").text("Add Lead");
    $("#saveLeadBtn").text("Add");
    $("#leadForm")[0].reset();
    $("#leadId").val("");
    $("#leadModal").modal("show");
  });

  //Edit Lead
  $("#lead-table").on("click", ".edit-lead", function () {
    isEdit = true;
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
    rowData.interestedModules.forEach((mod) => {
      $(`input[name='interestedModules'][value='${mod}']`).prop("checked", true);
    });
    console.log(rowData.interestedModules);
    
    if (rowData.id != null) {
      $("#email").prop("readOnly", true);
      $("#gstin").prop("readOnly", true);
    }

    $("#leadModal").modal("show");
  });


  //Validation methods
  $.validator.addMethod(
    "namePattern",
    (value) => /^[A-Za-z ]{1,50}$/.test(value),
    "Only alphabets and spaces allowed (1–50 chars)"
  );

  $.validator.addMethod(
    "emailPattern",
    (value) => /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value),
    "Enter a valid email"
  );

  $.validator.addMethod(
    "mobilePattern",
    (value) => /^[789]\d{9}$/.test(value),
    "Mobile must start with 7/8/9 and be 10 digits"
  );

  $.validator.addMethod(
    "gstinPattern",
    (value) => /^[A-Z0-9]{15}$/.test(value),
    "Enter valid GSTIN"
  );

  $.validator.addMethod(
    "addressPattern",
    (value) => /^[A-Za-z0-9\s,.\-/#]{1,100}$/.test(value),
    "Address can include letters, numbers & special chars (max 100 chars)"
  );

  $.validator.addMethod(
    "descriptionPattern",
    (value) => /^[A-Za-z0-9\s,.\-/#]{1,100}$/.test(value),
    "Description can include letters, numbers & special chars (max 100 chars)"
  );

  // Add Lead or edit lead call
  $("#leadForm").validate({
    rules: {
      firstName: { required: true, namePattern: true },
      lastName: { required: true, namePattern: true },
      email: { required: true, emailPattern: true },
      mobileNumber: { required: true, mobilePattern: true },
      gstin: { required: true, gstinPattern: true },
      businessAddress: { required:false , addressPattern: true },
      description: {  required:false ,descriptionPattern: true },
    },
    messages: {
      firstName: { required: "Please enter first name" },
      lastName: { required: "Please enter last name" },
      email: { required: "Please enter email" },
      mobileNumber: { required: "Please enter mobile number" },
      gstin: { required: "Please enter GSTIN" },
    },
    errorElement: "span",
    errorClass: "text-danger",
    submitHandler: function () {
      const leadId = $("#leadId").val();
      const leadData = {
        firstName: $("#firstName").val(),
        lastName: $("#lastName").val(),
        email: $("#email").val(),
        mobileNumber: $("#mobileNumber").val(),
        gstin: $("#gstin").val(),
        description: $("#description").val(),
        businessAddress: $("#businessAddress").val(),
        leadStatus: $("#leadStatus").val(),
        user: payload?.email,
        interestedModules: $(".form-check-input:checked")
          .map(function () {
            return $(this).val();
          })
          .get(),
      };

      //Add Lead
      const method = isEdit ? "PUT" : "POST";
      const url = isEdit
        ? `http://localhost:8080/crm/lead/${leadData.email}`
        : `http://localhost:8080/crm/lead/`;

      $.ajax({
        url,
        type: method,
        contentType: "application/json",
        headers: { Authorization: "Bearer " + token },
        data: JSON.stringify(leadData),
        success: function () {
          showAlert(
            isEdit ? "Lead updated successfully!" : "Lead added successfully!","success"
          );
          $("#leadModal").modal("hide");
          $("#lead-table").DataTable().ajax.reload();
        },
        error: function (err) {
          showAlert("Something went wrong. Please try again.","warning");
        },
      });
    },
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
