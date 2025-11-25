jQuery(function (params) {
    const token = sessionStorage.getItem("Authorization")
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

});