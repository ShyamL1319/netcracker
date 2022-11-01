package com.netcracker.netcracker.impl;

import com.netcracker.netcracker.dto.FileDto;
import com.netcracker.netcracker.entity.InputFile;
import com.netcracker.netcracker.exception.BadRequestException;
import com.netcracker.netcracker.exception.GCPFileUploadException;
import com.netcracker.netcracker.repository.FileRepository;
import com.netcracker.netcracker.service.FileService;
import com.netcracker.netcracker.util.DataBucketUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Component
public class FileServiceImpl implements FileService{

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private DataBucketUtil dataBucketUtil;

    public InputFile uploadFiles(MultipartFile file, String fileName) {

        LOGGER.debug("Start file uploading service "+ file);
        InputFile inputFiles = new InputFile();

        String originalFileName = file.getOriginalFilename();
        if(originalFileName == null){
            throw new BadRequestException("Original file name is null");
        }
        Path path = new File(originalFileName).toPath();

        try {
            String contentType = Files.probeContentType(path);
            FileDto fileDto = dataBucketUtil.uploadFile(file, fileName, contentType);

            if (fileDto != null) {
                inputFiles = new InputFile(fileDto.getFileName().replace("files/",""), fileDto.getFileUrl());
                LOGGER.debug("File uploaded successfully, file name: {} and url: {}",fileDto.getFileName(), fileDto.getFileUrl() );
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while uploading. Error: ", e);
            throw new GCPFileUploadException("Error occurred while uploading");
        }
        fileRepository.save(inputFiles);
        LOGGER.debug("File details successfully saved in the database");
        return inputFiles;
    }

    @Override
    public ArrayList<InputFile> getAllFiles() {
        try{
            return (ArrayList<InputFile>) fileRepository.findAll();
        }catch (Exception ex){
            throw new GCPFileUploadException("Files Not Found");
        }

    }
}
