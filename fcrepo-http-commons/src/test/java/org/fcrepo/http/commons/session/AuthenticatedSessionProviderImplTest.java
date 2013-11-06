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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.fcrepo.http.commons.session.AuthenticatedSessionProvider;
import org.junit.Before;
import org.junit.Test;
import org.modeshape.jcr.api.ServletCredentials;

public class AuthenticatedSessionProviderImplTest {

    private SessionFactory mockSessions;
    
    @Before
    public void setUp() {
        mockSessions = mock(SessionFactory.class);
    }

    @Test
    public void testCredentialsProvided() throws RepositoryException {
        final ServletCredentials mockCreds = mock(ServletCredentials.class);
        final AuthenticatedSessionProvider test =
                new AuthenticatedSessionProvider(mockSessions, (SecurityContext)null, (HttpServletRequest)null);
        test.provide();
        verify(mockSessions).getSession(null, null);
    }

    @Test
    public void testNoCredentialsProvided() throws RepositoryException {
        final AuthenticatedSessionProvider test =
            new AuthenticatedSessionProvider(mockSessions, (SecurityContext)null, (HttpServletRequest)null);
        test.provide();
        verify(mockSessions).getSession(null, null);
    }
}
