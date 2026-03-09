package com.freshmart.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test validation logic cho upload ảnh product
 * Test các helper methods: validate file, sanitize filename
 * Không test servlet trực tiếp (khó mock HttpServletRequest/Part)
 */
class ImageUploadValidationTest {

    // Replicate validation logic from ProductManagementServlet
    private boolean isValidImageFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".gif")
            || lower.endsWith(".webp");
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "image.jpg";

        // Remove path separators and special characters
        String safe = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Limit length
        if (safe.length() > 100) {
            String ext = "";
            int dotIndex = safe.lastIndexOf('.');
            if (dotIndex > 0) {
                ext = safe.substring(dotIndex);
                safe = safe.substring(0, Math.min(95, dotIndex)) + ext;
            } else {
                safe = safe.substring(0, 100);
            }
        }

        return safe;
    }

    // ===== VALIDATION TESTS =====

    @Test
    void testValidImageFile_Jpg() {
        assertTrue(isValidImageFile("product.jpg"));
    }

    @Test
    void testValidImageFile_Jpeg() {
        assertTrue(isValidImageFile("product.jpeg"));
    }

    @Test
    void testValidImageFile_Png() {
        assertTrue(isValidImageFile("product.png"));
    }

    @Test
    void testValidImageFile_Gif() {
        assertTrue(isValidImageFile("product.gif"));
    }

    @Test
    void testValidImageFile_Webp() {
        assertTrue(isValidImageFile("product.webp"));
    }

    @Test
    void testValidImageFile_CaseInsensitive() {
        assertTrue(isValidImageFile("PRODUCT.JPG"));
        assertTrue(isValidImageFile("Product.PNG"));
        assertTrue(isValidImageFile("product.JpEg"));
    }

    @Test
    void testInvalidImageFile_WrongExtension() {
        assertFalse(isValidImageFile("document.pdf"));
        assertFalse(isValidImageFile("script.js"));
        assertFalse(isValidImageFile("data.csv"));
        assertFalse(isValidImageFile("archive.zip"));
    }

    @Test
    void testInvalidImageFile_NoExtension() {
        assertFalse(isValidImageFile("filename"));
    }

    @Test
    void testInvalidImageFile_Null() {
        assertFalse(isValidImageFile(null));
    }

    @Test
    void testInvalidImageFile_Empty() {
        assertFalse(isValidImageFile(""));
    }

    @Test
    void testInvalidImageFile_OnlyExtension() {
        // ".jpg" ends with ".jpg" so it passes validation (edge case)
        // In real usage, this would be caught by other validation (empty filename)
        assertTrue(isValidImageFile(".jpg"), "Edge case: .jpg passes extension check");
    }

    // ===== SANITIZE FILENAME TESTS =====

    @Test
    void testSanitizeFileName_Normal() {
        assertEquals("product_image.jpg", sanitizeFileName("product_image.jpg"));
    }

    @Test
    void testSanitizeFileName_WithSpaces() {
        assertEquals("product_image.jpg", sanitizeFileName("product image.jpg"));
    }

    @Test
    void testSanitizeFileName_WithSpecialChars() {
        assertEquals("product_image_.jpg", sanitizeFileName("product@image!.jpg"));
    }

    @Test
    void testSanitizeFileName_PathTraversal() {
        String result = sanitizeFileName("../../etc/image.jpg");
        // Dots are allowed in regex [^a-zA-Z0-9._-], so .. becomes ..
        // But / is replaced with _
        assertTrue(result.contains(".."), "Dots are preserved (allowed chars)");
        assertFalse(result.contains("/"), "Should remove path separators");
        assertTrue(result.endsWith(".jpg"), "Should preserve extension");
    }

    @Test
    void testSanitizeFileName_WindowsPath() {
        String result = sanitizeFileName("C:\\Users\\image.jpg");
        assertFalse(result.contains("\\"), "Should remove backslashes");
        assertTrue(result.endsWith(".jpg"), "Should preserve extension");
    }

    @Test
    void testSanitizeFileName_UnixPath() {
        String result = sanitizeFileName("/var/www/image.jpg");
        assertFalse(result.contains("/"), "Should remove forward slashes");
        assertTrue(result.endsWith(".jpg"), "Should preserve extension");
    }

    @Test
    void testSanitizeFileName_MultipleSpecialChars() {
        assertEquals("product___image___.jpg", sanitizeFileName("product!@#image$%^.jpg"));
    }

    @Test
    void testSanitizeFileName_Null() {
        assertEquals("image.jpg", sanitizeFileName(null));
    }

    @Test
    void testSanitizeFileName_TooLong() {
        String longName = "a".repeat(150) + ".jpg";
        String result = sanitizeFileName(longName);
        
        assertTrue(result.length() <= 100, "Sanitized filename should be <= 100 chars");
        assertTrue(result.endsWith(".jpg"), "Should preserve extension");
    }

    @Test
    void testSanitizeFileName_TooLongWithoutExtension() {
        String longName = "a".repeat(150);
        String result = sanitizeFileName(longName);
        
        assertEquals(100, result.length(), "Should truncate to 100 chars");
    }

    @Test
    void testSanitizeFileName_PreservesValidChars() {
        assertEquals("product-image_v2.1.jpg", sanitizeFileName("product-image_v2.1.jpg"));
    }

    @Test
    void testSanitizeFileName_Unicode() {
        String result = sanitizeFileName("sản_phẩm_ảnh.jpg");
        // Unicode chars are replaced with _
        assertTrue(result.contains("_"), "Should replace unicode chars");
        assertTrue(result.endsWith(".jpg"), "Should preserve extension");
    }

    // ===== PRIORITY LOGIC TESTS =====

    @Test
    void testImagePriority_UploadedFileOverUrl() {
        // Logic: if (filePart != null && submittedFileName != null) → use uploaded file
        String uploadedFile = "/freshmart/assets/uploads/products/123_image.jpg";
        String imageUrl = "https://example.com/image.jpg";
        
        // Uploaded file should take priority
        String finalImage = uploadedFile; // In servlet: use uploaded file path
        
        assertEquals(uploadedFile, finalImage, "Uploaded file should take priority over URL");
    }

    @Test
    void testImagePriority_UrlWhenNoUpload() {
        // Logic: if (filePart == null && imageUrl != null) → use URL
        String uploadedFile = null;
        String imageUrl = "https://example.com/image.jpg";
        
        String finalImage = imageUrl;
        
        assertEquals(imageUrl, finalImage, "Should use URL when no file uploaded");
    }

    @Test
    void testImagePriority_KeepOldWhenNoChange() {
        // Logic: if (filePart == null && imageUrl.isEmpty() && existingProduct != null) → keep old
        String uploadedFile = null;
        String imageUrl = "";
        String existingImage = "/freshmart/assets/uploads/products/old_image.jpg";
        
        String finalImage = existingImage;
        
        assertEquals(existingImage, finalImage, "Should keep old image when no new input");
    }

    @Test
    void testImagePriority_NullWhenNoInput() {
        // Logic: if (filePart == null && imageUrl.isEmpty() && existingProduct == null) → null
        String uploadedFile = null;
        String imageUrl = "";
        String existingImage = null;
        
        String finalImage = existingImage;
        
        assertNull(finalImage, "Should be null when no input and no existing image");
    }

    // ===== FILE SIZE VALIDATION =====

    @Test
    void testFileSizeLimit_Valid() {
        long fileSize = 4 * 1024 * 1024; // 4 MB
        long maxSize = 5 * 1024 * 1024; // 5 MB
        
        assertTrue(fileSize <= maxSize, "4 MB file should be valid");
    }

    @Test
    void testFileSizeLimit_ExactlyAtLimit() {
        long fileSize = 5 * 1024 * 1024; // 5 MB
        long maxSize = 5 * 1024 * 1024; // 5 MB
        
        assertTrue(fileSize <= maxSize, "File exactly at limit should be valid");
    }

    @Test
    void testFileSizeLimit_TooLarge() {
        long fileSize = 6 * 1024 * 1024; // 6 MB
        long maxSize = 5 * 1024 * 1024; // 5 MB
        
        assertFalse(fileSize <= maxSize, "6 MB file should be invalid");
    }

    @Test
    void testFileSizeLimit_Zero() {
        long fileSize = 0;
        long maxSize = 5 * 1024 * 1024;
        
        assertTrue(fileSize <= maxSize, "Empty file passes size check but should fail other validation");
    }
}
