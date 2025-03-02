package com.ashokit.SpringBoot_AWS_Integration.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {

    @Autowired
    private AwsAccountDetails awsAccountDetails;

    @Bean
    public AmazonS3 s3Client() {
        //Preparing the AWSCredentials Object By Passing secret key & access key
        AWSCredentials credentials = new BasicAWSCredentials(awsAccountDetails.getAccessKey(), awsAccountDetails.getSecretKey());

        //Establishing Connectivity to AWS S3 Service
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(awsAccountDetails.getRegion()).build();
    }
}
