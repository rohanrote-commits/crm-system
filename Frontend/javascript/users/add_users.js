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

    // Show Add User Modal
    $("#addUser").click(function () {
        editUserId = null; // reset edit mode
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

        editUserId = rowData.id; // store user id for update
        console.log("Editing user:", rowData);

        // Set modal title and button
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
    $.validator.addMethod("mobilePattern", value => /^[789]\d{9}$/.test(value), "Mobile must start with 7/8/9 & be 10 digits");
    $.validator.addMethod("pinPattern", value => /^[0-9]{6}$/.test(value), "Pin must be 6 digits");

    // Form validation & submission
    $("#userForm").validate({
        rules: {
            firstName: { required: true }, // for add user
            lastName: { required: true },
            email: { required: true, email: true },
            mobileNumber: { required: true, mobilePattern: true },
            password: { required: function () { return !editUserId; } },
            confirmPassword: { equalTo: "#userPassword" },
            city: { required: () => $("#userAddress").val().trim() !== "" },
            state: { required: () => $("#userAddress").val().trim() !== "" },
            country: { required: () => $("#userAddress").val().trim() !== "" },
            pinCode: { required: () => $("#userAddress").val().trim() !== "", pinPattern: true }
        },
        errorClass: "error-message",
        errorPlacement: (error, element) => error.insertAfter(element),
        highlight: element => $(element).addClass("error"),
        unhighlight: element => $(element).removeClass("error"),

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
                // Add mode → send full DTO
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
                    alert(editUserId ? "✅ User Updated Successfully" : "✅ User Created Successfully");
                    location.reload();
                },
                error: xhr => {
                    if (xhr.status === 409) alert("Email or Mobile already exists");
                    else alert("Failed to save user");
                }
            });
        }
    });

});
