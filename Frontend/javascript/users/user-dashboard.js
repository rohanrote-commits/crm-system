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
    alert("Unauthorized. Please login.");
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
          alert("User deleted successfully.");
          loadUsers(token);
        },
        error: function () {
          showAlertlert("Error deleting user.","warning");
        },
      });
    }
  });

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
