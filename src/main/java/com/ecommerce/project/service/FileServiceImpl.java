package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{
    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String uniqueId = UUID.randomUUID().toString();
        String fileName = "";
        try{
            fileName = uniqueId.concat(originalFileName.substring(originalFileName.lastIndexOf(".")));
        }catch (StringIndexOutOfBoundsException e){
            throw new IOException("Invalid multipart file");
        }
        String filePath = path + File.separator+fileName;

        File folder = new File(path);
        if(!folder.exists()){
            folder.mkdir();
        }
        Files.copy(file.getInputStream(), Path.of(filePath));

        return fileName;
    }
}
