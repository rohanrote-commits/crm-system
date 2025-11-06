package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.ExcelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
                if(isRowEmpty(row)){
                    continue;
                }
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

                boolean hasLead = false;
                //8 validate the modules
                if (isEmpty(instreatedModules)) {
                    markError(row.getCell(6), "Invalid Module Selected ", errorStyle);
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
                // 6 Validate address
                if (!businessAddress.matches(ADDRESS_REGEX)) {
                    markError(row.getCell(7), "Address formate", errorStyle);
                    hasError = true;
                } else {
                    lead.setBusinessAddress(businessAddress);
                }


                // 7 Validate description
                if (!description.matches(DESCRIPTION_REGEX)) {
                    markError(row.getCell(8), "Invalid Description ", errorStyle);
                    hasError = true;
                } else {
                    lead.setDescription(description);
                }


                if(!hasError || !hasLead) {
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

//            if (uploadedCells != templateCells) {
//                return false; // Different number of columns
//            }

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


}
