package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.entity.Roles;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.ExcelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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

    public List<User> processExcelData(MultipartFile file)  {

        if(!this.validateExcelHeader(file)){
            throw new ExcelException(ErrorCode.WRONG_HEADERS);
        }

        List<User> users = new ArrayList<>();
        try(InputStream is = file.getInputStream();Workbook workbook = new XSSFWorkbook(is)){
            Sheet sheet = workbook.getSheetAt(1);

            // Error style (red background)
            CellStyle errorStyle = workbook.createCellStyle();
            errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            boolean hasError = false;
            for(Row row : sheet){
                if(row.getRowNum() == 0 || row.getRowNum()==1) continue;
                String srNo = getCellValue(row.getCell(0));
                if(srNo == "") break;
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

                //validate first name
                if(isEmpty(firstName) || !firstName.matches(NAME_REGEX)){
                    markError(row.getCell(1), "Invalid First Name", errorStyle);
                    hasError = true;
                }else{
                    user.setFirstName(firstName);
                }

                //validate last name
                if(isEmpty(lastName) || !lastName.matches(NAME_REGEX)){
                    markError(row.getCell(2), "Invalid Last Name", errorStyle);
                    hasError = true;
                }else{
                    user.setLastName(lastName);
                }

                //validate mobile number
                if(isEmpty(mobileNumber) || !mobileNumber.matches(MOBILE_REGEX)){
                    markError(row.getCell(3), "Invalid mobile number", errorStyle);
                    hasError = true;
                }else{
                    user.setMobileNumber(mobileNumber);
                }

                //validate email
                if(isEmpty(email) || !email.matches(EMAIL_REGEX)){
                    markError(row.getCell(4), "Invalid email", errorStyle);
                    hasError = true;
                }else{
                    user.setEmail(email);
                }

                // ✅ If address is empty → no validation needed, accept as blank
                if (isEmpty(address)) {
                    user.setAddress(null);
                    user.setCity(null);
                    user.setState(null);
                    user.setCountry(null);
                    user.setPinCode(null);

                    // no further checks on city/state/country/pin
                } else {

                    // ✅ If address exists, validate address format
                    if (!address.matches(ADDRESS_REGEX)) {
                        markError(row.getCell(5), "Invalid Address format", errorStyle);
                        hasError = true;
                    }

                    // ✅ Now city/state/country/pincode become compulsory
                    if (isEmpty(city) || !city.matches("^[A-Za-z ]{2,50}$")) {
                        markError(row.getCell(6), "Invalid or Missing City", errorStyle);
                        hasError = true;
                    }

                    if (isEmpty(state)) {
                        markError(row.getCell(7), "Invalid or Missing State", errorStyle);
                        hasError = true;
                    }

                    if (isEmpty(country)) {
                        markError(row.getCell(8), "Invalid or Missing Country", errorStyle);
                        hasError = true;
                    }

                    if (isEmpty(pinCode) || !pinCode.matches(PIN_CODE_REGEX)) {
                        markError(row.getCell(9), "Invalid or Missing Pin Code", errorStyle);
                        hasError = true;
                    }

                    if (hasError) continue;  // ✅ Do not save if any field invalid

                    // ✅ All valid, save
                    user.setAddress(address);
                    user.setCity(city);
                    user.setState(state);
                    user.setCountry(country);
                    user.setPinCode(pinCode);
                }

                //validate role
                if(isEmpty(role)){
                    markError(row.getCell(10), "Invalid Role", errorStyle);
                    hasError = true;

                }else{
                    //String roleOfUser = getCellValue(row.getCell(10));
                    if(role == "Basic"){
                        user.setRole(Roles.USER);
                    }else{
                        user.setRole(Roles.ADMIN);
                    }
                }

                //validate password
                if((isEmpty(password) || !password.matches(PASSWORD_REGEX)) &&
                        (isEmpty(confirmPassword) || !confirmPassword.matches(PASSWORD_REGEX))
                && (!password.equals(confirmPassword))){
                    markError(row.getCell(11), "Invalid Password", errorStyle);
                    markError(row.getCell(12), "Confirm Password does not match", errorStyle);
                    hasError = true;

                }else{
                    user.setPassword(password);
                }
                if(!hasError){
                    users.add(user);
                }else{
                    String errorFilePath = "Error_File.xlsx";
                    try(FileOutputStream out = new FileOutputStream(errorFilePath)){
                        workbook.write(out);

                    }catch (FileNotFoundException e){
                        log.error(e.getMessage());
                        throw new ExcelException(ErrorCode.FILE_NOT_FOUND_EXCEPTION);
                    }catch (IOException e){
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }

                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return users;
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



}
