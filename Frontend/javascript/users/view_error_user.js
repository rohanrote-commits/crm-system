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

   const payload = parseJwt(token);
  const id = sessionStorage.getItem("id");
  let docId;
  let uploadHistoryId;
  let oldEmail;

 let errorTable = $("#lead-table").DataTable({
    ajax: {
        url: `http://localhost:8080/crm/error/records/${id}`,
        type: "GET",
        headers: {
            Authorization: "Bearer " + token,
        },
        dataSrc: function (response) {
            docId = response.id;
            uploadHistoryId = response.uploadHistoryId;
            return response.errorUserList || [];
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
        { data: "firstName", title: "First Name" },
        { data: "lastName", title: "Last Name" },
        { data: "email", title: "Email" },
        { data: "mobileNumber", title: "Mobile" },
        { data: "address", title: "Address" },
        { data: "city", title: "City" },
        { data: "state", title: "State" },
        { data: "country", title: "Country" },
        { data: "pinCode", title: "Pin Code" },
        { data: "role", title: "Role" },
        { data: "registeredBy", title: "Registered By" },
        {
            data: "registeredOn",
            title: "Registered On",
            render: function (data) {
                return data ? new Date(data).toLocaleString("en-GB") : "-";
            }
        },
        {
            data: null,
            title: "Action",
            orderable: false,
            render: function (row) {
                return `
                    <div class="d-flex justify-content-center gap-2">
                        <button class="btn btn-sm btn-warning edit-lead" data-email="${row.email}">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-danger delete-lead" data-email="${row.email}">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>`;
            }
        }
    ],

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
        <option value="USER">USER</option>
    `;
} else if (payload.role === "ADMIN") {
    roleOptions += `<option value="USER">USER</option>`;
}

$('#userRole').html(roleOptions);


// Edit User (Error Record)
$("#lead-table").on("click", ".edit-lead", function () {
    const rowData = $("#lead-table")
        .DataTable()
        .row($(this).parents("tr"))
        .data();

    $("#userModalLabel").text("Edit User");
    $("#saveUserBtn").text("Update User");

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

    // Dynamically populate role options again based on logged-in user
    let roleOptions = '<option value="">Select Role</option>';
    if (payload.role === "MASTER_ADMIN") {
        roleOptions += `<option value="ADMIN">ADMIN</option><option value="USER">USER</option>`;
    } else if (payload.role === "ADMIN") {
        roleOptions += `<option value="USER">USER</option>`;
    }
    $('#userRole').html(roleOptions);

    // Set the current role of the record
    $("#userRole").val(rowData.role);

    oldEmail = rowData.email;
    $("#userModal").modal("show");
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
        userFirstName: { required: true, namePattern: true },
        userLastName: { required: true, namePattern: true },
        userEmail: { required: true, emailPattern: true },
        userPassword: { required: true, passwordPattern: true, minlength: 8, maxlength: 16 },
        userConfirmPassword: { required: true, equalTo: "#userPassword" },
        userMobileNumber: { required: true, mobilePattern: true },
        userAddress: { required: false, addressPattern: true },
        userCity: { required: function() { return $("#userAddress").val().trim() !== ""; } },
        userState: { required: function() { return $("#userAddress").val().trim() !== ""; } },
        userCountry: { required: function() { return $("#userAddress").val().trim() !== ""; } },
        userPinCode: { 
            required: function() { return $("#userAddress").val().trim() !== ""; },
            pinPattern: true
        },
        userRole: { required: true }
    },
    messages: {
        userFirstName: { required: "Please enter first name" },
        userLastName: { required: "Please enter last name" },
        userEmail: { required: "Please enter email" },
        userPassword: { required: "Please enter password", maxlength: "Max 16 characters allowed" },
        userConfirmPassword: { required: "Confirm password", equalTo: "Passwords do not match" },
        userMobileNumber: { required: "Enter mobile number" },
        userAddress: { required: "Enter address" },
        userCity: { required: "Enter city" },
        userState: { required: "Select state" },
        userCountry: { required: "Select country" },
        userPinCode: { required: "Enter pin code" },
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

        const url = `http://localhost:8080/crm/error/user/${(oldEmail)}/${uploadHistoryId}`;

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


      $("#downloadErrorFile").click(function (e) {
          e.preventDefault();
          const fileName = sessionStorage.getItem("file");
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
          deleteEmail = $(this).data("email");
          $("#deleteConfirmModal").modal("show");
      });
      // confirm delete
      $("#confirmDeleteBtn").click(function () {
        uploadHistoryId = sessionStorage.getItem("id");
          if (!deleteEmail) return;

          $.ajax({
              url: `http://localhost:8080/crm/error/user/${deleteEmail}/${uploadHistoryId}`,
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



});


