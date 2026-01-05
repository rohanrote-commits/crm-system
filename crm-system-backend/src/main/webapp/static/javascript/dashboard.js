
let reportDataTableInstance = null;

$(document).ready(function () {

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

        showPopup("Error","Unauthorized. Please login.", "error");
        window.location.href = "/crm/login";
        return;
    }

    const payload = parseJwt(token);
    const userRole = payload?.role?.trim();

    loadLeads(payload,token);

    handleInitialDashboardLoad(token, payload);

    // Sidebar navigation
    $(".sidebar-btn").click(function () {
        const target = $(this).data("target");

        $(".sidebar-btn").removeClass("active");
        $(this).addClass("active");

        $(".dashboard-section").hide();
        $("#" + target).show();
        console.log(target);

        // Saving the state
        sessionStorage.setItem("activeDashboardSection", target);

        if(target === "leads"){
            loadLeads(payload,token);
        }
        if(target === "reports"){
            // initializeReportModule(payload, token);
            loadReportHistory(token);
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
        sessionStorage.setItem("Authorization", token);
        window.location.href = "/crm/leads/upload";
    });



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
                sessionStorage.removeItem("hasVisitedDashboard")
                sessionStorage.removeItem("activeDashboardSection");

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
let leadId = null;
$(document).on("click", ".delete-lead", function () {
    leadId = $(this).data("id");
    $("#deleteConfirmModal").modal("show");
});


$("#confirmDeleteBtn").click(function () {
    if (!leadId) return;
    $.ajax({
        url: LEAD_API.DELETE(leadId),
        type: "DELETE",
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

    // --- Report Module ---

    // --- Modal Handlers ---
    const reportModalElement = $("#reportModal");
    $("#showReportModal").click(() => reportModalElement.show());
    $("#closeReportModal").click(() => reportModalElement.hide());
    reportModalElement.click(function (e) {
        if (e.target.id === "reportModal") $(this).hide();
    });

    // --- Date Picker Restrictions ---
    const today = new Date().toISOString().split("T")[0];
    $("#startDateInput").attr("max", today);
    $("#endDateInput").attr("max", today);

    $("#startDateInput").on("change", function () {
        const startDateValue = $(this).val();
        $("#endDateInput").attr("min", getNextDay(startDateValue));

        const endDateValue = $("#endDateInput").val();
        if (endDateValue && new Date(endDateValue) <= new Date(startDateValue)) {
            $("#endDateInput").val("");
        }
    });

    // --- Custom Validator ---
    $.validator.addMethod("dateMaxToday", function (value, element) {
        const inputDate = new Date(value);
        inputDate.setHours(0, 0, 0, 0);
        const maxDate = new Date();
        maxDate.setHours(0, 0, 0, 0);
        return this.optional(element) || inputDate <= maxDate;
    }, "**Date cannot be in the future");

    // --- Form Validation and Submission ---
    $("#getReport").validate({
        rules: {
            startDate: { required: true, dateMaxToday: true },
            endDate: { required: true, dateMaxToday: true }
        },
        messages: {
            startDate: { required: "**Start date is missing", dateMaxToday: "**Start date cannot be in the future" },
            endDate: { required: "**End date is missing", dateMaxToday: "**End date cannot be in the future" }
        },
        submitHandler: function () {
            const startDate = $("#startDateInput").val();
            const endDate = $("#endDateInput").val();

            const getTemplateUrl =  REPORT_API.GET_TEMPLATE(startDate,endDate);

            fetch(getTemplateUrl, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    userId: `${payload.sub}`
                }
            })
                .then(resp => {
                    if (!resp.ok) throw new Error(`HTTP Error! status: ${resp.status}`);
                    const contentType = resp.headers.get("content-type");
                    if(contentType && contentType.includes("application/zip")) {
                        return resp.blob().then(blob => ({ blob, resp }));
                    } else {
                        showPopup("Warning","No leads Registered from " + startDate + " To " + endDate + " !!", "warning");
                        reportModalElement.hide();
                        return null;
                    }
                })
                .then(result => { // Capture the returned value (either {blob, resp} or null)

                    if (result === null) {
                        return; // Exit the .then() block immediately, preventing the TypeError.
                    }

                    // Destructuring is now safe only if result is not null
                    const { blob, resp } = result;

                    let filename = "COVORO Report " + startDate + " To " + endDate + ".zip"; // fallback

                    const link = document.createElement("a");
                    link.href = URL.createObjectURL(blob);
                    link.download = filename;
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);

                    // updateDownloadStatus(startDate, endDate, "SUCCESS");
                    loadReportHistory(token);
                    reportModalElement.hide();

                    $("#startDateInput, #endDateInput").val("");
                })
                .catch(err => {
                    alert("Failed to download file : " + err.message);
                    reportModalElement.hide();
                });
        }
    });

    // --- Initial Load of Report History ---
    loadReportHistory(token);
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

    $('#lead-table').on('click', '.auto-change-status-btn', function () {
        let leadId = $(this).data('id');
        let currentStatus = $(this).data('status');

        const leadStatusIntegerMap = {
            "ADDED": 0,
            "CONTACTED": 1,
            "CONVERTED": 2,
            "NOT_CONVERTED": 3
        };

        const nextStatusMap = {
            "ADDED": "CONTACTED",
            "CONTACTED": "CONVERTED",
            "CONVERTED": "NOT_CONVERTED",
            "NOT_CONVERTED": "ADDED"
        };
        let nextStatus = nextStatusMap[currentStatus];
        let statusCode = leadStatusIntegerMap[nextStatus]; // convert to integer

        $.ajax({
            url: LEAD_API.UPDATE_STATUS(leadId),
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({ status: statusCode }),
            headers: { "Authorization": "Bearer " + token },
            success: function (response) {
                Swal.fire('Updated!', `Status changed to ${nextStatus}`, 'success');
                $('#lead-table').DataTable().ajax.reload();
            },
            error: function (error) {
                console.log(error);
                if (error.status===401){
                    showPopup("Error","Session expired. Login again.", "error");
                    window.location.href = "/crm/login";
                }
                Swal.fire('Error', 'Failed to update status', 'error');
            }
        });
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
}


// Function: Load Leads from API
function loadLeads(payload, token) {
    console.log("Loading leads for user:", payload.sub);
    $("#lead-table").DataTable({
        ajax: {
            url: LEAD_API.GET_BY_USER,
            type: "GET",
            headers: {
                "Authorization": "Bearer " + token
            },
            data: {
                userId: payload.sub
            },
            dataSrc: function (response) {
                console.log("Leads fetched:", response);
                return response || [];
            },
            error: function (xhr) {
                errorMsg = xhr.responseJSON.message;
                showPopup("Error", errorMsg, "error");
                // if (xhr.status === 401 || xhr.status === 403 ) {
                //     showPopup("Error", "Session expired. Login again.", "error");
                //     window.location.href = "/crm/login";
                // } else {
                //     showPopup("Error", "Error loading leads.", "error");
                // }
            }
        },

        columns: [
            // {data : "id",title: "id" , visible: false},
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
                render: function (data, type, row) {

                    let badgeClass = "";
                    switch (data) {
                        case "ADDED": badgeClass = "bg-primary"; break;
                        case "CONTACTED": badgeClass = "bg-warning"; break;
                        case "CONVERTED": badgeClass = "bg-success"; break;
                        case "NOT_CONVERTED": badgeClass = "bg-danger"; break;
                        default: badgeClass = "bg-secondary";
                    }

                    let label = data === "NOT_CONVERTED" ? "NOT CONVERTED" : data;

                    return `
            <span class="badge ${badgeClass}">${label}</span>
            <br>
            <button class="btn btn-sm btn-link auto-change-status-btn" data-id="${row.id}" data-status="${data}">
                Change
            </button>`;
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
                        <button class="btn btn-sm btn-danger delete-lead" data-id="${row.id}">
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


// Report
function handleInitialDashboardLoad(token, payload) {
    // Check session marker for "fresh login" vs "refresh"
    const isFirstLoadAfterLogin = !sessionStorage.getItem("hasVisitedDashboard");

    let activeSection;

    if (isFirstLoadAfterLogin) {
        // Flow: Login -> Always Leads
        activeSection = "leads";
        sessionStorage.setItem("activeDashboardSection", "leads");
    } else {
        // Flow: Refresh -> Last viewed section
        activeSection = sessionStorage.getItem("activeDashboardSection") || "leads";
    }

    // Apply active state
    $(".sidebar-btn").removeClass("active");
    $(`.sidebar-btn[data-target="${activeSection}"]`).addClass("active");
    $(".dashboard-section").hide();
    $("#" + activeSection).show();

    // Load data for the active section
    if (activeSection === "leads") {
        loadLeads(payload, token);
    } else if (activeSection === "reports") {
        // Assuming this function is outside and accepts (payload, token)
        loadReportHistory(payload, token);
    }

    // Mark the dashboard as visited for this session
    sessionStorage.setItem("hasVisitedDashboard", "true");
}

// --- Helper: start of End Date restriction ---
function getNextDay(dateString) {
    if (!dateString) return "";
    const date = new Date(dateString);
    date.setDate(date.getDate() + 1);
    return date.toISOString().split("T")[0];
}

// --- Initialize or Re-initialize DataTable ---
function initializeReportDataTable(initialData = []) {

    if ($.fn.DataTable.isDataTable("#report-table")) {
        reportDataTableInstance.destroy();
    }

    reportDataTableInstance = $("#report-table").DataTable({
        pageLength: 10,
        data: initialData,
        columns: [
            { data: null, title: "Sr No" },
            { data: "userName", title: "User Name" },
            { data: "downloadedAt", title: "Downloaded At" },
            { data: "dateRange", title: "Selected Date Range" },
            { data: "status", title: "Download Status" }
        ],
        order: [[2, "desc"]],
        "drawCallback": function (settings) {
            let api = this.api();
            let startIndex = api.context[0]._iDisplayStart;

            api.column(0, { page: "current" })
                .nodes()
                .each(function (cell, i) {
                    cell.innerHTML = startIndex + i + 1;
                });
        }
    });
}

// --- Fetch Report History from Backend ---
function loadReportHistory(token) {

    if (!token) {
        console.error("Token missing. Cannot fetch report history.");
        initializeReportDataTable([]);
        return;
    }

    fetch(REPORT_API.GET_REPORT_HISTORY, {
        method: "GET",
        headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
        },
    })
        .then(response => {
            if (!response.ok) {
                console.error("Failed to fetch report history. Status:", response.status);
                initializeReportDataTable([]);
                return null;
            }
            return response.json();
        })
        .then(data => {
            if (data) initializeReportDataTable(data);
        })
        .catch(error => {
            console.error("Network error:", error);
            // Send empty
            initializeReportDataTable([]);
        });
}

// --- Updates Download status of Report ---
// function updateDownloadStatus(startDate, endDate, status) {
//     if (!reportDataTableInstance) return;
//
//     // Find the row(s) matching startDate and endDate
//     reportDataTableInstance.rows().every(function () {
//         const data = this.data();
//         if (new Date(data.startDate).getTime() === new Date(startDate).getTime() &&
//             new Date(data.endDate).getTime() === new Date(endDate).getTime()) {
//             data.status = status;  // update status field
//             this.data(data);       // update row in DataTable
//         }
//     });
//     reportDataTableInstance.draw(false); // redraw table without resetting pagination
// }

// // --- Function: initializes report module ---
// function initializeReportModule(payload, token) {
//     loadReportHistory(token);
// }