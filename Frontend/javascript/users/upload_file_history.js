jQuery(function() {
  $("#header").load("/Frontend/html/components/header.html");
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
    console.log(payload.email)

    $.ajax({
      url: `http://localhost:8080/crm/history/user/${payload?.email}`,
      type: "GET",
      headers: {
        Authorization: "Bearer " + token,
      },
      success: function (fileList) {
        $("#upload-table").DataTable({
          data: fileList,
          columns: [
            {data:"id",
              visible:false,
            },
            {
                data: null, 
                title: "Sr.No.",
                orderable: false, 
                render: function (data, type, row, meta) {
                    return meta.row + meta.settings._iDisplayStart + 1;
                }
            },
            { data: "fileName" },
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
            }},
            { data: "uploadedBy" },
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
                return `<span class="badge ${badgeClass}">${
                  data === "PARTIALLY_SUCCESS" ? "PARTIALLY SUCCESS" : data
                }</span>`;
              },
            },
            {
              data: "errorFileName",
              title : "Action",
              orderable:false,
              render: function (data, type, row) {
                return `
                  <button class="btn btn-sm btn-secondary download-error" data-file="${data}">
                                    <i class="bi bi-download"></i>
                                </button>
                      <button class="btn btn-sm btn-secondary view-error-info" data-lead="${data}">
                            <i class="bi bi-eye"></i>
                        </button>              
                `;
              },
            },

          ],
          pageLength: 5,
          destroy: true,
          responsive: true,
          searching: true,
          paging: true,
          ordering: true,
          info: true,
        });
      },
    });

        $("#upload-table").on("click", ".download-error", function (e) {
          e.preventDefault();
          const fileName = $(this).data("file");
          $.ajax({
            url: `http://localhost:8080/crm/history/user/error/${fileName}`,
            type: "GET",
            headers: {
              Authorization: "Bearer " + token,
            },
            xhrFields: {
              responseType: "blob",
            },
            success: function (data, status, xhr) {
              const filename = `${fileName.replace(" ", "_")}.xlsx`;
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
              showAlert("Error File downloded successfully", "success");
            },
            error: function (xhr) {
              if (xhr.status === 401) {
                showAlert("Session expired. Please login again.", "warning");
                sessionStorage.clear();
                window.location.href = "/Frontend/html/login.html";
              } else {
                console.error("Token used:", token);
                showAlert("Error while downloading the Error File.", "danger");
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
        sessionStorage.setItem("file",row.errorFileName)
    
        sessionStorage.setItem("id", row.id);
        window.location.href = "/Frontend/html/users/view_error_user.html";
      });


});