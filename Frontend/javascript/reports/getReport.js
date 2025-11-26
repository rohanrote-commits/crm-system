$(function () {

  // --- URL ---

  //TODO: do not add url in code, add separately 
  const ReportHistoryAllUrl = "http://localhost:8080/crm/report/getDownloadedRecordHistory"; // fetch data from db


  // --- Parse JWT Token ---
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
  

  // Get token from sessionStorage and authorize
  const token = sessionStorage.getItem("Authorization");
  if (!token) {
    alert("Unauthorized. Please login.");
    window.location.href = "/Frontend/html/login.html";
    return;
  }

  const payload = parseJwt(token);
  const userRole = payload?.role?.trim() || "N/A";
  const userName = payload?.email || "N/A"; 

  let reportDataTableInstance = null; 

  // --- Data table set-up ---
  function initializeReportDataTable(initialData = []) {
    if ($.fn.DataTable.isDataTable("#report-table")) {
      // If instance exists, just destroy it completely
      reportDataTableInstance.destroy();
    } 
    
    // Initialize the DataTable and store the instance
    reportDataTableInstance = $("#report-table").DataTable({
      pageLength: 10,
      data: initialData, // Load initial data here
      columns: [
        { data: null, title: "Sr No" },
        { data: "userName", title: "User Name" },
        { data: "dateOfDownload", title: "Date of Download" },
        { data: "timeOfDownload", title: "Time of Download" },
        { data: "role", title: "Role" },
        { data: "startDate", title: "Start Date" },
        { data: "endDate", title: "End Date" },
        { data: "status", title: "Download Status" },
      ],
      order: [
        [2, "desc"],  // Sort by Date descending (newest entry first)
        [3, "desc"],  // Sort by Time ascending
      ], 

      // function to auto-generate Serial Number
      // drawCallBack- predefined (built-in) option in DataTables
      // .api() gives access to DataTables API methods
      // api.context[0] - This refers to the internal settings object for the first DataTable in the API instance.
      // iDisplayStart - auto-generated id indicating index in data table

      drawCallback: function (settings) {    
        let api = this.api();               
        let startIndex = api.context[0]._iDisplayStart;  
                                                        
        api
          .column(0, { page: "current" })
          .nodes()
          .each(function (cell, i) {
            cell.innerHTML = startIndex + i + 1; 
          });
      },
    });
  }



  /**
   * Fetches all existing report history records from the backend
   * and initializes/reloads the DataTable with the fetched data.
   */
  function loadReportHistory() {
    fetch(ReportHistoryAllUrl, {
      method: "GET",
      headers: {
        // Autorization : <type> ? <credentials> : '' // <type> = the authentication scheme (e.g., Basic, Bearer, Digest) // <credentials> = the password, token or key
        Authorization: token ? `Bearer ${token}` : "", //Ternary opertor
      }, 
    })
      .then((response) => {
        if (!response.ok) {

          console.error(
            "Failed to fetch report history. Status:",
            response.status
          );
          initializeReportDataTable([]);
          return null;
        }
        return response.json();
      })
      .then((data) => {
        if (data) {
          // Initialize table with the fetched data
          initializeReportDataTable(data);
        }
      })
      .catch((error) => {
        console.error("Network error fetching report history:", error); // Initialize table with empty data on network error
        initializeReportDataTable([]);
      });
  }

  loadReportHistory();

  const reportModalElement = document.getElementById("reportModal"); // Modal Handlers
  $("#showReportModal").click(function () {
    reportModalElement.style.display = "flex";
  });
  $("#closeReportModal").click(function () {
    reportModalElement.style.display = "none";
  });
  $("#reportModal").click(function (event) {
    if (event.target.id === "reportModal") {
      reportModalElement.style.display = "none";
    }
  });
  
  
  // --- Custom Validators ---
  $.validator.addMethod(
    "dateMaxToday",
    function (value, element) {
      // Get today's date at midnight
      const today = new Date();
      today.setHours(0, 0, 0, 0); // Get the input date at midnight

      const inputDate = new Date(value);
      inputDate.setHours(0, 0, 0, 0); // If the input date is before today or today, it's valid

      return this.optional(element) || inputDate.getTime() <= today.getTime();
    },
    "**Date cannot be in the future"
  ); 
  

  // --- Form validation and submission (omitted for brevity, assume it is correct) --- // Get today's date in YYYY-MM-DD
  const today = new Date().toISOString().split("T")[0]; // Set the max attribute for the Start Date input and end date input

  $("#startDateInput").attr("max", today);
  $("#endDateInput").attr("max", today); // To restrict end date before start date

  function getNextDay(dateString) {
    if (!dateString) return "";
    const date = new Date(dateString); // Add one day
    date.setDate(date.getDate() + 1); // Format as YYYY-MM-DD for the 'min' attribute
    return date.toISOString().split("T")[0];
  } 
  

  // Attach an event listener to the Start Date input
  $("#startDateInput").on("change", function () {
    const startDateValue = $(this).val(); // Calculate the next day's date (Start Date + 1)
    const minEndDate = getNextDay(startDateValue); // Set the 'min' attribute for the End Date picker // This visually restricts the calendar selection
    $("#endDateInput").attr("min", minEndDate); // Optional: If the currently selected End Date becomes invalid, clear it

    const endDateValue = $("#endDateInput").val();
    if (
      endDateValue &&
      startDateValue &&
      new Date(endDateValue) <= new Date(startDateValue)
    ) {
      $("#endDateInput").val("");
    }
  });


  $("#getReport").validate({
    rules: {
      startDate: {
        required: true,
        dateMaxToday: true,
      },
      endDate: {
        required: true,
        dateMaxToday: true,
      },
    },
    messages: {
      startDate: {
        required: "**Start date is missing",
        dateMaxToday: "**Start date cannot be in the future",
      },
      endDate: {
        required: "**End date is missing",
        dateMaxToday: "**End date cannot be in the future",
      },
    },

    submitHandler: function (form) {
      const startDate = $("#startDateInput").val();
      const endDate = $("#endDateInput").val();
      const requestHeadersData = {
        userName: userName, 
        role: userRole, 
      };
      const getTemplateUrl = `http://localhost:8080/crm/report/getTemplate?start=${startDate}&end=${endDate}`; // get downloaded Template
      

      fetch(getTemplateUrl, {
        method: "POST",
        headers: {
          Authorization: token ? `Bearer ${token}` : "",
        },
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error(`Http Error ! status: ${response.status}`);
          }
          return response.blob();
        })
        .then((blob) => {
          const href = URL.createObjectURL(blob);
          const link = document.createElement("a");
          link.href = href;
          link.download = "Report Template.zip";
          link.style.display = "none";

          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(href); // Clean up the temporary URL // Log a successful download

        //   logDownload(startDate, endDate, "Success"); // Clear the form fields after successful download/logging
          loadReportHistory();

          reportModalElement.style.display = "none";

          $("#startDateInput").val("");
          $("#endDateInput").val("");
        })
        .catch((error) => {
        //   logDownload(startDate, endDate, "FAIL"); // Log failure
          alert("Failed to download the file, Error: " + error.message);
        });
    },
  });
});
