$(document).ready(function () {

    const token = sessionStorage.getItem("Authorization");
    console.log("This is token " + token)
    if (!token) {
        alert("Unauthorized. Please login.");
        window.location.href = "/Frontend/html/login.html";
        return;
    }

    $.ajax({
        url: "http://localhost:8080/crm/user/users",
        type: "GET",
        headers: {
            "Authorization":"Bearer "+ token
        },
        success: function (userList) {

            $("#usersTable").DataTable({
                data: userList,
                columns: [
                    { data: "id" },
                    { data: "firstName" },
                    { data: "lastName" },
                    { data: "email" },
                    { data: "mobileNumber" },
                    { data: "role" },
                    {data : "emailOfAdminRegistered"}
                ],
                pageLength: 5
            });

        },
        error: function (xhr) {
            if (xhr.status == 401) {
                alert("Session expired. Login again.");
                sessionStorage.clear();
                window.location.href = "/Frontend/html/login.html";
            } else {
                console.log("token is : "+ token);
                alert("Error loading users.");
            }
        }
    });

});
