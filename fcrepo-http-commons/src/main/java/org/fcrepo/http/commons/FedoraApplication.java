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
package org.fcrepo.http.commons;

import javax.jcr.Session;

import org.fcrepo.http.commons.exceptionhandlers.AccessControlExceptionMapper;
import org.fcrepo.http.commons.exceptionhandlers.InvalidChecksumExceptionMapper;
import org.fcrepo.http.commons.exceptionhandlers.PathNotFoundExceptionMapper;
import org.fcrepo.http.commons.exceptionhandlers.RepositoryExceptionMapper;
import org.fcrepo.http.commons.exceptionhandlers.TransactionMissingExceptionMapper;
import org.fcrepo.http.commons.exceptionhandlers.WildcardExceptionMapper;
import org.fcrepo.http.commons.responses.RdfProvider;
import org.fcrepo.http.commons.session.AuthenticatedSessionProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;


public class FedoraApplication extends ResourceConfig {

    /**
     * THIS IS OUR RESOURCE CONFIG!
     */
    public FedoraApplication() {
        super();
        register(new FactoryBinder());
        register(JacksonFeature.class);
        register(MultiPartFeature.class);
        //TODO this is a temporary hack to get tests to run
        // until we know why classpath scanning from mvn isn't working
        register(RdfProvider.class);
        register(AccessControlExceptionMapper.class);
        register(InvalidChecksumExceptionMapper.class);
        register(PathNotFoundExceptionMapper.class);
        register(RepositoryExceptionMapper.class);
        register(TransactionMissingExceptionMapper.class);
        register(WildcardExceptionMapper.class);
    }

    static class FactoryBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bindFactory(AuthenticatedSessionProvider.class)
                .to(Session.class)
                .in(RequestScoped.class);
        }
    }
}
