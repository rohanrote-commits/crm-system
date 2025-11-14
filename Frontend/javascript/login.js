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
        errorPlacement: function (error, element) {
            if (element.attr("name") === "loginPassword") {
                // Place error after the wrapper div
                error.insertAfter(element.closest(".password-wrapper"));
            } else {
                error.insertAfter(element);
            }
        },

        submitHandler: function () {

            let user = {
                email: $('#loginEmail').val(),
                password: $('#loginPassword').val()
            };

            $.ajax({
                url: 'http://localhost:8080/crm/user/sign-in',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(user),
                success: function (token) {
                    alert(" Login Successful");
                    sessionStorage.setItem("Authorization", token);
                    window.location.href = "/Frontend/html/dashboard.html";
                    
                },
                error: function (xhr) {
                    if(xhr.status === 404){
                        console.log(xhr);
                        showAlert("Invalid Credentials","warinig");
                    }else{
                        alert("Server Side Error");
                    }

    
                }
            });
        }
    });

$(document).ready(function() {
  $('.pw-toggle').on('click', function() {
    const input = $('#loginPassword');
    const isHidden = input.attr('type') === 'password';
    input.attr('type', isHidden ? 'text' : 'password');
    
    // Update aria attributes for accessibility
    $(this).attr('aria-pressed', isHidden);
    $(this).attr('aria-label', isHidden ? 'Hide password' : 'Show password');
  });
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

