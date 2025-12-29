package com.notes.service;

import com.notes.dto.request.FolderRequest;
import com.notes.dto.response.FolderDTO;

import java.util.List;

public interface FolderService {
    
    List<FolderDTO> getFolderTree();
    
    FolderDTO getFolder(String uid);
    
    FolderDTO createFolder(FolderRequest request);
    
    FolderDTO updateFolder(String uid, FolderRequest request);
    
    void deleteFolder(String uid);
    
    void moveFolder(String uid, String newParentUid);
}



