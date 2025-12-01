package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.annotations.RoleRequired;
import com.example.crm_system_backend.dto.UploadHistoryDto;
import com.example.crm_system_backend.handler.DownloadHandler;
import com.example.crm_system_backend.handler.UploadedHistoryHandler;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Retrieves the lead upload history for a specific user based on their email.
     *
     * @param email the email address of the user whose lead upload history is to be retrieved
     * @return a ResponseEntity containing a list of UploadHistoryDto objects representing
     *         the lead upload history associated with the user
     *
     * @author Akshay Jadhav
     */
    @GetMapping("/lead/{email}")
    public ResponseEntity<List<UploadHistoryDto>> getLeadUploadHistoryByUser(@PathVariable String email){
        List<UploadHistoryDto> listOfUploadHistory =   uploadedHistoryHandler.findLeadUploadHistoryByEmail(email);
        return new ResponseEntity<>(listOfUploadHistory, HttpStatus.OK);
    }

    /**
     * Retrieves the upload history for a specific user based on their email.
     *
     * @param email the email address of the user whose upload history is to be retrieved
     * @return a ResponseEntity containing a list of UploadHistoryDto objects representing
     *         the upload history associated with the user
     */
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @GetMapping("/user/{email}")
    public ResponseEntity<List<UploadHistoryDto>> getUserUploadHistoryByUser(@PathVariable String email){
        List<UploadHistoryDto> listOfUploadHistory =   uploadedHistoryHandler.findUserUploadHistoryByEmail(email);
        return new ResponseEntity<>(listOfUploadHistory, HttpStatus.OK);
    }

    /**
     * Retrieves the lead error file associated with the given upload history ID.
     *
     * @param uploadHistoryId the unique identifier of the upload history for which the error file is to be retrieved
     * @return a ResponseEntity containing the error file as a byte array along with HTTP headers
     *         and content type for downloading the file
     */
    @GetMapping("/lead/error/{uploadHistoryId}")
    public ResponseEntity<byte []> getLeadErrorFile(@PathVariable String uploadHistoryId){
        byte [] file = downloadHandler.downloadErrorFile(uploadHistoryId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+"Lead_Error_"+uploadHistoryId+".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    /**
     * Retrieves the error file specified by the filename from the server.
     *
     * @param filename the name of the file to be retrieved
     * @return a ResponseEntity containing the file as a byte array along with the appropriate
     * HTTP headers and content type for a file download
     */
    @RoleRequired({"ADMIN", "MASTER_ADMIN"})
    @GetMapping("/user/error/{uploadHistoryId}")
    public ResponseEntity<byte []> getUserErrorFile(@PathVariable String uploadHistoryId){
        byte [] file = downloadHandler.downloadUserErrorFile(uploadHistoryId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+"User_"+uploadHistoryId+".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

}
