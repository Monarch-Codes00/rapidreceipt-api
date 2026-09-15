package com.rapidreceipt.common;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    String storeFile(MultipartFile file, Long userId) throws IOException;
}
