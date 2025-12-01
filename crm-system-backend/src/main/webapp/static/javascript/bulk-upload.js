$(document).ready(function () {

    // Create a hidden file input dynamically
    const fileInput = $('<input type="file" id="userFile" accept=".xlsx,.xls" style="display:none;">');
    $('body').append(fileInput);



    // Toggle dropdown when profile image is clicked
  $("#profilePic").on("click", function (e) {
   
    $("#profileDropdown").toggleClass("show"); // Toggle visibility
  });

  $("#manage-users").click(function() {
    window.location.href = "/Frontend/html/user-dashboard.jsp"
  })
  // Hide dropdown when clicking anywhere outside
  $(document).on("click", function (e) {
    if (!$(e.target).closest(".profile-menu").length) {
      $("#profileDropdown").removeClass("show");
    }
  });

    $("#back").click(function () {
    window.location.href = "/Frontend/html/user-dashboard";
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
        window.location.href = "/Frontend/html/login.jsp";
      },
      error: function (xhr) {
        alert("Failed to delete user: " + xhr.responseText);
      },
    });
  });

  //logout
    $("#logout").click(function () {
        if (!token) {
            window.location.href = "/Frontend/html/login.jsp";
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
                window.location.href = "/Frontend/html/login.jsp";
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
            showPopup("Warning","⚠ Session expired. Please login again.","warning");
            window.location.href = "/Frontend/html/login.jsp";
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
            success: function (response) {

                showPopup("Success",response,"success");

                fileInput.val(''); // Clear input
            },
            error: function (xhr) {
                 let error = xhr.responseJSON.message;
             showPopup("Error",error,"error");

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
                showPopup("Success","File Downloded successfully","success");
            },
            error: function () {
                    if (xhr.status === 401) {
        showPopup("Warning","Session expired. Please login again.","warning");
        sessionStorage.clear();
        window.location.href = "/Frontend/html/login.jsp";
      } else {
        console.error("Token used:", token);
        showPopup("Error while downloading the template.","danger");
      }
            }
        });

    });

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
 function showPopup(title, message, iconType) {
    Swal.fire({
        title: title,
        text: message,
        icon: iconType, // success, error, warning, info
        confirmButtonText: 'OK'
    })
}


});

