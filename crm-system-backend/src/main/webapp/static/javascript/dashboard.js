
$(document).ready(function () {
    $("#header").load("/Frontend/html/components/header.html");
    $("#profile-model").load("/Frontend/html/models/profile_model.html");
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

    // Get token from sessionStorage
    const token = sessionStorage.getItem("Authorization");
    if (!token) {
        showAlert("Unauthorized. Please login.","danger");
        window.location.href = "/crm/login";
        return;
    }

    const payload = parseJwt(token);
    const userRole = payload?.role?.trim();
    console.log(payload);

    loadLeads(payload,token);



    // Sidebar navigation
    $(".sidebar-btn").click(function () {
        const target = $(this).data("target");

        $(".sidebar-btn").removeClass("active");
        $(this).addClass("active");

        $(".dashboard-section").hide();
        $("#" + target).show();
        console.log(target);

        if(target === "leads"){
            loadLeads(payload,token);
        }

    });

  $("#profilePic").click(function () {
    $("#profileDropdown").toggleClass("show");
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
        window.location.href = "/crm/login";
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


// Toggle dropdown on button click
$("#addLeadBtn").on("click", function (e) {
    e.stopPropagation(); // prevent click from closing instantly
    $("#leadDropdown").toggleClass("show");
});

// Close dropdown when clicking outside
$(document).on("click", function () {
    $("#leadDropdown").removeClass("show");
});


    $("#importLead").click(function (event) {
          window.location.href = "/crm/leads/upload";
    });


    // //delete profile
    // $("#delete-profile").click(function () {

    //     if (!token) {
    //         alert("User not logged in!");
    //         return;
    //     }

    //     if (!confirm("Are you sure you want to delete your profile? This action is irreversible.")) {
    //         return;
    //     }

    //     $.ajax({
    //         url: `http://localhost:8080/crm/user/delete-user`,
    //         type: "DELETE",
    //         headers: {
    //             "Authorization": "Bearer " + token
    //         },
    //         success: function (response) {
    //             showAlert(response,"success");

    //             // remove token after success
    //             localStorage.removeItem("Authorization");

    //             // redirect to login page
    //             window.location.href = "/Frontend/html/login.jsp";
    //         },
    //         error: function (xhr) {
    //             showAlert("Failed to delete user: " + xhr.responseText,"danger");
    //         }
    //     });
    // });

//logout
    $("#logout").click(function () {
        if (!token) {
            window.location.href = "/crm/login";
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
                sessionStorage.removeItem("Authorization");

                // redirect to login
                window.location.href = "/crm/login";
            },
            error: function (xhr) {
              showPopup("Error","Failed to Logout", "error");
                showAlert("Failed to logout: " + xhr.responseText,"warning");
            }
        });
    });


//delete lead
let deleteEmail = null;
$(document).on("click", ".delete-lead", function () {
    deleteEmail = $(this).data("email");
    $("#deleteConfirmModal").modal("show");
});
// confirm delete
$("#confirmDeleteBtn").click(function () {
    if (!deleteEmail) return;

    $.ajax({
        url: "http://localhost:8080/crm/lead/",
        type: "DELETE",
        data: { email: deleteEmail },
        headers: { "Authorization": "Bearer " + token },
        success: function () {
            showAlert("Lead deleted successfully.", "success");
            $("#lead-table").DataTable().ajax.reload(null, false);
        },
        error: function () {
            showAlert("Error deleting lead.", "warning");
        }
    });

    $("#deleteConfirmModal").modal("hide");
});

    $('#user-table').on('click', '.delete-user', function() {
        const user = {
            email : $(this).data('email')
        };
        if (confirm("Are you sure you want to delete this User?")) {
            $.ajax({
                url: `http://localhost:8080/crm/user/delete-sub_user`,
                type: 'DELETE',
                contentType: "application/json",
                data : JSON.stringify(user),
                headers: { "Authorization": "Bearer " + token },
                success: function() {
                   showPopup("Success","User deleted successfully", "success");
                   //showAlert("User deleted successfully.","success");
                    $('#user-table').DataTable().ajax.reload();
                },
                error: function() {
                    showPopup("Error","Error deleting lead.", "error");
                    showAlert("Error deleting lead.","warning");
                }
            });
        }
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
        showPopup("Success","Profile updated successfully", "success");
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
        showPopup("Error","Failed to update profile", "error");
      }
    });
  });

    $(document).on("click", ".view-lead-info", function () {
        const lead = JSON.parse($(this).attr("data-lead"));

        $("#viewFirstName").text(lead.firstName || "-");
        $("#viewLastName").text(lead.lastName || "-");
        $("#viewEmail").text(lead.email || "-");
        $("#viewMobile").text(lead.mobileNumber || "-");
        $("#viewGstin").text(lead.gstin || "-");
        $("#viewDescription").text(lead.description || "-");
        $("#viewAddress").text(lead.businessAddress || "-");
        $("#viewStatus").text(lead.leadStatus || "-");
        $("#viewModules").text(lead.interestedModules || "-");

        $("#viewLeadModal").modal("show");
    });

});

