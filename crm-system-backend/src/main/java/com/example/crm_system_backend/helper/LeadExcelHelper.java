package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.beans.LeadList;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.ProductColumn;
import com.example.crm_system_backend.constants.RegxConstant;
import com.example.crm_system_backend.constants.UploadStatus;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.Product;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.service.serviceImpl.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Component
@AllArgsConstructor
public class LeadExcelHelper {


    private static final Logger log = LoggerFactory.getLogger(LeadExcelHelper.class);
    private final ProductService productService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;


    /**
     * Processes the data from an uploaded Excel file, validates the records,
     * and segregates them into valid and invalid lists based on certain rules.
     * Updates the upload history accordingly and generates error details for invalid records.
     *
     * @param file the uploaded Excel file to be processed
     * @param uploadHistory the record capturing metadata and status of the upload process
     * @return a {@link CompletableFuture} containing a {@link LeadList} object,
     *         which includes lists of valid and invalid leads extracted from the Excel file
     */
    @Async("bulkUploadExecutor")
    public CompletableFuture<LeadList> processExcelData(MultipartFile file, UploadHistory uploadHistory)  {
        log.info("Enter: LeadExcelHelper.processExcelData");
        List<Lead> validLeads = new ArrayList<>();
        List<Lead> invalidLeads = new ArrayList<>();
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
            //load the products from master db to validate
            Set<Product> productList = productService.getProducts();

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || row.getRowNum()==1 || row.getRowNum() == 2 ) continue;// skip header
                if(isRowEmpty(row)){
                    continue;
                }
                Lead lead = extractLead(row,productList);
                Map<String, String> errorMap = validateRowWithErrors(row, lead, errorStyle);
                if (!errorMap.isEmpty()) {
                    // Add to error rows list (for Excel file)
                    invalidLeads.add(lead);
                    // Build JSON error entry
                    InvalidLeadError err = new InvalidLeadError();
                    err.setRowNumber(row.getRowNum());
                    err.setLead(lead);
                    err.setErrors(errorMap);
                    jsonErrorList.add(err);
                } else {
                    validLeads.add(lead);
                   // mergeLead(leadMap, lead);
                }
            }
            //if the error row list has entries then generate the error file
            if (!jsonErrorList.isEmpty()) {
                if(!validLeads.isEmpty()){
                    uploadHistory.setUploadStatus(UploadStatus.PARTIALLY_SUCCESS);
                }
                uploadHistory.setInvalidRecords(jsonErrorList.size());
                leadList.setInvalidLeadList(invalidLeads);
            }

        } catch (IOException e) {
            log.error("Exit : LeadExcelHelper.processExcelData -->{}",e);
            uploadHistory.setUploadStatus(UploadStatus.FAILED);
            log.error(e.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
        uploadHistory.setTotalRecords((validLeads.size()+ jsonErrorList.size()));
        uploadHistory.setInvalidRecords(jsonErrorList.size());
        uploadHistory.setValidRecords(validLeads.size());

        String jsonData = null;
        try {
            jsonData = objectMapper.writeValueAsString(jsonErrorList);
        } catch (JsonProcessingException e) {
            log.error("Exception: LeadExcelHelper.processExcelData {}",e);
            throw new RuntimeException(e);
        }
        uploadHistory.setErrorRecord(jsonData);
        leadList.setValidLeadList(validLeads);
        log.info("Exit: LeadExcelHelper.processExcelData");
        return CompletableFuture.completedFuture(leadList);
    }

    /**
     * Safely retrieves the value of a given Excel cell as a string. This method handles
     * different cell types (numeric, date, or string) and provides a consistent string output.
     *
     * @param cell the Excel {@link Cell} to retrieve the value from. It can be null.
     * @return the string representation of the cell value. If the cell is null or empty,
     *         an empty string ("") is returned.
     */
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


    /**
     * Checks if a given Excel row is empty. A row is considered empty if all cells
     * in the row are either null or have a blank value.
     *
     * @param row the Excel {@link Row} to be checked for emptiness.
     *            It may contain multiple cells for validation.
     * @return {@code true} if the row is empty; {@code false} otherwise.
     * @author Akshay Jadhav
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
     * Checks if a given string is empty or null. A string is considered empty if it is null,
     * has a length of 0, or contains only whitespace characters.
     *
     * @param value the string to check for emptiness. It can be null or non-null.
     * @return {@code true} if the string is null, has zero length, or is composed only of whitespace;
     *         {@code false} otherwise.
     * @author Akshay Jadhav
     */
    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }


    /**
     * Extracts and constructs a Lead object from an Excel Row and a set of products.
     * The method populates the Lead object with details such as first name, last name,
     * mobile number, email, GSTIN, business address, description, and the products
     * the lead is interested in, based on the provided row data.
     *
     * @param row the Excel row containing the data for the lead. It is used to populate
     *            the fields of the Lead object.
     * @param productList the set of products available for selection. It is used to identify
     *                    the products the lead is interested in.
     * @return a populated {@link Lead} object based on the data extracted from the row
     *         and product information.
     * @author Akshay Jadhav
     */
    private Lead extractLead(Row row , Set<Product> productList ) {
        Lead lead = new Lead();
        //extract instreated products for particular  lead
        Set<Product> interestedProducts = this.extractInterestedProducts(row,productList);
        lead.setFirstName(getCellValue(row.getCell(1)));
        lead.setLastName(getCellValue(row.getCell(2)));
        lead.setMobileNumber(getCellValue(row.getCell(3)));
        lead.setEmail(getCellValue(row.getCell(4)));
        lead.setGstin(getCellValue(row.getCell(5)).toUpperCase());
        lead.getInterestedProducts().addAll(interestedProducts);
        lead.setBusinessAddress(getCellValue(row.getCell(13)));
        lead.setDescription(getCellValue(row.getCell(14)));
        return lead;
    }

    /**
     * Extracts a set of interested products from a given Excel row based on specific column values.
     * It checks predetermined columns of the row for a "Yes" response, maps them to product modules,
     * and returns the matching products from the provided product list.
     *
     * @param row the Excel {@link Row} containing multiple cells, where specific columns
     *            indicate interest in products.
     * @param productList a set of {@link Product} objects representing available products
     *                    that can be matched based on column data.
     * @return a {@link Set} of {@link Product} objects that are determined to be of interest
     *         based on the row data.
     * @author Akshay Jadhav
     */
    private Set<Product> extractInterestedProducts(Row row, Set<Product> productList ) {
        Set<Product> interestedProducts = new HashSet<>();
        for (int c = 6; c <= 12; c++) {
            if ("Yes".equalsIgnoreCase(getCellValue(row.getCell(c)))) {
                ProductColumn pc = ProductColumn.fromColumn(c);
                if (pc == null) continue;
                String moduleName = pc.getModuleName();
                productList.stream()
                        .filter(product -> product.getProductName().equalsIgnoreCase(moduleName))
                        .findFirst()
                        .ifPresent(interestedProducts::add);
            }
        }
        return interestedProducts;
    }


    /**
     * Validates the header row of the uploaded Excel file against a predefined template.
     * Ensures that the column order and names match between the uploaded file and the template.
     * This method reads both the uploaded file and the template file, comparing their headers row by row.
     * If the headers mismatch or an error occurs during processing, the method returns false or throws an exception.
     *
     * @param file the Excel {@link MultipartFile} to validate. This file should contain a sheet with headers to compare.
     * @return {@code true} if the uploaded file's header matches the template; {@code false} otherwise.
     * @throws ExcelException if an error occurs during file reading or validation.
     * @author Akshay Jadhav
     */
    private boolean validateExcelHeader(MultipartFile file) {
        ClassPathResource resource = new ClassPathResource("templates/Lead Template.xlsx");

        try (
                Workbook uploadedWorkbook = new XSSFWorkbook(file.getInputStream());
                InputStream is = resource.getInputStream();
                Workbook templateWorkbook = new XSSFWorkbook(is)
        ) {
            Sheet uploadedSheet = uploadedWorkbook.getSheetAt(1);
            Sheet templateSheet = templateWorkbook.getSheetAt(1);

            // Read header row (assumed to be first row)
            Row uploadedHeader = uploadedSheet.getRow(2);
            Row templateHeader = templateSheet.getRow(2);

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

    /**
     * Validates a given row and its corresponding lead object, identifying any errors
     * based on predefined validation rules. If errors are found, they are recorded
     * in a map keyed by field name. Additionally, it applies a specific cell style
     * to cells with errors (if marking logic is uncommented).
     *
     * @param row the Excel {@link Row} being validated. Represents the data in the current row of the Excel sheet.
     * @param lead the {@link Lead} object constructed from the row data. Contains the information to be validated.
     * @param errorStyle the {@link CellStyle} applied to Excel cells to visually indicate errors.
     * @return a map of error messages where the key represents the invalid field name
     *         and the value is the corresponding error message. Returns an empty map if no errors are found.
     * @author Akshay Jadhav
     */
    private Map<String, String> validateRowWithErrors(Row row, Lead lead, CellStyle errorStyle) {
        Map<String, String> errorMap = new HashMap<>();

        // First Name
        if (isEmpty(lead.getFirstName()) || !lead.getFirstName().matches(RegxConstant.NAME_REGEX)) {
            String msg = "Invalid First Name";
            errorMap.put("firstName", msg);
        }

        // Last Name
        if (isEmpty(lead.getLastName()) || !lead.getLastName().matches(RegxConstant.NAME_REGEX)) {
            String msg = "Invalid Last Name";
            errorMap.put("lastName", msg);
        }

        // Mobile
        if (isEmpty(lead.getMobileNumber()) || !lead.getMobileNumber().matches(RegxConstant.MOBILE_REGEX)) {
            String msg = "Invalid Mobile Number";
            errorMap.put("mobileNumber", msg);
        }

        // Email
        if (isEmpty(lead.getEmail()) || !lead.getEmail().matches(RegxConstant.EMAIL_REGEX)) {
            String msg = "Invalid Email";
            errorMap.put("email", msg);
        }

        // GSTIN
        if (isEmpty(lead.getGstin()) || !lead.getGstin().matches(RegxConstant.GSTIN_REGEX)) {
            String msg = "Invalid GSTIN";
            errorMap.put("gstin", msg);
        }

        // Modules
        if (lead.getInterestedProducts() == null || lead.getInterestedProducts().isEmpty()) {
            String msg = "No Modules Selected";
            errorMap.put("interestedProducts", msg);
        }

        // Address
        if (!isEmpty(lead.getBusinessAddress()) &&
                !lead.getBusinessAddress().matches(RegxConstant.ADDRESS_REGEX)) {
            String msg = "Invalid Address";
            errorMap.put("businessAddress", msg);
        }

        // Description
        if (!isEmpty(lead.getDescription()) &&
                !lead.getDescription().matches(RegxConstant.DESCRIPTION_REGEX)) {
            String msg = "Invalid Description";
            errorMap.put("description", msg);
        }

        return errorMap;
    }

    /**
     * Generates an Excel file containing error details from a list of invalid lead records.
     * The generated Excel highlights the errors in red and includes comments for each error
     * associated with specific rows in the file. The structure and format of the output follow
     * a predefined template.
     *
     * @param invalidLeads a list of {@link InvalidLeadError} objects containing invalid leads,
     *                     their associated data, and error messages for validation failures.
     * @return a byte array representing the content of the generated Excel file.
     * @throws Exception if an error occurs during file processing, such as reading the template
     *                   or writing the output file.
     * @author Akshay Jadhav
     */
    @Async("bulkUploadExecutor")
    public CompletableFuture<byte[]> generateErrorExcelFromJson(List<InvalidLeadError> invalidLeads) throws Exception {
        log.info("Enter: LeadExcelHelper.generateErrorExcelFromJson");
        ClassPathResource resource = new ClassPathResource("templates/Lead Template.xlsx");
        try (
                FileInputStream fis = new FileInputStream(resource.getFile());
                Workbook workbook = new XSSFWorkbook(fis)
        ) {
            Sheet sheet = workbook.getSheetAt(1);

            // Error style (red background)
            CellStyle errorStyle = workbook.createCellStyle();
            errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Drawing<?> drawing = sheet.createDrawingPatriarch();

            int rowIndex = 3;

            for (InvalidLeadError invalid : invalidLeads) {

                Lead lead = invalid.getLead();
                Integer rowNumber =  invalid.getRowNumber();
                Map<String, String> errors = invalid.getErrors();
                Set<Product> products = lead.getInterestedProducts();

                // If no products – write one row with empty product column
                if (products == null || products.isEmpty()) {
                    Row row = sheet.createRow(rowIndex);
                    row.createCell(0).setCellValue(rowNumber);
                    writeLeadRow(row, lead);   // null product
                    writeComments(sheet, drawing, rowIndex, errors,errorStyle);
                    rowIndex++;
                    continue;
                }
                    Row row = sheet.createRow(rowIndex);
                    row.createCell(0).setCellValue(rowNumber);
                    // Write all lead fields + single product
                    writeLeadRow(row, lead);
                    // Add comments once per row
                    writeComments(sheet, drawing, rowIndex, errors,errorStyle );
                    rowIndex++;

            }
            // Save
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();
            return CompletableFuture.completedFuture(out.toByteArray());
        }
        catch (Exception exception){
            log.error("Error writing error file: {}", exception.getMessage());
            log.error("Exit : LeadExcelHelper.generateErrorExcelFromJson --->exception ");
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
    }

    /**
     * Writes data of a Lead instance into a specified Excel row.
     * Populates the row with the Lead's personal and business details,
     * and denotes product interest with "Yes" or "No" for each product column.
     *
     * @param row the Excel Row object where Lead data will be written
     * @param lead the Lead object containing data to be written into the row
     * @author Akshay Jadhav
     */
    private void writeLeadRow(Row row, Lead lead) {
        row.createCell(1).setCellValue(lead.getFirstName());
        row.createCell(2).setCellValue(lead.getLastName());
        row.createCell(3).setCellValue(lead.getMobileNumber());
        row.createCell(4).setCellValue(lead.getEmail());
        row.createCell(5).setCellValue(lead.getGstin());

        // Convert Set<Product> → Set<String> module names
        Set<String> selectedModules = lead.getInterestedProducts()
                .stream()
                .map(Product::getProductName)
                .collect(Collectors.toSet());

        // Write Yes/No columns for products
        for (ProductColumn pc : ProductColumn.values()) {
            boolean selected = selectedModules.contains(pc.getModuleName());
            row.createCell(pc.getColumnIndex()).setCellValue(selected ? "Yes" : "No");
        }

        row.createCell(13).setCellValue(lead.getBusinessAddress());
        row.createCell(14).setCellValue(lead.getDescription());
    }

    /**
     * Adds error comments to specific cells in a given Excel sheet row based on the provided error details.
     * The method adds comments for predefined fields such as first name, last name, mobile number, email, GSTIN,
     * and optionally for all product module columns if there are errors for "interestedModules".
     * It customizes the comments with the provided cell style.
     *
     * @param sheet the {@link Sheet} object representing the Excel sheet where comments are to be added.
     * @param drawing the {@link Drawing} object used for adding cell comments in the sheet.
     * @param rowIndex the index of the row in the sheet where comments need to be added.
     * @param errors a map containing error messages for different fields. The keys correspond to the field names.
     * @param style the {@link CellStyle} to be applied to cells for which comments are added.
     * @author Akshay Jadhav
     */
    private void writeComments(Sheet sheet, Drawing<?> drawing, int rowIndex, Map<String, String> errors, CellStyle style) {

        addComment(sheet, drawing, rowIndex, 1, errors.get("firstName"), style);
        addComment(sheet, drawing, rowIndex, 2, errors.get("lastName"), style);
        addComment(sheet, drawing, rowIndex, 3, errors.get("mobileNumber"), style);
        addComment(sheet, drawing, rowIndex, 4, errors.get("email"), style);
        addComment(sheet, drawing, rowIndex, 5, errors.get("gstin"), style);

        // For module errors add comment to ALL YES/NO columns
        if (errors.get("interestedModules") != null) {
            for (ProductColumn pc : ProductColumn.values()) {
                addComment(sheet, drawing, rowIndex, pc.getColumnIndex(), errors.get("interestedModules"), style);
            }
        }
        addComment(sheet, drawing, rowIndex, 13, errors.get("businessAddress"), style);
        addComment(sheet, drawing, rowIndex, 14, errors.get("description"), style);
    }


    /**
     * Adds a cell comment to a specified cell in an Excel sheet and applies a given {@link CellStyle}.
     * The comment contains the specified text and is anchored to the cell.
     *
     * @param sheet  the {@link Sheet} where the comment is to be added. Must not be null.
     * @param drawing the {@link Drawing} object used to create cell comments. Must not be null.
     * @param row    the row index of the cell where the comment is to be added. Must be non-negative.
     * @param col    the column index of the cell where the comment is to be added. Must be non-negative.
     * @param text   the text content of the comment. If null, no comment is added.
     * @param style  the {@link CellStyle} to be applied to the cell. Must not be null.
     * @author Akshay Jadhav
     */
    private void addComment(Sheet sheet, Drawing<?> drawing, int row, int col, String text,CellStyle style) {
        if (text == null) return;
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(col);
        anchor.setCol2(col + 3);
        anchor.setRow1(row);
        anchor.setRow2(row + 2);
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(text));
        comment.setAuthor("crm system");
        sheet.getRow(row).getCell(col).setCellComment(comment);
        sheet.getRow(row).getCell(col).setCellStyle(style);
    }


}
