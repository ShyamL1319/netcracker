package com.netcracker.netcracker.controller;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Storage;
import com.netcracker.netcracker.entity.InputFile;
import com.netcracker.netcracker.exception.GCPFileUploadException;
import com.netcracker.netcracker.service.FileService;
import com.netcracker.netcracker.util.ApiGetResponse;
import com.netcracker.netcracker.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


@RestController()
@RequestMapping("/api")
@RequiredArgsConstructor()
@CrossOrigin(maxAge = 3600, origins = "*")
@Component
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private final FileService fileService;

    @Autowired
    private final Storage storage;

    @PostMapping(path = "/upload",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse> addFile(@RequestParam("file") MultipartFile file, @RequestParam("fileName") String fileName){
        LOGGER.info("Call addFile API"+fileName);
        try {
            HttpHeaders headers = new HttpHeaders();
            InputFile inputFile = fileService.uploadFiles(file,fileName);
            ApiResponse apiResponse = new ApiResponse(HttpStatus.OK.value(), "Success",inputFile);
            ResponseEntity<ApiResponse> tResponseEntity = new ResponseEntity<>(apiResponse,headers, HttpStatus.OK);
            return tResponseEntity;
        }catch(Exception ex){
            HttpHeaders headers = new HttpHeaders();
            InputFile inputFile = fileService.uploadFiles(file, fileName);
            ApiResponse apiResponse = new ApiResponse(HttpStatus.OK.value(), "Success",inputFile);
            ResponseEntity<ApiResponse> tResponseEntity = new ResponseEntity<>(apiResponse,headers, HttpStatus.EXPECTATION_FAILED);
            return  tResponseEntity;
        }

    }

    @GetMapping(path = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiGetResponse> listAllFiles(){
        try {
            HttpHeaders headers = new HttpHeaders();
            ArrayList<InputFile> inputFiles = fileService.getAllFiles();
            ApiGetResponse apiGetResponse = new ApiGetResponse(HttpStatus.OK.value(), "fetched successfully all files", inputFiles);
            return new ResponseEntity<>(apiGetResponse,headers,HttpStatus.OK);
        } catch (Exception e) {
            throw new GCPFileUploadException(e.getMessage());
        }
    }

    @GetMapping(path = "/read")
    public Message readFile() throws IOException {
        StringBuilder sb = new StringBuilder();
        try{
            ReadChannel rc = storage.reader("shyam-1319","files/shyamcv.PDF");
            ByteBuffer bb = ByteBuffer.allocate(1000 * 1024);
            while(rc.read(bb) > 0){
                    bb.flip();
                    String data = new String(bb.array(),0,bb.limit());
                    sb.append(data);
                    bb.clear();
            }
        }catch (Exception ex){

        }

        Message msg = new Message();
        msg.setMessage(sb.toString());
        return msg;
    }

}

class Message {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