function loadUsers(token){
    $.ajax({
        url: "http://localhost:8080/crm/user/users",
        type: "GET",
        
        headers: {
            "Authorization":"Bearer "+ token
        },
        success: function (userList) {

            $("#user-table").DataTable({
                data: userList,
                columns: [
                    { data: "id" },
                    { data: "firstName" },
                    { data: "lastName" },
                    { data: "email" },
                    { data: "mobileNumber" },
                    { data: "role" },
                    {data : "emailOfAdminRegistered"},
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
                        }
                    }
                ],
                pageLength: 5
            });
        }
    })
};


// Function: Load Leads from API
function loadLeads(payload, token) {

    $("#lead-table").DataTable({
    ajax: {
        url: `http://localhost:8080/crm/lead/by/${payload.sub}`,
        type: "GET",
        headers: {
            "Authorization": "Bearer " + token
        },
        dataSrc: function (response) {
            console.log("Leads fetched:", response);
            return response;   // must return array
        },
        error: function (xhr) {
            if (xhr.status === 401) {
                showPopup("Error","Session expired. Login again.", "error");
                sessionStorage.clear();
                window.location.href = "/crm/login";
            } else {
                if (xhr.status === 23) {
                  showPopup("Error","Session expired. Login again.", "error");
                    sessionStorage.clear();
                    window.location.href = "/crm/login";
                }
                showPopup("Error","Error loading leads.", "error");
            }
        }
    },

    columns: [
        { data: "firstName", title: "First Name" },
        { data: "lastName", title: "Last Name" },
        { data: "email", title: "Email" },
        { data: "mobileNumber", title: "Mobile", visible: false },
        { data: "gstin", title: "GSTIN" },
        { data: "description", title: "Description", visible: false },
        { data: "businessAddress", title: "Address", visible: false },

        {
            data: "leadStatus",
            title: "Status",
            orderable: false,
            render: function (data) {
                let badgeClass = "";
                switch (data) {
                    case "ADDED": badgeClass = "bg-primary"; break;
                    case "CONTACTED": badgeClass = "bg-warning"; break;
                    case "CONVERTED": badgeClass = "bg-success"; break;
                    case "NOT_CONVERTED": badgeClass = "bg-danger"; break;
                    default: badgeClass = "bg-secondary";
                }
                return `<span class="badge ${badgeClass}">${data === "NOT_CONVERTED" ? "NOT CONVERTED" : data}</span>`;
            }
        },

        {
            data: "interestedModules",
            title: "Interested Modules",
            orderable: false,
            render: (data) => data?.length ? data.join(", ") : "-"
        },

        {
            data: null,
            title: "Action",
            orderable: false,
            render: function (data, type, row) {
                const leadData = JSON.stringify(row).replace(/"/g, '&quot;');
                return `
                    <div class="d-flex justify-content-center gap-2">
                        <button class="btn btn-sm btn-warning edit-lead" data-email="${row.email}">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-danger delete-lead" data-email="${row.email}">
                            <i class="bi bi-trash"></i>
                        </button>
                        <button class="btn btn-sm btn-secondary view-lead-info" data-lead="${leadData}">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>
                `;
            }
        }
    ],

    destroy: true,
    responsive: true,
    searching: true,
    paging: true,
    ordering: true,
    info: true
});

}

    // Function to show bootstrap alert dynamically
    function showAlert(message, type) {
      const alertContainer = $("#alert-container");
      const alert = $(`
        <div class="alert alert-${type} small-alert alert-dismissible fade show" role="alert">
          ${message}
          <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
      `);
      alertContainer.append(alert);
      alertContainer.show();

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
    });
}