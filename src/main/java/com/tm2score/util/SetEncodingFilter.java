package com.tm2score.util;


import com.tm2score.service.LogService;
import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.annotation.WebFilter;

@WebFilter(filterName = "SetEncodingFilter", servletNames = {"Faces Servlet"})
public class SetEncodingFilter implements Filter
{

    private FilterConfig filterConfig = null;

    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.filterConfig = filterConfig;
    }

    public void destroy()
    {
        this.filterConfig = null;
    }


    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
    {
        try
        {
            //LogService.logIt( "SetEndodingFilter.doFilter() " + ((HttpServletRequest)req).getRequestURI() + " req charset=" + req.getCharacterEncoding()  + ", resp charset=" + resp.getCharacterEncoding() );

            // this is REQUIRED otherwise the inbound request doesn't seem to be seen as UTF-8
            req.setCharacterEncoding( "UTF-8" );

            resp.setCharacterEncoding( "UTF-8" );

            HttpServletResponse r = (HttpServletResponse) resp;

            r.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            r.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            r.setDateHeader("Expires", 0); // Proxies.

            // LogService.logIt( "SetEndodingFilter.doFilter() BBBBB AFTER req charset=" + req.getCharacterEncoding()  + ", resp charset=" + resp.getCharacterEncoding() );


            /*
            if( req instanceof HttpServletRequest && resp instanceof HttpServletResponse )
            {
                CookieUtils.setDefaultCookie( (HttpServletResponse) resp );
            }
            */

        }

        catch( Exception e )
        {
            LogService.logIt( e , "NONFATAL SetEncodingFilter.doFilter() " );
        }

        chain.doFilter(req, resp);
    }
}

