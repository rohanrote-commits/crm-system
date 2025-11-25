// /**
//  * Initializes the Report Dashboard module, handling authentication,
//  * report history loading, DataTable setup, form validation, and report downloading.
//  */
// function initializeReportModule() {
    
//     // --- Configuration Variables ---
//     const ReportHistoryAllUrl = "http://localhost:8080/crm/report/getDownloadedRecordHistory";
//     const ReportTemplateBaseUrl = "http://localhost:8080/crm/report/getTemplate";

//     let reportDataTableInstance = null;
//     let payload = null;
//     let token = null;
//     let userRole = "N/A";
//     let userName = "N/A";

//     // --- Utility Functions ---

//     // /**
//     //  * Parses the JWT token to extract the payload (e.g., user details).
//     //  * @param {string} t The JWT token string.
//     //  * @returns {object|null} The parsed payload object or null if parsing fails.
//     //  */
//     // function parseJwt(t) {
//     //     try {
//     //         const base64Url = t.split(".")[1];
//     //         const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
//     //         const jsonPayload = decodeURIComponent(
//     //             atob(base64)
//     //                 .split("")
//     //                 .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
//     //                 .join("")
//     //         );
//     //         return JSON.parse(jsonPayload);
//     //     } catch (e) {
//     //         console.error("Error parsing JWT:", e);
//     //         return null;
//     //     }
//     // }

//     /**
//      * Calculates the day immediately following the given date string (YYYY-MM-DD).
//      * Used to restrict the minimum selectable end date.
//      * @param {string} dateString The start date in YYYY-MM-DD format.
//      * @returns {string} The next day's date in YYYY-MM-DD format.
//      */
//     function getNextDay(dateString) {
//         if (!dateString) return "";
//         const date = new Date(dateString);
//         date.setDate(date.getDate() + 1);
//         return date.toISOString().split("T")[0];
//     }

//     // --- DataTable Setup ---

//     /**
//      * Initializes or re-initializes the DataTables instance for the report history.
//      * @param {Array} initialData The data to load into the table.
//      */
//     function initializeReportDataTable(initialData = []) {
//         // Destroy existing instance if it exists
//         if ($.fn.DataTable.isDataTable("#report-table")) {
//             reportDataTableInstance.destroy();
//         }

//         // Initialize the new DataTable instance
//         reportDataTableInstance = $("#report-table").DataTable({
//             pageLength: 10,
//             data: initialData,
//             columns: [
//                 { data: null, title: "Sr No" },
//                 { data: "userName", title: "User Name" },
//                 { data: "dateOfDownload", title: "Date of Download" },
//                 { data: "timeOfDownload", title: "Time of Download" },
//                 { data: "role", title: "Role" },
//                 { data: "startDate", title: "Start Date" },
//                 { data: "endDate", title: "End Date" },
//                 { data: "status", title: "Download Status" },
//             ],
//             order: [
//                 [2, "desc"], // Sort by Date descending
//                 [3, "desc"], // Sort by Time descending
//             ],
//             // Auto-generate Serial Number
//             drawCallback: function (settings) {
//                 let api = this.api();
//                 let startIndex = api.context[0]._iDisplayStart;

//                 api.column(0, { page: "current" })
//                     .nodes()
//                     .each(function (cell, i) {
//                         cell.innerHTML = startIndex + i + 1;
//                     });
//             },
//         });
//     }

//     // --- Data Fetching ---

//     /**
//      * Fetches all existing report history records and updates the DataTable.
//      */
//     function loadReportHistory() {
//         fetch(ReportHistoryAllUrl, {
//             method: "GET",
//             headers: {
//                 Authorization: token ? `Bearer ${token}` : "",
//             },
//         })
//         .then((response) => {
//             if (!response.ok) {
//                 console.error("Failed to fetch report history. Status:", response.status);
//                 initializeReportDataTable([]);
//                 return null;
//             }
//             return response.json();
//         })
//         .then((data) => {
//             if (data) {
//                 initializeReportDataTable(data);
//             }
//         })
//         .catch((error) => {
//             console.error("Network error fetching report history:", error);
//             initializeReportDataTable([]);
//         });
//     }

