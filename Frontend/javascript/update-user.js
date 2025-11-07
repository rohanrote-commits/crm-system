$(document).ready(function () {

    $("#address").on("input", function () {
        if ($(this).val().trim() !== "") {
            $("#addressFields").slideDown();
            $("#city, #state, #country, #pinCode").prop("required", true);
        } else {
            $("#addressFields").slideUp();
            $("#city, #state, #country, #pinCode").prop("required", false);
        }
    });

    $.validator.addMethod("mobilePattern", value => value === "" || /^[789]\d{9}$/.test(value), 
        "Mobile must start with 7/8/9 & be 10 digits");

    $.validator.addMethod("pinPattern", value => value === "" || /^[0-9]{6}$/.test(value),
        "Pin must be 6 digits");

    $("#updateUserForm").validate({
        rules: {
            email: { required: true, email: true },
            mobileNumber: { mobilePattern: true },
            city: { required: () => $("#address").val().trim() !== "" },
            state: { required: () => $("#address").val().trim() !== "" },
            country: { required: () => $("#address").val().trim() !== "" },
            pinCode: { required: () => $("#address").val().trim() !== "", pinPattern: true },
        },
        submitHandler: function () {

            const token = sessionStorage.getItem("Authorization");
            if (!token) {
                alert("Session expired. Login again.");
                window.location.href = "/Frontend/html/login.html";
                return;
            }

            const userDTO = {
                email: $("#email").val(),
                mobileNumber: $("#mobileNumber").val(),
                address: $("#address").val(),
                city: $("#city").val(),
                state: $("#state").val(),
                country: $("#country").val(),
                pinCode: $("#pinCode").val()
            };

            $.ajax({
                url: "http://localhost:8080/crm/user/update-sub_user",
                type: "PUT",
                headers: { "Authorization": "Bearer " + token },
                contentType: "application/json",
                data: JSON.stringify(userDTO),
                success: function () {
                    alert("User updated successfully");
                },
                error: function (xhr) {
                    if (xhr.status === 404){
                         alert("User not found");
                    }else if(xhr.status === 408){
                        alert("User Data not Updatable")
                    }
                    else alert("Failed to update user");
                }
            });

        }
    });

});
