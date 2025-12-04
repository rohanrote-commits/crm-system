jQuery(function () {

    const token = sessionStorage.getItem("Authorization");
      // Parse JWT
    function parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
                '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
            ).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            return null;
        }
    }
    if (!token) {
        showAlert("Unauthorized. Please login.","danger");
        window.location.href = "/crm/login";
        return;
    }

  $(".profile-pic").click(function () {
    $("#profileDropdown").toggleClass("show");
  });

    // Close profile dropdown when clicked outside
    $(document).click(function (event) {
        if (!$(event.target).closest(".profile-menu").length) {
            $("#profileDropdown").removeClass("show");
        }
    });

   const payload = parseJwt(token);
  const id = sessionStorage.getItem("id");
  let docId;
  let uploadHistoryId = id;
  let rowNumber;


 let errorTable = $("#lead-table").DataTable({
    ajax: {
        url:LEAD_API.ERROR_LEADS_BY_UPLOAD_HISTORY_ID(uploadHistoryId),
        type: "GET",
        headers: {
            Authorization: "Bearer " + token,
        },

        dataSrc: function (response) {
            console.log("Invalid Leads Response:", response);
            return response.map(item => ({
                rowNumber: item.rowNumber,
                errors: item.errors,
                firstName: item.lead.firstName,
                lastName: item.lead.lastName,
                email: item.lead.email,
                mobileNumber: item.lead.mobileNumber,
                gstin: item.lead.gstin,
                description: item.lead.description,
                businessAddress: item.lead.businessAddress,

                interestedModules: item.lead.interestedProducts?.map(p => p.productName) || []
            }));
        },
        error: function (xhr) {
          errorTable.clear().draw();
            if (xhr.status === 401) {
                showPopup("Error","Session expired. Login again.", "error");
                showAlert("Session expired. Login again.", "warning");
                sessionStorage.clear();
                window.location.href = "/crm/login";
                return;
            }
            if(xhr.status===400){
               showPopup("Error","No Invalid Leads Found for this File.", "error",() => {
                   window.history.back();
               });
              showAlert("No Invalid Leads for the Record","info")
            }
            else{
              showPopup("Error","No Invalid Leads Found.", "error",() => {
                  window.history.back();
              });
              showAlert("No Invalid Leads Found.", "danger");
            }
        }
    },

    columns: [
        {
            data: null,
            title: "Sr.No.",
            orderable: false,
            render: function (data, type, row, meta) {
                return meta.row + meta.settings._iDisplayStart + 1;
            }
        },

        {
            data: "firstName",
            title: "First Name",
            render: function (data, type, row) {
                return row.errors.firstName
                    ? `<span class="text-danger fw-bold" title="${row.errors.firstName}">${data}</span>`
                    : data;
            }
        },

        {
            data: "lastName",
            title: "Last Name",
            render: function (data, type, row) {
                return row.errors.lastName
                    ? `<span class="text-danger fw-bold" title="${row.errors.lastName}">${data}</span>`
                    : data;
            }
        },

        {
            data: "email",
            title: "Email",
            render: function (data, type, row) {
                return row.errors.email
                    ? `<span class="text-danger fw-bold" title="${row.errors.email}">${data}</span>`
                    : data;
            }
        },

        {
            data: "mobileNumber",
            title: "Mobile",
            render: function (data, type, row) {
                return row.errors.mobileNumber
                    ? `<span class="text-danger fw-bold" title="${row.errors.mobileNumber}">${data}</span>`
                    : data;
            }
        },

        {
            data: "gstin",
            title: "GSTIN",
            render: function (data, type, row) {
                return row.errors.gstin
                    ? `<span class="text-danger fw-bold" title="${row.errors.gstin}">${data}</span>`
                    : data;
            }
        },

        { data: "description", title: "Description" },

        { data: "businessAddress", title: "Address" },

        {
            data: "interestedModules",
            title: "Interested Modules",
            render: function (data) {
                return data && data.length ? data.join(", ") : "-";
            }
        },

        {
            data: null,
            title: "Action",
            orderable: false,
            render: function (data, type, row) {
                return `
                    <div class="d-flex justify-content-center gap-2">
                        <button class="btn btn-sm btn-warning edit-lead" data-email="${row.email}" title="Edit error record">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-danger delete-lead" data-row="${data.rowNumber}" title="Delete error record">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>`;
            }
        }
    ],

    pageLength: 10,
    destroy: true
});

     
   $("#lead-table").on("click", ".edit-lead", function () {

    const table = $("#lead-table").DataTable();
    const rowData = table.row($(this).closest("tr")).data();

    $("#confirmUpdateBtn").data("row", rowData); 
    $("#updateConfirmModal").modal("show");
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
    console.log(rowData)
     rowNumber = rowData.rowNumber;
    $("#updateConfirmModal").modal("hide");
    $("#leadModal").modal("show");
});
      


         //Validation methods
  $.validator.addMethod(
    "namePattern",
    (value) => REGX_CONSTANT.NAME.test(value),
    ERROR_MESSAGE_CONSTANTS.INVALID_NAME
  );

  $.validator.addMethod(
    "emailPattern",
    (value) => REGX_CONSTANT.EMAIL.test(value),
    ERROR_MESSAGE_CONSTANTS.INVALID_EMAIL
  );

  $.validator.addMethod(
    "mobilePattern",
    (value) => REGX_CONSTANT.MOBILE.test(value),
    ERROR_MESSAGE_CONSTANTS.INVALID_MOBILE_NUMBER
  );

  $.validator.addMethod(
    "gstinPattern",
    (value) => REGX_CONSTANT.EMAIL.test(value),
    ERROR_MESSAGE_CONSTANTS.INVALID_GSTIN
  );
  $.validator.addMethod(
    "addressPattern",
    function (value, element) {
      return this.optional(element) || REGX_CONSTANT.ADDRESS_DESC.test(value);
    },
    ERROR_MESSAGE_CONSTANTS.INVALID_ADDRESS
  );

  $.validator.addMethod(
    "descriptionPattern",
    function (value, element) {
      return this.optional(element) || REGX_CONSTANT.ADDRESS_DESC.test(value);
    },
    ERROR_MESSAGE_CONSTANTS.INVALID_DESCRIPTION
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
      //Edit Lead
      const url = LEAD_API.UPDATE_ERROR_LEADS(rowNumber,uploadHistoryId) ;
      $.ajax({
        url,
        type: "PUT",
        contentType: "application/json",
        headers: { Authorization: "Bearer " + token },
        data: JSON.stringify(leadData),
        success: function () {
          console.log(leadData);
          
          showAlert(
            "Lead updated successfully!","success"
          );
          $("#leadModal").modal("hide");
          $("#lead-table").DataTable().ajax.reload();
        },
        error: function (err) {
          console.log(err);
          showPopup("Error","Something went wrong. Please try again.","error");
          showAlert("Something went wrong. Please try again.","warning");
        },
      });
    },
  });



      $("#downloadErrorFile").click(function (e) {
          e.preventDefault();
          const fileName = "Lead_Error"
          console.log(uploadHistoryId)
          $.ajax({
            url: LEAD_API.ERROR_FILE_BY_HISTORY_ID(uploadHistoryId),
            type: "GET",
            headers: {
              Authorization: "Bearer " + token,
            },
            xhrFields: {
              responseType: "blob",
            },
              success: function (data, status, xhr) {
                  // Extract filename from response header if available
                  let disposition = xhr.getResponseHeader("Content-Disposition");
                  let filename = "Lead_Error"; // fallback filename

                  if (disposition && disposition.indexOf("filename=") !== -1) {
                      let filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                      let matches = filenameRegex.exec(disposition);
                      if (matches != null && matches[1]) {
                          filename = matches[1].replace(/['"]/g, '');
                      }
                  }

                  const blob = new Blob([data], {
                      type: xhr.getResponseHeader("Content-Type"),
                  });

                  const url = window.URL.createObjectURL(blob);
                  const a = document.createElement("a");
                  a.href = url;
                  a.download = filename;
                  document.body.appendChild(a);
                  a.click();
                  a.remove();
                  window.URL.revokeObjectURL(url);

                  showAlert("Error File downloaded successfully", "success");
              },
            error: function (xhr) {
              if (xhr.status === 401) {
                 showPopup("Error","Session expired. Login again.", "error");
                showAlert("Session expired. Please login again.", "warning");
                sessionStorage.clear();
                window.location.href = "/crm/login";
              } else {
                console.error("Token used:", token);
                console.error("Error downloading file:", xhr);
                showAlert("Error while downloading the Error File.", "danger");
                showPopup("Error","Error while downloading the Error File.", "error");
              }
            },
          });
        });

       //delete error lead
      let selectedRowNumber = null;
      $(document).on("click", ".delete-lead", function () {
           selectedRowNumber = $(this).data("row"); 
          $("#deleteConfirmModal").modal("show");
      });
      // confirm delete
      $("#confirmDeleteBtn").click(function () {
         const uploadHistoryId = sessionStorage.getItem("id");

    if (!selectedRowNumber || !uploadHistoryId) {
        showAlert("Missing rowNumber or uploadHistoryId", "warning");
        return;
    }

    $.ajax({
        url: LEAD_ERROR_API.DELETE_ERROR_LEADS(selectedRowNumber,uploadHistoryId),
        type: "DELETE",
        headers: { "Authorization": "Bearer " + token },
        success: function () {
            showAlert("Lead deleted successfully", "success");
            $("#lead-table").DataTable().ajax.reload(null, false);
        },
        error: function (err) {
            console.log(err);
            showPopup("Error","Error While Deleting Lead", "error");
            showAlert("Error deleting lead", "warning");
        }
    });

    $("#deleteConfirmModal").modal("hide");
      });

          // Open Modal on Button Click
    $('#uploadErrorFile').on('click', function() {
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
          showAlert('Leads imported successfully!',"success");
          $('#importLeadsModal').modal('hide');
          $('#importLeadsForm')[0].reset();
            $("#lead-table").DataTable().ajax.reload();
        },
        error: function(err) {
          showPopup("Error","Error While importing Lead", "error");
            $('#uploadLeadsModal').modal('hide');
        }
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

    function showPopup(title, message, iconType,callback = null) {
    Swal.fire({
        title: title,
        text: message,
        icon: iconType, // success, error, warning, info
        confirmButtonText: 'OK'
    }).then(() => {
        if (callback) callback();  // run custom logic
    });
}

});


