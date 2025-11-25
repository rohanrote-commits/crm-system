
$(document).ready(function () {


  // ----- 1. Initial Setup -----

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
        window.location.href = "/Frontend/html/login.html";
        return;
    }

    const payload = parseJwt(token);
    const userRole = payload?.role?.trim();
    console.log(payload);

    loadLeads(payload,token);


    // ----- 2. Sidebar navigation Handler -----
    $(".sidebar-btn").click(function () {
        const target = $(this).data("target");

        $(".sidebar-btn").removeClass("active");
        $(this).addClass("active");

        $(".dashboard-section").hide();
        $("#" + target).show();
        console.log(target);

        // Saving the state
        localStorage.setItem("activeDashboardSection", target);

        if(target === "leads"){
            loadLeads(payload,token);
        }

        if(target === "reports"){
            initializeReportModule(payload, token);
        }

    });


    // ----- 3. Profile, Logout, Lead/User Deletion Handlers -----

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
        window.location.href = "/Frontend/html/login.html";
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
          window.location.href = "leads/upload_lead.html";
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
                sessionStorage.removeItem("Authorization");

                // redirect to login
                window.location.href = "/Frontend/html/login.html";
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

    const savedTarget = localStorage.getItem("activeDashboardSection");

    if (savedTarget && savedTarget !== 'leads') {
    
    $(".sidebar-btn").removeClass("active"); 
    $(`.sidebar-btn[data-target="${savedTarget}"]`).addClass("active"); 

    $(".dashboard-section").hide();
    $("#" + savedTarget).show();

    if (savedTarget === "reports") {
        initializeReportModule(payload, token); 
    }
    
} else { 
    $(".sidebar-btn").removeClass("active"); // Clear previous active
    $(".sidebar-btn[data-target='leads']").addClass("active");
    
    $(".dashboard-section").hide();
    $("#leads").show();
    
    loadLeads(payload, token);
}


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


// Function: Load Report from API
/**
 * Initializes the Report Dashboard module,
 * report history loading, 
 * DataTable setup, 
 * form validation, and 
 * report downloading.
 */
function initializeReportModule(payload, token) {
    
    // --- Configuration Variables ---
    const ReportHistoryAllUrl = "http://localhost:8080/crm/report/getDownloadedRecordHistory";
    const ReportTemplateBaseUrl = "http://localhost:8080/crm/report/getTemplate";

    let reportDataTableInstance = null;


    /**
     * Calculates the day immediately after start date
     * Used to restrict the minimum selectable end date (which should be after start date).
     * @param {string} dateString The start date in YYYY-MM-DD format.
     * @returns {string} The next day's date in YYYY-MM-DD format.
     */
    function getNextDay(dateString) {
        if (!dateString) return "";
        const date = new Date(dateString);
        date.setDate(date.getDate() + 1);
        return date.toISOString().split("T")[0];
    }


    // --- DataTable Setup ---

    /**
     * Initializes or re-initializes the DataTables instance for the report history.
     * @param {Array} initialData The data to load into the table.
     */
    function initializeReportDataTable(initialData = []) {
        // Destroy existing instance if it exists
        if ($.fn.DataTable.isDataTable("#report-table")) {
            reportDataTableInstance.destroy();
        }

        // Initialize the new DataTable instance
        reportDataTableInstance = $("#report-table").DataTable({
            pageLength: 10,
            data: initialData,
            columns: [
                { data: null, title: "Sr No" },
                { data: "userName", title: "User Name" },
                { data: "downloadedAt", title: "Downloaded At"},
                // { data: "dateOfDownload", title: "Date of Download" },
                // { data: "timeOfDownload", title: "Time of Download" },
                // { data: "role", title: "Role" },
                { data: "startDate", title: "Start Date" },
                { data: "endDate", title: "End Date" },
                { data: "status", title: "Download Status" }
            ],
            order: [
                [2, "desc"], // Sort by Date descending
                // [3, "desc"], // Sort by Time descending
            ],
            // Auto-generate Serial Number
            drawCallback: function (settings) {
                let api = this.api();
                let startIndex = api.context[0]._iDisplayStart;

                api.column(0, { page: "current" })
                    .nodes()
                    .each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                    });
            },
        });
    }

    // --- Data Fetching ---

    /**
     * Fetches all existing report history records.
     */
    function loadReportHistory(token) {
        fetch(ReportHistoryAllUrl, {
            method: "GET",
            headers: {
                Authorization: token ? `Bearer ${token}` : "",
            },
        })
        .then((response) => {
            if (!response.ok) {
                console.error("Failed to fetch report history. Status:", response.status);
                initializeReportDataTable([]);
                return null;
            }
            return response.json();
        })
        .then((data) => {
            if (data) {
                initializeReportDataTable(data);
            }
        })
        .catch((error) => {
            console.error("Network error fetching report history:", error);
            initializeReportDataTable([]);
        });
    }

    // --- Main Logic: Run on Document Ready ---

    $(document).ready(function () {
      
        const userRole = payload?.role?.trim() || "N/A";
        const userName = payload?.email || "N/A";

        // 2. Initial Data Load
        loadReportHistory(token);

        // 3. Modal Handlers Setup
        const reportModalElement = document.getElementById("reportModal");
        if (reportModalElement) {
             $("#showReportModal").click(function () {
                 reportModalElement.style.display = "flex";
             });
             $("#closeReportModal").click(function () {
                 reportModalElement.style.display = "none";
             });
             $("#reportModal").click(function (event) {
                 if (event.target.id === "reportModal") {
                     reportModalElement.style.display = "none";
                 }
             });
        }


        // 4. Custom Validator Setup
        $.validator.addMethod(
            "dateMaxToday",
            function (value, element) {
                const today = new Date();
                today.setHours(0, 0, 0, 0);

                const inputDate = new Date(value);
                inputDate.setHours(0, 0, 0, 0);

                return this.optional(element) || inputDate.getTime() <= today.getTime();
            },
            "**Date cannot be in the future"
        );

        // 5. Date Picker Restrictions
        const today = new Date().toISOString().split("T")[0];
        $("#startDateInput").attr("max", today);
        $("#endDateInput").attr("max", today);

        // Event listener for Start Date to enforce date range
        $("#startDateInput").on("change", function () {
            const startDateValue = $(this).val();
            const minEndDate = getNextDay(startDateValue);
            
            $("#endDateInput").attr("min", minEndDate);

            const endDateValue = $("#endDateInput").val();
            if (
                endDateValue &&
                startDateValue &&
                new Date(endDateValue) <= new Date(startDateValue)
            ) {
                $("#endDateInput").val(""); // Clear invalid End Date
            }
        });

        // 6. Form Validation and Submission
        $("#getReport").validate({
            rules: {
                startDate: {
                    required: true,
                    dateMaxToday: true,
                },
                endDate: {
                    required: true,
                    dateMaxToday: true,
                },
            },
            messages: {
                startDate: {
                    required: "**Start date is missing",
                    dateMaxToday: "**Start date cannot be in the future",
                },
                endDate: {
                    required: "**End date is missing",
                    dateMaxToday: "**End date cannot be in the future",
                },
            },

            submitHandler: function (form) {
                const startDate = $("#startDateInput").val();
                const endDate = $("#endDateInput").val();
                const getTemplateUrl = `${ReportTemplateBaseUrl}?start=${startDate}&end=${endDate}`;

                fetch(getTemplateUrl, {
                    method: "POST",
                    headers: {
                        Authorization: token ? `Bearer ${token}` : "",
                    },
                })
                .then((response) => {
                    if (!response.ok) {
                        throw new Error(`Http Error! status: ${response.status}`);
                    }
                    return response.blob();
                })
                .then((blob) => {
                    // Create temporary link for download
                    const href = URL.createObjectURL(blob);
                    const link = document.createElement("a");
                    link.href = href;
                    link.download = "Report Template.zip";
                    link.style.display = "none";

                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                    URL.revokeObjectURL(href); // Clean up temp URL

                    // Reload history and clear form/modal
                    // loadReportHistory();

                    updateDownloadStatus(token, startDate, endDate, "SUCCESS");

                    reportModalElement.style.display = "none";
                    $("#startDateInput").val("");
                    $("#endDateInput").val("");

                    loadReportHistory();

                })
                .catch((error) => {
                    alert("Failed to download the file, Error: " + error.message);
                    updateDownloadStatus(token, startDate, endDate, "FAILED");
                    reportModalElement.style.display = "none";
                });
            },
        });
    });
}


/**
 * Updates the download status on the server.
 * @param {string} startDate The report start date.
 * @param {string} endDate The report end date.
 * @param {string} finalStatus The status to set ('Success' or 'Fail').
 */
function updateDownloadStatus(token, startDate, endDate, finalStatus) {
    const updateUrl = `${ReportTemplateBaseUrl}/updateStatus`;
    
    // We only need to inform the server of the outcome
    const data = {
        startDate: startDate,
        endDate: endDate,
        status: finalStatus
    };

    fetch(updateUrl, {
        method: "POST", // Or PUT, depending on your API design
        headers: {
            "Authorization": token ? `Bearer ${token}` : "",
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            console.error("Failed to update status record on server.");
        }
        // After updating the status, reload the table to show the new status
        loadReportHistory(token); 
    })
    .catch(error => {
        console.error("Network error when updating status:", error);
    });
}



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
                window.location.href = "/Frontend/html/login.html";
            } else {
                if (xhr.status === 23) {
                  showPopup("Error","Session expired. Login again.", "error");
                    sessionStorage.clear();
                    window.location.href = "/Frontend/html/login.html";
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
