package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.dto.UploadHistoryDto;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.handler.DownloadHandler;
import com.example.crm_system_backend.handler.UploadedHistoryHandler;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/crm/history")
@AllArgsConstructor
public class UploadHistoryController {



    private final UploadedHistoryHandler uploadedHistoryHandler;

    private  final  ModelMapper modelMapper;
    private final DownloadHandler downloadHandler;
    private static final Logger log = LoggerFactory.getLogger(UploadHistoryController.class);


    /**
     * Retrieves the upload history associated with a specific user based on their email.
     *
     * @param email the email address of the user whose upload history is to be retrieved
     * @return a ResponseEntity containing a list of UploadHistoryDto objects representing the upload history of the user
     */
    @GetMapping("/lead/{email}")
    public ResponseEntity<List<UploadHistoryDto>> getLeadUploadHistoryByUser(@PathVariable String email){
        List<UploadHistoryDto> listOfUploadHistory =   uploadedHistoryHandler.findLeadUploadHistoryByEmail(email);
        return new ResponseEntity<>(listOfUploadHistory, HttpStatus.OK);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<UploadHistoryDto>> getUserUploadHistoryByUser(@PathVariable String email){
        List<UploadHistoryDto> listOfUploadHistory =   uploadedHistoryHandler.findUserUploadHistoryByEmail(email);
        return new ResponseEntity<>(listOfUploadHistory, HttpStatus.OK);
    }

    /**
     * Retrieves the error file specified by the uploadHistoryId from the server.
     *
     * @param uploadHistoryId the name of the file to be retrieved
     * @return a ResponseEntity containing the file as a byte array along with the appropriate
     * HTTP headers and content type for a file download
     */
    @GetMapping("/lead/error/{uploadHistoryId}")
    public ResponseEntity<byte []> getLeadErrorFile(@PathVariable String uploadHistoryId){
        File file = downloadHandler.downloadErrorFile(uploadHistoryId);
        byte[] fileBytes = null;
        try {
            fileBytes = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+file.getName())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileBytes);
    }

    @GetMapping("/user/error/{filename}")
    public ResponseEntity<byte []> getUserErrorFile(@PathVariable String filename){
        File file = new File(filename);
        byte[] fileBytes = null;
        try {
            fileBytes = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileBytes);
    }

}
