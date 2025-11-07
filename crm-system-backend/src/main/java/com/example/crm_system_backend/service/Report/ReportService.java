package com.example.crm_system_backend.service.Report;

import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    IUserRepo userRepo;

    @Autowired
    ILeadRepository leadRepo;

    public Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public List<User> getList(Date start, Date end) {
        List<User> userList = new ArrayList<>();
        for(User user : userRepo.findAll()) {
            Date date = convertLocalDateTimeToDate(user.getRegisteredOn());
            if(date.after(start) && date.before(end)) {
                userList.add(user);
            }
        }
        return userList;
    }

    public byte[] listToExcel(List<User> users, Date start, Date end) throws IOException {

        // 1. Create new workbook and sheet
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reports");

            //Create style for Head
            Font headFont = workbook.createFont();
            headFont.setBold(true);
            headFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(headFont);
            headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headStyle.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
            headStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headStyle.setAlignment(HorizontalAlignment.LEFT);

            // Create style for headers
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
//            headerStyle.setBorderLeft(BorderStyle.THIN);
//            headerStyle.setBorderRight(BorderStyle.THIN);
//            headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
//            headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());

            // Create style for Data rows
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setWrapText(true);

            // 2. Define Headers

            int columnCount = 0;
            String[] headers = {"Sr No", "Name", "Email", "Total No of Leads Added", "Total No of Leads Processed", "Total No of Leads Converted", "Processed %", "Success %", "Comment"};

            // Row-1
            String head = " From: " + start + " To: " + end;
            Row headRow = sheet.createRow(0);
            Cell headCell = headRow.createCell(0);
            headCell.setCellValue(head);
            headCell.setCellStyle(headStyle);

            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, headers.length-1));

            // Row-2
            Row headerRow = sheet.createRow(2);
            for(String header : headers) {
                Cell headerCell = headerRow.createCell(columnCount++);
                headerCell.setCellValue(header);
                headerCell.setCellStyle(headerStyle);
            }

            for(int i = 0;i < headers.length;i++) {
                sheet.addMergedRegion(new CellRangeAddress(2, 3, i, i));
            }


            // 3. Populate Data Rows
            int index = 0;
            int rowNum = 4;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.setRowStyle(dataStyle);
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
                    if (status.equalsIgnoreCase("PROCESSED")) {
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
                int neitherProcessedNorConverted = 0;
                List<String> status = leadRepo.getLeadStatusByUserId(userId);
                for(String state : status) {
                    if (state.equalsIgnoreCase("IN_PROCESS") || state.equalsIgnoreCase("NOT_PROCESSED") || state.equalsIgnoreCase("NOT_CONVERTED")) {
                        neitherProcessedNorConverted++;
                    }
                }
                String data = "Leads neither \n Processed nor \n Converted: \n" + neitherProcessedNorConverted;
                row.createCell(cellNum++).setCellValue(data);

            }


            //4. For displaying complete cell content
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i, true);
            }

            // 5. Write Workbook to response stream
            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}

