package com.freshmart.web.servlet.staff;

import com.freshmart.entity.Supplier;
import com.freshmart.service.SupplierService;
import com.freshmart.service.SupplierImportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/staff/suppliers")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1 MB
    maxFileSize = 1024 * 1024 * 5,      // 5 MB
    maxRequestSize = 1024 * 1024 * 10   // 10 MB
)
public class SupplierManagementServlet extends HttpServlet {

    private SupplierService supplierService;
    private SupplierImportService importService;

    @Override
    public void init() {
        supplierService = new SupplierService();
        importService = new SupplierImportService();
    }

    // ==========================
    // GET (only for display: list / create form / edit form)
    // ==========================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            listSuppliers(request, response);
            return;
        }

        switch (action) {

            case "create":
                request.setAttribute("supplier", new Supplier());
                request.getRequestDispatcher("/WEB-INF/jsp/staff/supplier_form.jsp")
                        .forward(request, response);
                break;

            case "edit":
                Long editId = Long.parseLong(request.getParameter("id"));
                Supplier supplier = supplierService.getById(editId);
                request.setAttribute("supplier", supplier);

                request.getRequestDispatcher("/WEB-INF/jsp/staff/supplier_form.jsp")
                        .forward(request, response);
                break;

            default:
                listSuppliers(request, response);
        }
    }

    // ==========================
    // POST (Create / Update / Delete)
    // ==========================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String action = request.getParameter("action");

        if (action != null && action.equals("delete")) {
            handleDelete(request, response);
        } else if (action != null && action.equals("import")) {
            handleImport(request, response);
        } else {
            handleCreateOrUpdate(request, response);
        }
    }

    // ==========================
    // Handle IMPORT
    // ==========================
    private void handleImport(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            Part filePart = request.getPart("csvFile");
            
            if (filePart == null || filePart.getSize() == 0) {
                HttpSession session = request.getSession();
                session.setAttribute("errorMessage", "Please select a CSV file to import.");
                response.sendRedirect(request.getContextPath() + "/staff/suppliers");
                return;
            }

            // Check file type
            String fileName = filePart.getSubmittedFileName();
            if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
                HttpSession session = request.getSession();
                session.setAttribute("errorMessage", "Only CSV files are supported.");
                response.sendRedirect(request.getContextPath() + "/staff/suppliers");
                return;
            }

            // Process import
            SupplierImportService.ImportResult result = importService.importFromCsv(filePart.getInputStream());

            // Build result message
            StringBuilder message = new StringBuilder();
            message.append("Import completed: ");
            message.append(result.getSuccessCount()).append(" success, ");
            message.append(result.getErrorCount()).append(" errors ");
            message.append("(Total: ").append(result.getTotalRows()).append(" rows)");

            HttpSession session = request.getSession();
            
            if (result.getErrorCount() > 0) {
                // Show errors
                StringBuilder errorDetail = new StringBuilder();
                errorDetail.append(message).append("<br/><br/><strong>Errors:</strong><ul>");
                for (String error : result.getErrors()) {
                    errorDetail.append("<li>").append(error).append("</li>");
                }
                errorDetail.append("</ul>");
                session.setAttribute("errorMessage", errorDetail.toString());
            } else {
                session.setAttribute("successMessage", message.toString());
            }

            response.sendRedirect(request.getContextPath() + "/staff/suppliers");
            
        } catch (Exception e) {
            HttpSession session = request.getSession();
            session.setAttribute("errorMessage", "Import failed: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/staff/suppliers");
        }
    }

    // ==========================
    // Handle DELETE
    // ==========================
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Long deleteId = Long.parseLong(request.getParameter("id"));
            supplierService.deleteById(deleteId);

            HttpSession session = request.getSession();
            session.setAttribute("successMessage", "Supplier deleted successfully!");
            
            response.sendRedirect(request.getContextPath() + "/staff/suppliers");
        } catch (Exception e) {
            HttpSession session = request.getSession();
            session.setAttribute("errorMessage", "Failed to delete supplier: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/staff/suppliers");
        }
    }

    // ==========================
    // Handle CREATE / UPDATE
    // ==========================
    private void handleCreateOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            String idParam = request.getParameter("id");

            Supplier supplier;

            if (idParam == null || idParam.isEmpty()) {
                supplier = new Supplier();
            } else {
                supplier = supplierService.getById(Long.parseLong(idParam));
            }

            // Read and validate fields
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String address = request.getParameter("address");
            String certificate = request.getParameter("certificate");
            String leadTimeStr = request.getParameter("leadTimeDays");
            String note = request.getParameter("note");

            // Trim data
            if (name != null) name = name.trim();
            if (email != null) email = email.trim();
            if (phone != null) phone = phone.trim();
            if (address != null) address = address.trim();
            if (certificate != null) certificate = certificate.trim();
            if (note != null) note = note.trim();

            // Validation
            StringBuilder errors = new StringBuilder();

            if (name == null || name.isEmpty()) {
                errors.append("Name is required. ");
            }

            if (email == null || email.isEmpty()) {
                errors.append("Email is required. ");
            } else {
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    errors.append("Invalid email format. ");
                }
            }

            if (phone == null || phone.isEmpty()) {
                errors.append("Phone is required. ");
            } else {
                // allow digits, space, plus, hyphen, parentheses
                if (!phone.matches("^[0-9+\\-\\s()]+$")) {
                    errors.append("Phone can only contain digits and +-() spaces. ");
                } else {
                    String digits = phone.replaceAll("[^0-9]", "");
                    if (digits.length() < 9 || digits.length() > 15) {
                        errors.append("Phone number must have between 9 and 15 digits. ");
                    }
                }
            }

            if (leadTimeStr != null && !leadTimeStr.trim().isEmpty()) {
                try {
                    int leadTime = Integer.parseInt(leadTimeStr.trim());
                    if (leadTime <= 0) {
                        errors.append("Lead time must be a positive number. ");
                    }
                } catch (NumberFormatException e) {
                    errors.append("Lead time must be a valid number. ");
                }
            }

            // If validation fails, forward back to form with errors
            if (errors.length() > 0) {
                request.setAttribute("supplier", supplier);
                request.setAttribute("errors", errors.toString().trim());
                // Keep user input
                supplier.setName(name);
                supplier.setEmail(email);
                supplier.setPhone(phone);
                supplier.setAddress(address);
                supplier.setCertificate(certificate);
                supplier.setNote(note);
                if (leadTimeStr != null && !leadTimeStr.trim().isEmpty()) {
                    try {
                        supplier.setLeadTimeDays(Integer.parseInt(leadTimeStr.trim()));
                    } catch (NumberFormatException e) {
                        supplier.setLeadTimeDays(1);
                    }
                }
                request.getRequestDispatcher("/WEB-INF/jsp/staff/supplier_form.jsp").forward(request, response);
                return;
            }

            // Set validated data
            supplier.setName(name);
            supplier.setEmail(email);
            supplier.setPhone(phone);
            supplier.setAddress(address);
            supplier.setCertificate(certificate);
            if (leadTimeStr != null && !leadTimeStr.trim().isEmpty()) {
                supplier.setLeadTimeDays(Integer.parseInt(leadTimeStr.trim()));
            }
            supplier.setNote(note);

            supplierService.save(supplier);

            HttpSession session = request.getSession();
            session.setAttribute("successMessage", "Supplier saved successfully!");
            
            response.sendRedirect(request.getContextPath() + "/staff/suppliers");
        } catch (Exception e) {
            HttpSession session = request.getSession();
            session.setAttribute("errorMessage", "Failed to save supplier: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/staff/suppliers");
        }
    }

    // ==========================
    // LIST
    // ==========================
    private void listSuppliers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // --- read search/filter/paging parameters with safe defaults ---
        String q = request.getParameter("q");
        if (q != null) q = q.trim();
        if (q != null && q.isEmpty()) q = null;
        String certificate = request.getParameter("certificate");
        if (certificate != null) certificate = certificate.trim();
        if (certificate != null && certificate.isEmpty()) certificate = null;

        // date range filters
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        LocalDate fromDate = null;
        LocalDate toDate = null;
        try {
            if (fromDateStr != null && !fromDateStr.isEmpty()) {
                fromDate = LocalDate.parse(fromDateStr);
            }
        } catch (DateTimeParseException e) {
            // ignore invalid input
        }
        try {
            if (toDateStr != null && !toDateStr.isEmpty()) {
                toDate = LocalDate.parse(toDateStr);
            }
        } catch (DateTimeParseException e) {
            // ignore invalid input
        }

        int page = 1;
        String pageParam = request.getParameter("page");
        try {
            if (pageParam != null) {
                page = Integer.parseInt(pageParam);
            }
        } catch (NumberFormatException e) {
            // ignore and keep default
        }
        if (page < 1) page = 1;

        final int pageSize = 10; // may adjust later or make configurable

        // --- statistics for header cards (always global, not affected by filters) ---
        // these values are also used by the new charts on supplier_list.jsp
        long totalSuppliers = supplierService.totalSuppliers();
        long withCert = supplierService.countWithCertificate();
        long withoutCert = supplierService.countWithoutCertificate();
        double avgLead = supplierService.averageLeadTime();
        java.util.List<SupplierService.SupplierProductCount> topSuppliers =
                supplierService.topSuppliersByProductCount(5);

        // search result + paging
        List<Supplier> suppliers = supplierService.search(q, certificate, fromDate, toDate, page, pageSize);
        long total = supplierService.count(q, certificate, fromDate, toDate);
        int totalPages = (int) ((total + pageSize - 1) / pageSize);

        request.setAttribute("suppliers", suppliers);
        request.setAttribute("statsTotal", totalSuppliers);
        request.setAttribute("statsWithCert", withCert);
        request.setAttribute("statsWithoutCert", withoutCert);
        request.setAttribute("statsAvgLead", avgLead);
        request.setAttribute("topSuppliers", topSuppliers);
        request.setAttribute("search", q);
        request.setAttribute("certificateFilter", certificate);
        request.setAttribute("fromDate", fromDateStr);
        request.setAttribute("toDate", toDateStr);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", total);
        request.setAttribute("pageSize", pageSize);

        request.getRequestDispatcher("/WEB-INF/jsp/staff/supplier_list.jsp")
                .forward(request, response);
    }
}