package com.ashokit.SpringBoot_AWS_Integration.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.ashokit.SpringBoot_AWS_Integration.config.AwsBucketDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AwsFileServiceImpl implements  AwsFileService{

    @Autowired
    private AwsBucketDetails awsBucketDetails;

    private final AmazonS3 amazonS3;

    public AwsFileServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String saveFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        int count = 0;
        int maxTries = 3;
        while(true) {
            try {
                //converting multipart file object into File object
                File file1 = convertMultiPartToFile(file);

                //sending file object into amazon s3 service
                PutObjectResult putObjectResult = amazonS3.putObject(awsBucketDetails.getBucketName(), originalFilename, file1);

                //returning the hash for uploaded file object
                //return putObjectResult.getContentMd5();

                //returning the pre-signed url for uploaded file object
                return generatePreSignedUrl(originalFilename,30);
            } catch (IOException e) {
                if (++count == maxTries) throw new RuntimeException(e);
            }
        }

    }

    @Override
    public byte[] downloadFile(String filename) {
        //Getting S3 object
        S3Object object = amazonS3.getObject(awsBucketDetails.getBucketName(), filename);

        S3ObjectInputStream objectContent = object.getObjectContent();
        try {
            return IOUtils.toByteArray(objectContent);
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    @Override
    public String deleteFile(String filename) {
        amazonS3.deleteObject(awsBucketDetails.getBucketName(),filename);
        return "File deleted";
    }

    @Override
    public List<String> listAllFiles() {
        ListObjectsV2Result listObjectsV2Result = amazonS3.listObjectsV2(awsBucketDetails.getBucketName());
        return listObjectsV2Result.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());

    }

    private File convertMultiPartToFile(MultipartFile file ) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public String generatePreSignedUrl(String fileName, int expirationMinutes) {

        // Set expiration time
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += (long) expirationMinutes * 60 * 1000;  // Convert minutes to milliseconds
        expiration.setTime(expTimeMillis);

        // Generate pre-signed URL request
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(awsBucketDetails.getBucketName(), fileName)
                        .withMethod(HttpMethod.GET)  // Method: GET for downloading
                        .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }
}