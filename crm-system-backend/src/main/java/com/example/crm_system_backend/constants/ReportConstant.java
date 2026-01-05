package com.example.crm_system_backend.constants;

public class ReportConstant {

    public static final String noDataText = "Dear recipient, No leads have registered in this time period. Therefore, no report attachment was generated.";

    public static final String noSummaryReport = "Dear recipient, No leads have registered in this time period. Therefore, no summary report was generated.";

    public static final String getSummary = "Successfully generated Summary report.";

    public static final String getPerUser = "Successfully generated per-user report.";

    public static final String[] summaryReport_headers = {"Sr No", "Username", "Email", "Total No of Leads Added", "Total No of Leads Contacted", "Total No of Leads Converted", "Contacted %", "Success %"};

    public static final String[] perUserReport_headers = {"Sr No", "First Name", "Last Name", "Email", "GSTIN", "Interested Products", "Status", "Description"};

}
