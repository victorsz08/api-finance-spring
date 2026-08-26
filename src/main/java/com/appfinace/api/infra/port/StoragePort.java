package com.appfinace.api.infra.port;

import org.springframework.web.multipart.MultipartFile;



public interface StoragePort {
    String uploadImage(MultipartFile multipartFile);
}
