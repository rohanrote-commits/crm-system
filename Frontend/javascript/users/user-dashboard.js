$(document).ready(function () {
  // Parse JWT Token
  function parseJwt(token) {
    try {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join("")
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  }
  //
  $("#back").click(function () {
    window.location.href = "/Frontend/html/dashboard.html";
  });

  // Get token from sessionStorage
  const token = sessionStorage.getItem("Authorization");
    if (!token) {
        showAlert("Unauthorized. Please login.","danger");
        window.location.href = "/Frontend/html/login.html";
        return;
    }
  $("#profilePic").click(function () {
    $("#profileDropdown").toggle();
  });

  const $dropdown = $("#userDropdown");

  // Toggle dropdown when clicking the main button
  $("#addUserBtn").click(function (e) {
    e.stopPropagation(); // prevent document click from closing immediately
    $dropdown.toggle();
  });

  //delete profile
  $("#delete-profile").click(function () {
    if (!token) {
      showAlert("User not logged in!","danger");
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
        showrAlert(response,"info");

        // remove token after success
        localStorage.removeItem("Authorization");

        // redirect to login page
        window.location.href = "/Frontend/html/login.html";
      },
      error: function (xhr) {
        showAlert("Failed to delete user: " + xhr.responseText,"warning");
      },
    });
  });

  $("#clearUserBtn").click(function () {
    $("#userForm")[0].reset();
    $("#addressFields").slideUp();
  });

  // Close dropdown if clicked outside
  $(document).click(function (event) {
    if (!$(event.target).closest("#userDropdown, #addUserBtn").length) {
      $dropdown.hide();
    }
  });

  // Click Bulk Import
  $("#importUser").click(function () {
    $dropdown.hide();
    window.location.href = "bulk-upload.html"
  });

  const payload = parseJwt(token);
  const userRole = payload?.role?.trim();
  console.log("Decoded Token:", payload);

  // Load users only if role is ADMIN or MASTER_ADMIN
  if (userRole === "ADMIN" || userRole === "MASTER_ADMIN") {
    loadUsers(token);
  } else {
    showAlert("Access Denied: Only admins can view users.","warning");
    return;
  }

  // DELETE USER (sub-user)
  $("#user-table").on("click", ".delete-user", function () {
    const user = { email: $(this).data("email") };

    if (confirm("Are you sure you want to delete this User?")) {
      $.ajax({
        url: `http://localhost:8080/crm/user/delete-sub_user`,
        type: "DELETE",
        contentType: "application/json",
        data: JSON.stringify(user),
        headers: { Authorization: "Bearer " + token },
        success: function () {
          showAlert("User deleted successfully.","success");
          loadUsers(token);
        },
        error: function () {
          showAlertlert("Error deleting user.","warning");
        },
      });
    }
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

            // Load values into input fields
            $("#profileName").val(user.firstName + " " + user.lastName);
            $("#profileEmail").val(user.email);
            $("#profileMobile").val(user.mobileNumber);
            $("#profileAddress").val(user.address || "");
            $("#profileCity").val(user.city || "");
            $("#profileState").val(user.state || "");
            $("#profileCountry").val(user.country || "");
            $("#profilePin").val(user.pinCode || "");
            $("#profileRole").val(user.role);
            $("#profileDate").val(user.registeredOn);

            

            // Ensure all fields stay READONLY initially
            $("#profileModal input, #profileModal textarea").prop("readonly", true);

            // Reset buttons
            $("#editProfileBtn").removeClass("d-none");
            $("#saveProfileBtn").addClass("d-none");

            $("#profileModal").modal("show");
        },
        error: function () {
            showAlert("Failed to fetch profile", "info");
        }
    });
});
$("#editProfileBtn").click(function () {

    // Make ONLY required fields editable
    $("#profileMobile").prop("readonly", false);
    $("#profileAddress").prop("readonly", false);
    $("#profileCity").prop("readonly", false);
    $("#profileState").prop("readonly", false);
    $("#profileCountry").prop("readonly", false);
    $("#profilePin").prop("readonly", false);

    // Toggle buttons
    $("#editProfileBtn").addClass("d-none");
    $("#saveProfileBtn").removeClass("d-none");
});

    $.validator.addMethod("mobilePattern", function(value, element) {
        return this.optional(element) || /^[789]\d{9}$/.test(value);
    }, "Mobile must start with 7/8/9 and be 10 digits");

    $.validator.addMethod("addressPattern", function(value, element) {
        return this.optional(element) || /^[A-Za-z0-9 ,./#\-]{1,200}$/.test(value);
    }, "Address can contain letters, numbers, ,./#- (max 100)");

    $.validator.addMethod("pinPattern", function(value, element) {
        return this.optional(element) || /^[0-9]{6}$/.test(value);
    }, "Pin code must be 6 digits");

    $("#profileForm").validate({
    rules: {
        profileMobile: { required: true, mobilePattern: true },
        profileAddress: { required: true, addressPattern: true }
    }
});

$("#saveProfileBtn").click(function () {

    // Validate form first
    if (!$("#profileForm").valid()) {
        return; // Stop if validation fails
    }

    // Create object to send to backend
    const updatedProfile = {
        email: $("#profileEmail").val(),          
        mobileNumber: $("#profileMobile").val(),
        address: $("#profileAddress").val(),
        city: $("#profileCity").val(),
        state: $("#profileState").val(),
        country: $("#profileCountry").val(),
        pinCode: $("#profilePin").val()
    };

    $.ajax({
        url: `http://localhost:8080/crm/user/update`,
        type: "POST",
        headers: {
            "Authorization": "Bearer " + token,
            "Content-Type": "application/json"
        },
        data: JSON.stringify(updatedProfile),

        success: function () {
            alert("Profile updated successfully", "success");

            // Make editable fields readonly again
            $("#profileModal input, #profileModal textarea").prop("readonly", true);

            // Toggle buttons back
            $("#editProfileBtn").removeClass("d-none");
            $("#saveProfileBtn").addClass("d-none");

            // Hide modal
            $("#profileModal").modal("hide");
        },

        error: function () {
            showAlert("Failed to update profile", "danger");
        }
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

  // LOAD ALL USERS FUNCTION

  function loadUsers(token) {
    console.log(token);
    $.ajax({
      url: "http://localhost:8080/crm/user/users",
      type: "GET",
      headers: {
        Authorization: "Bearer " + token,
      },
      success: function (userList) {
        $("#user-table").DataTable({
          pageLength: 5, // show 5 rows per page by default
          autoWidth: false,
          fixedHeader: true,
          ordering: true,
          lengthMenu: [5, 10, 25, 50],
          data: userList,
          columns: [
            {
              data: null, 
              title: "S.No", // Column header
              orderable: false,
              searchable: false,
              render: function (data, type, row, meta) {
                return meta.row + 1 + meta.settings._iDisplayStart;
              },
            },
            { data: "firstName" },
            { data: "lastName" },
            { data: "email" },
            { data: "mobileNumber" },
            { data: "role" },
            { data: "emailOfAdminRegistered" },
            {
              data: null,
              title: "Action",
              orderable: false, // Prevent sorting on this column
              render: function (data, type, row) {
                return `
                            <div class="d-flex justify-content-center gap-2">
                                <button class="btn btn-sm btn-warning edit-user" data-email="${row.email}">
                                    <i class="bi bi-pencil"></i>
                                </button>
                                <button class="btn btn-sm btn-danger delete-user" data-email="${row.email}">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </div>
                        `;
              },
            },
          ],
          columnDefs: [
            { targets: [3, 5], searchable: false }, //
          ],
          destroy: true,
        });
      },
    });
  }
});
