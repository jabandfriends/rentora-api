package com.rentora.api.controller;

import com.rentora.api.model.dto.ApiResponse;
import com.rentora.api.model.dto.readingContact.response.ContactReadDetailDTO;
import com.rentora.api.service.ReadingContactService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReadingContactController {

    private final ReadingContactService readingContactService;


    @PostMapping("/readcontact")
    public ResponseEntity<ApiResponse<ContactReadDetailDTO>> uploadAndExtractText(@RequestParam("file") MultipartFile file)
            throws TesseractException, IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }

        File tempFile = null;

        try {
            String originalFileName = file.getOriginalFilename();
            String prefix = originalFileName.substring(0, originalFileName.lastIndexOf("."));
            String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));

            tempFile = File.createTempFile(prefix + "_ocr_", suffix);
            file.transferTo(tempFile);

            ContactReadDetailDTO contactReadDetailDTO = readingContactService.extractContractDetails(tempFile);

            return ResponseEntity.ok(ApiResponse.success(contactReadDetailDTO));

        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}