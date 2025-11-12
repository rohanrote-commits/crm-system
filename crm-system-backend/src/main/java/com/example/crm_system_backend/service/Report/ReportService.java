package com.example.crm_system_backend.service.Report;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.repository.ILeadRepository;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ReportService {

    @Autowired
    ReportExcelHelper helper;

    @Autowired
    ILeadRepository leadRepo;

    public byte[] ListToExcel(List<User> users, Date start, Date end) throws IOException {

        // Create new workbook and sheet
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            CellStyle head_style = helper.headStyle(workbook);       // Create style for Head
            CellStyle header_style = helper.headerStyle(workbook);   // Create style for headers
            CellStyle data_style = helper.dataStyle(workbook);       // Create style for Data rows

            int columnCount = 0;

            // Summary Report
            Sheet summaryReport_sheet = workbook.createSheet("Summary Report");

            String[] summaryReport_headers = {"Sr No", "Name", "Email", "Total No of Leads Added", "Total No of Leads Processed", "Total No of Leads Converted", "Processed %", "Success %", "Comment"};

            Map<Long, List<Lead>> map = SummaryReport(head_style, header_style, data_style,
                    summaryReport_sheet, summaryReport_headers, columnCount,
                    users, start, end);

            for (int i = 0; i < summaryReport_headers.length; i++) {
                summaryReport_sheet.autoSizeColumn(i, true);
            }

            // Personalized Report
            for(User user : users) {

                String name = user.getFirstName() + " " + user.getLastName();
                Sheet perUserReport_sheet = workbook.createSheet(name);

                String[] perUserReport_headers = new String[]{"Lead ID", "First Name", "Last Name", "Email", "GSTIN", "Products", "Status"};

                perUserReport(head_style, header_style, data_style,
                                perUserReport_sheet, perUserReport_headers, columnCount,
                                map.get(user.getId()));

                for (int i = 0; i < perUserReport_headers.length; i++) {
                    perUserReport_sheet.autoSizeColumn(i, true);
                }
            }

            // Write Workbook to response stream
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    public Map<Long, List<Lead>> SummaryReport(CellStyle head_style, CellStyle header_style, CellStyle data_style,
                              Sheet sheet, String[] headers, int columnCount,
                              List<User> users, Date start, Date end) throws IOException {

        Map<Long, List<Lead>> map = new HashMap<>();

        // Row-1
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String head = " From: " + dateFormat.format(start) + ", To: " + dateFormat.format(end);

        Row headRow = sheet.createRow(0);
        Cell headCell = headRow.createCell(0);
        headCell.setCellValue(head);
        headCell.setCellStyle(head_style);

        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, headers.length - 1));

        // Row-2
        Row headerRow = sheet.createRow(2);
        for (String header : headers) {
            Cell headerCell = headerRow.createCell(columnCount++);
            headerCell.setCellValue(header);
            headerCell.setCellStyle(header_style);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.addMergedRegion(new CellRangeAddress(2, 3, i, i));
        }


        // Populate Data Rows
        int index = 0;
        int rowNum = 4;
        List<Lead> leads = null;
        for (User user : users) {

            long userId = user.getId();
            leads = leadRepo.getLeads(userId);
            map.put(userId, leads);

            Row row = sheet.createRow(rowNum++);
            row.setRowStyle(data_style);
            int cellNum = 0;
            String name = user.getFirstName() + " " + user.getLastName();

            // Column 0:
            row.createCell(cellNum++).setCellValue(++index);
            // Column 1:
            row.createCell(cellNum++).setCellValue(name);
            // Column 2:
            row.createCell(cellNum++).setCellValue(user.getEmail());
            // Column 3:
            int count = leadRepo.getLeadCountByUserId(userId);
            row.createCell(cellNum++).setCellValue(count);
            // Column 4:
            int processed = 0;
            int converted = 0;
            List<String> statusList = leadRepo.getLeadStatusByUserId(userId);
            for (String status : statusList) {
                if (status.equalsIgnoreCase("ADDED")) {
                    processed++;
                } else if (status.equalsIgnoreCase("CONVERTED")) {
                    converted++;
                }
            }
            row.createCell(cellNum++).setCellValue(processed);
            // Column 5:
            row.createCell(cellNum++).setCellValue(converted);
            // Column 6:
            float process_percent = (float) processed / count * 100;
            String process = String.format("%.2f", process_percent) + " %";
            row.createCell(cellNum++).setCellValue(process);
            // Column 7:
            float convert_percent = (float) converted / count * 100;
            String convert = String.format("%.2f", convert_percent) + " %";
            row.createCell(cellNum++).setCellValue(convert);
            //Column 8:
            int neitherContactedNorConverted = 0;
            List<String> status = leadRepo.getLeadStatusByUserId(userId);
            for (String state : status) {
                if (state.equalsIgnoreCase("CONTACTED") || state.equalsIgnoreCase("NOT_CONVERTED")) {
                    neitherContactedNorConverted++;
                }
            }

            String data = "Leads neither \n Processed nor \n Converted: \n" + neitherContactedNorConverted;
            row.createCell(cellNum++).setCellValue(data);
        }
        return map;
    }


    public void perUserReport(CellStyle head_style, CellStyle header_style, CellStyle data_style,
                              Sheet sheet, String[] headers, int columnCount,
                              List<Lead> leads) throws IOException {

        // Row 1
        Row headRow = sheet.createRow(0);
        Cell headCell = headRow.createCell(0);
        headCell.setCellStyle(head_style);

        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, headers.length-1));

        // Row 2
        Row headerRow = sheet.createRow(2);
        for(String header : headers) {
            Cell headerCell = headerRow.createCell(columnCount++);
            headerCell.setCellValue(header);
            headerCell.setCellStyle(header_style);
        }

        // Populate Data Rows
        int rowNum = 3;

        for (Lead lead : leads) {

            Set<String> products = lead.getInterestedModules();

            for (String product : products) {
                Row row = sheet.createRow(rowNum++);
                row.setRowStyle(data_style);
                int cellNum = 0;
                row.createCell(cellNum++).setCellValue(lead.getId());
                row.createCell(cellNum++).setCellValue(lead.getFirstName());
                row.createCell(cellNum++).setCellValue(lead.getLastName());
                row.createCell(cellNum++).setCellValue(lead.getEmail());
                row.createCell(cellNum++).setCellValue(lead.getGstin());
                row.createCell(cellNum++).setCellValue(product);
                row.createCell(cellNum++).setCellValue(lead.getLeadStatus().toString());
            }
        }
    }
}

