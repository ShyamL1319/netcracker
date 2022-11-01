package com.netcracker.netcracker.util;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Identity;
import com.google.cloud.Policy;
import com.google.cloud.storage.*;
import com.netcracker.netcracker.dto.FileDto;
import com.netcracker.netcracker.exception.BadRequestException;
import com.netcracker.netcracker.exception.FileWriteException;
import com.netcracker.netcracker.exception.GCPFileUploadException;
import com.netcracker.netcracker.exception.InvalidFileTypeException;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

@Component()
@Configuration()
@EnableConfigurationProperties()
@ConfigurationProperties()
public class DataBucketUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBucketUtil.class);

    @Value("${gcp.config.file}")
    private String gcpConfigFile;

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${gcp.bucket.id}")
    private String gcpBucketId;

    @Value("${gcp.dir.name}")
    private String gcpDirectoryName;


    public FileDto uploadFile(MultipartFile multipartFile, String fileName, String contentType) {

        try{
            if(multipartFile.getSize() > 1024 * 1024){
                LOGGER.info("Start file uploading process on GCS "+ multipartFile.getSize());
                throw new FileWriteException("File Size is greaterv than 1 MB");
            }
            byte[] fileData = FileUtils.readFileToByteArray(convertFile(multipartFile));

            InputStream inputStream = new ClassPathResource(gcpConfigFile).getInputStream();

            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId)
                    .setCredentials(GoogleCredentials.fromStream(inputStream)).build();

            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId,Storage.BucketGetOption.fields());

            RandomString id = new RandomString(7, ThreadLocalRandom.current());
            Blob blob = bucket.create(gcpDirectoryName + "/" + fileName + "-" + id.nextString() + checkFileExtension(contentType), fileData, contentType);
            //storage.createAcl(blob.getBlobId(), Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
            Policy originalPolicy = storage.getIamPolicy(gcpBucketId);
            storage.setIamPolicy(
                    gcpBucketId,
                    originalPolicy
                            .toBuilder()
                            .addIdentity(StorageRoles.objectViewer(), Identity.allUsers()) // All users can view
                            .build());
//            Bucket bucket = storage.get(bucketName);
//            Acl newDefaultOwner = Acl.of(new User(userEmail), Role.OWNER);

           // bucket.createDefaultAcl(newDefaultOwner);
            if(blob != null){
                LOGGER.debug("File successfully uploaded to GCS");
                return new FileDto(blob.getName(), blob.getMediaLink());
            }

        }catch (Exception e){
            LOGGER.error("An error occurred while uploading data. Exception: ", e);
            throw new GCPFileUploadException("An error occurred while storing data to GCS");
        }
        throw new GCPFileUploadException("An error occurred while storing data to GCS");
    }

    private File convertFile(MultipartFile file) {

        try{
            if(file.getOriginalFilename() == null){
                throw new BadRequestException("Original file name is null");
            }
            File convertedFile = new File(file.getOriginalFilename());
            FileOutputStream outputStream = new FileOutputStream(convertedFile);
            outputStream.write(file.getBytes());
            outputStream.close();
            LOGGER.debug("Converting multipart file : {}", convertedFile);
            return convertedFile;
        }catch (Exception e){
            throw new FileWriteException("An error has occurred while converting the file");
        }
    }

    private String checkFileExtension(String fileName) {
        LOGGER.info(fileName+"  helllo check");
        if(fileName != null && fileName.contains("application/pdf")){


            LOGGER.debug("Accepted file type : pdf");
            return ".pdf";
        }
            LOGGER.error("Not a permitted file type");
            throw new InvalidFileTypeException("Not a permitted file type");

    }
}
