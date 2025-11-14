package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.dto.UploadHistoryDto;
import com.example.crm_system_backend.entity.UploadHistory;
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


    private final UploadHistoryService uploadHistoryService;

    ModelMapper modelMapper;

    @GetMapping("/{email}")
    public ResponseEntity<List<UploadHistoryDto>> getUploadHistoryByUser(@PathVariable String email){
     List<UploadHistoryDto>  uploadHistoryDtos =  uploadHistoryService.findByUser(email).stream().map( uploadHistory ->
     {
         return modelMapper.map(uploadHistory,UploadHistoryDto.class);
     }).toList();

        return new ResponseEntity<>(uploadHistoryDtos, HttpStatus.OK);
    }

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
