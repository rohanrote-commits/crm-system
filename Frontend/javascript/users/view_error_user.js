jQuery(function () {
    let rowNumber = null;

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

   const payload = parseJwt(token);
  const id = sessionStorage.getItem("id");
  let docId;
  let uploadHistoryId = id;
  let oldEmail;

 let errorTable = $("#lead-table").DataTable({
    ajax: {
        url: `http://localhost:8080/crm/error/records/user/${id}`,
        type: "GET",
        headers: {
            Authorization: "Bearer " + token,
        },
        pageLength: 5,

        dataSrc: function (response) {
            console.log("Invalid User Response:", response);

            return response.map(item => ({
                rowNumber: item.rowNumber,
                errors: item.errors,

                firstName: item.user.firstName,
                lastName: item.user.lastName,
                email: item.user.email,
                mobileNumber: item.user.mobileNumber,
                address: item.user.address,
                city: item.user.city,
                state: item.user.state,
                country : item.user.country,
                pinCode :item.user.pinCode,
                role : item.user.role,
                registeredby : item.user.registeredby,
                registeredOn : item.user.registeredOn
            }));
        },
        error: function (xhr) {
            if (xhr.status === 401) {
                showAlert("Session expired. Login again.", "warning");
                sessionStorage.clear();
                window.location.href = "/Frontend/html/login.html";
                return;
            }
            if (xhr.status === 400) {
                showAlert("No Invalid Users for the Record", "info");
            } else {
                showAlert("No Invalid Users Found.", "danger");
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
            return highlightError(data, row, "firstName");
        }
    },
    {
        data: "lastName",
        title: "Last Name",
        render: function (data, type, row) {
            return highlightError(data, row, "lastName");
        }
    },
    {
        data: "email",
        title: "Email",
        render: function (data, type, row) {
            return highlightError(data, row, "email");
        }
    },
    {
        data: "mobileNumber",
        title: "Mobile",
        render: function (data, type, row) {
            return highlightError(data, row, "mobileNumber");
        }
    },
    // {
    //     data: "address",
    //     title: "Address",
    //     render: function (data, type, row) {
    //         return highlightError(data, row, "address");
    //     }
    // },
    // {
    //     data: "city",
    //     title: "City",
    //     render: function (data, type, row) {
    //         return highlightError(data, row, "city");
    //     }
    // },
    // {
    //     data: "state",
    //     title: "State",
    //     render: function (data, type, row) {
    //         return highlightError(data, row, "state");
    //     }
    // },
    // {
    //     data: "country",
    //     title: "Country",
    //     render: function (data, type, row) {
    //         return highlightError(data, row, "country");
    //     }
    // },
    // {
    //     data: "pinCode",
    //     title: "Pin Code",
    //     render: function (data, type, row) {
    //         return highlightError(data, row, "pinCode");
    //     }
    // },
    {
        data: "role",
        title: "Role",
        render: function (data, type, row) {
            return highlightError(data, row, "role");
        }
    },
    // {
    //     data: "registeredBy",
    //     title: "Registered By",
    //     render: function (data, type, row) {
    //         return highlightError(data, row, "registeredBy");
    //     }
    // },
    // {
    //     data: "registeredOn",
    //     title: "Registered On",
    //     render: function (data) {
    //         return data ? new Date(data).toLocaleString("en-GB") : "-";
    //     }
    // },
    {
        data: null,
        title: "Action",
        orderable: false,
        render: function (data,type,row) {
            return `
                <div class="d-flex justify-content-center gap-2">
                    <button class="btn btn-sm btn-warning edit-lead" data-email="${row.email}">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-danger delete-lead" data-row="${data.rowNumber}">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>`;
        }
    }
]
,

    pageLength: 10,
    lengthMenu: [10, 25, 50, 100],
    destroy: true,
    responsive: true,
    searching: true,
    paging: true,
    ordering: true,
    info: true,
    language: {
        emptyTable: "No error records found"
    }
});


function validateText(regex) {
    return function (data) {
        if (!regex.test(data)) {
            return `<span class="text-danger fw-bold">${data}</span>`;
        }
        return data;
    };
}
// Set Role options based on logged-in user's role
let roleOptions = '<option value="">Select Role</option>';

if (payload.role === "MASTER_ADMIN") {
    roleOptions += `
        <option value="ADMIN">ADMIN</option>
        <option value="BASIC">BASIC</option>
    `;
} else if (payload.role === "ADMIN") {
    roleOptions += `<option value="BASIC">BASIC</option>`;
}

$('#userRole').html(roleOptions);


// Edit User (Error Record)
$("#lead-table").on("click", ".edit-lead", function () {

    const rowData = $("#lead-table").DataTable().row($(this).parents("tr")).data();
    rowNumber  = rowData.rowNumber;
    deleteEmail = rowData.email;
    showUpdateConfirm().then((ok) => {
  if (!ok) return;
    const rowData = $("#lead-table")
        .DataTable()
        .row($(this).parents("tr"))
        .data();

    $("#userModalLabel").text("Edit User");
    $("#saveUserBtn").text("Update");

    $("#userId").val(rowData.id);
    $("#userFirstName").val(rowData.firstName);
    $("#userLastName").val(rowData.lastName);
    $("#userEmail").val(rowData.email);
    $("#userMobileNumber").val(rowData.mobileNumber);
    $("#userPassword").val(rowData.password);
    $("#userConfirmPassword").val(rowData.password);
    $("#userAddress").val(rowData.address);


    if (rowData.address && rowData.address.trim() !== "") {
        $("#addressFields").removeClass("d-none");
        $("#userCity").val(rowData.city);
        $("#userState").val(rowData.state);
        $("#userCountry").val(rowData.country);
        $("#userPinCode").val(rowData.pinCode);
    } else {
        $("#addressFields").addClass("d-none");
        $("#userCity").val("");
        $("#userState").val("");
        $("#userCountry").val("");
        $("#userPinCode").val("");
    }
    const fieldIdMap = {
        firstName: "userFirstName",
        lastName: "userLastName",
        email: "userEmail",
        mobileNumber: "userMobileNumber",
        password: "userPassword",
        confirmPassword: "userConfirmPassword",
        address: "userAddress",
        city: "userCity",
        state: "userState",
        country: "userCountry",
        pinCode: "userPinCode",
        role: "userRole"
    };

    // Set field values
    for (const field in fieldIdMap) {
        const inputId = fieldIdMap[field];
        $(`#${inputId}`).val(rowData[field] || "");
    }
     // Highlight backend errors
    for (const field in fieldIdMap) {
        const inputId = fieldIdMap[field];
        const errorMsg = rowData.errors && rowData.errors[field] ? rowData.errors[field] : null;

        if (errorMsg) {
            const $input = $(`#${inputId}`);
            $input.addClass("is-invalid");
            if ($input.next(".error-message").length === 0) {
                $input.after(`<span class="error-message">${errorMsg}</span>`);
            } else {
                $input.next(".error-message").text(errorMsg);
            }

            // Remove error when user starts typing
            $input.off("input.clearError").on("input.clearError", function () {
                $input.removeClass("is-invalid");
                $input.next(".error-message").remove();
            });
        }
    }

    // Dynamically populate role options again based on logged-in user
    let roleOptions = '<option value="">Select Role</option>';
    if (payload.role === "MASTER_ADMIN") {
        roleOptions += `<option value="ADMIN">ADMIN</option><option value="BASIC">BASIC</option>`;
    } else if (payload.role === "ADMIN") {
        roleOptions += `<option value="BASIC">BASIC</option>`;
    }
    $('#userRole').html(roleOptions);

    // Set the current role of the record
    $("#userRole").val(rowData.role);

    oldEmail = rowData.email;
    $("#userModal").modal("show");
});
});

// Show/hide dependent address fields dynamically
$("#userAddress").on('input', function() {
    if ($(this).val().trim() !== "") {
        $("#addressFields").slideDown();
        $("#userCity, #userState, #userCountry, #userPinCode").prop('required', true);
    } else {
        $("#addressFields").slideUp();
        $("#userCity, #userState, #userCountry, #userPinCode").prop('required', false);
    }
});

// Custom regex validation methods
$.validator.addMethod("namePattern", function(value, element) {
    return this.optional(element) || /^[A-Za-z ]{1,50}$/.test(value);
}, "Name can contain letters and spaces only (max 50)");

$.validator.addMethod("emailPattern", function(value, element) {
    return this.optional(element) || /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value);
}, "Enter a valid email");

$.validator.addMethod("passwordPattern", function (value) {
    return value.length >= 8 && /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).*$/.test(value);
}, "Password must have at least one upper, one lower, one number, and one special char");

