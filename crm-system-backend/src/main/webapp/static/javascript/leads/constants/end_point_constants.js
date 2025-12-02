

const API_BASE_URL = "http://localhost:8080/crm/leads";
const REPORT_BASE_URL = "http://localhost:8080/crm/report";

const LEAD_API = {


    GET_ALL: `${API_BASE_URL}`,
    GET_BY_USER: `${API_BASE_URL}`,
    GET_BY_EMAIL: (email) => `${API_BASE_URL}?email=${email}`,   // GET

    CREATE: `${API_BASE_URL}`,    // POST

    UPDATE: (id) => `${API_BASE_URL}/${id}`,    // PUT

    DELETE: (id) => `${API_BASE_URL}/${id}`,    // DELETE

    BULK_IMPORT:  `${API_BASE_URL}/bulk`, // POST (multipart)
    UPDATE_STATUS: (id) => `${API_BASE_URL}/${id}/status`  ,// PUT

    LEAD_UPLOAD_HISTORY: (email)=> `http://localhost:8080/crm/history/lead/${email}`,

    ERROR_FILE_BY_HISTORY_ID: (uploadHistoryId)=> `http://localhost:8080/crm/history/lead/error/${uploadHistoryId} `,

    ERROR_LEADS_BY_UPLOAD_HISTORY_ID: (uploadHistoryId)=>  `http://localhost:8080/crm/error/records/${uploadHistoryId}` ,

    UPDATE_ERROR_LEADS: (rowNumber,uploadHistoryId)=> `http://localhost:8080/crm/error/lead/${rowNumber}/${uploadHistoryId}`,
};

const LEAD_ERROR_API = {
    DELETE_ERROR_LEADS: (selectedRowNumber,uploadHistoryId)=> `http://localhost:8080/crm/error/${selectedRowNumber}/${uploadHistoryId}`
};

const REPORT_API = {

    GET_TEMPLATE: (startDate, endDate) => `${REPORT_BASE_URL}/getTemplate?start=${startDate}&end=${endDate}`,
    GET_REPORT_HISTORY: `${REPORT_BASE_URL}/getDownloadedRecordHistory`,

};


