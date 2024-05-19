package org.apache.catalina.core;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Host;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * Engine容器的基础阀
 *
 * @author Craig R. McClanahan
 */
final class StandardEngineValve extends ValveBase {

    StandardEngineValve() {
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        // 拿到请求中的Host容器
        // 请求到达Engine容器中之前，Mapper组件已经对请求进⾏了路由处理，Mapper组件通过请求的URL定位了相应的容器，并且把容器对象保存到了请求对象中。
        Host host = request.getHost();
        if (host == null) {
            if (!response.isError()) {
                response.sendError(404);
            }
            return;
        }
        if (request.isAsyncSupported()) {
            request.setAsyncSupported(host.getPipeline().isAsyncSupported());
        }
        // 调⽤Host容器中的Pipeline中的第⼀个Valv
        host.getPipeline().getFirst().invoke(request, response);
    }
}
