package com.rentora.api.service;

import com.rentora.api.model.dto.readingContact.response.ContactReadDetailDTO;
import com.rentora.api.model.entity.Contract;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReadingContactService {

    @Value("${tessdata.path:/usr/share/tesseract-ocr/5/tessdata}")
    private String tessDataPath;

    public ContactReadDetailDTO extractContractDetails(File imageFile) throws TesseractException {

        ITesseract tesseract = new Tesseract();

        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage("eng");

        String ocrResult = tesseract.doOCR(imageFile);

        String cleanedText = ocrResult.replaceAll("\\s*:\\s*", ": ").trim();
        cleanedText = cleanedText.replaceAll("[^\\S\\r\\n]+", " ");

        ContactReadDetailDTO contractDetails = new ContactReadDetailDTO();

        contractDetails.setFirstName(extractValue(cleanedText, "First Name"));
        contractDetails.setLastName(extractValue(cleanedText, "Last Name"));
        contractDetails.setEmail(extractValue(cleanedText, "Email"));
        contractDetails.setPhoneNumber(extractValue(cleanedText, "Phone Number"));
        contractDetails.setNationalId(extractValue(cleanedText, "National ID"));
        contractDetails.setEmergencyContactName(extractValue(cleanedText, "Emergency Contact Name"));
        contractDetails.setEmergencyContactPhone(extractValue(cleanedText, "Emergency Contact Phone"));

        String dobStr = extractValue(cleanedText, "Date of Birth");
        contractDetails.setDateOfBirth(parseLocalDate(dobStr));

        return contractDetails;
    }

    private String extractValue(String text, String keyword) {
        String patternString = keyword + "\\s*([^\\n\\r]*)\\s*";

        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", value);
            return null;
        }
    }



}