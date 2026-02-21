package com.freshmart.web.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * Force UTF-8 for requests/responses so Vietnamese text displays correctly.
 */
public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
