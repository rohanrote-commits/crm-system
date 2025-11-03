package com.example.crm_system_backend.exception;

import lombok.Getter;

import java.io.IOException;


@Getter
public class FileDownloadException extends IOException {
    private final ErrorCode errorCode;
    public FileDownloadException(ErrorCode errorInFileDownload) {
        this.errorCode = errorInFileDownload;
    }
}
