package org.fcrepo.http.commons;

import javax.annotation.ManagedBean;

@ManagedBean
public class IntegrationTestConfig {

    private String location = "/web.xml";
    private int port = 8080;
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getLocation() {
        return this.location;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public int getPort() {
        return this.port;
    }
    
}
