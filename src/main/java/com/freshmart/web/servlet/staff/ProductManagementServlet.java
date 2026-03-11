package com.freshmart.web.servlet.staff;

import com.freshmart.entity.Product;
import com.freshmart.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/staff/products")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1MB
    maxFileSize = 1024 * 1024 * 5,      // 5MB
    maxRequestSize = 1024 * 1024 * 10   // 10MB
)
public class ProductManagementServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() {
        productService = new ProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.isBlank()) {
            listProducts(request, response);
            return;
        }

        switch (action) {
            case "add":
                showForm(request, response, null);
                break;

            case "edit": {
                String idStr = request.getParameter("id");
                if (idStr == null || idStr.isBlank()) {
                    request.getSession().setAttribute("flash", "Missing product id.");
                    response.sendRedirect(request.getContextPath() + "/staff/products");
                    return;
                }
                Long id = Long.parseLong(idStr);
                Product product = productService.getById(id);
                if (product == null) {
                    request.getSession().setAttribute("flash", "Product not found.");
                    response.sendRedirect(request.getContextPath() + "/staff/products");
                    return;
                }
                showForm(request, response, product);
                break;
            }

            default:
                listProducts(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        // DELETE bằng POST
        if ("delete".equalsIgnoreCase(action)) {
            deleteProductPost(request, response);
            return;
        }

        // SAVE (create/update)
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String category = request.getParameter("category");
        String unit = request.getParameter("unit");
        String priceStr = request.getParameter("sellPrice");
        String imageUrl = request.getParameter("imageUrl");
        String description = request.getParameter("description");
        String activeParam = request.getParameter("active");

        // Trim data
        if (name != null) name = name.trim();
        if (category != null) category = category.trim();
        if (unit != null) unit = unit.trim();
        if (imageUrl != null) imageUrl = imageUrl.trim();
        if (description != null) description = description.trim();
        boolean active = "on".equalsIgnoreCase(activeParam) || "true".equalsIgnoreCase(activeParam);

        Product product;
        boolean isCreate = (idStr == null || idStr.isBlank());

        if (isCreate) {
            product = new Product();
        } else {
            Long id = Long.parseLong(idStr);
            product = productService.getById(id);
            if (product == null) {
                request.getSession().setAttribute("flash", "Product not found.");
                response.sendRedirect(request.getContextPath() + "/staff/products");
                return;
            }
        }

        // Validation
        StringBuilder errors = new StringBuilder();

        if (name == null || name.isEmpty()) {
            errors.append("Product name is required. ");
        }

        if (category == null || category.isEmpty()) {
            errors.append("Category is required. ");
        }

        if (unit == null || unit.isEmpty()) {
            errors.append("Unit is required. ");
        }

        if (priceStr != null && !priceStr.trim().isEmpty()) {
            try {
                BigDecimal price = new BigDecimal(priceStr.trim());
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    errors.append("Sell price cannot be negative. ");
                }
            } catch (NumberFormatException e) {
                errors.append("Invalid sell price format. ");
            }
        } else {
            errors.append("Sell price is required. ");
        }

        // Handle file upload
        Part filePart = null;
        try {
            filePart = request.getPart("imageFile");
        } catch (Exception e) {
            System.err.println("[UPLOAD] Error getting file part: " + e.getMessage());
        }
        
        String uploadedImagePath = null;

        if (filePart != null && filePart.getSize() > 0) {
            String fileName = getSubmittedFileName(filePart);
            System.out.println("[UPLOAD] File detected: " + fileName + ", size: " + filePart.getSize() + " bytes");

            // Validate file extension
            if (!isValidImageFile(fileName)) {
                errors.append("Invalid image file. Only JPG, PNG, GIF, WEBP are allowed. ");
                System.err.println("[UPLOAD] Invalid file type: " + fileName);
            } else {
                try {
                    uploadedImagePath = saveUploadedFile(filePart, fileName, request);
                    System.out.println("[UPLOAD] Upload successful: " + uploadedImagePath);
                } catch (Exception e) {
                    errors.append("Failed to upload image: " + e.getMessage() + " ");
                    System.err.println("[UPLOAD] Upload failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("[UPLOAD] No file uploaded (filePart is null or size is 0)");
        }

        // validate imageUrl if provided and no file uploaded
        if (uploadedImagePath == null && imageUrl != null && !imageUrl.isEmpty()) {
            try {
                new java.net.URL(imageUrl);
            } catch (Exception e) {
                errors.append("Image URL is not a valid URL. ");
            }
        }

        // If validation fails, forward back to form with errors
        if (errors.length() > 0) {
            request.setAttribute("product", product);
            request.setAttribute("errors", errors.toString().trim());
            // Keep user input
            product.setName(name);
            product.setCategory(category);
            product.setUnit(unit);
            product.setImageUrl(imageUrl);
            product.setDescription(description);
            product.setActive(active);
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                try {
                    product.setSellPrice(new BigDecimal(priceStr.trim()));
                } catch (NumberFormatException e) {
                    product.setSellPrice(BigDecimal.ZERO);
                }
            }
            request.getRequestDispatcher("/WEB-INF/jsp/staff/product_form.jsp").forward(request, response);
            return;
        }

        // Set validated data
        product.setName(name);
        product.setCategory(category);
        product.setUnit(unit);

        // parse sellPrice an toàn
        if (priceStr != null && !priceStr.isBlank()) {
            product.setSellPrice(new BigDecimal(priceStr.trim()));
        }

        // Priority: uploaded file > new imageUrl > keep old image
        if (uploadedImagePath != null) {
            // New file uploaded - use it
            product.setImageUrl(uploadedImagePath);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            // New imageUrl provided - use it
            product.setImageUrl(imageUrl);
        }
        // else: keep existing imageUrl (for edit case, no changes)

        product.setDescription(description);
        product.setActive(active);

        productService.save(product);

        request.getSession().setAttribute("flash",
                isCreate ? "Product created successfully!" : "Product updated successfully!");
        response.sendRedirect(request.getContextPath() + "/staff/products");
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String category = request.getParameter("category");
        String showInactiveParam = request.getParameter("showInactive");
        String pageParam = request.getParameter("page");

        if (keyword != null) keyword = keyword.trim();
        if (category != null) category = category.trim();

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean showInactive = "on".equalsIgnoreCase(showInactiveParam) || "true".equalsIgnoreCase(showInactiveParam);

        // Pagination parameters
        int pageSize = 10; // items per page
        int currentPage = 1;
        
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                currentPage = Integer.parseInt(pageParam);
                if (currentPage < 1) currentPage = 1;
            } catch (NumberFormatException e) {
                currentPage = 1;
            }
        }

        // Get total count
        long totalItems = (hasKeyword || hasCategory)
                ? productService.countSearch(keyword, category, null, showInactive)
                : productService.countAll(showInactive);

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages < 1) totalPages = 1;

        // Handle edge case: currentPage exceeds totalPages
        if (currentPage > totalPages && totalItems > 0) {
            currentPage = totalPages;
        }

        // Get paginated products
        List<Product> products = (hasKeyword || hasCategory)
                ? productService.searchPaginated(keyword, category, null, showInactive, currentPage, pageSize)
                : productService.listAllPaginated(showInactive, currentPage, pageSize);

        // Set attributes for JSP
        request.setAttribute("products", products);
        request.setAttribute("keyword", keyword);
        request.setAttribute("category", category);
        request.setAttribute("showInactive", showInactive);
        
        // Pagination attributes
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("/WEB-INF/jsp/staff/product_list.jsp")
                .forward(request, response);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Product product)
            throws ServletException, IOException {

        request.setAttribute("product", product);
        request.getRequestDispatcher("/WEB-INF/jsp/staff/product_form.jsp")
                .forward(request, response);
    }

    private void deleteProductPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            request.getSession().setAttribute("flash", "Missing product id.");
            response.sendRedirect(request.getContextPath() + "/staff/products");
            return;
        }

        Long id = Long.parseLong(idStr);
        productService.deleteById(id);

        request.getSession().setAttribute("flash", "Product deleted successfully!");
        response.sendRedirect(request.getContextPath() + "/staff/products");
    }

    /**
     * Extract filename from Part header
     */
    private String getSubmittedFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    /**
     * Validate image file extension
     */
    private boolean isValidImageFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".gif")
            || lower.endsWith(".webp");
    }

    /**
     * Save uploaded file to disk and return web-accessible path
     */
    private String saveUploadedFile(Part filePart, String originalFileName, HttpServletRequest request)
            throws IOException {

        System.out.println("[UPLOAD] Starting saveUploadedFile for: " + originalFileName);

        // Generate safe filename: timestamp + sanitized original name
        String timestamp = String.valueOf(System.currentTimeMillis());
        String safeFileName = sanitizeFileName(originalFileName);
        String finalFileName = timestamp + "_" + safeFileName;

        System.out.println("[UPLOAD] Final filename: " + finalFileName);

        // 1. Determine runtime directory (webapps/freshmart/assets/uploads/products/)
        String runtimePath = request.getServletContext().getRealPath("/assets/uploads/products");
        java.io.File runtimeUploadDir = new java.io.File(runtimePath);
        
        System.out.println("[UPLOAD] Runtime directory: " + runtimeUploadDir.getAbsolutePath());
        
        if (!runtimeUploadDir.exists()) {
            boolean created = runtimeUploadDir.mkdirs();
            System.out.println("[UPLOAD] Runtime directory created: " + created);
            if (!created) {
                throw new IOException("Failed to create runtime directory: " + runtimeUploadDir.getAbsolutePath());
            }
        }

        // 2. Write file to runtime directory
        java.io.File runtimeFile = new java.io.File(runtimeUploadDir, finalFileName);
        System.out.println("[UPLOAD] Writing to runtime: " + runtimeFile.getAbsolutePath());
        
        try {
            filePart.write(runtimeFile.getAbsolutePath());
            
            if (runtimeFile.exists() && runtimeFile.length() > 0) {
                System.out.println("[UPLOAD] Runtime write successful, file size: " + runtimeFile.length());
            } else {
                throw new IOException("Runtime file was not written or is empty: " + runtimeFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[UPLOAD] Runtime write failed: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to write file to runtime directory: " + e.getMessage(), e);
        }

        // 3. Copy to source directory for git tracking (hardcoded project path)
        String projectSourcePath = "C:/Users/Admin/prj-ass/freshmart-jsp-servlet/src/main/webapp/assets/uploads/products";
        java.io.File sourceUploadDir = new java.io.File(projectSourcePath);
        System.out.println("[UPLOAD] Source directory: " + sourceUploadDir.getAbsolutePath());
        
        if (!sourceUploadDir.exists()) {
            boolean created = sourceUploadDir.mkdirs();
            System.out.println("[UPLOAD] Source directory created: " + created);
        }

        java.io.File sourceFile = new java.io.File(sourceUploadDir, finalFileName);
        System.out.println("[UPLOAD] Copying to source: " + sourceFile.getAbsolutePath());
        
        try {
            java.nio.file.Files.copy(runtimeFile.toPath(), sourceFile.toPath(), 
                                     java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[UPLOAD] Source copy successful, file size: " + sourceFile.length());
        } catch (Exception e) {
            System.err.println("[UPLOAD] Source copy failed (non-critical): " + e.getMessage());
            e.printStackTrace();
            // Don't throw - source copy failure is not critical if runtime is saved
        }

        // Return web-accessible path
        String webPath = request.getContextPath() + "/assets/uploads/products/" + finalFileName;
        System.out.println("[UPLOAD] Returning web path: " + webPath);
        return webPath;
    }

    /**
     * Sanitize filename to prevent path traversal and special characters
     */
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
}
