
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
        alert("⚠ Unauthorized. Please login.");
        window.location.href = "/Frontend/html/login.html";
        return;
    }

    const payload = parseJwt(token);
    const userRole = payload?.role?.trim();
    console.log(payload);
    
    loadLeads(payload?.email,token);


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
        loadLeads(payload?.email,token);
    }
    else if(target === "users"){
        loadUsers(token)
    }
    
  });

  // Profile dropdown
  $("#profilePic").click(function () {
    $("#profileDropdown").toggle();
  });

  // Close dropdown when clicked outside
  $(document).click(function (event) {
    if (!$(event.target).closest(".profile-menu").length) {
      $("#profileDropdown").hide();
    }
  });

    // Delete button click
    $('#lead-table').on('click', '.delete-lead', function() {
        const email = $(this).data('email');

        if (confirm("Are you sure you want to delete this lead?")) {
            $.ajax({
                url: `http://localhost:8080/api/crm/lead/`,
                type: 'DELETE',
                data :{
                    email : email
                },
                headers: { "Authorization": "Bearer " + token },
                success: function() {
                    alert("Lead deleted successfully.");
                    $('#lead-table').DataTable().ajax.reload();
                },
                error: function() {
                    alert("Error deleting lead.");
                }
            });
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

        },
        error: function (xhr) {
            if (xhr.status == 401) {
                alert("Session expired. Login again.");
                sessionStorage.clear();
                window.location.href = "/Frontend/html/login.html";
            } else {
                alert("Error loading users.");
            }
        }
    });
}

function loadLeads(email, token) {
    $.ajax({
        url: `http://localhost:8080/api/crm/lead/by/email/${email}`,
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
            { data: "mobileNumber", title: "Mobile" },
            { data: "gstin", title: "GSTIN" },
            { data: "description", title: "Description" },
            { data: "businessAddress", title: "Address" },
            {
                data: "leadStatus",
                title: "Status",
                render: function (data) {
                    let badgeClass = "";
                    switch (data) {
                        case "ADDED": badgeClass = "bg-primary"; break;
                        case "CONTACTED": badgeClass = "bg-warning"; break;
                        case "CONVERTED": badgeClass = "bg-success"; break;
                        case "NOT_CONVERTED": badgeClass = "bg-danger"; break;
                        default: badgeClass = "bg-secondary";
                    }
                    return `<span class="badge ${badgeClass}">${data}</span>`;
                }
            },
            {
                data: "interestedModules",
                title: "Interested Modules",
                render: function (data) {
                    return data && data.length ? data.join(", ") : "-";
                }
            },
            {
            data: null,
            title: "Action",
            orderable: false, // Prevent sorting on this column
            render: function (data, type, row) {
                return `
                    <div class="d-flex justify-content-center gap-2">
                        <button class="btn btn-sm btn-warning edit-lead" data-email="${row.email}">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-danger delete-lead" data-email="${row.email}">
                            <i class="bi bi-trash"></i>
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
