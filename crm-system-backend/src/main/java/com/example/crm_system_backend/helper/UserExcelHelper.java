package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.beans.UserList;
import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.ExcelProcessingError;
import com.example.crm_system_backend.exception.UserException;
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

    private static final String NAME_REGEX = "^[A-Za-z ]{1,50}$";

    private static final String ADDRESS_REGEX = "^[A-Za-z0-9 ,./#\\-]{1,200}$";

    private static final String MOBILE_REGEX = "^[789]\\d{9}$";

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,16}$";

    private static final String PIN_CODE_REGEX = "^[0-9]{6}$";

    /**
     * Extracts and returns the value of a given cell as a String. Handles numeric, date-formatted,
     * and string cell types. If the cell is null, an empty String is returned.
     *
     * @param cell the cell from which to extract the value, may be null
     * @return the cell value as a String; an empty String if the cell is null
     */
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

    /**
     * Checks whether a given string is empty or contains only whitespace characters.
     *
     * @param value the string to be checked, may be null
     * @return true if the string is null, empty, or contains only whitespace characters; false otherwise
     */
    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Marks the given cell as erroneous by applying an error-specific style and attaching
     * a comment with the provided error message.
     *
     * @param cell       the cell to be marked as erroneous; if null, the method does nothing
     * @param message    the error message to add as a comment to the cell
     * @param errorStyle the cell style to apply for indicating the error
     */
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

    /**
     * Checks whether a given row in an Excel sheet is empty.
     * A row is considered empty if all its cells are either null or contain a blank value.
     *
     * @param row the row to be checked, must not be null
     * @return true if the row is empty; false otherwise
     */
    private static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Processes the given Excel file to extract user information, validate the data,
     * and populate a list of successfully validated users. Errors encountered during
     * processing are marked in the Excel file.
     *
     * @param file          the Excel file to be processed, containing user information
     * @param userRole      the role of the user performing the upload, used for validation
     * @param uploadHistory an object to track the upload process, storing the status
     *                      and counts of valid and invalid records
     * @return a list of valid {@code User} objects extracted from the Excel file
     * @throws ExcelException if the file has invalid headers or if there are issues
     *                        during file processing
     */
    public UserList processExcelData(MultipartFile file, String userRole, UploadHistory uploadHistory) {
        int countDown = 5;


        if (!this.validateExcelHeader(file)) {
            throw new ExcelException(ErrorCode.WRONG_HEADERS);
        }

        List<User> users = new ArrayList<>(); //valid users
        List<Row> errorRows = new ArrayList<>();//error rows
        UserList userList = new UserList();

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

                boolean isAddressPresent = !isEmpty(address);
                boolean isAnyLocationFieldPresent =
                        !isEmpty(city) || !isEmpty(state) || !isEmpty(country) || !isEmpty(pinCode);

                if (isAddressPresent) {

                    if (!address.matches(ADDRESS_REGEX)) {
                        markError(row.getCell(5), "Invalid Address", errorStyle);
                        hasError = true;
                    } else {
                        user.setAddress(address);
                    }

                    if (isEmpty(city) || !city.matches(NAME_REGEX)) {
                        markError(row.getCell(6), "City required", errorStyle);
                        hasError = true;
                    } else {
                        user.setCity(city);
                    }

                    if (isEmpty(state) || !state.matches(NAME_REGEX)) {
                        markError(row.getCell(7), "State required", errorStyle);
                        hasError = true;
                    } else {
                        user.setState(state);
                    }

                    if (isEmpty(country) || !country.matches(NAME_REGEX)) {
                        markError(row.getCell(8), "Country required", errorStyle);
                        hasError = true;
                    } else {
                        user.setCountry(country);
                    }

                    if (isEmpty(pinCode) || !pinCode.matches(PIN_CODE_REGEX)) {
                        markError(row.getCell(9), "Invalid Pincode", errorStyle);
                        hasError = true;
                    } else {
                        user.setPinCode(pinCode);
                    }

                } else if (isAnyLocationFieldPresent) {

                    markError(row.getCell(5), "Address required", errorStyle);
                    markError(row.getCell(6), "City requires Address", errorStyle);
                    markError(row.getCell(7), "State requires Address", errorStyle);
                    markError(row.getCell(8), "Country requires Address", errorStyle);
                    markError(row.getCell(9), "Pincode requires Address", errorStyle);
                    hasError = true;
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
                if (!users.isEmpty()) {
                    uploadHistory.setUploadStatus(UploadStatus.PARTIALLY_SUCCESS);
                }
                uploadHistory.setInvalidRecords(errorRows.size());
                writeErrorFile(errorRows, uploadHistory);
                List<User> errorUserList = errorRows.stream().map(this::extractUser).toList();
                userList.setInvalidUserList(errorUserList);

            }
           userList.setValidUserList(users);

        }
        catch (IOException e) {
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            log.error(e.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
        uploadHistory.setTotalRecords((users.size() + errorRows.size()));
        uploadHistory.setInvalidRecords(errorRows.size());
        uploadHistory.setValidRecords(users.size());
        return userList;
    }

    /**
     * Creates and returns the content of the given Excel workbook as a byte array.
     * Writes the workbook data to an in-memory output stream and converts it to a byte array.
     * If an error occurs during processing, an {@code ExcelException} is thrown with a relevant error code.
     *
     * @param workbook the Excel workbook to be converted to a byte array; must not be null
     * @return a byte array representation of the workbook's content
     * @throws ExcelException if an I/O error occurs while writing the workbook data
     */
    private byte[] getErrorFileAsBytes(Workbook workbook) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
    }

    /**
     * Validates if the header of the provided Excel file matches the template header.
     * Compares each column in the first row of the given file against the predefined template.
     * If the headers do not match, logs the discrepancy and returns false.
     *
     * @param file the Excel file to be validated, must not be null
     * @return true if the Excel file's header matches the template; false otherwise
     * @throws ExcelException if an error occurs while processing the file
     */
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

    /**
     * Generates an error file in Excel format to record invalid rows encountered
     * during an upload process. The invalid rows are copied into a new sheet
     * within a predefined template and styled with error-specific formatting.
     * The generated file is saved with a timestamped name and its path is set
     * in the provided {@code UploadHistory} object.
     *
     * @param errorRows     a list of invalid rows to be included in the error file
     * @param uploadHistory an object used to track the upload process, where the
     *                      path of the generated error file is stored
     * @throws IOException if there is an issue accessing or writing to the file
     */
    public void writeErrorFile(List<Row> errorRows, UploadHistory uploadHistory) throws IOException {
        File templateFile = new ClassPathResource("templates/UsersTemplate.xlsx").getFile();

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
    private User extractUser(Row row) {
        User user = new User();
        user.setFirstName(getCellValue(row.getCell(1)));
        user.setLastName(getCellValue(row.getCell(2)));
        user.setMobileNumber(getCellValue(row.getCell(3)));
        user.setEmail(getCellValue(row.getCell(4)));
        user.setAddress(getCellValue(row.getCell(5)));
        user.setCity(getCellValue(row.getCell(6)));
        user.setState(getCellValue(row.getCell(7)));
        user.setCountry(getCellValue(row.getCell(8)));
        user.setPinCode(getCellValue(row.getCell(9)));
        String role = getCellValue(row.getCell(10));
        if ("Basic".equals(role)) {
            user.setRole(Roles.USER);
        }else{
            user.setRole(Roles.ADMIN);
        }


        user.setPassword(getCellValue(row.getCell(11)));


        return user;
    }

}
