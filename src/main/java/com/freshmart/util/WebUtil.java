package com.freshmart.util;

import jakarta.servlet.http.HttpServletRequest;

public final class WebUtil {
    private WebUtil() {}

    public static String contextPath(HttpServletRequest req) {
        return req.getContextPath() == null ? "" : req.getContextPath();
    }

    public static String fullPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String qs = req.getQueryString();
        return qs == null ? uri : uri + "?" + qs;
    }
}
