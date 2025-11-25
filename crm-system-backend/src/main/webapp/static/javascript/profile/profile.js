$(document).ready(function () {
  
      const token = sessionStorage.getItem("Authorization");
      // Parse JWT
    function parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
                '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
            ).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            return null;
        }
    }
    if (!token) {
        showAlert("Unauthorized. Please login.","danger");
        window.location.href = "/Frontend/html/login.jsp";
        return;
    }
  $("#profilePic").click(function () {
    $("#profileDropdown").toggle();
  });

  const $dropdown = $("#userDropdown");

  // Toggle dropdown when clicking the main button
  $("#addUserBtn").click(function (e) {
    e.stopPropagation();
    $dropdown.toggle();
  });

  // Delete profile
  $("#delete-profile").click(function () {
    if (!token) {
      showAlert("User not logged in!", "danger");
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
        showAlert(response.message || response, "info");

        localStorage.removeItem("Authorization");
        window.location.href = "/Frontend/html/login.jsp";
      },
      error: function (xhr) {
        let errorMsg = "Failed to delete user";
        if (xhr.responseJSON && xhr.responseJSON.message) {
          errorMsg = xhr.responseJSON.message;
        }
        showAlert(errorMsg, "danger");
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
    window.location.href = "bulk-upload.jsp";
  });

  const payload = parseJwt(token);
  const userRole = payload?.role?.trim();
  console.log("Decoded Token:", payload);

  if (userRole === "ADMIN" || userRole === "MASTER_ADMIN") {
    loadUsers(token);
  } else {
    showAlert("Access Denied: Only admins can view users.", "warning");
    return;
  }



  // Logout
  $("#logout").click(function () {
    if (!token) {
      window.location.href = "/Frontend/html/login.jsp";
      return;
    }
    $.ajax({
      url: `http://localhost:8080/crm/user/logout`,
      type: "GET",
      headers: { Authorization: "Bearer " + token },
      success: function (response) {
        showAlert(response.message || response, "success");
        localStorage.removeItem("Authorization");
        window.location.href = "/Frontend/html/login.jsp";
      },
      error: function (xhr) {
        let errorMsg = "Failed to logout";
        if (xhr.responseJSON && xhr.responseJSON.message) {
          errorMsg = xhr.responseJSON.message;
        }
        showAlert(errorMsg, "danger");
      },
    });
  });

  // View profile
  $("#view-profile").click(function () {
    $.ajax({
      url: `http://localhost:8080/crm/user/get-user`,
      type: "GET",
      headers: { Authorization: "Bearer " + token },
      success: function (user) {
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

        $("#profileModal input, #profileModal textarea").prop("readonly", true);
        $("#editProfileBtn").removeClass("d-none");
        $("#saveProfileBtn").addClass("d-none");
        $("#profileModal").modal("show");
      },
      error: function (xhr) {
        let errorMsg = "Failed to load profile";
        if (xhr.responseJSON && xhr.responseJSON.message) {
          errorMsg = xhr.responseJSON.message;
        }
        showAlert(errorMsg, "danger");
      },
    });
  });

  // Edit profile
  $("#editProfileBtn").click(function () {
    $("#profileMobile, #profileAddress, #profileCity, #profileState, #profileCountry, #profilePin").prop("readonly", false);
    $("#editProfileBtn").addClass("d-none");
    $("#saveProfileBtn").removeClass("d-none");
  });

  $.validator.addMethod("mobilePattern", function (value, element) {
    return this.optional(element) || /^[789]\d{9}$/.test(value);
  }, "Mobile must start with 7/8/9 and be 10 digits");

  $.validator.addMethod("addressPattern", function (value, element) {
    return this.optional(element) || /^[A-Za-z0-9 ,./#\-]{1,200}$/.test(value);
  }, "Address can contain letters, numbers, ,./#- (max 100)");

  $.validator.addMethod("pinPattern", function (value, element) {
    return this.optional(element) || /^[0-9]{6}$/.test(value);
  }, "Pin code must be 6 digits");

  $("#profileForm").validate({
    rules: {
      profileMobile: { required: true, mobilePattern: true },
      profileAddress: { required: true, addressPattern: true }
    }
  });

  // Save profile
  $("#saveProfileBtn").click(function () {
    if (!$("#profileForm").valid()) return;

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
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      data: JSON.stringify(updatedProfile),
      success: function () {
        showAlert("Profile updated successfully", "success");
        $("#profileModal input, #profileModal textarea").prop("readonly", true);
        $("#editProfileBtn").removeClass("d-none");
        $("#saveProfileBtn").addClass("d-none");
        $("#profileModal").modal("hide");
      },
      error: function (xhr) {
        let errorMsg = "Failed to update profile";
        if (xhr.responseJSON && xhr.responseJSON.message) {
          errorMsg = xhr.responseJSON.message;
        }
        showAlert(errorMsg, "danger");
      }
    });
  });
});