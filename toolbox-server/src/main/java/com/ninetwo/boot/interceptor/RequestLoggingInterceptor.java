package com.ninetwo.boot.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    private static final String START_TIME_ATTR = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTR, startTime);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = request.getRemoteAddr();

        logger.info("========== Request Start ==========");
        logger.info("API: {} {}", method, uri);
        if (queryString != null) {
            logger.info("QueryString: {}", queryString);
        }
        logger.info("Client IP: {}", remoteAddr);

        // 打印请求参数（form-data / query params）
        logRequestParams(request);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 可以在这里处理响应前的逻辑
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        logger.info("API: {} {}", method, uri);
        logger.info("Status: {}", status);
        logger.info("Execution Time: {} ms", executionTime);
        logger.info("========== Request End ==========\n");

        if (ex != null) {
            logger.error("Request Error: ", ex);
        }
    }

    private void logRequestParams(HttpServletRequest request) {
        // 打印普通表单参数
        java.util.Map<String, String[]> params = request.getParameterMap();
        if (!params.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            params.forEach((key, values) -> {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                // 过滤掉文件类型的参数，只打印普通参数
                if (!key.equals("file")) {
                    sb.append(key).append("=").append(String.join(",", values));
                } else {
                    sb.append(key).append("=[File]");
                }
            });
            logger.info("Params: {}", sb.toString());
        }
    }
}
