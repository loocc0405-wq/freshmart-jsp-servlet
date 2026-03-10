package com.freshmart.service;

import com.freshmart.entity.Supplier;
import com.freshmart.repository.SupplierRepository;
import com.freshmart.util.JpaExecutor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SupplierImportService {
    private final JpaExecutor executor = new JpaExecutor();
    private final SupplierRepository repo = new SupplierRepository();
    private final SupplierService supplierService = new SupplierService();

    public static class ImportResult {
        private int totalRows;
        private int successCount;
        private int errorCount;
        private List<String> errors = new ArrayList<>();

        public int getTotalRows() { return totalRows; }
        public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        public List<String> getErrors() { return errors; }
        public void addError(String error) { this.errors.add(error); }
    }

    public ImportResult importFromCsv(InputStream inputStream) {
        ImportResult result = new ImportResult();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip header
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    Supplier supplier = parseCsvLine(line, lineNumber);
                    
                    // Check if supplier exists by email
                    Supplier existing = executor.execute(em -> repo.findByEmail(em, supplier.getEmail()).orElse(null));
                    
                    if (existing != null) {
                        // Update existing supplier
                        existing.setName(supplier.getName());
                        existing.setPhone(supplier.getPhone());
                        existing.setAddress(supplier.getAddress());
                        existing.setCertificate(supplier.getCertificate());
                        existing.setLeadTimeDays(supplier.getLeadTimeDays());
                        existing.setNote(supplier.getNote());
                        supplierService.save(existing);
                    } else {
                        // Insert new supplier
                        supplierService.save(supplier);
                    }
                    
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    
                } catch (Exception e) {
                    result.setErrorCount(result.getErrorCount() + 1);
                    result.addError("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.addError("File reading error: " + e.getMessage());
        }

        return result;
    }

    private Supplier parseCsvLine(String line, int lineNumber) throws Exception {
        // Simple CSV parser (handles quoted fields)
        List<String> fields = parseCsvFields(line);
        
        if (fields.size() < 3) {
            throw new Exception("Insufficient columns (minimum: name, email, phone)");
        }

        Supplier supplier = new Supplier();
        
        // name (required)
        String name = fields.get(0).trim();
        if (name.isEmpty()) {
            throw new Exception("Name is required");
        }
        supplier.setName(name);

        // email (required)
        String email = fields.get(1).trim();
        if (email.isEmpty()) {
            throw new Exception("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new Exception("Invalid email format: " + email);
        }
        supplier.setEmail(email);

        // phone (required)
        String phone = fields.size() > 2 ? fields.get(2).trim() : "";
        if (phone.isEmpty()) {
            throw new Exception("Phone is required");
        }
        if (!phone.matches("^[0-9+\\-\\s()]+$")) {
            throw new Exception("Phone can only contain digits and +-() spaces");
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < 9 || digits.length() > 15) {
            throw new Exception("Phone must have between 9 and 15 digits");
        }
        supplier.setPhone(phone);

        // address (optional)
        if (fields.size() > 3) {
            supplier.setAddress(fields.get(3).trim());
        }

        // certificate (optional)
        if (fields.size() > 4) {
            supplier.setCertificate(fields.get(4).trim());
        }

        // leadTimeDays (optional, default 1)
        if (fields.size() > 5) {
            String leadTimeStr = fields.get(5).trim();
            if (!leadTimeStr.isEmpty()) {
                try {
                    int leadTime = Integer.parseInt(leadTimeStr);
                    if (leadTime <= 0) {
                        throw new Exception("Lead time must be positive");
                    }
                    supplier.setLeadTimeDays(leadTime);
                } catch (NumberFormatException e) {
                    throw new Exception("Invalid lead time: " + leadTimeStr);
                }
            } else {
                supplier.setLeadTimeDays(1);
            }
        } else {
            supplier.setLeadTimeDays(1);
        }

        // note (optional)
        if (fields.size() > 6) {
            supplier.setNote(fields.get(6).trim());
        }

        return supplier;
    }

    private List<String> parseCsvFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());

        return fields;
    }
}
