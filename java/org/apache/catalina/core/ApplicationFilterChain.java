package org.apache.catalina.core;

import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.security.SecurityUtil;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;

/**
 * 过滤器链
 * 1， Filter链中除了有Filter对象的数组，还有⼀个整数变量pos，这个变量⽤来记录当前被调⽤的Filter在数组中的位置。
 * 2. Filter链中有个Servlet实例，这个好理解，因为上⾯提到了，每个Filter链最后都会调到⼀个Servlet。
 * 3. Filter链本身也实现了doFilter⽅法，直接调⽤了⼀个内部⽅法internalDoFilter。
 * 4. internalDoFilter⽅法的实现⽐较有意思，它做了⼀个判断，如果当前Filter的位置⼩于Filter数组的⻓度，
 * 也就是说Filter还没调完，就从Filter数组拿下⼀个Filter，调⽤它的doFilter⽅法。否则，意味着所有Filter都调到了，就调⽤Servlet的service⽅法。
 *
 * @author Craig R. McClanahan
 */
public final class ApplicationFilterChain implements FilterChain {

    /** Filter链中有Filter数组、当前的调⽤位置、总共有多少了Filte、每个Filter链对应⼀个Servlet（它要调⽤的Servlet） */
    private ApplicationFilterConfig[] filters = new ApplicationFilterConfig[0];
    private int pos = 0;
    private int n = 0;
    private Servlet servlet = null;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {

        if (Globals.IS_SECURITY_ENABLED) {
            final ServletRequest req = request;
            final ServletResponse res = response;
            try {
                java.security.AccessController.doPrivileged((java.security.PrivilegedExceptionAction<Void>) () -> {
                    internalDoFilter(req, res);
                    return null;
                });
            } catch (PrivilegedActionException pe) {
                Exception e = pe.getException();
                if (e instanceof ServletException) {
                    throw (ServletException) e;
                } else if (e instanceof IOException) {
                    throw (IOException) e;
                } else if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new ServletException(e.getMessage(), e);
                }
            }
        } else {
            internalDoFilter(request, response);
        }
    }

    private void internalDoFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {

        // Call the next filter if there is one
        if (pos < n) {
            ApplicationFilterConfig filterConfig = filters[pos++];
            try {
                Filter filter = filterConfig.getFilter();

                if (request.isAsyncSupported() &&
                    "false".equalsIgnoreCase(filterConfig.getFilterDef().getAsyncSupported())) {
                    request.setAttribute(Globals.ASYNC_SUPPORTED_ATTR, Boolean.FALSE);
                }
                if (Globals.IS_SECURITY_ENABLED) {
                    final ServletRequest req = request;
                    final ServletResponse res = response;
                    Principal principal = ((HttpServletRequest) req).getUserPrincipal();

                    Object[] args = new Object[] { req, res, this };
                    SecurityUtil.doAsPrivilege("doFilter", filter, classType, args, principal);
                } else {
                    filter.doFilter(request, response, this);
                }
            } catch (IOException | ServletException | RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                e = ExceptionUtils.unwrapInvocationTargetException(e);
                ExceptionUtils.handleThrowable(e);
                throw new ServletException(sm.getString("filterChain.filter"), e);
            }
            return;
        }

        // We fell off the end of the chain -- call the servlet instance
        try {
            if (ApplicationDispatcher.WRAP_SAME_OBJECT) {
                lastServicedRequest.set(request);
                lastServicedResponse.set(response);
            }

            if (request.isAsyncSupported() && !servletSupportsAsync) {
                request.setAttribute(Globals.ASYNC_SUPPORTED_ATTR, Boolean.FALSE);
            }
            // Use potentially wrapped request from this point
            if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse) &&
                Globals.IS_SECURITY_ENABLED) {
                final ServletRequest req = request;
                final ServletResponse res = response;
                Principal principal = ((HttpServletRequest) req).getUserPrincipal();
                Object[] args = new Object[] { req, res };
                SecurityUtil.doAsPrivilege("service", servlet, classTypeUsedInService, args, principal);
            } else {
                servlet.service(request, response);
            }
        } catch (IOException | ServletException | RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            e = ExceptionUtils.unwrapInvocationTargetException(e);
            ExceptionUtils.handleThrowable(e);
            throw new ServletException(sm.getString("filterChain.servlet"), e);
        } finally {
            if (ApplicationDispatcher.WRAP_SAME_OBJECT) {
                lastServicedRequest.set(null);
                lastServicedResponse.set(null);
            }
        }
    }














    // Used to enforce requirements of SRV.8.2 / SRV.14.2.5.1
    private static final ThreadLocal<ServletRequest> lastServicedRequest;
    private static final ThreadLocal<ServletResponse> lastServicedResponse;

    static {
        if (ApplicationDispatcher.WRAP_SAME_OBJECT) {
            lastServicedRequest = new ThreadLocal<>();
            lastServicedResponse = new ThreadLocal<>();
        } else {
            lastServicedRequest = null;
            lastServicedResponse = null;
        }
    }

    // -------------------------------------------------------------- Constants


    public static final int INCREMENT = 10;


    // ----------------------------------------------------- Instance Variables


    /**
     * Does the associated servlet instance support async processing?
     */
    private boolean servletSupportsAsync = false;

    /**
     * The string manager for our package.
     */
    private static final StringManager sm = StringManager.getManager(ApplicationFilterChain.class);


    /**
     * Static class array used when the SecurityManager is turned on and <code>doFilter</code> is invoked.
     */
    private static final Class<?>[] classType =
            new Class[] { ServletRequest.class, ServletResponse.class, FilterChain.class };

    /**
     * Static class array used when the SecurityManager is turned on and <code>service</code> is invoked.
     */
    private static final Class<?>[] classTypeUsedInService =
            new Class[] { ServletRequest.class, ServletResponse.class };


    // ---------------------------------------------------- FilterChain Methods



    /**
     * The last request passed to a servlet for servicing from the current thread.
     *
     * @return The last request to be serviced.
     */
    public static ServletRequest getLastServicedRequest() {
        return lastServicedRequest.get();
    }


    /**
     * The last response passed to a servlet for servicing from the current thread.
     *
     * @return The last response to be serviced.
     */
    public static ServletResponse getLastServicedResponse() {
        return lastServicedResponse.get();
    }


    // -------------------------------------------------------- Package Methods

    /**
     * Add a filter to the set of filters that will be executed in this chain.
     *
     * @param filterConfig The FilterConfig for the servlet to be executed
     */
    void addFilter(ApplicationFilterConfig filterConfig) {

        // Prevent the same filter being added multiple times
        for (ApplicationFilterConfig filter : filters) {
            if (filter == filterConfig) {
                return;
            }
        }

        if (n == filters.length) {
            ApplicationFilterConfig[] newFilters = new ApplicationFilterConfig[n + INCREMENT];
            System.arraycopy(filters, 0, newFilters, 0, n);
            filters = newFilters;
        }
        filters[n++] = filterConfig;

    }


    /**
     * Release references to the filters and wrapper executed by this chain.
     */
    void release() {
        for (int i = 0; i < n; i++) {
            filters[i] = null;
        }
        n = 0;
        pos = 0;
        servlet = null;
        servletSupportsAsync = false;
    }


    /**
     * Prepare for reuse of the filters and wrapper executed by this chain.
     */
    void reuse() {
        pos = 0;
    }


    /**
     * Set the servlet that will be executed at the end of this chain.
     *
     * @param servlet The Wrapper for the servlet to be executed
     */
    void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }


    void setServletSupportsAsync(boolean servletSupportsAsync) {
        this.servletSupportsAsync = servletSupportsAsync;
    }


    /**
     * Identifies the Filters, if any, in this FilterChain that do not support async.
     *
     * @param result The Set to which the fully qualified class names of each Filter in this FilterChain that does not
     *                   support async will be added
     */
    public void findNonAsyncFilters(Set<String> result) {
        for (int i = 0; i < n; i++) {
            ApplicationFilterConfig filter = filters[i];
            if ("false".equalsIgnoreCase(filter.getFilterDef().getAsyncSupported())) {
                result.add(filter.getFilterClass());
            }
        }
    }
}
