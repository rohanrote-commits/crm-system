$(document).ready(function () {

    function parseJwt(token) {
        try {
            const base64Url = token.split(".")[1];
            const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
            const jsonPayload = decodeURIComponent(
                atob(base64)
                    .split("")
                    .map(c => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
                    .join("")
            );
            return JSON.parse(jsonPayload);
        } catch (e) {
            return null;
        }
    }

    // ========== AUTH ==========
    const token = sessionStorage.getItem("Authorization");
    if (!token) {
        showPopup("Unauthorized", "Please login.", "warning");
        window.location.href = "/crm/login";
        return;
    }

    const payload = parseJwt(token);
    let isEdit = false;

    $("#addSingleLeadBtn").click(function () {
        isEdit = false;
        $("#leadModalLabel").text("Add Lead");
        $("#saveLeadBtn").text("Add");
        $("#leadForm")[0].reset();
        $("#leadId").val("");
        $("#email").prop("readOnly", false);
        $("#gstin").prop("readOnly", false);
        $("#leadModal").modal("show");
    });

    $(document).on("click", ".edit-lead", function () {

        const table = $("#lead-table").DataTable();
        const rowData = table.row($(this).closest("tr")).data();

        isEdit = true;

        $("#leadModalLabel").text("Edit Lead");
        $("#saveLeadBtn").text("Update Lead");

        $("#leadId").val(rowData.id);
        $("#firstName").val(rowData.firstName);
        $("#lastName").val(rowData.lastName);
        $("#email").val(rowData.email);
        $("#mobileNumber").val(rowData.mobileNumber);
        $("#gstin").val(rowData.gstin);
        $("#leadStatus").val(rowData.leadStatus);
        $("#businessAddress").val(rowData.businessAddress);
        $("#description").val(rowData.description);

        $("input[name='interestedModules']").prop("checked", false);
        rowData.interestedModules.forEach(mod => {
            $(`input[name='interestedModules'][value='${mod}']`).prop("checked", true);
        });

        $("#email").prop("readOnly", true);
        $("#gstin").prop("readOnly", true);

        $("#leadModal").modal("show");
    });

    $("#saveLeadBtn").click(function () {

        if ($("#leadForm").valid()) {

            // UPDATE FLOW
            if (isEdit) {
                $("#updateConfirmModal").modal("show");
            }
            // ADD FLOW → directly submit
            else {
                $("#leadForm").attr("data-confirmed", "1");
                $("#leadForm").submit();
            }
        }
    });

    $("#confirmUpdateBtn").click(function () {
        $("#updateConfirmModal").modal("hide");
        $("#leadForm").attr("data-confirmed", "1");
        $("#leadForm").submit(); // UPDATE
    });

    // ========== VALIDATION ==========
    $.validator.addMethod("namePattern", value => REGX_CONSTANT.NAME.test(value),
       ERROR_MESSAGE_CONSTANTS.INVALID_NAME );

    $.validator.addMethod("emailPattern", value => REGX_CONSTANT.EMAIL.test(value),
       ERROR_MESSAGE_CONSTANTS.INVALID_EMAIL);

    $.validator.addMethod("mobilePattern", value =>REGX_CONSTANT.MOBILE.test(value),
        ERROR_MESSAGE_CONSTANTS.INVALID_MOBILE_NUMBER);

    $.validator.addMethod("gstinPattern", value => REGX_CONSTANT.GSTIN.test(value),
     ERROR_MESSAGE_CONSTANTS.INVALID_GSTIN );

    $.validator.addMethod("addressPattern", function (value, element) {
        return this.optional(element) || REGX_CONSTANT.ADDRESS_DESC.test(value);
    }, ERROR_MESSAGE_CONSTANTS.INVALID_ADDRESS);

    $.validator.addMethod("descriptionPattern", function (value, element) {
        return this.optional(element) || REGX_CONSTANT.ADDRESS_DESC.test(value);
    },  ERROR_MESSAGE_CONSTANTS.INVALID_DESCRIPTION);


    $("#leadForm").validate({
        rules: {
            firstName: { required: true, namePattern: true },
            lastName: { required: true, namePattern: true },
            email: { required: true, emailPattern: true },
            mobileNumber: { required: true, mobilePattern: true },
            gstin: { required: true, gstinPattern: true },
            businessAddress: { addressPattern: true },
            description: { descriptionPattern: true }
        },
        errorElement: "span",
        errorClass: "text-danger",

        submitHandler: function (form) {

            // Block unconfirmed update
            if (isEdit && $("#leadForm").attr("data-confirmed") !== "1") {
                return false;
            }

            const leadId = $("#leadId").val();

            const leadData = {
                firstName: $("#firstName").val(),
                lastName: $("#lastName").val(),
                email: $("#email").val(),
                mobileNumber: $("#mobileNumber").val(),
                gstin: $("#gstin").val(),
                description: $("#description").val(),
                businessAddress: $("#businessAddress").val(),
                leadStatus: $("#leadStatus").val(),
                user: payload?.email,   // FIXED KEY
                interestedModules: $(".form-check-input:checked")
                    .map(function () { return $(this).val(); })
                    .get()
            };

            const method = isEdit ? "PUT" : "POST";
            const url = isEdit ? LEAD_API.UPDATE(leadId) : LEAD_API.CREATE;

            $.ajax({
                url,
                type: method,
                contentType: "application/json",
                headers: { Authorization: "Bearer " + token },
                data: JSON.stringify(leadData),

                success: function () {
                    const msg = isEdit
                        ? "Lead updated successfully!"
                        : "Lead added successfully!";

                    showPopup("Success", msg, "success");
                    showAlert(msg, "success");
                    $("#leadModal").modal("hide");
                    $("#lead-table").DataTable().ajax.reload();
                },

                error: function (xhr) {
                    if (xhr.status === 409) {
                        showPopup("Duplicate", "Email already exists!", "error");
                    } else {
                        console.log(xhr);
                        showPopup("Error", "Something went wrong. Please try again", "error");
                    }
                }
            });

            isEdit = false;
        }
    });

});

function showAlert(message, type) {
    const alertContainer = $("#alert-container");
    const alert = $(`
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `);
    alertContainer.append(alert);

    setTimeout(() => {
        alert.alert('close');
    }, 5000);
}

function showPopup(title, message, iconType) {
    Swal.fire({
        title,
        text: message,
        icon: iconType,
        confirmButtonText: 'OK'
    });
}
