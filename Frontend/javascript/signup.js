$(document).ready(function() {

    // Show/hide dependent address fields
    $("#address").on('input', function() {
        if ($(this).val().trim() !== "") {
            $("#addressFields").slideDown();
            $("#city, #state, #country, #pinCode").prop('required', true);
        } else {
            $("#addressFields").slideUp();
            $("#city, #state, #country, #pinCode").prop('required', false);
        }
    });

    // Custom regex validation methods
    $.validator.addMethod("namePattern", function(value, element) {
        return this.optional(element) || /^[A-Za-z ]{1,50}$/.test(value);
    }, "Name can contain letters and spaces only (max 50)");

    $.validator.addMethod("emailPattern", function(value, element) {
        return this.optional(element) || /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value);
    }, "Enter a valid email");

    $.validator.addMethod("passwordPattern", function(value, element) {
        return this.optional(element) || /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,16}$/.test(value);
    }, "Password must be 8-16 chars with upper, lower, number, special char");

    $.validator.addMethod("mobilePattern", function(value, element) {
        return this.optional(element) || /^[789]\d{9}$/.test(value);
    }, "Mobile must start with 7/8/9 and be 10 digits");

    $.validator.addMethod("addressPattern", function(value, element) {
        return this.optional(element) || /^[A-Za-z0-9 ,./#\-]{1,100}$/.test(value);
    }, "Address can contain letters, numbers, ,./#- (max 100)");

    $.validator.addMethod("pinPattern", function(value, element) {
        return this.optional(element) || /^[0-9]{6}$/.test(value);
    }, "Pin code must be 6 digits");

    // jQuery Validate setup
    $('#signUpForm').validate({
        rules: {
            firstName: { required: true, namePattern: true },
            lastName: { required: true, namePattern: true },
            email: { required: true, emailPattern: true },
            password: { required: true, passwordPattern: true },
            confirmPassword: { required: true, equalTo: "#password" },
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
            password: { required: "Please enter password" },
            confirmPassword: { required: "Confirm your password", equalTo: "Passwords do not match" },
            mobileNumber: { required: "Enter mobile number" },
            address: { required: "Enter address" },
            city: { required: "Enter city" },
            state: { required: "Select state" },
            country: { required: "Select country" },
            pinCode: { required: "Enter pin code" }
        },
        errorClass: "error-message",
        errorPlacement: function(error, element) {
            error.insertAfter(element);
        },
        highlight: function(element) {
            $(element).addClass('error');
        },
        unhighlight: function(element) {
            $(element).removeClass('error');
        },
        submitHandler: function(form) {
            const user = {
                firstName: $("#firstName").val(),
                lastName: $("#lastName").val(),
                email: $("#email").val(),
                password: $("#password").val(),
                confirmPassword: $("#confirmPassword").val(),
                mobileNumber: $("#mobileNumber").val(),
                address: $("#address").val(),
                city: $("#city").val(),
                state: $("#state").val(),
                country: $("#country").val(),
                pinCode: $("#pinCode").val()
            };

            $.ajax({
                url: "http://localhost:8080/crm/user/sign-up",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(user),
                success: function(response) {
                    alert("Sign Up Successful");
                    window.location.href = "/Frontend/html/login.html";
                },
                error: function(xhr) {
                    if (xhr.status === 409) alert("Email already registered");
                    else if (xhr.status === 400) alert("Passwords do not match");
                    else alert("Sign Up Failed: " + (xhr.responseText || "Server error"));
                }
            });
        }
    });

});
