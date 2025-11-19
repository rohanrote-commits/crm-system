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
        window.location.href = "/Frontend/html/login.html";
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

//   let errorTable = $("#lead-table").DataTable({
//     ajax: {
//         url: `http://localhost:8080/crm/error/records/${id}`,
//         type: "GET",
//         headers: {
//             Authorization: "Bearer " + token,
//         },

//         dataSrc: function (response) {
//             console.log("Leads fetched:", response);

//             docId = response.id;
//             uploadHistoryId = response.uploadHistoryId;

//             return response.errorsList || [];
//         },

//         error: function (xhr) {
//           errorTable.clear().draw();
//             if (xhr.status === 401) {
//                 showAlert("Session expired. Login again.", "warning");
//                 sessionStorage.clear();
//                 window.location.href = "/Frontend/html/login.html";
//                 return;
//             }
//             if(xhr.status===400){
//               showAlert("No Invalid Leads for the Record","info")
//             }
//             else{
//             showAlert("No Invalid Leads Found.", "danger");
//             }
//         }
//     },

//     columns: [
//         {
//             data: null,
//             title: "Sr.No.",
//             orderable: false,
//             render: function (data, type, row, meta) {
//                 return meta.row + meta.settings._iDisplayStart + 1;
//             }
//         },

//         { data: "firstName", title: "First Name", render: validateText(/^[A-Za-z ]{1,50}$/) },
//         { data: "lastName", title: "Last Name", render: validateText(/^[A-Za-z ]{1,50}$/) },
//         { data: "email", title: "Email", render: validateText(/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/) },
//         { data: "mobileNumber", title: "Mobile", render: validateText(/^[789]\d{9}$/) },
//         { data: "gstin", title: "GSTIN", render: validateText(/^[A-Z0-9]{15}$/) },
//         { data: "description", title: "Description", render: validateText(/^[A-Za-z0-9\s,.\-/#]{1,100}$/) },
//         { data: "businessAddress", title: "Address", render: validateText(/^[A-Za-z0-9\s,.\-/#]{1,100}$/) },

//         {
//             data: "interestedModules",
//             title: "Interested Modules",
//             orderable: false,
//             render: function (data) {
//                 return data && data.length ? data.join(", ") : "-";
//             }
//         },

//         {
//             data: null,
//             title: "Action",
//             orderable: false,
//             render: function (row) {
//                 return `
//                     <div class="d-flex justify-content-center gap-2">
//                         <button class="btn btn-sm btn-warning edit-lead" data-email="${row.email}" title="Edit error record">
//                             <i class="bi bi-pencil"></i>
//                         </button>
//                         <button class="btn btn-sm btn-danger delete-lead" data-email="${row.email}" title="Delete error record">
//                             <i class="bi bi-trash"></i>
//                         </button>
//                     </div>`;
//             }
//         }
//     ],
//     pageLength: 10,
//     lengthMenu: [10, 25, 50, 100],
//     destroy: true,
//     responsive: true,
//     searching: true,
//     paging: true,
//     ordering: true,
//     info: true,
//     language: {
//         emptyTable: "No error records found",
//     }
// });

// function validateText(regex) {
//     return function (data) {
//         if (!regex.test(data)) {
//             return `<span class="text-danger fw-bold">${data}</span>`;
//         }
//         return data;
//     };
// }

       //Edit Lead
     
 let errorTable = $("#lead-table").DataTable({
    ajax: {
        url: `http://localhost:8080/crm/error/records/${id}`,
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

                interestedModules: item.lead.interestedProducts?.map(p => p.moduleName) || []
            }));
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
      console.log(rowNumber ,uploadHistoryId)
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
      console.log(rowNumber ,uploadHistoryId)
      //Edit Lead
      const url =  `http://localhost:8080/crm/error/${rowNumber}/${uploadHistoryId}`;
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
          showAlert("Something went wrong. Please try again.","warning");
        },
      });
    },
  });



      $("#downloadErrorFile").click(function (e) {
          e.preventDefault();
          const fileName = sessionStorage.getItem("file");
          $.ajax({
            url: `http://localhost:8080/crm/history/error/${fileName}`,
            type: "GET",
            headers: {
              Authorization: "Bearer " + token,
            },
            xhrFields: {
              responseType: "blob",
            },
            success: function (data, status, xhr) {
              const filename = `${fileName.replace(" ", "_")}`;
              const blob = new Blob([data], {
                type: xhr.getResponseHeader("Content-Type"),
              });
              // Create a download link dynamically
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement("a");
              a.href = url;
              a.download = filename;
              document.body.appendChild(a);
              a.click();
              a.remove();
              window.URL.revokeObjectURL(url);
              showAlert("Error File downloded successfully", "success");
            },
            error: function (xhr) {
              if (xhr.status === 401) {
                showAlert("Session expired. Please login again.", "warning");
                sessionStorage.clear();
                window.location.href = "/Frontend/html/login.html";
              } else {
                console.error("Token used:", token);
                showAlert("Error while downloading the Error File.", "danger");
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
        url: `http://localhost:8080/crm/error/${selectedRowNumber}/${uploadHistoryId}`,
        type: "DELETE",
        headers: { "Authorization": "Bearer " + token },
        success: function () {
            showAlert("Lead deleted successfully", "success");
            $("#lead-table").DataTable().ajax.reload(null, false);
        },
        error: function (err) {
            console.log(err);
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

});


