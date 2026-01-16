package com.notes.dto.request;

import lombok.Data;

@Data
public class SpreadsheetRequest {
    
    private String title;
    private String data;
    private String folderUid;
}
