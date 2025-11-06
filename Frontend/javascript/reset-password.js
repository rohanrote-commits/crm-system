$(document).ready(function () {

    // ✅ Custom password validation (8-16 chars, upper, lower, number, special)
    $.validator.addMethod("passwordPattern", function (value) {
        return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,16}$/.test(value);
    }, "Password must be 8-16 chars with upper, lower, number, special char");

    // ✅ Custom email validation
    $.validator.addMethod("emailPattern", function (value) {
        return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value);
    }, "Enter a valid email");

    // ✅ Form validation
    $("#resetPasswordForm").validate({
        rules: {
            resetEmail: { required: true, emailPattern: true },
            newPassword: { required: true, passwordPattern: true },
            confirmPassword: { required: true, equalTo: "#newPassword" }
        },
        messages: {
            resetEmail: { required: "Email is required" },
            newPassword: { required: "Enter new password" },
            confirmPassword: { required: "Confirm password", equalTo: "Passwords do not match" }
        },
        errorClass: "error-message",
        highlight: function (element) { $(element).addClass("error"); },
        unhighlight: function (element) { $(element).removeClass("error"); },

        submitHandler: function () {
            let user = {
                email: $("#resetEmail").val(),
                password: $("#newPassword").val()
            };

            $.ajax({
                url: "http://localhost:8080/crm/user/forget", 
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(user),
                success: function () {
                    alert("✅ Password reset successfully");
                    window.location.href = "login.html";
                },
                error: function (xhr) {
                    if (xhr.status === 404) alert("❌ Email not found");
                    else alert("❌ Password reset failed. Server error.");
                }
            });
        }
    });

});
