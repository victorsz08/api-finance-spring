package com.appfinace.api.infra;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.appfinace.api.infra.port.StoragePort;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


@Component
public class S3StoragePort implements  StoragePort {
    private final S3Client s3Client;

    private final String bucketName;

    private final String region;

    public S3StoragePort(@Value("${aws.s3.bucketName}") String bucketName, @Value("${aws.region}") String region) {
        this.bucketName = bucketName;
        this.region = region;

        this.s3Client = S3Client.builder()
                    .region(Region.of(this.region))
                    .build();
    }


    @Override
    public String uploadImage(MultipartFile multipartFile) {
        try {
            String originalFileName = multipartFile.getOriginalFilename();
            String extension = originalFileName != null && originalFileName.contains(".") ?
                originalFileName.substring(originalFileName.lastIndexOf(".")) : "";

            String fileName = UUID.randomUUID() + extension;

            return uploadFile(multipartFile.getBytes(), fileName, multipartFile.getContentType());
         } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo de imagem", e);
         }
    }

    public String uploadFile(byte[] file, String fileName, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file));

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }

}
