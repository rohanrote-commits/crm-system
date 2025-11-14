$(document).ready(function () {

    // Create a hidden file input dynamically
    const fileInput = $('<input type="file" id="userFile" accept=".xlsx,.xls" style="display:none;">');
    $('body').append(fileInput);



    // Toggle dropdown when profile image is clicked
  $("#profilePic").on("click", function (e) {
   
    $("#profileDropdown").toggleClass("show"); // Toggle visibility
  });

  $("#manage-users").click(function() {
    window.location.href = "/Frontend/html/user-dashboard.html"
  })
  // Hide dropdown when clicking anywhere outside
  $(document).on("click", function (e) {
    if (!$(e.target).closest(".profile-menu").length) {
      $("#profileDropdown").removeClass("show");
    }
  });

    $("#back").click(function () {
    window.location.href = "/Frontend/html/user-dashboard.html";
  });
  //delete profile
  $("#delete-profile").click(function () {
    if (!token) {
      alert("User not logged in!");
      return;
    }

    if (
      !confirm(
        "Are you sure you want to delete your profile? This action is irreversible."
      )
    ) {
      return;
    }

    $.ajax({
      url: `http://localhost:8080/crm/user/delete-user`,
      type: "DELETE",
      headers: {
        Authorization: "Bearer " + token,
      },
      success: function (response) {
        alert(response);

        // remove token after success
        localStorage.removeItem("Authorization");

        // redirect to login page
        window.location.href = "/Frontend/html/login.html";
      },
      error: function (xhr) {
        alert("Failed to delete user: " + xhr.responseText);
      },
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
                alert(response);

                // remove token
                localStorage.removeItem("Authorization");

                // redirect to login
                window.location.href = "/Frontend/html/login.html";
            },
            error: function (xhr) {
                alert("Failed to logout: " + xhr.responseText);
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
                alert("Failed to fetch profile");
            }
        });
    });



    // Trigger file select when upload button is clicked
    $("#uploadTemplateBtn").click(function () {
        fileInput.click();
    });

    // Handle file selection and upload
    fileInput.on('change', function () {
        let file = this.files[0];
        if (!file) {
            return;
        }

        const token = sessionStorage.getItem("Authorization");
        if (!token) {
            alert("⚠ Session expired. Please login again.");
            window.location.href = "/Frontend/html/login.html";
            return;
        }

        let formData = new FormData();
        formData.append("file", file);

        $.ajax({
            url: 'http://localhost:8080/crm/user/upload-user-file',
            type: 'POST',
            headers: { "Authorization": "Bearer " + token },
            data: formData,
            processData: false,
            contentType: false,
            success: function () {
                alert("✅ Data Inserted Successfully!");
                fileInput.val(''); // Clear input
            },
            error: function () {
                alert("⚠ Error in uploading file. Please check the file format.");
            }
        });
    });

    // Download template
    $("#downloadTemplate").click(function (e) {
     
        $.ajax({
            url: 'http://localhost:8080/crm/files/user-template',
            type: 'GET',
            xhrFields: { responseType: 'blob' },
            success: function (data,status,xhr) {
                const filename = "User_Template.xlsx";
                const blob = new Blob([data],{
                     type: xhr.getResponseHeader("Content-Type"),
                });
                const url = window.URL.createObjectURL(blob);

                const a = document.createElement("a");
                a.href = url;
                a.download = filename;
                document.body.appendChild(a);
                a.click();
                a.remove();

                window.URL.revokeObjectURL(url);
                showAlert("File Downloded successfully","success");
            },
            error: function () {
                    if (xhr.status === 401) {
        showAlert("Session expired. Please login again.","warning");
        sessionStorage.clear();
        window.location.href = "/Frontend/html/login.html";
      } else {
        console.error("Token used:", token);
        showAlert("Error while downloading the template.","danger");
      }
            }
        });

    });

});
