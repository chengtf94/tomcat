package javax.servlet;

import java.io.IOException;
import java.util.Enumeration;

/**
 * GenericServlet抽象类
 */
public abstract class GenericServlet implements Servlet, ServletConfig, java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /** Servlet配置参数 */
    private transient ServletConfig config;

    public GenericServlet() {
        // NOOP
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        this.init();
    }
    public void init() throws ServletException {
        // NOOP by default
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public String getServletInfo() {
        return "";
    }

    @Override
    public abstract void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;

    @Override
    public void destroy() {
        // NOOP by default
    }

    public void log(String message) {
        getServletContext().log(getServletName() + ": " + message);
    }
    public void log(String message, Throwable t) {
        getServletContext().log(getServletName() + ": " + message, t);
    }

    @Override
    public String getServletName() {
        return config.getServletName();
    }

    @Override
    public ServletContext getServletContext() {
        return getServletConfig().getServletContext();
    }

    @Override
    public String getInitParameter(String name) {
        return getServletConfig().getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return getServletConfig().getInitParameterNames();
    }

}
