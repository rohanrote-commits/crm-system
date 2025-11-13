package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.FileDownloadException;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class DownloadHandler {

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
}
