$(document).ready(function () {

   
$.validator.addMethod("passwordPattern", function (value) {
    return value.length >= 8 && /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).*$/.test(value);
}, "Password should have at least one upper case, one lower case, one number and one special char");

    $.validator.addMethod("emailPattern", function (value) {
        return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value);
    }, "Enter a valid email");

    $("#resetPasswordForm").validate({
        rules: {
            resetEmail: { required: true, emailPattern: true },
            newPassword: { required: true, passwordPattern: true, minlength : 8, maxlength : 16 },
            confirmPassword: { required: true, equalTo: "#newPassword", minlength:8, maxlength : 16 }
        },
        messages: {
            resetEmail: { required: "Email is required" },
            newPassword: { required: "Enter new password",
          minlength: "Password must be at least 8 characters",
            maxlength: "Password cannot exceed 16 characters" },
            confirmPassword: { required: "Confirm password", 
            minlength: "Password must be at least 8 characters",
            maxlength: "Password cannot exceed 16 characters",
            equalTo: "Passwords do not match" }
        },
        errorClass: "error-message",
        highlight: function (element) { $(element).addClass("error"); },
        unhighlight: function (element) { $(element).removeClass("error"); },
        errorPlacement: function (error, element) {
            if (element.attr("name") === "newPassword" || element.attr("name") === "confirmPassword") {
                error.insertAfter(element.closest(".password-wrapper"));
            } else {
                error.insertAfter(element);
            }
        },

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
                    alert("Password reset successfully");
                    window.location.href = "login.html";
                },
                error: function (xhr) {
                    if (xhr.status === 404) alert(" Email not found");
                    else alert("Password reset failed. Server error.");
                }
            });
        }
    });

   $('.pw-toggle').on('click', function () {
        const input = $(this).siblings('input'); // get the input in the same wrapper
        const isHidden = input.attr('type') === 'password';
        input.attr('type', isHidden ? 'text' : 'password');

        $(this).attr('aria-pressed', isHidden);
        $(this).attr('aria-label', isHidden ? 'Hide password' : 'Show password');
    });
});
