package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.ExcelProcessingError;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UserExcelHelper {

    private static  final String NAME_REGEX = "^[A-Za-z ]{1,50}$";

    private static  final String ADDRESS_REGEX = "^[A-Za-z0-9 ,./#\\-]{1,100}$";

    private static  final String MOBILE_REGEX = "^[789]\\d{9}$";

    private static  final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,16}$";

    private static final String PIN_CODE_REGEX = "^[0-9]{6}$";
    public List<User> processExcelData(MultipartFile file, String userRole, UploadHistory uploadHistory) {
        int countDown = 5;


        if (!this.validateExcelHeader(file)) {
            throw new ExcelException(ErrorCode.WRONG_HEADERS);
        }

        List<User> users = new ArrayList<>(); //valid users
        List<Row> errorRows = new ArrayList<>(); //error rows

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(1);

            CellStyle errorStyle = workbook.createCellStyle();
            errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (Row row : sheet) {
                boolean hasError = false;

                if (row.getRowNum() <= 1) continue;
                if (isRowEmpty(row)) {
                    countDown--;
                    if (countDown == 0) break;
                    continue;
                }

                User user = new User();

                String firstName = getCellValue(row.getCell(1));
                String lastName = getCellValue(row.getCell(2));
                String mobileNumber = getCellValue(row.getCell(3));
                String email = getCellValue(row.getCell(4));
                String address = getCellValue(row.getCell(5));
                String city = getCellValue(row.getCell(6));
                String state = getCellValue(row.getCell(7));
                String country = getCellValue(row.getCell(8));
                String pinCode = getCellValue(row.getCell(9));
                String role = getCellValue(row.getCell(10));
                String password = getCellValue(row.getCell(11));
                String confirmPassword = getCellValue(row.getCell(12));


                if (isEmpty(firstName) || !firstName.matches(NAME_REGEX)) {
                    markError(row.getCell(1), "Invalid First Name", errorStyle);
                    hasError = true;
                } else {
                    user.setFirstName(firstName);
                }

                if (isEmpty(lastName) || !lastName.matches(NAME_REGEX)) {
                    markError(row.getCell(2), "Invalid Last Name", errorStyle);
                    hasError = true;
                } else {
                    user.setLastName(lastName);
                }

                if (isEmpty(mobileNumber) || !mobileNumber.matches(MOBILE_REGEX)) {
                    markError(row.getCell(3), "Invalid mobile number", errorStyle);
                    hasError = true;
                } else {
                    user.setMobileNumber(mobileNumber);
                }

                if (isEmpty(email) || !email.matches(EMAIL_REGEX)) {
                    markError(row.getCell(4), "Invalid email", errorStyle);
                    hasError = true;
                } else {
                    user.setEmail(email);
                }




                if (isEmpty(role) || ("ADMIN".equals(userRole) && !"Basic".equals(role))) {
                    markError(row.getCell(10), "Invalid Role", errorStyle);
                    hasError = true;
                } else {
                    if ("Basic".equals(role)) {
                        user.setRole(Roles.USER);
                    } else {
                        user.setRole(Roles.ADMIN);
                    }
                }

                if ((isEmpty(password) || !password.matches(PASSWORD_REGEX))
                        || (isEmpty(confirmPassword) || !confirmPassword.matches(PASSWORD_REGEX))
                        || (!password.equals(confirmPassword))) {

                    markError(row.getCell(11), "Invalid Password", errorStyle);
                    markError(row.getCell(12), "Confirm Password does not match", errorStyle);
                    hasError = true;
                } else {
                    user.setPassword(password);
                }


                if (!hasError) {
                    users.add(user);
                } else {
                    errorRows.add(row);
                }
            }
            if (!errorRows.isEmpty()) {
                if(!users.isEmpty()){
                    uploadHistory.setUploadStatus(UploadStatus.PARTIALLY_SUCCESS);
                }
                uploadHistory.setInvalidRecords(errorRows.size());
                writeErrorFile(errorRows,uploadHistory);
            }

//            if (isThereError) {
//                log.error("Error in file processing");
//
//                throw new ExcelProcessingError(ErrorCode.ERROR_IN_FILE_PROCESSING,getErrorFileAsBytes(workbook));
//            }

        } catch (IOException e) {
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            log.error(e.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
        uploadHistory.setTotalRecords((users.size()+ errorRows.size()));
        uploadHistory.setInvalidRecords(errorRows.size());
        uploadHistory.setValidRecords(users.size());
        return users;
    }

    private byte[] getErrorFileAsBytes(Workbook workbook) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
    }



    private boolean validateExcelHeader(MultipartFile file) {
        File templateFile = new File("crm-system-backend/src/main/resources/templates/UsersTemplate.xlsx");

        try (
                InputStream uploadedIs = file.getInputStream();
                InputStream templateIs = new ClassPathResource("templates/UsersTemplate.xlsx").getInputStream();

                Workbook uploadedWorkbook = new XSSFWorkbook(uploadedIs);
                Workbook templateWorkbook = new XSSFWorkbook(templateIs)
        ) {
            Sheet uploadedSheet = uploadedWorkbook.getSheetAt(0);
            Sheet templateSheet = templateWorkbook.getSheetAt(0);

            // Read header row (assumed to be first row)
            Row uploadedHeader = uploadedSheet.getRow(0);
            Row templateHeader = templateSheet.getRow(0);

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

                if (!uploadedHeaderValue.equalsIgnoreCase(templateHeaderValue)) {
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
    private static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }


    public void writeErrorFile(List<Row> errorRows,UploadHistory uploadHistory) throws IOException {
        File templateFile =  new ClassPathResource("templates/UsersTemplate.xlsx").getFile();

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
            String errorFilePath = "Error_File_" + timestamp + ".xlsx";
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
