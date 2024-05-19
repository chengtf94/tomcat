package javax.servlet;

import java.io.IOException;

/**
 * Servlet接口
 */
public interface Servlet {

    /** 生命周期方法：初始化资源 */
    void init(ServletConfig config) throws ServletException;

    /** 获取配置参数，例如在web.xml给Servlet配置参数 */
    ServletConfig getServletConfig();

    /**
     * Called by the servlet container to allow the servlet to respond to a request.
     * 最重要：实现业务处理逻辑，ServletRequest⽤来封装请求信息，ServletResponse⽤来封装响应信息，因此本质上这两个类是对通信协议的封装。
     * HTTP协议：请求和响应分别对应HttpServletRequest和HttpServletResponse
     */
    void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;

    String getServletInfo();

    /** 生命周期方法：销毁资源 */
    void destroy();

}