//     // --- Main Logic: Run on Document Ready ---

//     $(document).ready(function () {
        
//         // 1. Authorization and User Details Extraction
//         token = sessionStorage.getItem("Authorization");
//         if (!token) {
//             alert("Unauthorized. Please login.");
//             window.location.href = "/Frontend/html/login.html";
//             return; // Stop execution if unauthorized
//         }

//         payload = parseJwt(token);
//         userRole = payload?.role?.trim() || "N/A";
//         userName = payload?.email || "N/A";

//         // 2. Initial Data Load
//         loadReportHistory();

//         // 3. Modal Handlers Setup
//         const reportModalElement = document.getElementById("reportModal");
//         if (reportModalElement) {
//              $("#showReportModal").click(function () {
//                  reportModalElement.style.display = "flex";
//              });
//              $("#closeReportModal").click(function () {
//                  reportModalElement.style.display = "none";
//              });
//              $("#reportModal").click(function (event) {
//                  if (event.target.id === "reportModal") {
//                      reportModalElement.style.display = "none";
//                  }
//              });
//         }


//         // 4. Custom Validator Setup
//         $.validator.addMethod(
//             "dateMaxToday",
//             function (value, element) {
//                 const today = new Date();
//                 today.setHours(0, 0, 0, 0);

//                 const inputDate = new Date(value);
//                 inputDate.setHours(0, 0, 0, 0);

//                 return this.optional(element) || inputDate.getTime() <= today.getTime();
//             },
//             "**Date cannot be in the future"
//         );

//         // 5. Date Picker Restrictions
//         const today = new Date().toISOString().split("T")[0];
//         $("#startDateInput").attr("max", today);
//         $("#endDateInput").attr("max", today);

//         // Event listener for Start Date to enforce date range
//         $("#startDateInput").on("change", function () {
//             const startDateValue = $(this).val();
//             const minEndDate = getNextDay(startDateValue);
            
//             $("#endDateInput").attr("min", minEndDate);

//             const endDateValue = $("#endDateInput").val();
//             if (
//                 endDateValue &&
//                 startDateValue &&
//                 new Date(endDateValue) <= new Date(startDateValue)
//             ) {
//                 $("#endDateInput").val(""); // Clear invalid End Date
//             }
//         });

//         // 6. Form Validation and Submission
//         $("#getReport").validate({
//             rules: {
//                 startDate: {
//                     required: true,
//                     dateMaxToday: true,
//                 },
//                 endDate: {
//                     required: true,
//                     dateMaxToday: true,
//                 },
//             },
//             messages: {
//                 startDate: {
//                     required: "**Start date is missing",
//                     dateMaxToday: "**Start date cannot be in the future",
//                 },
//                 endDate: {
//                     required: "**End date is missing",
//                     dateMaxToday: "**End date cannot be in the future",
//                 },
//             },

//             submitHandler: function (form) {
//                 const startDate = $("#startDateInput").val();
//                 const endDate = $("#endDateInput").val();
//                 const getTemplateUrl = `${ReportTemplateBaseUrl}?start=${startDate}&end=${endDate}`;

//                 fetch(getTemplateUrl, {
//                     method: "POST",
//                     headers: {
//                         Authorization: token ? `Bearer ${token}` : "",
//                     },
//                 })
//                 .then((response) => {
//                     if (!response.ok) {
//                         throw new Error(`Http Error! status: ${response.status}`);
//                     }
//                     return response.blob();
//                 })
//                 .then((blob) => {
//                     // Create temporary link for download
//                     const href = URL.createObjectURL(blob);
//                     const link = document.createElement("a");
//                     link.href = href;
//                     link.download = "Report Template.zip";
//                     link.style.display = "none";

//                     document.body.appendChild(link);
//                     link.click();
//                     document.body.removeChild(link);
//                     URL.revokeObjectURL(href); // Clean up temp URL

//                     // Reload history and clear form/modal
//                     loadReportHistory();
//                     reportModalElement.style.display = "none";
//                     $("#startDateInput").val("");
//                     $("#endDateInput").val("");
//                 })
//                 .catch((error) => {
//                     alert("Failed to download the file, Error: " + error.message);
//                 });
//             },
//         });
//     });
// }