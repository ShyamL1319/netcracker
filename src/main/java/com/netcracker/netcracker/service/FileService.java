package com.netcracker.netcracker.service;

import com.netcracker.netcracker.entity.InputFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public interface FileService {

    public InputFile uploadFiles(MultipartFile file, String fileName);

    public ArrayList<InputFile> getAllFiles() throws FileNotFoundException;
}
