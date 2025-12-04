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



  // Get token from sessionStorage
  const token = sessionStorage.getItem("Authorization");
  if (!token) {
    showPopup("Warning", "Unauthorized. Please login.", "warning");
    window.location.href = "/crm/login";
    return;
  }

  const decoded = parseJwt(token);
  const userRole = decoded ? decoded.role : null;
  console.log("Role of User is  :", userRole); // Hide Add/Import button for USER role
  // --- START: ROLE-BASED UI RESTRICTIONS ---
  if (userRole === "BASIC") {
    console.log(userRole);
    $("#addUserBtn").hide();
  }
  // --- END: ROLE-BASED UI RESTRICTIONS ---



  const $dropdown = $("#userDropdown"); // Toggle dropdown when clicking the main button
    $("#manage-users").hide();

  $("#addUserBtn").click(function (e) {
    e.stopPropagation();
    $dropdown.toggle();
  }); // Delete profile (Deletes the logged-in user's own account)


  $("#clearUserBtn").click(function () {
    $("#userForm")[0].reset();
    $("#addressFields").slideUp();
  }); // Close dropdown if clicked outside

  $(document).click(function (event) {
    if (!$(event.target).closest("#userDropdown, #addUserBtn").length) {
      $dropdown.hide();
    }
  }); // Click Bulk Import

  $("#importUser").click(function () {
    $dropdown.hide();
    window.location.href = "/crm/users/bulk-upload";
  }); // Load users (pass role to handle column visibility)

  loadUsers(token, userRole);

  // DELETE USER (sub-user)
  $("#user-table").on("click", ".delete-user", function () {
    // RESTRICTION: Block USER role from deleting sub-users
    if (userRole === "BASIC") {
      showPopup(
        "Warning",
        "Access Denied: You do not have permission to delete users.",
        "warning"
      );
      return;
    }
    const user = { email: $(this).data("email") };

    showDeleteConfirm().then((ok) => {
      if (!ok) return;

      $.ajax({
        url: `http://localhost:8080/crm/user/delete-sub_user`,
        type: "DELETE",
        contentType: "application/json",
        data: JSON.stringify(user),
        headers: { Authorization: "Bearer " + token },
        success: function () {
          showPopup("Success", "User deleted successfully.", "success");
          loadUsers(token, userRole);
        },
        error: function (xhr) {
          let errorMsg = "Error deleting user";
          if (xhr.responseJSON && xhr.responseJSON.message) {
            errorMsg = xhr.responseJSON.message;
          }
          showPopup("Error", errorMsg, "error");
        },
      });
    });
  }); // Logout

 // LOAD ALL USERS FUNCTION

  function loadUsers(token, userRole) {
    // Added userRole parameter
    $.ajax({
      url: "http://localhost:8080/crm/user/users",
      type: "GET",
      headers: { Authorization: "Bearer " + token },
      success: function (userList) {
        userList.sort((a, b) => {
          const order = {
            MASTER_ADMIN: 1,
            ADMIN: 2,
            BASIC: 3,
          };
          return order[a.role] - order[b.role];
        }); // Initialize DataTable as before

        const table = $("#user-table").DataTable({
          pageLength: 5,
          autoWidth: false,
          fixedHeader: true,
          ordering: true,
          lengthMenu: [5, 10, 25, 50],
          data: userList,
          columns: [
            {
              data: null,
              title: "S.No",
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
              orderable: false,
              width: "20px", // FIX: Remove explicit height and use ultra-minimal padding
              createdCell: function (td, cellData, rowData, row, col) {
                $(td).css({
                  "padding-top": "0.1rem !important", // Ultra small padding
                  "padding-bottom": "0.1rem !important", // Ultra small padding
                  "vertical-align": "middle", // Height is REMOVED to allow row to collapse to fit buttons
                  "line-height": "1",
                });
              },
              render: function (data, type, row) {
                return `
<div class="action-buttons d-flex">
    <button class="btn btn-warning btn-sm action-btn edit-user" data-email="${row.email}">
        <i class="bi bi-pencil"></i>
    </button>
    <button class="btn btn-danger btn-sm action-btn delete-user" data-email="${row.email}">
        <i class="bi bi-trash"></i>
    </button>
</div>

                `;
              },
            },
          ],
          columnDefs: [{ targets: [3, 5], searchable: false }],
          destroy: true,
        }); // RESTRICTION: Conditionally hide the Action column (index 7) for USER role
        if (userRole === "BASIC") {
          // Get the Action column (index 7) and hide it
          table.column(7).visible(false);
        }
      },
      error: function (xhr) {
        let errorMsg = "Failed to load users";
        if (xhr.responseJSON && xhr.responseJSON.message) {
          errorMsg = xhr.responseJSON.message;
        }
        console.log(xhr.responseJSON.status);
        if (xhr.responseJSON.status === "UNAUTHORIZED") {
          showPopup("Error", errorMsg, "error");
          window.location.href = `/crm/login`;
        } else {
          showPopup("Error", errorMsg, "error");
        }
      },
    });
  }
});
function showUpdateConfirm() {
  return new Promise((resolve) => {
    const modalEl = document.getElementById("updateConfirmModal");
    const modal = new bootstrap.Modal(modalEl);

    const confirmBtn = document.getElementById("confirmUpdateBtn"); // When user clicks "Yes, Update"

    confirmBtn.onclick = function () {
      modal.hide();
      resolve(true);
    }; // When modal closes without confirming

    modalEl.addEventListener("hidden.bs.modal", () => resolve(false), {
      once: true,
    });

    modal.show();
  });
}
function showDeleteConfirm() {
  return new Promise((resolve) => {
    const modalEl = document.getElementById("deleteConfirmModal");
    const modal = new bootstrap.Modal(modalEl);

    const confirmBtn = document.getElementById("confirmDeleteBtn"); // When user clicks "Yes, Delete"

    confirmBtn.onclick = function () {
      modal.hide();
      resolve(true);
    }; // When modal closes without confirming

    modalEl.addEventListener("hidden.bs.modal", () => resolve(false), {
      once: true,
    });
    modal.show();
  });
}
// LOAD ALL USERS FUNCTION
function loadUsers(token) {
  $.ajax({
    url: "http://localhost:8080/crm/user/users",
    type: "GET",
    headers: { Authorization: "Bearer " + token },
    success: function (userList) {
      $("#user-table").DataTable({
        pageLength: 5,
        autoWidth: false,
        fixedHeader: true,
        ordering: true,
        lengthMenu: [5, 10, 25, 50],
        data: userList,
        columns: [
          {
            data: null,
            title: "S.No",
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
            orderable: false,
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
        columnDefs: [{ targets: [3, 5], searchable: false }],
        destroy: true,
      });
    },
    error: function (xhr) {
      let errorMsg = "Failed to load users";
      if (xhr.responseJSON && xhr.responseJSON.message) {
        errorMsg = xhr.responseJSON.message;
      }
      console.log(xhr.responseJSON.status);
      if (xhr.responseJSON.status === "UNAUTHORIZED") {
        showPopup("Error", errorMsg, "error");
        window.location.href = `/crm/login`;
      } else {
        showPopup("Error", errorMsg, "error");
      }
    },
  });
}
function showPopup(title, message, iconType) {
  Swal.fire({
    title: title,
    text: message,
    icon: iconType, // success, error, warning, info
    confirmButtonText: "OK",
  });
}

// });

//     modal.show();
//   });
// }
