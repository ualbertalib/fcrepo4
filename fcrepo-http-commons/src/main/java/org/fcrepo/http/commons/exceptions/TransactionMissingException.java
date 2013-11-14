package org.fcrepo.http.commons.exceptions;

import javax.ws.rs.WebApplicationException;


public class TransactionMissingException extends WebApplicationException {

    /**
     * 
     */
    private static final long serialVersionUID = -336224627233731847L;

    public TransactionMissingException(Exception e) {
        super(e);
    }

}
