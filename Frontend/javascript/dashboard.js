$(document).ready(function() {

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
    console.log(userRole);

    // Hide Users tab for non-admins
    if (userRole !== "ADMIN" && userRole !== "MASTER_ADMIN") {
        $(".tab-btn[data-target='users']").remove();
    }

    // Show default tab: Leads
    $(".dashboard-section").hide();
    $("#leads").show();
    $(".tab-btn[data-target='leads']").addClass("active");

    // Tab click handler
    $(".tab-btn").click(function () {
        const target = $(this).data("target");

        // Show target section, hide others
        $(".dashboard-section").hide();
        $("#" + target).show();

        // Active tab styling
        $(".tab-btn").removeClass("active");
        $(this).addClass("active");
    });

    $("#profilePic").click(function(){
        $("#profileDropdown").toggle();
    });

    // Hide dropdown when clicked outside
    $(document).click(function(event) {
        if (!$(event.target).closest('.profile-menu').length) {
            $("#profileDropdown").hide();
        }
    })

    //get users button
    $("#getUsersBtn").click(function () {
    window.location.href = "/Frontend/html/all-users.html";

});


});
