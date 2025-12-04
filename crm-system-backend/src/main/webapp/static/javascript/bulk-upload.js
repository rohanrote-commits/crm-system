$(document).ready(function () {

    // Create a hidden file input dynamically
    const fileInput = $('<input type="file" id="userFile" accept=".xlsx,.xls" style="display:none;">');
    $('body').append(fileInput);

    $("#manage-users").hide();

    // Trigger file select when upload button is clicked
    $("#uploadTemplateBtn").click(function () {
        fileInput.click();
    });

    // Handle file selection and upload
    fileInput.on('change', function () {
        let file = this.files[0];
        if (!file) {
            return;
        }

        const token = sessionStorage.getItem("Authorization");
        if (!token) {
            showPopup("Warning","⚠ Session expired. Please login again.","warning");
            window.location.href = "/Frontend/html/login.jsp";
            return;
        }

        let formData = new FormData();
        formData.append("file", file);

        $.ajax({
            url: 'http://localhost:8080/crm/user/upload-user-file',
            type: 'POST',
            headers: { "Authorization": "Bearer " + token },
            data: formData,
            processData: false,
            contentType: false,
            success: function (response) {

                showPopup("Success",response,"success");

                fileInput.val(''); // Clear input
            },
            error: function (xhr) {
                 let error = xhr.responseJSON.message;
             showPopup("Error",error,"error");

            }
        });
    });

    // Download template
    $("#downloadTemplate").click(function (e) {
     
        $.ajax({
            url: 'http://localhost:8080/crm/files/user-template',
            type: 'GET',
            xhrFields: { responseType: 'blob' },
            success: function (data,status,xhr) {
                const filename = "User_Template.xlsx";
                const blob = new Blob([data],{
                     type: xhr.getResponseHeader("Content-Type"),
                });
                const url = window.URL.createObjectURL(blob);

                const a = document.createElement("a");
                a.href = url;
                a.download = filename;
                document.body.appendChild(a);
                a.click();
                a.remove();

                window.URL.revokeObjectURL(url);
                showPopup("Success","File Downloded successfully","success");
            },
            error: function () {
                    if (xhr.status === 401) {
        showPopup("Warning","Session expired. Please login again.","warning");
        sessionStorage.clear();
        window.location.href = "/Frontend/html/login.jsp";
      } else {
        console.error("Token used:", token);
        showPopup("Error while downloading the template.","danger");
      }
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
    })
}


});

