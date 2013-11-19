package org.fcrepo.jcr;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Activated " + getClass().getName());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Deactivated " + getClass().getName());
    }

}