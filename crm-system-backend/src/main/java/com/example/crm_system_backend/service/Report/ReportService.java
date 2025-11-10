package com.example.crm_system_backend.service.Report;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.ReportType;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    IUserRepo userRepo;

    @Autowired
    ILeadRepository leadRepo;

    public Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public List<User> getUserList(Date start, Date end) {
        List<User> userList = new ArrayList<>();
        for(User user : userRepo.findAll()) {
            Date date = convertLocalDateTimeToDate(user.getRegisteredOn());
            if(date.after(start) && date.before(end)) {
                userList.add(user);
            }
        }
        return userList;
    }

    public List<Lead> getLeadList(String email) {

        // Get leads of specific user logic
        Optional<User> user = userRepo.getUserByEmail(email);
        if(user.isEmpty()) {
            System.err.println("User with email " + email + " not found");
            return null;
        }
        Optional<List<Lead>> listOfLeads = leadRepo.getLeadsByUser(user);
        if(listOfLeads.isEmpty()) {
            System.err.println("User with email " + email + " has not registered any leads");
            return null;
        }

        return listOfLeads.get();
    }

    // Styles
    public CellStyle headStyle(Workbook workbook) {
        Font headFont = workbook.createFont();
        headFont.setBold(true);
        headFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headStyle = workbook.createCellStyle();
        headStyle.setFont(headFont);
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
        headStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headStyle.setAlignment(HorizontalAlignment.LEFT);

        return headStyle;
    }

    public CellStyle headerStyle(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);

        return headerStyle;
    }

    public CellStyle dataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);

        return dataStyle;
    }

    public byte[] ListToExcel(List<User> users, Date start, Date end,
                              List<Lead> leads) throws IOException {

        // Create new workbook and sheet
        try (Workbook workbookU = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbookU.createSheet("Reports");

            //Create style for Head
            CellStyle head_style = headStyle(workbookU);

            // Create style for headers
            CellStyle header_style = headerStyle(workbookU);

            // Create style for Data rows
            CellStyle data_style = dataStyle(workbookU);

            // Define Headers
            int columnCount = 0;
            String[] headers = new String[0];

            // Logic

            ReportType reportType = null;

            if(users != null && !users.isEmpty()) {
                reportType = ReportType.SUMMARY;
            } else if(leads != null && !leads.isEmpty()) {
                reportType = ReportType.PER_USER;
            }

            if(reportType == ReportType.SUMMARY) {
                headers = new String[]{"Sr No", "Name", "Email", "Total No of Leads Added", "Total No of Leads Processed", "Total No of Leads Converted", "Processed %", "Success %", "Comment"};

                SummaryReport(head_style, header_style, data_style,
                        sheet, headers, columnCount,
                        users, start, end);
            } else if(reportType == ReportType.PER_USER) {
                headers = new String[]{"Lead ID", "First Name", "Last Name", "Email", "GSTIN", "Products", "Status"};

                perUserReport(head_style, header_style, data_style,
                        sheet, headers, columnCount,
                        leads);
            }

            // For displaying complete cell content
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i, true);
            }

            // Write Workbook to response stream
            workbookU.write(baos);
            return baos.toByteArray();
        }
    }

    public void SummaryReport(CellStyle head_style, CellStyle header_style, CellStyle data_style,
                              Sheet sheet, String[] headers, int columnCount,
                              List<User> users, Date start, Date end) throws IOException {
        // Row-1

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String head = " From: " + dateFormat.format(start) + ", To: " + dateFormat.format(end);

        Row headRow = sheet.createRow(0);
        Cell headCell = headRow.createCell(0);
        headCell.setCellValue(head);
        headCell.setCellStyle(head_style);

        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, headers.length-1));

        // Row-2
        Row headerRow = sheet.createRow(2);
        for(String header : headers) {
            Cell headerCell = headerRow.createCell(columnCount++);
            headerCell.setCellValue(header);
            headerCell.setCellStyle(header_style);
        }

        for(int i = 0;i < headers.length;i++) {
            sheet.addMergedRegion(new CellRangeAddress(2, 3, i, i));
        }


        // 3. Populate Data Rows
        int index = 0;
        int rowNum = 4;
        for (User user : users) {
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
            Long userId = user.getId();
            int count = leadRepo.getLeadCountByUserId(userId);
            row.createCell(cellNum++).setCellValue(count);
            // Column 4:
            int processed = 0;
            int converted = 0;
            List<String> statusList = leadRepo.getLeadStatusByUserId(userId);
            for(String status : statusList) {
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
            float process_percent = (float)processed/count*100;
            String process = String.format("%.2f", process_percent) + " %";
            row.createCell(cellNum++).setCellValue(process);
            // Column 7:
            float convert_percent = (float)converted/count*100;
            String convert = String.format("%.2f", convert_percent) + " %";
            row.createCell(cellNum++).setCellValue(convert);
            //Column 8:
            int neitherContactedNorConverted = 0;
            List<String> status = leadRepo.getLeadStatusByUserId(userId);
            for(String state : status) {
                if (state.equalsIgnoreCase("CONTACTED") || state.equalsIgnoreCase("NOT_CONVERTED")) {
                    neitherContactedNorConverted++;
                }
            }

            String data = "Leads neither \n Processed nor \n Converted: \n" + neitherContactedNorConverted;
            row.createCell(cellNum++).setCellValue(data);
        }
    }

    public static void createDropdownCell(Sheet sheet, int rowIndex, int cellIndex, Set<String> modules) {

        String[] moduleArray = modules.toArray(new String[0]);
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();

        if (String.join(",", moduleArray).length() > 255) {
            System.err.println("Values too long for direct list constraint!");
            return;
        }

        DataValidationConstraint constraint = dvHelper.createExplicitListConstraint(moduleArray);
        CellRangeAddressList addressList = new CellRangeAddressList(rowIndex, rowIndex, cellIndex, cellIndex);
        DataValidation validation = dvHelper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        sheet.addValidationData(validation);
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
        int index = 0;
        int rowNum = 4;
        for (Lead lead : leads) {
            Row row = sheet.createRow(rowNum++);
            row.setRowStyle(data_style);
            int cellNum = 0;

            // Column 0:
            row.createCell(cellNum++).setCellValue(lead.getId());
            // Column 1:
            row.createCell(cellNum++).setCellValue(lead.getFirstName());
            // Column 2:
            row.createCell(cellNum++).setCellValue(lead.getLastName());
            // Column 3:
            row.createCell(cellNum++).setCellValue(lead.getEmail());
            // Column 4:
            row.createCell(cellNum++).setCellValue(lead.getGstin());
            // Column 5:
            Set<String> allDropdownOptions = new HashSet<>();
            allDropdownOptions.add("GSTR");
            allDropdownOptions.add("ITC Reconciliation");
            allDropdownOptions.add("E Invoice");
            allDropdownOptions.add("EWay Bill");
            allDropdownOptions.add("LMS");
            allDropdownOptions.add("Third Eye");
            allDropdownOptions.add("Safe Sign");

            String leadSelectedModule = "Select Module";

            Set<String> interestedModules = lead.getInterestedModules();

            if(interestedModules != null && !interestedModules.isEmpty()) {
                leadSelectedModule = interestedModules.iterator().next();
            }

            int dropdownCellIndex = cellNum;

            createDropdownCell(sheet, rowNum - 1, dropdownCellIndex, allDropdownOptions);

            row.createCell(dropdownCellIndex).setCellValue(leadSelectedModule);

            cellNum++;

            // Column 6:
            row.createCell(cellNum++).setCellValue(lead.getLeadStatus().toString());
        }
    }

}

