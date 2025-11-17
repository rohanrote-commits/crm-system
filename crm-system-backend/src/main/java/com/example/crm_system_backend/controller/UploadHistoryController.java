package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.dto.UploadHistoryDto;
import com.example.crm_system_backend.entity.UploadHistory;
import com.example.crm_system_backend.handler.UploadedHistoryHandler;
import com.example.crm_system_backend.service.serviceImpl.UploadHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
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

    /**
     * Retrieves the upload history associated with a specific user based on their email.
     *
     * @param email the email address of the user whose upload history is to be retrieved
     * @return a ResponseEntity containing a list of UploadHistoryDto objects representing the upload history of the user
     */
    @GetMapping("/{email}")
    public ResponseEntity<List<UploadHistoryDto>> getUploadHistoryByUser(@PathVariable String email){
        List<UploadHistoryDto> listOfUploadHistory =   uploadedHistoryHandler.findLeadUploadHistoryByEmail(email);
        return new ResponseEntity<>(listOfUploadHistory, HttpStatus.OK);
    }

    /**
     * Retrieves the error file specified by the filename from the server.
     *
     * @param filename the name of the file to be retrieved
     * @return a ResponseEntity containing the file as a byte array along with the appropriate
     * HTTP headers and content type for a file download
     */
    @GetMapping("/error/{filename}")
    public ResponseEntity<byte []> getErrorFile(@PathVariable String filename){
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
