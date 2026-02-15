package com.freshmart.web.filter;

import com.freshmart.config.AppConstants;
import com.freshmart.util.WebUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authentication filter: requires login for protected routes.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        Object user = request.getSession().getAttribute(AppConstants.SESSION_USER);
        if (user == null) {
            String returnUrl = WebUtil.fullPath(request);
            response.sendRedirect(WebUtil.contextPath(request) + "/login?return=" + java.net.URLEncoder.encode(returnUrl, "UTF-8"));
            return;
        }
        chain.doFilter(req, resp);
    }
}
