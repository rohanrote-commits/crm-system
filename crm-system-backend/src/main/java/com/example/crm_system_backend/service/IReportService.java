package com.example.crm_system_backend.service;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface IReportService {

    void ListToExcelStream(Set<Lead> leads, Date start, Date end, OutputStream outputStream) throws IOException;
    ResponseEntity<StreamingResponseBody> excelToZipConverter(Set<Lead> leadList, Date start, Date end);

    void SummaryReport(CellStyle head_style, CellStyle header_style, CellStyle data_style,
                       Sheet sheet, String[] headers,
                       Set<User> users, Date start, Date end);

    void perUserReport(CellStyle head_style, CellStyle header_style, CellStyle data_style,
                       Sheet sheet, String[] headers, int columnCount,
                       List<Lead> leads, Date start, Date end);

}
