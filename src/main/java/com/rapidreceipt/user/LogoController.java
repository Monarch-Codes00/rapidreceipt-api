package com.rapidreceipt.user;

import com.rapidreceipt.common.ApiException;
import com.rapidreceipt.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile/logo")
@RequiredArgsConstructor
public class LogoController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ProfileResponse> uploadLogo(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        
        if (file.isEmpty()) {
            throw new ApiException("Please select a file to upload", HttpStatus.BAD_REQUEST);
        }

        try {
            String logoUrl = fileStorageService.storeFile(file, user.getId());
            
            User freshUser = userRepository.findById(user.getId()).orElseThrow();
            freshUser.setLogoUrl(logoUrl);
            userRepository.save(freshUser);

            return ResponseEntity.ok(profileService.getProfile(freshUser));
        } catch (Exception e) {
            throw new ApiException("Could not upload file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
