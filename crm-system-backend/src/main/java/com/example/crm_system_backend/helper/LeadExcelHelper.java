package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.ExcelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class LeadExcelHelper {

    private static  final String NAME_REGEX = "^[A-Za-z ]{1,50}$";

    private static  final String ADDRESS_REGEX = "^[A-Za-z0-9 ,./#\\-]{1,100}$";

    private static  final String MOBILE_REGEX = "^[789]\\d{9}$";

    private static  final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private static  final String DESCRIPTION_REGEX = "^[A-Za-z0-9 ,./#@!$%^&*()_\\-]{1,100}$";

    private static  final String GSTIN_REGEX = "^[A-Z0-9]{15}$";

    List<String> headers = List.of("Sr No","First Name (M)","Last Name (M)","Mobile Number (M)","Email (M)","GSTIN (M)","Interested Modules (M)","Business Address (O)","Description (O)");


    public List<Lead> processExcelData(MultipartFile file)  {

        if(!this.validateExcelHeader(file)){
            throw new ExcelException(ErrorCode.WRONG_HEADERS);
        }
        List<Lead> leads = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(1);
            // Error style (red background)
            CellStyle errorStyle = workbook.createCellStyle();
            errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            boolean hasError = false;

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || row.getRowNum()==1) continue; // skip header
                Lead lead = new Lead();
                String firstName = getCellValue(row.getCell(1));
                String lastName = getCellValue(row.getCell(2));
                String mobileNumber = getCellValue(row.getCell(3));
                String email = getCellValue(row.getCell(4));
                String GSTIN = getCellValue(row.getCell(5));
                String instreatedModules = getCellValue(row.getCell(6));
                String businessAddress = getCellValue(row.getCell(7));
                String description = getCellValue(row.getCell(8));

                // 1️ Validate first name
                if (isEmpty(firstName) || !firstName.matches(NAME_REGEX)) {
                    markError(row.getCell(1), "Invalid First Name", errorStyle);
                    hasError = true;
                } else {
                    lead.setFirstName(firstName);
                }

                // 2️ Validate last Name
                if (isEmpty(lastName) || !lastName.matches(NAME_REGEX)) {
                    markError(row.getCell(2), "Invalid Last Name", errorStyle);
                    hasError = true;
                } else {
                    lead.setLastName(lastName);
                }

                // 3️ Validate mobile
                if (isEmpty(mobileNumber) || !mobileNumber.matches(MOBILE_REGEX)) {
                    markError(row.getCell(3), "Invalid mobile number", errorStyle);
                    hasError = true;
                } else {
                    lead.setMobileNumber(mobileNumber);
                }

                // 4️ Validate Email
                if (isEmpty(email) || !email.matches(EMAIL_REGEX)) {
                    markError(row.getCell(4), "Invalid email", errorStyle);
                    hasError = true;
                } else {
                    lead.setEmail(email);
                }

                // 5️ Validate GSTIN
                if (isEmpty(GSTIN) || !GSTIN.matches(GSTIN_REGEX)) {
                    markError(row.getCell(5), "Invalid GSTIN Number", errorStyle);
                    hasError = true;
                } else {
                    lead.setGstin(GSTIN);
                }

                // 6 Validate address
                if (!businessAddress.matches(ADDRESS_REGEX)) {
                    markError(row.getCell(6), "Address formate", errorStyle);
                    hasError = true;
                } else {
                    lead.setBusinessAddress(businessAddress);
                }


                // 7 Validate description
                if (!description.matches(DESCRIPTION_REGEX)) {
                    markError(row.getCell(7), "Invalid Description ", errorStyle);
                    hasError = true;
                } else {
                    lead.setDescription(description);
                }
                boolean hasLead = false;
                //8 validate the modules
                if (isEmpty(instreatedModules)) {
                    markError(row.getCell(8), "Invalid Module Selected ", errorStyle);
                    hasError = true;
                } else {

                    if(leads.stream().anyMatch((lead1 -> {
                        return lead1.getEmail().equals(lead.getEmail());
                    }))){
                        leads.stream().filter(

                                lead1 -> lead1.getEmail().equals(lead.getEmail())).
                                findFirst().ifPresent(lead1 -> {
                                    lead1.getInterestedModules().add(instreatedModules);
                                });
                        hasLead = true;
                    }
                    else {
                        lead.getInterestedModules().add(instreatedModules);
                    }
                }

                if
                (!hasError && !hasLead) {
                    leads.add(lead);
                }
            }

            if (hasError){
                // If any errors → create an error file
                String errorFilePath = "Error_File.xlsx";
                try (FileOutputStream out = new FileOutputStream(errorFilePath)) {
                    workbook.write(out);
                } catch (FileNotFoundException e) {
                    log.error(e.getMessage());
                    throw new ExcelException(ErrorCode.FILE_NOT_FOUND_EXCEPTION);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }

        return leads;
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

    private boolean validateExcelHeader(MultipartFile file)  {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(1);
            for (Row row : sheet) {
                if (row.getRowNum() == 1) {
                    for (Cell cell : row) {
                        if (!cell.getCellType().toString().equals(headers.get(cell.getColumnIndex()))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return true;
        }
        catch (IOException ioException) {
            log.error(ioException.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
    }
}
