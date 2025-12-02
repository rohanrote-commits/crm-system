package com.example.crm_system_backend.entity;

import com.example.crm_system_backend.constants.FileTemplateType;
import com.example.crm_system_backend.constants.UploadStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * Represents the history of file uploads in the system.
 * This entity captures detailed information about each file
 * upload process including metadata, status, and error details.
 *
 * Fields:
 * - id: A unique identifier for the upload history record.
 * - fileName: The name of the file that was uploaded.
 * - uploadedAt: The timestamp of when the file was uploaded.
 * - updatedAt: The timestamp of the last update made to this record.
 * - uploadedBy: The username or identifier of the user who uploaded the file.
 * - totalRecords: The total number of records contained in the uploaded file.
 * - uploadStatus: The status of the upload process (e.g., FAILED, PROCESSING, SUCCESS, PARTIALLY_SUCCESS).
 * - validRecords: The count of records that were considered valid out of the uploaded file.
 * - invalidRecords: The count of records that were considered invalid out of the uploaded file.
 * - errorFileName: The name of the file containing detailed information about the errors in the upload.
 * - fileTemplateType: The type of file template being used (e.g., LEAD, USER, REPORT).
 * - errorRecord: A JSON representation of error details for invalid records, if any.
 *
 * Database Entity:
 * - Annotated as a JPA entity for persistence.
 * - The id field is auto-generated using the UUID strategy.
 * - errorRecord column is stored as a JSON in the database.
 *
 * Enumerated Fields:
 * - uploadStatus and fileTemplateType are stored as strings in the database.
 *
 * This class is a data-carrying object for managing and tracking file upload details in the system.
 *
 * Author: Akshay Jadhav
 */
@Entity
@Data
public class UploadHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
   private String id;
   private String fileName;
   private LocalDateTime uploadedAt;
   private LocalDateTime updatedAt;
   private String uploadedBy;
   private int totalRecords;
   @Enumerated(EnumType.STRING)
   private UploadStatus uploadStatus;
   private int validRecords;
   private  int invalidRecords;
   private String errorFileName;
   @Enumerated(EnumType.STRING)
   private FileTemplateType fileTemplateType;
    @Column(columnDefinition = "JSON")
   private String errorRecord;

}
