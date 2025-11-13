package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.constants.RegxConstant;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.ExcelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@Component
public class LeadExcelHelper {


    List<Lead> validLeads = new ArrayList<>();
    List<Row> errorRows = new ArrayList<>();

    public List<Lead> processExcelData(MultipartFile file, UploadHistory uploadHistory)  {

        Map<String, Lead> leadMap = new HashMap<>(); // merge duplicate leads

        if(!this.validateExcelHeader(file)){
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            throw new ExcelException(ErrorCode.WRONG_HEADERS);
        }

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(1);
            // Error style (red background)
            CellStyle errorStyle = workbook.createCellStyle();
            errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            boolean hasError = false;

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || row.getRowNum()==1) continue;// skip header
                if(isRowEmpty(row)){
                    continue;
                }
                Lead lead = extractLead(row);
                boolean rowHasError = validateRow(row, lead, errorStyle, leadMap);
                if (rowHasError) {
                    // Store row with errors for writing later
                    errorRows.add(row);
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
            }

        } catch (IOException e) {
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            log.error(e.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
        uploadHistory.setTotalRecords((validLeads.size()+ errorRows.size()));
        uploadHistory.setInvalidRecords(errorRows.size());
        uploadHistory.setValidRecords(validLeads.size());
        return validLeads;
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
        lead.setGstin(getCellValue(row.getCell(5)));
        lead.getInterestedModules().add(getCellValue(row.getCell(6)));
        lead.setBusinessAddress(getCellValue(row.getCell(7)));
        lead.setDescription(getCellValue(row.getCell(8)));
        return lead;
    }

//    private boolean validateExcelHeader(MultipartFile file)  {
//        File tempFile = new File("crm-system-backend\\src\\main\\resources\\Lead Template.xlsx");
//        try (
//            Workbook workbook = new XSSFWorkbook(file.getInputStream());
//            Workbook tempWorkbook = new XSSFWorkbook(tempFile.getAbsolutePath())){
//            Sheet sheet = workbook.getSheetAt(1);
//            for (Row row : sheet) {
//                if (row.getRowNum() == 1) {
//                    for (Cell cell : row) {
//                        if (!cell.getCellType().toString().equals(headers.get(cell.getColumnIndex()))) {
//                            return false;
//                        }
//                    }
//                    return true;
//                }
//            }
//            return true;
//        }
//        catch (IOException ioException) {
//            log.error(ioException.getMessage());
//            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
//        }
//    }
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

    private boolean validateRow(Row row, Lead lead, CellStyle errorStyle, Map<String, Lead> leadMap) {
        boolean hasError = false;
        // 1. First Name
        if (isEmpty(lead.getFirstName()) || !lead.getFirstName().matches(RegxConstant.NAME_REGEX)) {
            markError(row.getCell(1), "Invalid First Name", errorStyle);
            hasError = true;
        }

        // 2. Last Name
        if (isEmpty(lead.getLastName()) || !lead.getLastName().matches(RegxConstant.NAME_REGEX)) {
            markError(row.getCell(2), "Invalid Last Name", errorStyle);
            hasError = true;
        }

        // 3. Mobile
        if (isEmpty(lead.getMobileNumber()) || !lead.getMobileNumber().matches(RegxConstant.MOBILE_REGEX)) {
            markError(row.getCell(3), "Invalid Mobile Number", errorStyle);
            hasError = true;
        }

        // 4. Email
        if (isEmpty(lead.getEmail()) || !lead.getEmail().matches(RegxConstant.EMAIL_REGEX)) {
            markError(row.getCell(4), "Invalid Email", errorStyle);
            hasError = true;
        }

        // 5. GSTIN
        if (isEmpty(lead.getGstin()) || !lead.getGstin().matches(RegxConstant.GSTIN_REGEX)) {
            markError(row.getCell(5), "Invalid GSTIN", errorStyle);
            hasError = true;
        }

        // 6. Interested Modules
        if (lead.getInterestedModules() == null || lead.getInterestedModules().isEmpty()) {
            markError(row.getCell(6), "No Modules Selected", errorStyle);
            hasError = true;
        }
//        else {
//            for (String module : lead.getInterestedModules()) {
//                if (!ALLOWED_MODULES.contains(module.toUpperCase())) {
//                    markError(row.getCell(6), "Invalid Module: " + module, errorStyle);
//                    hasError = true;
//                }
//            }
//        }

        // 7. Address
        if (!isEmpty(lead.getBusinessAddress())){
            if(!lead.getBusinessAddress().matches(RegxConstant.ADDRESS_REGEX)) {
                markError(row.getCell(7), "Invalid Address", errorStyle);
                hasError = true;
            }
        }

        // 8. Description
        if (!isEmpty(lead.getDescription()) ){
            if(!lead.getDescription().matches(RegxConstant.DESCRIPTION_REGEX)) {
                markError(row.getCell(8), "Invalid Description", errorStyle);
                hasError = true;
            }
        }
        return hasError;
    }

    private void mergeLead(Map<String, Lead> leadMap, Lead lead) {
        String emailKey = lead.getEmail().trim().toLowerCase();

        if (leadMap.containsKey(emailKey)) {
            Lead existingLead = leadMap.get(emailKey);

            // Merge interested modules (avoid duplicates)
            existingLead.getInterestedModules().addAll(lead.getInterestedModules());

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

    private void writeErrorFile(List<Row> errorRows,UploadHistory uploadHistory) {
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
