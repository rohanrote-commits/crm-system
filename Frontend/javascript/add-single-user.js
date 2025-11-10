$(document).ready(function () {
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

    // Editing 
     $("#user-table").on("click", ".edit-user", function () {
    isEdit = true;
    const rowData = $("#user-table")
      .DataTable()
      .row($(this).parents("tr"))
      .data();

    $("#leadModalLabel").text("Edit Lead");
    $("#saveLeadBtn").text("Update Lead");

    // Fill data
   // $("#leadId").val(rowData.id);
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

    // Get token from sessionStorage
    const token = sessionStorage.getItem("Authorization");
    if (!token) {
        alert("⚠ Unauthorized. Please login.");
        window.location.href = "/Frontend/html/login.html";
        return;
    }

    const payload = parseJwt(token);
    const role = payload?.role?.trim();
    console.log(role);

    // Show/Hide dependent address fields
    $("#address").on('input', function () {
        if ($(this).val().trim() !== "") {
            $("#addressFields").slideDown();
            $("#city, #state, #country, #pinCode").prop('required', true);
        } else {
            $("#addressFields").slideUp();
            $("#city, #state, #country, #pinCode").prop('required', false);
        }
    });

    // Custom validation patterns
    $.validator.addMethod("namePattern", value => /^[A-Za-z]{1,50}$/.test(value), "Only alphabets allowed (1–50 chars)");
    $.validator.addMethod("emailPattern", value => /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value), "Enter a valid email");
    $.validator.addMethod("mobilePattern", value => /^[789]\d{9}$/.test(value), "Mobile must start with 7/8/9 & be 10 digits");
    $.validator.addMethod("passwordPattern", value => /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,16}$/.test(value), "Password must be 8–16 chars with upper, lower, number, special char");
    $.validator.addMethod("pinPattern", value => /^[0-9]{6}$/.test(value), "Pin must be 6 digits");


    
    if (role === "MASTER_ADMIN") {
        $("#role").append(`<option value="ADMIN">ADMIN</option><option value="USER">USER</option>`);
    } else if (role === "ADMIN") {
        $("#role").append(`<option value="USER">USER</option>`);
    } else {
        $("#role").append(`<option disabled>No Permission</option>`).prop("disabled", true);
    }

    // jQuery Validation
    $("#addUserForm").validate({
        rules: {
            firstName: { required: true, namePattern: true },
            lastName: { required: true, namePattern: true },
            email: { required: true, emailPattern: true },
            mobileNumber: { required: true, mobilePattern: true },
            password: { required: true, passwordPattern: true },
            confirmPassword: { required: true, equalTo: "#password" },
            city: { required: () => $("#address").val().trim() !== "" },
            state: { required: () => $("#address").val().trim() !== "" },
            country: { required: () => $("#address").val().trim() !== "" },
            pinCode: { required: () => $("#address").val().trim() !== "", pinPattern: true },
            role: { required: true }
        },
        messages: {
            firstName: "Enter first name",
            lastName: "Enter last name",
            email: "Enter valid email",
            mobileNumber: "Enter valid mobile number of 10 digits",
            password: "Enter password of 8 characters to 16 characters,having atleast 1 uppercase, 1 lowecase, 1 special character and 1 Number",
            confirmPassword: "Passwords do not match",
            city: "Enter city",
            state: "Select state",
            country: "Select country",
            pinCode: "Enter pin code",
            role: "Select a role"
        },
        errorClass: "error-message",
        errorPlacement: (error, element) => error.insertAfter(element),
        highlight: element => $(element).addClass("error"),
        unhighlight: element => $(element).removeClass("error"),

        submitHandler: function () {
            const token = sessionStorage.getItem("Authorization");
            console.log(token);
            if (!token) {
                alert("Session expired. Please login again.");
                window.location.href = "/Frontend/html/login.html";
                return;
            }

            const user = {
                firstName: $("#firstName").val(),
                lastName: $("#lastName").val(),
                email: $("#email").val(),
                mobileNumber: $("#mobileNumber").val(),
                password: $("#password").val(),
                address: $("#address").val(),
                city: $("#city").val(),
                state: $("#state").val(),
                country: $("#country").val(),
                pinCode: $("#pinCode").val(),
                role: $("#role").val()
            };

            $.ajax({
                url: "http://localhost:8080/crm/user/register",
                type: "POST",
                headers: { "Authorization": "Bearer " + token },
                contentType: "application/json",
                data: JSON.stringify(user),
                success: () => {
                    alert("✅ User Created Successfully");
                    window.location.href = "add-single-user.html";
                },
                error: xhr => {
                    if (xhr.status === 409) alert("Email or Mobile already exists");
                    else alert("Failed to create user");
                     window.location.href = "add-single-user.html";
                }
            });
        }
    });

});
