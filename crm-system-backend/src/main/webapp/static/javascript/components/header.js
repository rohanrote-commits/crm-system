$(document).ready(function () {
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
    const token = sessionStorage.getItem("Authorization");
    console.log(token);
    console.log("Decoded Token:", parseJwt(token));

    $("#back").click(function () {
        window.history.back();
    });
    $("#profilePic").click(function () {
        $("#profileDropdown").toggle();
    });

    $("#logout").click(function () {
        if (!token) {
            window.location.href = "/crm/login";
            return;
        }
        $.ajax({
            url: `http://localhost:8080/crm/user/logout`,
            type: "GET",
            headers: { Authorization: "Bearer " + token },
            success: function (response) {
                showPopup("Success", response.message || response, "success");
                localStorage.removeItem("Authorization");
                window.location.href = "/crm/login";
            },
            error: function (xhr) {
                let errorMsg = "Failed to logout";
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMsg = xhr.responseJSON.message;
                }
                showPopup("Error", errorMsg, "error");
            },
        });
    });


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
                let dateStr = user.registeredOn; // example: "2025-11-24T14:32:10"
                let dateObj = new Date(dateStr);

                // Convert to readable format
                let readable = dateObj.toLocaleString("en-IN", {
                    day: "2-digit",
                    month: "short",
                    year: "numeric",
                    hour: "2-digit",
                    minute: "2-digit",
                    hour12: true,
                });

                $("#profileDate").val(readable);

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
                showPopup("Error", errorMsg, "error");
            },
        });
    }); // Edit profile

    $("#editProfileBtn").click(function () {
        $(
            "#profileMobile, #profileAddress, #profileCity, #profileState, #profileCountry, #profilePin"
        ).prop("readonly", false);
        $("#editProfileBtn").addClass("d-none");
        $("#saveProfileBtn").removeClass("d-none");
    });

    $.validator.addMethod(
        "mobilePattern",
        function (value, element) {
            return this.optional(element) || /^[789]\d{9}$/.test(value);
        },
        "Mobile must start with 7/8/9 and be 10 digits"
    );

    $.validator.addMethod(
        "addressPattern",
        function (value, element) {
            return (
                this.optional(element) || /^[A-Za-z0-9 ,./#\-]{1,200}$/.test(value)
            );
        },
        "Address can contain letters, numbers, ,./#- (max 100)"
    );

    $.validator.addMethod(
        "pinPattern",
        function (value, element) {
            return this.optional(element) || /^[0-9]{6}$/.test(value);
        },
        "Pin code must be 6 digits"
    );

    $("#profileForm").validate({
        rules: {
            profileMobile: { required: true, mobilePattern: true },
            profileAddress: { required: true, addressPattern: true },
        },
    });

    $("#saveProfileBtn").click(function () {
        if (!$("#profileForm").valid()) return;

        const updatedProfile = {
            email: $("#profileEmail").val(),
            mobileNumber: $("#profileMobile").val(),
            address: $("#profileAddress").val(),
            city: $("#profileCity").val(),
            state: $("#profileState").val(),
            country: $("#profileCountry").val(),
            pinCode: $("#profilePin").val(),
        };

        $.ajax({
            url: `http://localhost:8080/crm/user/update`,
            type: "POST",
            headers: {
                Authorization: "Bearer " + token,
                "Content-Type": "application/json",
            },
            data: JSON.stringify(updatedProfile),
            success: function () {
                showPopup("Success", "Profile updated successfully", "success");
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
                showPopup("Error", errorMsg, "error");
            },
        });
    });
    // Close dropdown if clicked outside
    $(document).click(function(e){
        if(!$(e.target).closest("#profilePic, #profileDropdown").length){
            $("#profileDropdown").hide();
        }
    });

    $("#delete-profile").click(function () {
        if (!token) {
            showPopup("Warning", "User not logged in!", "warning");
            return;
        }

        showDeleteConfirm().then((ok) => {
            if (!ok) return;

            $.ajax({
                url: `http://localhost:8080/crm/user/delete-user`,
                type: "DELETE",
                headers: {
                    Authorization: "Bearer " + token,
                },
                success: function (response) {
                    showPopup("Info", response.message || response, "info");

                    localStorage.removeItem("Authorization");
                    window.location.href = "/crm/login";
                },
                error: function (xhr) {
                    let errorMsg = "Failed to delete user";
                    if (xhr.responseJSON && xhr.responseJSON.message) {
                        errorMsg = xhr.responseJSON.message;
                    }
                    showPopup("Error", errorMsg, "error");
                },
            });
        });
    });

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
    function showPopup(title, message, iconType) {
        Swal.fire({
            title: title,
            text: message,
            icon: iconType, // success, error, warning, info
            confirmButtonText: "OK",
        });
    }

});
