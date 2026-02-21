package com.freshmart.web.servlet;

import com.freshmart.entity.Product;
import com.freshmart.repository.ProductRepository;
import com.freshmart.service.InventoryService;
import com.freshmart.util.JpaExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet(urlPatterns = {"/product"})
public class ProductDetailServlet extends HttpServlet {

    private final JpaExecutor executor = new JpaExecutor();
    private final ProductRepository productRepo = new ProductRepository();
    private final InventoryService inventoryService = new InventoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idRaw = req.getParameter("id");
        if (idRaw == null) {
            resp.sendError(400, "Missing id");
            return;
        }

        Long id = Long.parseLong(idRaw);

        executor.execute(em -> {
            Product p = productRepo.findById(em, id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            int available = inventoryService.getAvailableQty(em, id, LocalDate.now());
            req.setAttribute("product", p);
            req.setAttribute("availableQty", available);
            return null;
        });

        req.getRequestDispatcher("/WEB-INF/jsp/catalog/product_detail.jsp").forward(req, resp);
    }
}
