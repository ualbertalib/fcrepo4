package org.fcrepo.http.commons.exceptions;

import javax.ws.rs.WebApplicationException;


public class RepositoryException extends WebApplicationException {

    /**
     * 
     */
    private static final long serialVersionUID = -336224627233731847L;

    public RepositoryException(javax.jcr.RepositoryException e) {
        super(e);
    }

}
