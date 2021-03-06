package com.zhxh.admin.filters;

import com.zhxh.admin.entity.SystemProgram;
import com.zhxh.admin.entity.SystemUser;
import com.zhxh.admin.service.AuthenticateService;
import com.zhxh.admin.service.SystemProgramService;
import com.zhxh.admin.service.SystemUserService;
import com.zhxh.core.env.SysEnv;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ServletComponentScan
@WebFilter(urlPatterns = "/*")
public class LoginFilter implements Filter {
    @Resource(name = "systemUserService")
    private SystemUserService systemUserService;

    @Resource(name = "authenticateService")
    private AuthenticateService authenticateService;

    @Resource(name = "systemProgramService")
    private SystemProgramService systemProgramService;

    @Override
    public void init(FilterConfig filterConfig) {
    }
    /**
     * 获取链接的后缀名
     *
     * @return
     */
    private static String parseSuffix(String url) {
        int index = url.lastIndexOf(".");
        if(index >0){
            return url.substring(index +1);
        }
        return url;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;

                String redirectUrl = "";
                String url = httpServletRequest.getRequestURI();

                String postFix = parseSuffix(url);
                boolean isSecuredUrl = SysEnv.GetSecuredUrlPatterns().contains(postFix) || url.equalsIgnoreCase("/");
                if (isSecuredUrl || systemProgramService.isProgramUrl(url)) {
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    httpServletResponse.setHeader("cache-control","max-age=1,no-store,no-cache");
                }else{
                    chain.doFilter(request, response);
                    return;
                }

                if (!url.contains(SysEnv.getUrlLoginPage())) {
                    SystemUser currentLogin = authenticateService.getCurrentLogin();
                    if (currentLogin == null /* 没有登录 */
                            || !systemUserService.canRun(currentLogin.getRecordId(), url) /*当前用户没有权限 */) {
                        redirectUrl = SysEnv.getAppRoot() + SysEnv.getUrlLoginPage();
                    }
                }
                if (!redirectUrl.isEmpty()) {
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    httpServletResponse.sendRedirect(redirectUrl);
                    return;
                }
            }
            chain.doFilter(request, response);
        } finally {
        }
    }

    @Override
    public void destroy() {
    }

}
