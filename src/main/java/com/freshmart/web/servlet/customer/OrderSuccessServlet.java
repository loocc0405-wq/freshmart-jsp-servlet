package com.freshmart.web.servlet.customer;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.IOException;

/* ===================== */
/*  ADDED IMPORTS START  */
/* ===================== */

import com.freshmart.service.OrderService;
import com.freshmart.entity.Order;

/* ===================== */
/*   ADDED IMPORTS END   */
/* ===================== */

@WebServlet("/customer/order-success")
public class OrderSuccessServlet extends HttpServlet {

    /* ===================== */
    /*   ADDED FIELD START   */
    /* ===================== */

    // Service để lấy order từ database
    private OrderService orderService = new OrderService();

    /* ===================== */
    /*    ADDED FIELD END    */
    /* ===================== */

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /* ===================== */
        /*   ADDED LOGIC START   */
        /* ===================== */

        // Lấy order id từ URL
        String idParam = req.getParameter("id");

        if (idParam != null) {

            try {

                Long orderId = Long.parseLong(idParam);

                // Lấy order từ database
                Order order = orderService.findById(orderId);

                /* ===================== */
                /*   NULL CHECK ADDED    */
                /* ===================== */

                // tránh JSP crash nếu order null
                if (order != null) {
                    req.setAttribute("order", order);
                }

                /* ===================== */
                /*  END NULL CHECK ADDED */
                /* ===================== */

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

        /* ===================== */
        /*    ADDED LOGIC END    */
        /* ===================== */


        // CODE CŨ (GIỮ NGUYÊN)
        req.getRequestDispatcher("/WEB-INF/jsp/customer/order-success.jsp")
           .forward(req, resp);
    }
}