package com.freshmart.web.servlet.staff;

import com.freshmart.entity.Supplier;
import com.freshmart.service.SupplierService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/staff/suppliers")
public class SupplierManagementServlet extends HttpServlet {

    private SupplierService supplierService;

    @Override
    public void init() {
        supplierService = new SupplierService();
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
            throws IOException {

        String action = request.getParameter("action");

        if (action != null && action.equals("delete")) {
            handleDelete(request, response);
        } else {
            handleCreateOrUpdate(request, response);
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
            throws IOException {
        try {
            String idParam = request.getParameter("id");

            Supplier supplier;

            if (idParam == null || idParam.isEmpty()) {
                supplier = new Supplier();
            } else {
                supplier = supplierService.getById(Long.parseLong(idParam));
            }

            supplier.setName(request.getParameter("name"));
            supplier.setEmail(request.getParameter("email"));
            supplier.setPhone(request.getParameter("phone"));
            supplier.setAddress(request.getParameter("address"));
            // additional fields
            supplier.setCertificate(request.getParameter("certificate"));
            String leadTime = request.getParameter("leadTimeDays");
            if (leadTime != null && !leadTime.isEmpty()) {
                try {
                    supplier.setLeadTimeDays(Integer.parseInt(leadTime));
                } catch (NumberFormatException nfe) {
                    supplier.setLeadTimeDays(1);
                }
            }
            supplier.setNote(request.getParameter("note"));

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

        List<Supplier> suppliers = supplierService.listAll();
        request.setAttribute("suppliers", suppliers);

        request.getRequestDispatcher("/WEB-INF/jsp/staff/supplier_list.jsp")
                .forward(request, response);
    }
}