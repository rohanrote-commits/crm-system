package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.beans.InvalidLeadError;
import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.FileDownloadException;
import com.example.crm_system_backend.exception.UploadHistoryException;
import com.example.crm_system_backend.helper.LeadExcelHelper;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@AllArgsConstructor
public class DownloadHandler {


    private static final Logger log = LoggerFactory.getLogger(DownloadHandler.class);
    private final LeadExcelHelper leadExcelHelper;
    private final UploadHistoryService uploadHistoryService;
    private final   ObjectMapper objectMapper;

    /**
     * Downloads the user template Excel file from the application's resources and returns its content as a byte array.
     * @return a byte array containing the data of the user template Excel file
     * @throws FileDownloadException if an error occurs during file retrieval or reading
     */
    public byte[] downloadUserTemplate() throws FileDownloadException {
        ClassPathResource resource = new ClassPathResource("templates/UsersTemplate.xlsx");

        // Read the file into a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);

            }
        } catch (IOException ex) {
            throw new FileDownloadException(ErrorCode.ERROR_IN_FILE_DOWNLOAD);
        }

        byte[] fileBytes = outputStream.toByteArray();

    return fileBytes;
    }

    /**
     * Downloads the lead template Excel file from the application's resources and returns its content as a byte array.
     *
     * @return a byte array containing the data of the lead template Excel file
     * @throws FileDownloadException if an error occurs during file retrieval or reading
     */
    public byte[] downloadLeadTemplate() throws FileDownloadException {
        ClassPathResource resource = new ClassPathResource("templates/Lead Template.xlsx");

        // Read the file into a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);

            }
        } catch (IOException ex) {
            throw new FileDownloadException(ErrorCode.ERROR_IN_FILE_DOWNLOAD);
        }

        byte[] fileBytes = outputStream.toByteArray();

        return fileBytes;
    }

    public File downloadErrorFile(String uploadHistoryId){
        log.info("Enter: DownloadErrorFile.dowloadErrorFile");
        UploadHistory uploadHistory = uploadHistoryService.findById(uploadHistoryId);
        if (uploadHistory.getErrorRecord() == null) {
            log.error("Exception: DownloadHandler.downloadErrorFile ");
            throw new UploadHistoryException(ErrorCode.NO_ERROR_RECORDS);
        }
        try {
            // 1 Read JSON → List<InvalidLeadError>
            List<InvalidLeadError> errorList =
                    objectMapper.readValue(
                            uploadHistory.getErrorRecord(),
                            new TypeReference<List<InvalidLeadError>>() {
                            }
                    );
            File errorFile = leadExcelHelper.generateErrorExcelFromJson(errorList);
            log.info("Exit: DownloadErrorFile.dowloadErrorFile");
            return errorFile;

        }
        catch (JsonProcessingException ex) {
            log.error("JsonProcessingException", ex);
            log.error("Exception: DownloadHanlder.generateErrorExcelFromJson ");
            throw new ExcelException(ErrorCode.ERROR_IN_FILE_DOWNLOAD);
        }
        catch (Exception exception){
            log.error(exception.getMessage(),exception);
            log.error("Exception: DownloadHanlder.generateErrorExcelFromJson ");
            throw new ExcelException(ErrorCode.ERROR_IN_FILE_DOWNLOAD);
        }
    }
}
