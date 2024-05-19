package javax.servlet;

import java.util.Enumeration;

/**
 * A servlet configuration object used by a servlet container to pass information to a servlet during initialization.
 */
public interface ServletConfig {

    /**
     * Returns the name of this servlet instance.
     */
    String getServletName();

    /**
     * Returns a reference to the {@link ServletContext} in which the caller is executing.
     */
    ServletContext getServletContext();

    /**
     * Returns a <code>String</code> containing the value of the named initialization parameter, or <code>null</code> if
     * the parameter does not exist.
     */
    String getInitParameter(String name);

    /**
     * Returns the names of the servlet's initialization parameters as an <code>Enumeration</code> of
     * <code>String</code> objects, or an empty <code>Enumeration</code> if the servlet has no initialization
     * parameters.
     */
    Enumeration<String> getInitParameterNames();

}
