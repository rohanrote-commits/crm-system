
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
        alert("Unauthorized. Please login.");
        window.location.href = "/Frontend/html/login.html";
        return;
    }

    const payload = parseJwt(token);
    const userRole = payload?.role?.trim();
    console.log(payload);

    loadLeads(payload,token);


// Hide Users tab for non-admin roles
    if (userRole !== "ADMIN" && userRole !== "MASTER_ADMIN") {
        $(".sidebar-btn[data-target='users']").hide();
    } else {
        $(".sidebar-btn[data-target='users']").show();
    }

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

    // Toggle dropdown when profile image is clicked
  $("#profilePic").on("click", function (e) {
    e.stopPropagation(); // Prevent click from bubbling up
    $("#profileDropdown").toggleClass("show"); // Toggle visibility
  });

  // Hide dropdown when clicking anywhere outside
  $(document).on("click", function (e) {
    if (!$(e.target).closest(".profile-menu").length) {
      $("#profileDropdown").removeClass("show");
    }
  });

    $("#addLeadBtn").on("click", function () {
        $("#leadDropdown").toggleClass("show");
    });

// Optional: close dropdown when clicking outside
    $(document).on("click", function (e) {
        if (!$(e.target).closest(".section-buttons").length) {
            $("#leadDropdown").removeClass("show");
        }
    })

    $("#importLead").click(function (event) {
          window.location.href = "leads/upload_lead.html";
    });


    //delete profile
    $("#delete-profile").click(function () {

        if (!token) {
            alert("User not logged in!");
            return;
        }

        if (!confirm("Are you sure you want to delete your profile? This action is irreversible.")) {
            return;
        }

        $.ajax({
            url: `http://localhost:8080/crm/user/delete-user`,
            type: "DELETE",
            headers: {
                "Authorization": "Bearer " + token
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
            }
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



    // Delete button click
    $('#lead-table').on('click', '.delete-lead', function() {
        const email = $(this).data('email');

        if (confirm("Are you sure you want to delete this lead?")) {
            $.ajax({
                url: `http://localhost:8080/crm/lead/`,
                type: 'DELETE',
                data :{
                    email : email
                },
                headers: { "Authorization": "Bearer " + token },
                success: function() {
                    alert("Lead deleted successfully.");
                    $('#lead-table').DataTable().reload();
                },
                error: function() {
                    alert("Error deleting lead.");
                }
            });
            $('#lead-table').DataTable().ajax.reload();
        }
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
                    alert("User deleted successfully.");
                    $('#user-table').DataTable().ajax.reload();
                },
                error: function() {
                    alert("Error deleting lead.");
                }
            });
        }
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
    console.log(token);
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
    $.ajax({
        url: `http://localhost:8080/crm/lead/by/${payload.sub}`,
        type: "GET",
        headers: {
            "Authorization": "Bearer " + token
        },
        success: function (response) {
            console.log(" Leads fetched:", response);
            initializeLeadTable(response);
        },
        error: function (xhr) {
            if (xhr.status === 401) {
                alert("Session expired. Login again.");
                sessionStorage.clear();
                window.location.href = "/Frontend/html/login.html";
            } else {
                console.error("token is : " + token);
                alert("Error loading leads.");
            }
        }
    });
}


// Initialize DataTable with dynamic data
function initializeLeadTable(data) {
    if ($.fn.DataTable.isDataTable("#lead-table")) {
        $("#lead-table").DataTable().clear().rows.add(data).draw();
        return;
    }
    $("#lead-table").DataTable({
        data: data,
        columns: [
            { data: "firstName", title: "First Name" },
            { data: "lastName", title: "Last Name" },
            { data: "email", title: "Email" },
            { data: "mobileNumber", title: "Mobile", visible: false },
            { data: "gstin", title: "GSTIN" },
            { data: "description", title: "Description", visible: false  },
            { data: "businessAddress", title: "Address", visible: false  },
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
                    return `<span class="badge ${badgeClass}">${data === "NOT_CONVERTED"?"NOT CONVERTED":data}</span>`;
                }
            },
            {
                data: "interestedModules",
                title: "Interested Modules",
                orderable: false,
                render: function (data) {
                    return data && data.length ? data.join(`,\n`) : "-";
                }
            },
            {
                data: null,
                title: "Action",
                orderable: false, // Prevent sorting on this column
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
