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
    window.location.href = "/crm/login";
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
  $("#confirmUpdateBtn").click(function () {

    const rowData = $(this).data("row");

    isEdit = true;

    $("#leadModalLabel").text("Edit Lead");
    $("#saveLeadBtn").text("Update Lead");

    // Fill form
    $("#leadId").val(rowData.id);
    $("#firstName").val(rowData.firstName);
    $("#lastName").val(rowData.lastName);
    $("#email").val(rowData.email);
    $("#mobileNumber").val(rowData.mobileNumber);
    $("#gstin").val(rowData.gstin);
    $("#leadStatus").val(rowData.leadStatus);
    $("#businessAddress").val(rowData.businessAddress);
    $("#description").val(rowData.description);

    // Clear old module selection
    $("input[name='interestedModules']").prop("checked", false);

    // Set new selected modules
    rowData.interestedModules.forEach(mod => {
        $(`input[name='interestedModules'][value='${mod}']`).prop("checked", true);
    });

    if (isEdit) {
        $("#email").prop("readOnly", true);
        $("#gstin").prop("readOnly", true);
    }

    $("#updateConfirmModal").modal("hide");
    $("#leadModal").modal("show");
});

  

  // Open Update Modal
$(document).on("click", ".edit-lead", function () {

    const table = $("#lead-table").DataTable();
    const rowData = table.row($(this).closest("tr")).data();

    $("#confirmUpdateBtn").data("row", rowData); 
    $("#updateConfirmModal").modal("show");
});


    $("#addLeadBtn").click(function () {
        $("#leadDropdown").slideToggle(200);
    });

    // Clicking outside closes dropdown
    $(document).click(function (e) {
        if (!$(e.target).closest(".section-buttons").length) {
            $("#leadDropdown").slideUp(200);
        }
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
  function (value, element) {
    return this.optional(element) || /^[A-Za-z0-9\s,.\-/#]{1,100}$/.test(value);
  },
  "Address can include letters, numbers & special chars (max 100 chars)"
);

$.validator.addMethod(
  "descriptionPattern",
  function (value, element) {
    return this.optional(element) || /^[A-Za-z0-9\s,.\-/#]{1,100}$/.test(value);
  },
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
           showPopup("Error", isEdit ? "Lead updated successfully!" : "Lead added successfully!", "success");
          showAlert(
            isEdit ? "Lead updated successfully!" : "Lead added successfully!","success"
          );
          $("#leadModal").modal("hide");
            $("#lead-table").DataTable().ajax.reload();
        },
        error: function (err) {
          showPopup("Error","Something went wrong. Please try again", "error");
         // showAlert("Something went wrong. Please try again.","warning");
        },
      });
      isEdit = false;
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

     function showPopup(title, message, iconType) {
    Swal.fire({
        title: title,
        text: message,
        icon: iconType, // success, error, warning, info
        confirmButtonText: 'OK'
    });
}