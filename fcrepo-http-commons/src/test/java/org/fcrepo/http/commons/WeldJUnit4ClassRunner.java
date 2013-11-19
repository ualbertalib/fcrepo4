package org.fcrepo.http.commons;

import javax.enterprise.inject.spi.CDI;

import org.fcrepo.http.commons.test.util.ContainerWrapper;
import org.jboss.weld.Weld;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;


public class WeldJUnit4ClassRunner extends BlockJUnit4ClassRunner {
    private Class<?> klass;
    private Weld weld;
    private CDI<Object> container;
    
    public WeldJUnit4ClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
        this.klass = klass;
        this.weld = new Weld();
        IntegrationTestConfig config = weld.select(IntegrationTestConfig.class).get();

        if (klass.isAnnotationPresent(IntegrationTestPort.class)) {
            config.setPort(klass.getAnnotation(IntegrationTestPort.class).value());
        }
        if (klass.isAnnotationPresent(IntegrationTestConfigLocation.class)) {
            config.setLocation(klass.getAnnotation(IntegrationTestConfigLocation.class).value());
        }
        ContainerWrapper cw = weld.select(ContainerWrapper.class).get();
    }

    @Override
    protected Object createTest() throws Exception {
        return this.weld.select(this.klass).get();
    }

}
