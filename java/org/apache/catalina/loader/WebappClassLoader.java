package org.apache.catalina.loader;

import org.apache.catalina.LifecycleException;

public class WebappClassLoader extends WebappClassLoaderBase {

    /**
     * 构造方法
     */
    public WebappClassLoader() {
        super();
    }
    public WebappClassLoader(ClassLoader parent) {
        super(parent);
    }


    @Override
    public WebappClassLoader copyWithoutTransformers() {
        WebappClassLoader result = new WebappClassLoader(getParent());
        super.copyStateWithoutTransformers(result);
        try {
            result.start();
        } catch (LifecycleException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    @Override
    protected Object getClassLoadingLock(String className) {
        return this;
    }

}
