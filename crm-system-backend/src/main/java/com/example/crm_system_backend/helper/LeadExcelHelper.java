package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.beans.LeadList;
import com.example.crm_system_backend.constants.RegxConstant;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.dto.LeadDto;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.service.serviceImpl.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;




@Component
@AllArgsConstructor
public class LeadExcelHelper {


    private static final Logger log = LoggerFactory.getLogger(LeadExcelHelper.class);
    private final ProductService productService;
    private final ModelMapper modelMapper;

    @Async("bulkUploadExecutor")
    public LeadList processExcelData(MultipartFile file, UploadHistory uploadHistory)  {
        log.info("Enter: LeadExcelHelper.processExcelData");
        Map<String, Lead> leadMap = new HashMap<>(); // merge duplicate leads
        List<Lead> validLeads = new ArrayList<>();
        List<Row> errorRows = new ArrayList<>();
        LeadList leadList = new LeadList();
        List<InvalidLeadError> jsonErrorList = new ArrayList<>();


        if(!this.validateExcelHeader(file)){
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            uploadHistory.setValidRecords(0);
            uploadHistory.setInvalidRecords(0);
            log.error("Exit: LeadExcelHelper.processExcelData: Invalid Excel Header");
            throw new ExcelException(ErrorCode.WRONG_HEADERS);
        }

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(1);
            // Error style (red background)
            CellStyle errorStyle = workbook.createCellStyle();
            errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || row.getRowNum()==1) continue;// skip header
                if(isRowEmpty(row)){
                    continue;
                }
                Lead lead = extractLead(row);
                Map<String, String> errorMap = validateRowWithErrors(row, lead, errorStyle);
                if (!errorMap.isEmpty()) {
                    // Add to error rows list (for Excel file)
                    errorRows.add(row);
                    // Build JSON error entry
                    InvalidLeadError err = new InvalidLeadError();
                    err.setRowNumber(row.getRowNum());
                    err.setLead(lead);
                    err.setErrors(errorMap);
                    jsonErrorList.add(err);
                } else {
                    mergeLead(leadMap, lead);
                }
            }
            validLeads.addAll(leadMap.values());
            //if the error row list has entries then generate the error file
            if (!errorRows.isEmpty()) {
                if(!validLeads.isEmpty()){
                    uploadHistory.setUploadStatus(UploadStatus.PARTIALLY_SUCCESS);
                }
                uploadHistory.setInvalidRecords(errorRows.size());
                writeErrorFile(errorRows,uploadHistory);
                List<Lead> errorList = errorRows.stream().map(this::extractLead
                        ).toList();
                leadList.setInvalidLeadList(errorList);
               // errorRecordHandler.saveErrorRecord(errorList,uploadHistory);
            }

        } catch (IOException e) {
            log.error("Exit : LeadExcelHelper.processExcelData -->{}",e);
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            log.error(e.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
        uploadHistory.setTotalRecords((validLeads.size()+ errorRows.size()));
        uploadHistory.setInvalidRecords(errorRows.size());
        uploadHistory.setValidRecords(validLeads.size());

        ObjectMapper mapper = new ObjectMapper();
        String jsonData = null;
        try {
            jsonData = mapper.writeValueAsString(jsonErrorList);
        } catch (JsonProcessingException e) {
            log.error("Exception: LeadExcelHelper.processExcelData {}",e);
            throw new RuntimeException(e);
        }
        uploadHistory.setErrorRecord(jsonData);
        leadList.setValidLeadList(validLeads);
        log.info("Exit: LeadExcelHelper.processExcelData");
        return leadList;
    }

    // Helper to read any cell as string safely
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate().toString();
            }
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return cell.getStringCellValue().trim();
    }


    private static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void markError(Cell cell, String message, CellStyle errorStyle) {
        if (cell == null) return;
        cell.setCellStyle(errorStyle);
        Sheet sheet = cell.getSheet();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setRow1(cell.getRowIndex());
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(message));
        cell.setCellComment(comment);
    }

    private Lead extractLead(Row row) {
        Lead lead = new Lead();
        lead.setFirstName(getCellValue(row.getCell(1)));
        lead.setLastName(getCellValue(row.getCell(2)));
        lead.setMobileNumber(getCellValue(row.getCell(3)));
        lead.setEmail(getCellValue(row.getCell(4)));
        lead.setGstin(getCellValue(row.getCell(5)).toUpperCase());
        lead.getInterestedProducts().add(productService.getProductByName(getCellValue(row.getCell(6))));
        lead.setBusinessAddress(getCellValue(row.getCell(7)));
        lead.setDescription(getCellValue(row.getCell(8)));
        return lead;
    }

    private boolean validateExcelHeader(MultipartFile file) {
        File templateFile = new File("crm-system-backend/src/main/resources/templates/Lead Template.xlsx");

        try (
                Workbook uploadedWorkbook = new XSSFWorkbook(file.getInputStream());
                Workbook templateWorkbook = new XSSFWorkbook(templateFile.getAbsolutePath())
        ) {
            Sheet uploadedSheet = uploadedWorkbook.getSheetAt(1);
            Sheet templateSheet = templateWorkbook.getSheetAt(1);

            // Read header row (assumed to be first row)
            Row uploadedHeader = uploadedSheet.getRow(1);
            Row templateHeader = templateSheet.getRow(1);

            if (uploadedHeader == null || templateHeader == null) {
                return false;
            }

            int uploadedCells = uploadedHeader.getLastCellNum();
            int templateCells = templateHeader.getLastCellNum();

            if (uploadedCells != templateCells) {
                return false; // Different number of columns
            }

            for (int i = 0; i < templateCells; i++) {
                Cell uploadedCell = uploadedHeader.getCell(i);
                Cell templateCell = templateHeader.getCell(i);

                String uploadedHeaderValue = getCellValue(uploadedCell);
                String templateHeaderValue = getCellValue(templateCell);

                if (!uploadedHeaderValue.equals(templateHeaderValue)) {
                    log.error("Header mismatch at column {}: expected '{}', found '{}'",
                            i, templateHeaderValue, uploadedHeaderValue);
                    return false;
                }
            }

            return true; // All headers match

        } catch (IOException e) {
            log.error("Excel header validation failed: {}", e.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
    }

    private Map<String, String> validateRowWithErrors(Row row, Lead lead, CellStyle errorStyle) {
        Map<String, String> errorMap = new HashMap<>();

        // First Name
        if (isEmpty(lead.getFirstName()) || !lead.getFirstName().matches(RegxConstant.NAME_REGEX)) {
            String msg = "Invalid First Name";
            markError(row.getCell(1), msg, errorStyle);
            errorMap.put("firstName", msg);
        }

        // Last Name
        if (isEmpty(lead.getLastName()) || !lead.getLastName().matches(RegxConstant.NAME_REGEX)) {
            String msg = "Invalid Last Name";
            markError(row.getCell(2), msg, errorStyle);
            errorMap.put("lastName", msg);
        }

        // Mobile
        if (isEmpty(lead.getMobileNumber()) || !lead.getMobileNumber().matches(RegxConstant.MOBILE_REGEX)) {
            String msg = "Invalid Mobile Number";
            markError(row.getCell(3), msg, errorStyle);
            errorMap.put("mobileNumber", msg);
        }

        // Email
        if (isEmpty(lead.getEmail()) || !lead.getEmail().matches(RegxConstant.EMAIL_REGEX)) {
            String msg = "Invalid Email";
            markError(row.getCell(4), msg, errorStyle);
            errorMap.put("email", msg);
        }

        // GSTIN
        if (isEmpty(lead.getGstin()) || !lead.getGstin().matches(RegxConstant.GSTIN_REGEX)) {
            String msg = "Invalid GSTIN";
            markError(row.getCell(5), msg, errorStyle);
            errorMap.put("gstin", msg);
        }

        // Modules
        if (lead.getInterestedProducts() == null || lead.getInterestedProducts().isEmpty()) {
            String msg = "No Modules Selected";
            markError(row.getCell(6), msg, errorStyle);
            errorMap.put("interestedProducts", msg);
        }

        // Address
        if (!isEmpty(lead.getBusinessAddress()) &&
                !lead.getBusinessAddress().matches(RegxConstant.ADDRESS_REGEX)) {
            String msg = "Invalid Address";
            markError(row.getCell(7), msg, errorStyle);
            errorMap.put("businessAddress", msg);
        }

        // Description
        if (!isEmpty(lead.getDescription()) &&
                !lead.getDescription().matches(RegxConstant.DESCRIPTION_REGEX)) {
            String msg = "Invalid Description";
            markError(row.getCell(8), msg, errorStyle);
            errorMap.put("description", msg);
        }

        return errorMap;
    }


    private void mergeLead(Map<String, Lead> leadMap, Lead lead) {
        String emailKey = lead.getEmail().trim().toLowerCase();

        if (leadMap.containsKey(emailKey)) {
            Lead existingLead = leadMap.get(emailKey);

            // Merge interested modules (avoid duplicates)
            existingLead.getInterestedProducts().addAll(lead.getInterestedProducts());

            // Optional: If other fields are blank in the first record, fill them from new one
            if (isEmpty(existingLead.getFirstName()) && !isEmpty(lead.getFirstName()))
                existingLead.setFirstName(lead.getFirstName());

            if (isEmpty(existingLead.getLastName()) && !isEmpty(lead.getLastName()))
                existingLead.setLastName(lead.getLastName());

            if (isEmpty(existingLead.getMobileNumber()) && !isEmpty(lead.getMobileNumber()))
                existingLead.setMobileNumber(lead.getMobileNumber());

            if (isEmpty(existingLead.getBusinessAddress()) && !isEmpty(lead.getBusinessAddress()))
                existingLead.setBusinessAddress(lead.getBusinessAddress());

            if (isEmpty(existingLead.getDescription()) && !isEmpty(lead.getDescription()))
                existingLead.setDescription(lead.getDescription());

        } else {
            leadMap.put(emailKey, lead);
        }
    }

    public void writeErrorFile(List<Row> errorRows,UploadHistory uploadHistory) {
        File templateFile = new File("crm-system-backend/src/main/resources/templates/Lead Template.xlsx");

        try (
                FileInputStream fis = new FileInputStream(templateFile);
                Workbook errorWorkbook = new XSSFWorkbook(fis)
        ) {
            Sheet templateSheet = errorWorkbook.getSheetAt(1);

            int startRow = 2; // after header
            for (Row sourceRow : errorRows) {
                Row targetRow = templateSheet.createRow(startRow++);

                for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
                    Cell sourceCell = sourceRow.getCell(i);
                    if (sourceCell == null) continue;

                    Cell targetCell = targetRow.createCell(i);

                    // Copy cell value
                    switch (sourceCell.getCellType()) {
                        case STRING -> targetCell.setCellValue(sourceCell.getStringCellValue());
                        case NUMERIC -> targetCell.setCellValue(sourceCell.getNumericCellValue());
                        default -> targetCell.setCellValue(getCellValue(sourceCell));
                    }

                    // If source has error style, apply it
                    if (sourceCell.getCellStyle().getFillForegroundColor() == IndexedColors.RED.getIndex()) {
                        CellStyle style = errorWorkbook.createCellStyle();
                        style.cloneStyleFrom(sourceCell.getCellStyle());
                        targetCell.setCellStyle(style);

                        // Copy comments if any
                        if (sourceCell.getCellComment() != null) {
                            CreationHelper factory = errorWorkbook.getCreationHelper();
                            Drawing<?> drawing = templateSheet.createDrawingPatriarch();
                            ClientAnchor anchor = factory.createClientAnchor();
                            anchor.setCol1(i);
                            anchor.setRow1(targetRow.getRowNum());

                            Comment comment = drawing.createCellComment(anchor);
                            comment.setString(factory.createRichTextString(
                                    sourceCell.getCellComment().getString().getString()));
                            targetCell.setCellComment(comment);
                        }
                    }
                }
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String errorFilePath = "Lead_Error_File_" + timestamp + ".xlsx";
            try (FileOutputStream out = new FileOutputStream(errorFilePath)) {
                uploadHistory.setErrorFileName(errorFilePath);
                errorWorkbook.write(out);
            }

            log.info("Error file generated with {} invalid rows", errorRows.size());

        } catch (IOException e) {
            log.error("Error writing error file: {}", e.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
    }


}
