/**
 * Copyright 2013 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.http.commons.session;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Retrieve a JCR session by just passing along the HTTP
 * credentials.
 */
@RequestScoped
@Provider
public class AuthenticatedSessionProvider implements
        Factory<Session> {

    private final SessionFactory sessions;

    private final SecurityContext securityContext;

    private final HttpServletRequest request;
    /**
     * Get a new session provider for the JCR repository
     *
     * @param sessions
     * @param request
     */
    @Inject
    public AuthenticatedSessionProvider(final SessionFactory sessions,
            final SecurityContext securityContext,
            final HttpServletRequest request) {
        this.sessions = sessions;
        this.securityContext = securityContext;
        this.request = request;
    }

    @Override
    @Produces
    public Session provide() {
        Session result = sessions.getSession(securityContext, request);
        return result;
    }

    @Override
    public void dispose(@Disposes Session instance) {
        // no-op until we can get this called
        // it ought to be where we handle logout
    }

}
