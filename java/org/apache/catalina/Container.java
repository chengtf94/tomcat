package org.apache.catalina;

import java.beans.PropertyChangeListener;
import java.io.File;

import javax.management.ObjectName;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.juli.logging.Log;

/**
 * 容器接口
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 */
public interface Container extends Lifecycle {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The ContainerEvent event type sent when a child container is added
     * by <code>addChild()</code>.
     */
    String ADD_CHILD_EVENT = "addChild";


    /**
     * The ContainerEvent event type sent when a valve is added
     * by <code>addValve()</code>, if this Container supports pipelines.
     */
    String ADD_VALVE_EVENT = "addValve";


    /**
     * The ContainerEvent event type sent when a child container is removed
     * by <code>removeChild()</code>.
     */
    String REMOVE_CHILD_EVENT = "removeChild";


    /**
     * The ContainerEvent event type sent when a valve is removed
     * by <code>removeValve()</code>, if this Container supports pipelines.
     */
    String REMOVE_VALVE_EVENT = "removeValve";


    // ------------------------------------------------------------- Properties

    Log getLogger();

    String getLogName();

    ObjectName getObjectName();

    String getDomain();

    String getMBeanKeyProperties();

    Pipeline getPipeline();

    Cluster getCluster();
    void setCluster(Cluster cluster);

    int getBackgroundProcessorDelay();
    void setBackgroundProcessorDelay(int delay);

    String getName();
    void setName(String name);

    Container getParent();
    void setParent(Container container);

    ClassLoader getParentClassLoader();
    void setParentClassLoader(ClassLoader parent);

    Realm getRealm();
    void setRealm(Realm realm);


    /**
     * Find the configuration path where a configuration resource
     * is located.
     * @param container The container
     * @param resourceName The resource file name
     * @return the configuration path
     */
    static String getConfigPath(Container container, String resourceName) {
        StringBuilder result = new StringBuilder();
        Container host = null;
        Container engine = null;
        while (container != null) {
            if (container instanceof Host) {
                host = container;
            } else if (container instanceof Engine) {
                engine = container;
            }
            container = container.getParent();
        }
        if (host != null && ((Host) host).getXmlBase() != null) {
            result.append(((Host) host).getXmlBase()).append('/');
        } else {
            result.append("conf/");
            if (engine != null) {
                result.append(engine.getName()).append('/');
            }
            if (host != null) {
                result.append(host.getName()).append('/');
            }
        }
        result.append(resourceName);
        return result.toString();
    }


    /**
     * Return the Service to which this container belongs.
     * @param container The container to start from
     * @return the Service, or null if not found
     */
    static Service getService(Container container) {
        while (container != null && !(container instanceof Engine)) {
            container = container.getParent();
        }
        if (container == null) {
            return null;
        }
        return ((Engine) container).getService();
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    void backgroundProcess();


    /**
     * Add a new child Container to those associated with this Container,
     * if supported.  Prior to adding this Container to the set of children,
     * the child's <code>setParent()</code> method must be called, with this
     * Container as an argument.  This method may thrown an
     * <code>IllegalArgumentException</code> if this Container chooses not
     * to be attached to the specified Container, in which case it is not added
     *
     * @param child New child Container to be added
     *
     * @exception IllegalArgumentException if this exception is thrown by
     *  the <code>setParent()</code> method of the child Container
     * @exception IllegalArgumentException if the new child does not have
     *  a name unique from that of existing children of this Container
     * @exception IllegalStateException if this Container does not support
     *  child Containers
     */
    void addChild(Container child);


    /**
     * Add a container event listener to this component.
     *
     * @param listener The listener to add
     */
    void addContainerListener(ContainerListener listener);


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);


    Container findChild(String name);
    Container[] findChildren();


    /**
     * Obtain the container listeners associated with this Container.
     *
     * @return An array containing the container listeners associated with this
     *         Container. If this Container has no registered container
     *         listeners, a zero-length array is returned.
     */
    ContainerListener[] findContainerListeners();


    /**
     * Remove an existing child Container from association with this parent
     * Container.
     *
     * @param child Existing child Container to be removed
     */
    void removeChild(Container child);


    /**
     * Remove a container event listener from this component.
     *
     * @param listener The listener to remove
     */
    void removeContainerListener(ContainerListener listener);


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);


    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    void fireContainerEvent(String type, Object data);


    /**
     * Log a request/response that was destined for this container but has been
     * handled earlier in the processing chain so that the request/response
     * still appears in the correct access logs.
     * @param request       Request (associated with the response) to log
     * @param response      Response (associated with the request) to log
     * @param time          Time taken to process the request/response in
     *                      milliseconds (use 0 if not known)
     * @param   useDefault  Flag that indicates that the request/response should
     *                      be logged in the engine's default access log
     */
    void logAccess(Request request, Response response, long time,
            boolean useDefault);


    /**
     * Obtain the AccessLog to use to log a request/response that is destined
     * for this container. This is typically used when the request/response was
     * handled (and rejected) earlier in the processing chain so that the
     * request/response still appears in the correct access logs.
     *
     * @return The AccessLog to use for a request/response destined for this
     *         container
     */
    AccessLog getAccessLog();


    /**
     * Obtain the number of threads available for starting and stopping any
     * children associated with this container. This allows start/stop calls to
     * children to be processed in parallel.
     *
     * @return The currently configured number of threads used to start/stop
     *         children associated with this container
     */
    int getStartStopThreads();


    /**
     * Sets the number of threads available for starting and stopping any
     * children associated with this container. This allows start/stop calls to
     * children to be processed in parallel.
     * @param   startStopThreads    The new number of threads to be used
     */
    void setStartStopThreads(int startStopThreads);


    /**
     * Obtain the location of CATALINA_BASE.
     *
     * @return  The location of CATALINA_BASE.
     */
    File getCatalinaBase();


    /**
     * Obtain the location of CATALINA_HOME.
     *
     * @return The location of CATALINA_HOME.
     */
    File getCatalinaHome();
}
