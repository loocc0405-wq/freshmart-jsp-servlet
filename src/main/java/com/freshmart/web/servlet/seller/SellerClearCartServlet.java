package com.freshmart.web.servlet.seller;

import com.freshmart.config.AppConstants;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/seller/pos/clear"})
public class SellerClearCartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.getSession().removeAttribute(AppConstants.SESSION_SELLER_CART);
        resp.sendRedirect(req.getContextPath() + "/seller/pos");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }
}
