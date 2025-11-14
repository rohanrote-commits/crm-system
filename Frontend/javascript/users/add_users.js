$(document).ready(function () {

    // Parse JWT token
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

    // Get token from sessionStorage
    const token = sessionStorage.getItem("Authorization");
    if (!token) {
        alert("⚠ Unauthorized. Please login.");
        window.location.href = "/Frontend/html/login.html";
        return;
    }

    const payload = parseJwt(token);
    const role = payload?.role?.trim();
    console.log("Logged in role:", role);

    let editUserId = null; // store user id when editing


$('#userModal').on('hidden.bs.modal', function () {
    const form = $("#userForm");


    if (form.length) form[0].reset();

    const validator = form.data('validator') || form.validate(); // get existing validator or create one
    if (validator) {
        validator.resetForm();          // removes error labels and errorClass (for many setups)
        // also clear validator's errorList & errorMap just in case
        validator.errorList = [];
        validator.errorMap = {};
    }

    form.find(".error, .is-invalid").removeClass("error is-invalid");   // your errorClass + bootstrap invalid class
    form.find("[aria-invalid]").removeAttr("aria-invalid");


    $('#userModal').find('.error-message, label.error').remove();

    $("#addressFields").addClass("d-none");
    editUserId = null;
    $("#userModalLabel").text("Add User");
    $("#saveUserBtn").text("Save User");

    $('#userModal').find('.password-wrapper + .error-message, .password-wrapper + label.error').remove();
});
    $('.pw-toggle').on('click', function() {
    const input = $(this).siblings('input'); // input inside same wrapper
    const isHidden = input.attr('type') === 'password';
    input.attr('type', isHidden ? 'text' : 'password');

    $(this).attr('aria-pressed', isHidden);
    $(this).attr('aria-label', isHidden ? 'Hide password' : 'Show password');
  });

    // Show Add User Modal
    $("#addUser").click(function () {
        editUserId = null; 
        $("#userForm")[0].reset();
        $("#userModalLabel").text("Add User");
        $("#saveUserBtn").text("Save User");
        $("#addressFields").addClass("d-none"); 

        // Make all fields editable
        $("#userFirstName, #userLastName, #userEmail, #userRole, #userPassword, #userConfirmPassword").prop("readonly", false);
        $("#userPassword, #userConfirmPassword").prop("required", true).closest(".col-md-6").show();

        var userModalEl = document.getElementById('userModal');
        var userModal = new bootstrap.Modal(userModalEl);
        userModal.show();
    });

    // Edit User functionality
    $("#user-table").on("click", ".edit-user", function () {
        const rowData = $("#user-table").DataTable().row($(this).parents("tr")).data();

        if (!rowData) {
            alert("Failed to get user data");
            return;
        }

        editUserId = rowData.id; 
        console.log("Editing user:", rowData);

        $("#userModalLabel").text("Edit User");
        $("#saveUserBtn").text("Update User");

        // Fill form fields
        $("#userFirstName").val(rowData.firstName).prop("readonly", true);
        $("#userLastName").val(rowData.lastName).prop("readonly", true);
        $("#userEmail").val(rowData.email).prop("readonly", true);
        $("#userMobileNumber").val(rowData.mobileNumber).prop("readonly", false);
        $("#userAddress").val(rowData.address || "").prop("readonly", false);

        // Show/hide address fields
        if (rowData.address && rowData.address.trim() !== "") {
            $("#addressFields").removeClass('d-none');
            $("#userCity, #userState, #userCountry, #userPinCode").prop('required', true).prop("readonly", false);
        } else {
            $("#addressFields").addClass('d-none');
            $("#userCity, #userState, #userCountry, #userPinCode").prop('required', false);
        }

        $("#userCity").val(rowData.city || "");
        $("#userPinCode").val(rowData.pinCode || "");
        $("#userState").val(rowData.state || "");
        $("#userCountry").val(rowData.country || "");
        $("#userRole").val(rowData.role).prop("readonly", true);

        // Hide password fields in edit mode
        $("#userPassword, #userConfirmPassword").val("").prop("required", false).closest(".col-md-6").hide();

        // Show modal
        var userModalEl = document.getElementById('userModal');
        var userModal = new bootstrap.Modal(userModalEl);
        userModal.show();
    });

    // Show/hide address fields on input
    $("#userAddress").on('input', function () {
        if ($(this).val().trim() !== "") {
            $("#addressFields").removeClass('d-none');
            $("#userCity, #userState, #userCountry, #userPinCode").prop('required', true);
        } else {
            $("#addressFields").addClass('d-none');
            $("#userCity, #userState, #userCountry, #userPinCode").prop('required', false);
        }
    });

    // Populate Role dropdown based on logged-in user's role
    const roleSelect = $("#userRole");
    if (role === "MASTER_ADMIN") {
        roleSelect.append(`<option value="ADMIN">ADMIN</option><option value="USER">USER</option>`);
    } else if (role === "ADMIN") {
        roleSelect.append(`<option value="USER">USER</option>`);
    } else {
        roleSelect.append(`<option disabled>No Permission</option>`).prop("disabled", true);
    }

    // Custom validation methods
   $.validator.addMethod("namePattern", function(value, element) {
        return this.optional(element) || /^[A-Za-z ]{1,50}$/.test(value);
    }, "Name can contain letters and spaces only (max 50)");

    $.validator.addMethod("emailPattern", function(value, element) {
        return this.optional(element) || /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value);
    }, "Enter a valid email");

$.validator.addMethod("passwordPattern", function (value) {
    return value.length >= 8 && /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).*$/.test(value);
}, "Password should have at least one upper case, one lower case, one number and one special char");

    $.validator.addMethod("mobilePattern", function(value, element) {
        return this.optional(element) || /^[789]\d{9}$/.test(value);
    }, "Mobile must start with 7/8/9 and be 10 digits");

    $.validator.addMethod("addressPattern", function(value, element) {
        return this.optional(element) || /^[A-Za-z0-9 ,./#\-]{1,200}$/.test(value);
    }, "Address can contain letters, numbers, ,./#- (max 100)");

    $.validator.addMethod("pinPattern", function(value, element) {
        return this.optional(element) || /^[0-9]{6}$/.test(value);
    }, "Pin code must be 6 digits");

    // Form validation & submission
    $("#userForm").validate({
 rules: {
            firstName: { required: true, namePattern: true },
            lastName: { required: true, namePattern: true },
            email: { required: true, emailPattern: true },
            password: { required: true, passwordPattern: true , minlength : 8, maxlength : 16},
            confirmPassword: { required: true, equalTo: "#userPassword" },
            mobileNumber: { required: true, mobilePattern: true },
            address: { required: false, addressPattern: true },
            city: { required: function() { return $("#address").val().trim() !== ""; } },
            state: { required: function() { return $("#address").val().trim() !== ""; } },
            country: { required: function() { return $("#address").val().trim() !== ""; } },
            pinCode: { 
                required: function() { return $("#address").val().trim() !== ""; },
                pinPattern: true
            }
        },
        messages: {
            firstName: { required: "Please enter your first name" },
            lastName: { required: "Please enter your last name" },
            email: { required: "Please enter your email" },
            password: { required: "Please enter password", maxlength : "Password should not have more than 16 characters"},
            confirmPassword: { required: "Confirm your password", equalTo: "Passwords do not match"},
            mobileNumber: { required: "Enter mobile number" },
            address: { required: "Enter address" },
            city: { required: "Enter city" },
            state: { required: "Select state" },
            country: { required: "Select country" },
            pinCode: { required: "Enter pin code" }
        },
        errorClass: "error-message",
        errorPlacement: function (error, element) {
    error.insertAfter(element);
},

        highlight: function(element) {
            $(element).addClass('error');
        },
        unhighlight: function(element) {
            $(element).removeClass('error');
        },
        submitHandler: function () {
            let url, method, payload;

            if (editUserId) {
                // Edit mode → send only email, mobileNumber, address fields
                payload = {
                    email: $("#userEmail").val(),
                    mobileNumber: $("#userMobileNumber").val(),
                    address: $("#userAddress").val(),
                    city: $("#userCity").val(),
                    state: $("#userState").val(),
                    country: $("#userCountry").val(),
                    pinCode: $("#userPinCode").val()
                };
                url = `http://localhost:8080/crm/user/update-sub_user`;
                method = "PUT";
            } else {
            
                payload = {
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
                url = "http://localhost:8080/crm/user/register";
                method = "POST";
            }

            $.ajax({
                url: url,
                type: method,
                headers: { "Authorization": "Bearer " + token },
                contentType: "application/json",
                data: JSON.stringify(payload),
                success: () => {
                    showAlert(editUserId ? "User Updated Successfully" : "User Created Successfully","info");
                    location.reload();
                },
                error: xhr => {
                    if (xhr.status === 409) alert("Email or Mobile already exists");
                    else showAlert("Failed to save user","danger");
                }
            });
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
