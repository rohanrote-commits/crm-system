const token = sessionStorage.getItem("Authorization");

  $("#manage-users").click(function() {
    window.location.href = "/Frontend/html/users/user-dashboard.html"
  })
  // Hide dropdown when clicking anywhere outside
  $(document).on("click", function (e) {
    if (!$(e.target).closest(".profile-menu").length) {
      $("#profileDropdown").removeClass("show");
    }
  });

  

    //delete profile
    $("#delete-profile").click(function () {

        if (!token) {
            alert("User not logged in!");
            return;
        }

        if (!confirm("Are you sure you want to delete your profile? This action is irreversible.")) {
            return;
        }

        $.ajax({
            url: `http://localhost:8080/crm/user/delete-user`,
            type: "DELETE",
            headers: {
                "Authorization": "Bearer " + token
            },
            success: function (response) {
                showAlert(response,"success");

                // remove token after success
                localStorage.removeItem("Authorization");

                // redirect to login page
                window.location.href = "/Frontend/html/login.html";
            },
            error: function (xhr) {
                showAlert("Failed to delete user: " + xhr.responseText,"danger");
            }
        });
    });

//logout
    $("#logout").click(function () {
        if (!token) {
            window.location.href = "/Frontend/html/login.html";
            return;
        }
        $.ajax({
            url: `http://localhost:8080/crm/user/logout`,
            type: "GET",
            headers: {
                "Authorization": "Bearer " + token
            },
            success: function (response) {
                showAlert(response,"success");

                // remove token
                localStorage.removeItem("Authorization");

                // redirect to login
                window.location.href = "/Frontend/html/login.html";
            },
            error: function (xhr) {
                showAlert("Failed to logout: " + xhr.responseText,"warning");
            }
        });
    });
    $("#view-profile").click(function () {


        $.ajax({
            url: `http://localhost:8080/crm/user/get-user`,
            type: "GET",
            headers: {
                "Authorization": "Bearer " + token
            },
            success: function (user) {

                $("#profileName").text(user.firstName + " " + user.lastName);
                $("#profileEmail").text(user.email);
                $("#profileMobile").text(user.mobileNumber);
                $("#profileAddress").text(user.address || "-");
                $("#profileCity").text(user.city || "-");
                $("#profileState").text(user.state || "-");
                $("#profileCountry").text(user.country || "-");
                $("#profilePin").text(user.pinCode || "-");
                $("#profileRole").text(user.role);
                $("#profileDate").text(user.registeredOn);

                $("#profileModal").modal("show");
            },
            error: function () {
                showAlert("Failed to fetch profile","info");
            }
        });
    });