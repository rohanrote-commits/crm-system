package com.example.crm_system_backend.constants;

public class ReportConstant {

    public static final String noDataText = "Dear recipient, No leads have registered in this time period. Therefore, no report attachment was generated.";

    public static final String[] summaryReport_headers = {"Sr No", "Username", "Email", "Total No of Leads Added", "Total No of Leads Processed", "Total No of Leads Converted", "Processed %", "Success %"};

    public static final String[] perUserReport_headers = new String[]{"Sr No", "First Name", "Last Name", "Email", "GSTIN", "Interested Module", "Status", "Description"};


}
