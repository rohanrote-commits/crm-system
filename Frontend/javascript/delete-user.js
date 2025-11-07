$(document).ready(function () {

    $("#deleteUserForm").submit(function (e) {
        e.preventDefault(); // prevent form default submit

        const email = $("#email").val().trim();
        if (!email) {
            alert("⚠ Please enter a valid email!");
            return;
        }

        const token = sessionStorage.getItem("Authorization");
        if (!token) {
            alert("⚠ Session expired. Please login again.");
            window.location.href = "/Frontend/html/login.html";
            return;
        }
        console.log(email);

        const userDTO = { 
            email: email 
        };

        $.ajax({
            url: 'http://localhost:8080/crm/user/delete-sub_user',
            type: 'DELETE',
            headers: { "Authorization": "Bearer " + token },
            contentType: 'application/json',
            data: JSON.stringify(userDTO),
            success: function (response) {
                alert("✅ User deleted successfully!");
            },
            error: function (xhr) {
                if (xhr.status === 404) {
                    alert("❌ User not found!");
                } else {
                    alert("❌ Failed to delete user!");
                }
            }
        });
    });

});
