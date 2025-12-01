jQuery(function() {
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
    let fileName = "";
    if (!token) {
       showPopup("Error","Unauthorized. Please login.", "error");
        showAlert("Unauthorized. Please login.","danger");
        window.location.href = "/crm/login";
        return;
    }
    
    const payload = parseJwt(token);
    const userRole = payload?.role?.trim();
    console.log(payload.email)

      $("#upload-table").DataTable({
        ajax: {
          url: LEAD_API.LEAD_UPLOAD_HISTORY,
          type: "GET",
          headers: {
            Authorization: "Bearer " + token,
          },
          dataSrc: "",
          error: function (xhr) {
          errorTable.clear().draw();
            if (xhr.status === 401) {
                showPopup("Error","Session expired. Login again.", "error");
                showAlert("Session expired. Login again.", "warning");
                sessionStorage.clear();
                window.location.href = "/crm/login";
                return;
            }
            if(xhr.status===400){
               showPopup("Error","No File Histroy Found.", "error");
              showAlert("No Invalid Leads for the Record","info")
            }
            else{
              showPopup("Error","No File Histroy Found.", "error");
              showAlert("No Invalid Leads Found.", "danger");
            }
        }
        },

        columns: [
          {
            data: null,
            title: "Sr.No.",
            orderable: false,
            render: function (data, type, row, meta) {
              return meta.row + meta.settings._iDisplayStart + 1;
            },
          },
          { data: "fileName", title: "File Name" },
          {
            data: "uploadedAt",
            render: function (data) {
              if (!data) return "-";

              const date = new Date(data);

              const options = {
                year: "numeric",
                month: "short",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit",
                hour12: true,
              };

              return date.toLocaleString("en-GB", options);
            },
          },
          { data: "uploadedBy", title: "Uploaded By" },

          {
            data: "uploadStatus",
            title: "Status",
            orderable: false,
            render: function (data) {
              let badgeClass = "";
              switch (data) {
                case "PROCESSING":
                  badgeClass = "bg-primary";
                  break;
                case "PARTIALLY_SUCCESS":
                  badgeClass = "bg-warning";
                  break;
                case "SUCCESS":
                  badgeClass = "bg-success";
                  break;
                case "FAILED":
                  badgeClass = "bg-danger";
                  break;
                default:
                  badgeClass = "bg-secondary";
              }
              return `<span class="badge ${badgeClass}">
                              ${
                                data === "PARTIALLY_SUCCESS"
                                  ? "PARTIALLY SUCCESS"
                                  : data
                              }
                          </span>`;
            },
          },

          {
            data: "uploadStatus",
            title: "Action",
            orderable: false,
            render: function (data, type, row) {
              console.log(row.id);
              if (data === "SUCCESS") {
                // Only show view button
                return `
                <button class="btn btn-sm btn-secondary view-error-info" data-id="${row.id}">
                    <i class="bi bi-eye"></i>
                </button>
            `;
              } else {
                // Show both download + view
                return `
                <button class="btn btn-sm btn-secondary download-error" data-id="${row.id}">
                    <i class="bi bi-download"></i>
                </button>
                <button class="btn btn-sm btn-secondary view-error-info" data-id="${row.id}">
                    <i class="bi bi-eye"></i>
                </button>
            `;
              }
            },
          },
        ],

        pageLength: 10, 
        lengthMenu: [
          [10, 25, 50, -1],
          [10, 25, 50, "All"],
        ],
        destroy: true,
        responsive: true,
        searching: true,
        paging: true,
        ordering: true,
        info: true,
      });


        $("#upload-table").on("click", ".download-error", function (e) {
                    e.preventDefault();
          const uploadHistoryId = $(this).data("id");
          const fileName = "Lead_Error"
          $.ajax({
            url: LEAD_API.ERROR_FILE_BY_HISTORY_ID,
            type: "GET",
            headers: {
              Authorization: "Bearer " + token,
            },
            xhrFields: {
              responseType: "blob",
            },
            success: function (data, status, xhr) {
              const filename = `${fileName.replace(" ", "_")}`;
              const blob = new Blob([data], {
                type: xhr.getResponseHeader("Content-Type"),
              });
              // Create a download link dynamically
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement("a");
              a.href = url;
              a.download = filename;
              document.body.appendChild(a);
              a.click();
              a.remove();
              window.URL.revokeObjectURL(url);
              showPopup("Success","Error File downloded successfully", "success");
              showAlert("Error File downloded successfully", "success");
            },
            error: function (xhr) {
              if (xhr.status === 401) {
                 showPopup("Error","Session expired. Login again.", "error");
                showAlert("Session expired. Please login again.", "warning");
                sessionStorage.clear();
                window.location.href = "/crm/login";
              } 
              else if(xhr.status===404){
                  showPopup("Error","Not Error Records Found this File", "error");
                 showAlert("Not Error Records Found this File", "danger");
              }
              else {
                showAlert("Error while downloading the Error File.", "danger");
                showPopup("Error","Error while downloading the Error File.", "error");
              }
            },
          });
        });

      //Function to save id of each row in session storage and use it on next storage
      $("#upload-table").on("click", ".view-error-info", function () {
        const row = $("#upload-table")
          .DataTable()
          .row($(this).closest("tr"))
          .data();

        sessionStorage.setItem("id", row.id);
        sessionStorage.setItem("file",row.errorFileName)
        window.location.href = "/crm/leads/view-error";
      });

          function showPopup(title, message, iconType) {
    Swal.fire({
        title: title,
        text: message,
        icon: iconType, // success, error, warning, info
        confirmButtonText: 'OK'
    });
}
});