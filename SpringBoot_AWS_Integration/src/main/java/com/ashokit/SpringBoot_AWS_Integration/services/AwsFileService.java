package com.ashokit.SpringBoot_AWS_Integration.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AwsFileService {

    public String saveFile(MultipartFile file);

    public byte[] downloadFile(String filename);

    public String deleteFile(String filename);

    public List<String> listAllFiles();
}
