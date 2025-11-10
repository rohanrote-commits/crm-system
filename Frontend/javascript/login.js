$(document).ready(function () {

    $.validator.addMethod("emailPattern", function (value) {
        return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value);
    }, "Enter a valid email");

    $("#loginForm").validate({
        rules: {
            loginEmail: { required: true, emailPattern: true },
            loginPassword: { required: true }
        },
        messages: {
            loginEmail: { required: "Email is required" },
            loginPassword: { required: "Password is required" }
        },
        errorClass: "error-message",
        highlight: function (element) { $(element).addClass("error"); },
        unhighlight: function (element) { $(element).removeClass("error"); },

        submitHandler: function () {

            let user = {
                email: $('#loginEmail').val(),
                password: $('#loginPassword').val()
            };

            $.ajax({
                url: 'http://localhost:8080/api/crm/user/sign-in',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(user),
                success: function (token) {
                    alert(" Login Successful");
                    sessionStorage.setItem("Authorization", token);
                    window.location.href = "dashboard-test.html";
                    
                },
                error: function (xhr) {
                    if(xhr.status == 404){
                        alert("Invalid Credentials");
                    }else{
                        alert("Server Side Error");
                    }

    
                }
            });
        }
    });

});
