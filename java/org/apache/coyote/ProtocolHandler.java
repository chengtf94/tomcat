package org.apache.coyote;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.tomcat.util.net.SSLHostConfig;

/**
 * ProtocolHandler：封装I/O模型和应⽤层协议这两种变化点（即Endpoint和Processor放在⼀起抽象成了ProtocolHandler组件）
 * 各种协议和通信模型的组合有相应的具体实现类，例如Http11NioProtocol等
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 */
public interface ProtocolHandler {

    /**
     * Create a new ProtocolHandler for the given protocol.
     */
    @SuppressWarnings("deprecation")
    static ProtocolHandler create(String protocol, boolean apr)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
        InvocationTargetException, NoSuchMethodException, SecurityException {
        if (protocol == null || "HTTP/1.1".equals(protocol) ||
            (!apr && org.apache.coyote.http11.Http11NioProtocol.class.getName().equals(protocol)) ||
            (apr && org.apache.coyote.http11.Http11AprProtocol.class.getName().equals(protocol))) {
            if (apr) {
                return new org.apache.coyote.http11.Http11AprProtocol();
            } else {
                return new org.apache.coyote.http11.Http11NioProtocol();
            }
        } else if ("AJP/1.3".equals(protocol) ||
            (!apr && org.apache.coyote.ajp.AjpNioProtocol.class.getName().equals(protocol)) ||
            (apr && org.apache.coyote.ajp.AjpAprProtocol.class.getName().equals(protocol))) {
            if (apr) {
                return new org.apache.coyote.ajp.AjpAprProtocol();
            } else {
                return new org.apache.coyote.ajp.AjpNioProtocol();
            }
        } else {
            // Instantiate protocol handler
            Class<?> clazz = Class.forName(protocol);
            return (ProtocolHandler) clazz.getConstructor().newInstance();
        }
    }

    Adapter getAdapter();
    void setAdapter(Adapter adapter);

    Executor getExecutor();
    void setExecutor(Executor executor);

    ScheduledExecutorService getUtilityExecutor();
    void setUtilityExecutor(ScheduledExecutorService utilityExecutor);

    void init() throws Exception;

    void start() throws Exception;

    void pause() throws Exception;

    void resume() throws Exception;

    void stop() throws Exception;

    void destroy() throws Exception;

    /**
     * Close the server socket (to prevent further connections) if the server socket was bound on {@link #start()}
     * (rather than on {@link #init()} but do not perform any further shutdown.
     */
    void closeServerSocketGraceful();

    /**
     * Wait for the client connections to the server to close gracefully. The method will return when all of the client
     * connections have closed or the method has been waiting for {@code waitTimeMillis}.
     */
    long awaitConnectionsClose(long waitMillis);

    @Deprecated boolean isAprRequired();

    boolean isSendfileSupported();

    void addSslHostConfig(SSLHostConfig sslHostConfig);

    void addSslHostConfig(SSLHostConfig sslHostConfig, boolean replace);

    SSLHostConfig[] findSslHostConfigs();

    void addUpgradeProtocol(UpgradeProtocol upgradeProtocol);

    UpgradeProtocol[] findUpgradeProtocols();

    default int getDesiredBufferSize() {
        return -1;
    }

    default String getId() {
        return null;
    }

}