$.validator.addMethod("mobilePattern", function(value, element) {
    return this.optional(element) || /^[789]\d{9}$/.test(value);
}, "Mobile must start with 7/8/9 and be 10 digits");

$.validator.addMethod("addressPattern", function(value, element) {
    return this.optional(element) || /^[A-Za-z0-9 ,./#\-]{1,200}$/.test(value);
}, "Address can contain letters, numbers, ,./#- (max 200)");

$.validator.addMethod("pinPattern", function(value, element) {
    return this.optional(element) || /^[0-9]{6}$/.test(value);
}, "Pin code must be 6 digits");

// User Form Validation
$("#userForm").validate({
    rules: {
        firstName: { required: true, namePattern: true },
        lastName: { required: true, namePattern: true },
        email: { required: true, emailPattern: true },
        password: { required: true, passwordPattern: true, minlength: 8, maxlength: 16 },
        confirmPassword: { required: true, equalTo: "#userPassword" },
        mobileNumber: { required: true, mobilePattern: true },
        address: { required: false, addressPattern: true },
        city: { required: function() { return $("#userAddress").val().trim() !== ""; } },
        state: { required: function() { return $("#userAddress").val().trim() !== ""; } },
        country: { required: function() { return $("#userAddress").val().trim() !== ""; } },
        pinCode: { 
            required: function() { return $("#userAddress").val().trim() !== ""; },
            pinPattern: true
        },
        role: { required: true }
    },
    messages: {
        userFirstName: { required: "Please enter first name" , namePattern:true},
        userLastName: { required: "Please enter last name",namePattern:true },
        userEmail: { required: "Please enter email",emailPattern:true },
        userPassword: { required: "Please enter password", maxlength: "Max 16 characters allowed",passwordPattern:true },
        userConfirmPassword: { required: "Confirm password", equalTo: "Passwords do not match",passwordPattern:true },
        userMobileNumber: { required: "Enter mobile number",mobilePattern : true },
        userAddress: { required: "Enter address",addressPattern :true },
        userCity: { required: "Enter city" },
        userState: { required: "Select state" },
        userCountry: { required: "Select country" },
        userPinCode: { required: "Enter pin code",pinPattern:true },
        userRole: { required: "Select role" }
    },
    errorClass: "error-message",
    errorPlacement: function(error, element) {
        if (element.attr("id") === "userPassword" || element.attr("id") === "userConfirmPassword") {
            error.insertAfter(element.closest(".password-wrapper"));
        } else {
            error.insertAfter(element);
        }
    },
    highlight: function(element) {
        $(element).addClass('error');
    },
    unhighlight: function(element) {
        $(element).removeClass('error');
    },
        onkeyup: function(element) {
        $(element).valid()},
    submitHandler: function(form) {
        // Your existing AJAX update code
        const userData = {
            firstName: $("#userFirstName").val(),
            lastName: $("#userLastName").val(),
            email: $("#userEmail").val(),
            mobileNumber: $("#userMobileNumber").val(),
            password: $("#userPassword").val(),
            address: $("#userAddress").val(),
            city: $("#userCity").val(),
            state: $("#userState").val(),
            country: $("#userCountry").val(),
            pinCode: $("#userPinCode").val(),
            role: $("#userRole").val()
        };

        const url = `http://localhost:8080/crm/error/user/${(rowNumber)}/${uploadHistoryId}`;

        $.ajax({
            url: url,
            type: "PUT",
            contentType: "application/json",
            headers: { Authorization: "Bearer " + token },
            data: JSON.stringify(userData),
            success: function () {
                showAlert("User updated successfully!", "success");
                $("#userModal").modal("hide");
                $("#lead-table").DataTable().ajax.reload();
            },
            error: function (xhr) {
                let error = xhr.responseJSON.message;

                showAlert(error, "warning");
            }
        });
    }
});
    $('.pw-toggle').on('click', function() {
    const input = $(this).siblings('input'); // input inside same wrapper
    const isHidden = input.attr('type') === 'password';
    input.attr('type', isHidden ? 'text' : 'password');

    $(this).attr('aria-pressed', isHidden);
    $(this).attr('aria-label', isHidden ? 'Hide password' : 'Show password');
  });

      $("#downloadErrorFile").click(function (e) {
        console.log("Now in downloadErrorFile");
          const fileName = sessionStorage.getItem("file");
          console.log(fileName);
          $.ajax({
            url: `http://localhost:8080/crm/history/user/error/${fileName}`,
            type: "GET",
            headers: {
              Authorization: "Bearer " + token,
            },
            xhrFields: {
              responseType: "blob",
            },
            success: function (data, status, xhr) {
              const filename = `${fileName.replace(" ", "_")}.xlsx`;
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
      let deleteEmail = null;
      $(document).on("click", ".delete-lead", function () {
        showDeleteConfirm().then((ok) => {
  if (!ok) return;
const rowData = $("#lead-table").DataTable().row($(this).parents("tr")).data();
    deleteEmail = rowData.email;       // keep your email
    rowNumber = rowData.rowNumber;     // set the rowNumber for API
          $("#deleteConfirmModal").modal("show");
      });
      // confirm delete
      $("#confirmDeleteBtn").click(function () {
        uploadHistoryId = sessionStorage.getItem("id");
          if (!deleteEmail) return;

          $.ajax({
              url: `http://localhost:8080/crm/error/user/${rowNumber}/${uploadHistoryId}`,
              type: "DELETE",
              data: { email: deleteEmail },
              headers: { "Authorization": "Bearer " + token },
              success: function () {
                  showAlert("User deleted successfully.", "success");
                  $("#lead-table").DataTable().ajax.reload(null, false);
              },
              error: function () {
                  showAlert("Error deleting User.", "warning");
              }
          });

          $("#deleteConfirmModal").modal("hide");
        });
      });




      
// Open file chooser when clicking the button
$('#uploadErrorFileBtn').on('click', function () {
    $('#uploadErrorFile').click();
});

// Upload file when selected
$('#uploadErrorFile').on('change', function () {

    let file = this.files[0];
    if (!file) return;

    const token = sessionStorage.getItem("Authorization");

    let formData = new FormData();
    formData.append("file", file);

    $.ajax({
        url: 'http://localhost:8080/crm/user/upload-user-file',
        type: 'POST',
        headers: { "Authorization": "Bearer " + token },
        data: formData,
        processData: false,
        contentType: false,
        success: function () {
            showAlert(" Data Inserted Successfully!","success");
            $('#uploadErrorFile').val('');
        },
        error: function (xhr) {
            let error = xhr.responseJSON.message;
            showAlert(error,"danger");
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
function showUpdateConfirm() {
  return new Promise((resolve) => {
    const modalEl = document.getElementById("updateConfirmModal");
    const modal = new bootstrap.Modal(modalEl);

    const confirmBtn = document.getElementById("confirmUpdateBtn");

    // When user clicks "Yes, Update"
    confirmBtn.onclick = function () {
      modal.hide();
      resolve(true);
    };

    // When modal closes without confirming
    modalEl.addEventListener(
      "hidden.bs.modal",
      () => resolve(false),
      { once: true }
    );

    modal.show();
  });
}
function showDeleteConfirm() {
  return new Promise((resolve) => {
    const modalEl = document.getElementById("deleteConfirmModal");
    const modal = new bootstrap.Modal(modalEl);

    const confirmBtn = document.getElementById("confirmDeleteBtn");

    // When user clicks "Yes, Delete"
    confirmBtn.onclick = function () {
      modal.hide();
      resolve(true);
    };

    // When modal closes without confirming
    modalEl.addEventListener(
      "hidden.bs.modal",
      () => resolve(false),
      { once: true }
    );

    modal.show();
  });
}



});
function highlightError(data, row, field) {
    return row.errors && row.errors[field]
        ? `<span class="text-danger fw-bold" title="${row.errors[field]}">${data}</span>`
        : data;
}



