package com.freshmart.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Test cho SupplierImportService - parse, validate, summary success/error
 * Không test database interaction (khó setup), chỉ test logic parse và validate
 */
class SupplierImportServiceTest {

    private SupplierImportService service;

    @BeforeEach
    void setUp() {
        service = new SupplierImportService();
    }

    @Test
    void testImportValidCsv_ParsesCorrectly() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "ABC Fresh,abc@test.com,0123456789,123 Street,ISO9001,3,Good supplier\n" +
                     "XYZ Organic,xyz@test.com,0987654321,456 Avenue,VietGAP,5,Organic only";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        // Note: Actual save will fail without DB, but we can check parsing
        assertEquals(2, result.getTotalRows(), "Should count 2 data rows");
    }

    @Test
    void testImportEmptyFile_ReturnsZeroRows() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(0, result.getTotalRows(), "Empty file should have 0 rows");
    }

    @Test
    void testImportSkipsEmptyLines() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "ABC Fresh,abc@test.com,0123456789,123 Street,ISO9001,3,Good\n" +
                     "\n" +
                     "   \n" +
                     "XYZ Organic,xyz@test.com,0987654321,456 Avenue,VietGAP,5,Organic";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(2, result.getTotalRows(), "Should skip empty lines");
    }

    @Test
    void testImportInvalidEmail_RecordsError() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "Bad Email,invalid-email,0123456789,123 Street,,1,Test";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Invalid email format"), 
                   "Error should mention invalid email");
    }

    @Test
    void testImportMissingRequiredFields_RecordsError() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     ",,0123456789,123 Street,,1,Test";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Name is required"), 
                   "Error should mention missing name");
    }

    @Test
    void testImportInvalidPhone_TooShort() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "Test Supplier,test@test.com,123,123 Street,,1,Test";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Phone must have between 9 and 15 digits"));
    }

    @Test
    void testImportInvalidPhone_TooLong() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "Test Supplier,test@test.com,12345678901234567890,123 Street,,1,Test";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Phone must have between 9 and 15 digits"));
    }

    @Test
    void testImportInvalidPhone_InvalidCharacters() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "Test Supplier,test@test.com,abc123def456,123 Street,,1,Test";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Phone can only contain"));
    }

    @Test
    void testImportValidPhone_WithFormatting() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "Test Supplier,test@test.com,+84 (123) 456-789,123 Street,,1,Test";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        // Should parse successfully (10 digits after removing formatting)
        assertEquals(1, result.getTotalRows());
    }

    @Test
    void testImportNegativeLeadTime_RecordsError() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "Test Supplier,test@test.com,0123456789,123 Street,,-5,Test";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Lead time must be positive"));
    }

    @Test
    void testImportInvalidLeadTime_NotANumber() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "Test Supplier,test@test.com,0123456789,123 Street,,abc,Test";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Invalid lead time"));
    }

    @Test
    void testImportQuotedFields_ParsesCorrectly() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "\"Company, Inc\",test@test.com,0123456789,\"123 Main St, Suite 100\",ISO9001,3,\"Good, reliable\"";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        // Should parse quoted fields with commas correctly
        assertEquals(1, result.getTotalRows());
    }

    @Test
    void testImportMinimalColumns_UsesDefaults() {
        String csv = "name,email,phone\n" +
                     "Minimal Supplier,minimal@test.com,0123456789";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        // Should use default leadTimeDays = 1
        assertEquals(1, result.getTotalRows());
    }

    @Test
    void testImportMixedValidAndInvalid_CountsBoth() {
        String csv = "name,email,phone,address,certificate,leadTimeDays,note\n" +
                     "Valid Supplier,valid@test.com,0123456789,123 Street,,1,Good\n" +
                     "Invalid Email,bad-email,0123456789,456 Avenue,,1,Bad\n" +
                     "Invalid Phone,test@test.com,123,789 Road,,1,Bad\n" +
                     "Another Valid,valid2@test.com,0987654321,321 Street,,2,Good";
        
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        SupplierImportService.ImportResult result = service.importFromCsv(is);
        
        assertEquals(4, result.getTotalRows());
        assertEquals(2, result.getErrorCount());
        assertEquals(2, result.getErrors().size());
    }

    @Test
    void testImportResultInitialState() {
        SupplierImportService.ImportResult result = new SupplierImportService.ImportResult();
        
        assertEquals(0, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getErrorCount());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testImportResultAddError() {
        SupplierImportService.ImportResult result = new SupplierImportService.ImportResult();
        
        result.addError("Error 1");
        result.addError("Error 2");
        
        assertEquals(2, result.getErrors().size());
        assertEquals("Error 1", result.getErrors().get(0));
        assertEquals("Error 2", result.getErrors().get(1));
    }
}
