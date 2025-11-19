package com.example.crm_system_backend.service.Report;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.LeadException;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static com.example.crm_system_backend.constants.ReportConstant.perUserReport_headers;
import static com.example.crm_system_backend.constants.ReportConstant.summaryReport_headers;
import static com.example.crm_system_backend.constants.Roles.ADMIN;
import static com.example.crm_system_backend.constants.Roles.MASTER_ADMIN;

@Slf4j
@Service
public class ReportService {

    @Autowired
    ReportExcelHelper helper;

    @Autowired
    ILeadRepository leadRepo;

    @Autowired
    IUserRepo userRepo;

    private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());

    /**
     * Creates the Excel Template and adds multiple sheets (summary report-1, per user reports-multiple) in it according to requirement
     * @param leads
     * @param start
     * @param end
     * @param outputStream
     * @throws IOException
     */
    public void ListToExcelStream(Set<Lead> leads, Date start, Date end, OutputStream outputStream) throws IOException {

        // Create new workbook and sheet
        try (Workbook workbook = new XSSFWorkbook()) {;

            CellStyle head_style = helper.headStyle(workbook);       // Create style for Head
            CellStyle header_style = helper.headerStyle(workbook);   // Create style for headers
            CellStyle data_style = helper.dataStyle(workbook);       // Create style for Data rows

            int columnCount = 0;

            // Summary Report
            Sheet summaryReport_sheet = workbook.createSheet("Summary Report");

            Set<User> users = new HashSet<>();
            for (Lead lead : leads) {
                users.add(lead.getUser());
            }

            Map<User, List<Lead>> map = new HashMap<>();
            List<Long> userIdList = new ArrayList<>();

            for (User user : users) {
                List<Lead> associatedLeads = new ArrayList<>();
                userIdList.add(user.getId());
                for (Lead lead : leads) {
                    if (lead.getUser().getId().equals(user.getId())) {
                        associatedLeads.add(lead);
                    }
                }
                map.put(user, associatedLeads);
            }

            if (!users.isEmpty()) {
                SummaryReport(head_style, header_style, data_style,
                        summaryReport_sheet, summaryReport_headers,
                        users, start, end);
            } else {
                throw new LeadException(ErrorCode.LEAD_NOT_FOUND);
            }

            for (int i = 0; i < summaryReport_headers.length; i++) {
                summaryReport_sheet.autoSizeColumn(i, true);
            }

            // Personalized Report
            for (User user : users) {

                String name = user.getFirstName() + " " + user.getLastName() + "_" + user.getEmail();
                Sheet perUserReport_sheet = workbook.createSheet(name);


                perUserReport(head_style, header_style, data_style,
                        perUserReport_sheet, perUserReport_headers, columnCount,
                        map.get(user));

                for (int i = 0; i < perUserReport_headers.length; i++) {
                    perUserReport_sheet.autoSizeColumn(i, true);
                }
            }

            // Write Workbook to response stream
            workbook.write(outputStream);
        }
    }


    public ResponseEntity<StreamingResponseBody> excelToZipConverter(Set<Lead> leadList, Date start, Date end) {

        LOGGER.log(Level.INFO, "Converting Excel Template into a ZIP file");

        if (leadList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        HttpHeaders headers = new HttpHeaders();

        // Sets the content type to ZIP
        headers.setContentType(MediaType.parseMediaType("application/zip"));

        // Sets the final downloaded filename
        headers.setContentDispositionFormData("attachment", "ReportTemplate.zip");

        String excelFileName = "Report Template.xlsx";

        StreamingResponseBody responseBody = outputStream -> {
            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {

                // 1. Create a new entry for the Excel file inside the ZIP
                ZipEntry zipEntry = new ZipEntry(excelFileName);
                zos.putNextEntry(zipEntry);

                // 2. Call the service to write Excel data directly to the ZipOutputStream
                ListToExcelStream(leadList, start, end, zos);
                LOGGER.log(Level.INFO, "Received Excel Template, proceeding to convert it into Zip file");

                // 3. Close the current ZIP entry
                zos.closeEntry();

                LOGGER.log(Level.INFO, "Successfully converted Excel Template into a ZIP file");

            } catch (IOException e) {
                log.error("Error streaming ZIP content.");
                LOGGER.log(Level.SEVERE, "Service :: Report :: ReportService :: excelToZipConverter() ", e);
                throw new ExcelException(ErrorCode.ERROR_IN_FILE_DOWNLOAD);
            }
        };
        return ResponseEntity.ok().headers(headers).body(responseBody);
    }



    /**
     * Accepts all required data from ListToExcelStream() method and creates summary report sheet
     * @param head_style
     * @param header_style
     * @param data_style
     * @param sheet
     * @param headers
     * @param users
     * @param start
     * @param end
     */
    public void SummaryReport(CellStyle head_style, CellStyle header_style, CellStyle data_style,
                              Sheet sheet, String[] headers,
                              Set<User> users, Date start, Date end) {

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
        Row headerRow_Top = sheet.createRow(2);
        Row headerRow_Bottom = sheet.createRow(3);

        int columnCount_top = 0;
        int columnCount_bottom = 0;

        for (String header : headers) {

            Cell headerCell_Top = headerRow_Top.createCell(columnCount_top++);
            headerCell_Top.setCellValue(header);
            headerCell_Top.setCellStyle(header_style);

            Cell headerCell_Bottom = headerRow_Bottom.createCell(columnCount_bottom++);
            headerCell_Bottom.setCellValue("");
            headerCell_Bottom.setCellStyle(header_style);
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
            int cellNum = 0;
            String name = user.getFirstName() + " " + user.getEmail();

            // Column 0:
            row.createCell(cellNum++).setCellValue(++index);
            row.getCell(0).setCellStyle(data_style);
            // Column 1:
            row.createCell(cellNum++).setCellValue(name);
            row.getCell(cellNum - 1).setCellStyle(data_style);
            // Column 2:
            row.createCell(cellNum++).setCellValue(user.getEmail());
            row.getCell(cellNum - 1).setCellStyle(data_style);
            // Column 3:
            int count = leadRepo.getLeadCountByUserId(userId);
            row.createCell(cellNum++).setCellValue(count);  //These are ADDED
            row.getCell(cellNum - 1).setCellStyle(data_style);
            // Column 4:
            int contacted = 0;
            int converted = 0;
            List<String> statusList = leadRepo.getLeadStatusByUserId(userId);
            for (String status : statusList) {
                if (status.equalsIgnoreCase("CONTACTED")) {
                    contacted++;
                } else if (status.equalsIgnoreCase("CONVERTED")) {
                    contacted++;
                    converted++;
                }
            }
            row.createCell(cellNum++).setCellValue(contacted);
            row.getCell(cellNum - 1).setCellStyle(data_style);
            // Column 5:
            row.createCell(cellNum++).setCellValue(converted);
            row.getCell(cellNum - 1).setCellStyle(data_style);
            // Column 6:
            float process_percent = (float) contacted / count * 100;
            String process = String.format("%.2f", process_percent) + " %";
            row.createCell(cellNum++).setCellValue(process);
            row.getCell(cellNum - 1).setCellStyle(data_style);
            // Column 7:
            float convert_percent = (float) converted / count * 100;
            String convert = String.format("%.2f", convert_percent) + " %";
            row.createCell(cellNum++).setCellValue(convert);
            row.getCell(cellNum - 1).setCellStyle(data_style);

        }
    }


    /**
     * Accepts all required data from ListToExcelStream() method and creates per user report sheets
     * @param head_style
     * @param header_style
     * @param data_style
     * @param sheet
     * @param headers
     * @param columnCount
     * @param leads
     */
    public void perUserReport(CellStyle head_style, CellStyle header_style, CellStyle data_style,
                              Sheet sheet, String[] headers, int columnCount,
                              List<Lead> leads) {

        // Row 1
        Row headRow = sheet.createRow(0);
        Cell headCell = headRow.createCell(0);
        headCell.setCellStyle(head_style);

        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, headers.length - 1));

        // Row 2
        Row headerRow = sheet.createRow(2);
        for (String header : headers) {
            Cell headerCell = headerRow.createCell(columnCount++);
            headerCell.setCellValue(header);
            headerCell.setCellStyle(header_style);
        }

        // Populate Data Rows
        int index = 0;
        int rowNum = 3;

        for (Lead lead : leads) {

            Set<String> products = lead.getInterestedModules();

            for (String product : products) {
                Row row = sheet.createRow(rowNum++);
                int cellNum = 0;
                row.createCell(cellNum++).setCellValue(++index);
                row.getCell(0).setCellStyle(data_style);
                row.createCell(cellNum++).setCellValue(lead.getFirstName());
                row.getCell(cellNum - 1).setCellStyle(data_style);
                row.createCell(cellNum++).setCellValue(lead.getLastName());
                row.getCell(cellNum - 1).setCellStyle(data_style);
                row.createCell(cellNum++).setCellValue(lead.getEmail());
                row.getCell(cellNum - 1).setCellStyle(data_style);
                row.createCell(cellNum++).setCellValue(lead.getGstin());
                row.getCell(cellNum - 1).setCellStyle(data_style);
                row.createCell(cellNum++).setCellValue(product);
                row.getCell(cellNum - 1).setCellStyle(data_style);
                row.createCell(cellNum++).setCellValue(lead.getLeadStatus().toString());
                row.getCell(cellNum - 1).setCellStyle(data_style);
                row.createCell(cellNum++).setCellValue(lead.getDescription());
                row.getCell(cellNum - 1).setCellStyle(data_style);
            }
        }
    }


    /**
     * Required to get the set of leads according to the role of the user (Master Admin, Admin and Basic)
     * @param start
     * @param end
     * @return Set of leads to be written in Report Template in all sheets
     */
//    @Transactional
    public Set<Lead> getLeads(Date start, Date end) {

        LOGGER.log(Level.INFO, "Getting leads list ");

        List<User> userList = new ArrayList<>();
        List<Lead> leadList = helper.getLeadList(start, end);
        Set<Lead> finalLeads = new HashSet<>(leadList);

        for (Lead lead : finalLeads) {
            if (lead.getUser().getRole().equals(MASTER_ADMIN) || lead.getUser().getRole().equals(ADMIN)) {
                Long id = lead.getUser().getId();
                for (User user : userRepo.findAll()) {
                    if (user.getRegisteredBy() == id) {
                        userList.add(user);
                    }
                }

                for (Lead lead1 : leadRepo.findAll()) {
                    for (User user : userList) {
                        if (lead1.getUser().getId().equals(user.getId())) {
                            finalLeads.add(lead1);
                        }
                    }
                }
            }
        }

        LOGGER.log(Level.INFO, "Received leads list ");
        return finalLeads;
    }

    public List<downloadReport> getFilteredDownloadHistory(String currentUserRole, Long currentUserId, String currentUserEmail) {
        return List.of();
    }
}


