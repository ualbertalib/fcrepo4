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

package org.fcrepo.integration.http.api;

import static com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.parseInt;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.jena.riot.WebContent.contentTypeToLang;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.fcrepo.http.commons.domain.RDFMediaType;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.slf4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

@RunWith(PaxExam.class)
public abstract class AbstractResourceIT {

    protected Logger logger;

    @Configuration
    public Option[] configurationBase() {
        return new Option[] {
                junitBundles(),
                org.ops4j.pax.exam.CoreOptions.mavenBundle("org.apache.httpcomponents", "httpclient"),
                org.ops4j.pax.exam.CoreOptions.mavenBundle("org.apache.httpcomponents", "httpcore"),
                };
    }

    @Before
    public void setLogger() {
        logger = getLogger(this.getClass());
    }

    protected static final int SERVER_PORT = parseInt(System.getProperty(
            "test.port", "8080"));

    protected static final String HOSTNAME = "localhost";

    protected static final String serverAddress = "http://" + HOSTNAME + ":" +
            SERVER_PORT + "/";

    protected final PoolingClientConnectionManager connectionManager =
            new PoolingClientConnectionManager();

    protected static HttpClient client;

    public AbstractResourceIT() {
        connectionManager.setMaxTotal(MAX_VALUE);
        connectionManager.setDefaultMaxPerRoute(5);
        connectionManager.closeIdleConnections(3, SECONDS);
        client = new DefaultHttpClient(connectionManager);
    }

    protected static HttpPost postObjMethod(final String pid) {
        return new HttpPost(serverAddress + pid);
    }

    protected static HttpPost postObjMethod(final String pid, final String query) {
        if (query.equals("")) {
            return new HttpPost(serverAddress + pid);
        } else {
            return new HttpPost(serverAddress + pid + "?" + query);
        }
    }

    protected static HttpPost postDSMethod(final String pid, final String ds,
        final String content) throws UnsupportedEncodingException {
        final HttpPost post =
                new HttpPost(serverAddress + pid + "/" + ds +
                        "/fcr:content");
        post.setEntity(new StringEntity(content));
        return post;
    }

    protected static HttpPut putDSMethod(final String pid, final String ds,
        final String content) throws UnsupportedEncodingException {
        final HttpPut put =
                new HttpPut(serverAddress + pid + "/" + ds +
                        "/fcr:content");

        put.setEntity(new StringEntity(content));
        return put;
    }

    protected HttpResponse execute(final HttpUriRequest method)
        throws ClientProtocolException, IOException {
        logger.debug("Executing: " + method.getMethod() + " to " +
                method.getURI());
        return client.execute(method);
    }

    protected int getStatus(final HttpUriRequest method)
        throws ClientProtocolException, IOException {
        final HttpResponse response = execute(method);
        final int result = response.getStatusLine().getStatusCode();
        if (!(result > 199) || !(result < 400)) {
            logger.warn(EntityUtils.toString(response.getEntity()));
        }
        return result;
    }

    protected Model extract(final String serialization) throws IOException {
        return extract(serialization, RDFMediaType.TURTLE_TYPE);
    }
    
    protected Model extract(final String serialization,
            final MediaType mediaType)
    throws IOException {
        String lang = contentTypeToLang(mediaType.toString()).getName();
        logger.debug("Reading {} RDF:\n{}", lang, serialization);
        return createDefaultModel().read(new StringReader(serialization), null,lang);
    }

    public String getTestObjectPath(String parent) throws ClientProtocolException, IOException {
        final HttpPost createObjMethod =
                postObjMethod(parent);
        HttpResponse response = execute(createObjMethod);
        assertEquals(201, response.getStatusLine().getStatusCode());
        String objPath = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
        return objPath.substring(serverAddress.length());
    }

    protected static MediaType getMediaType(HttpEntity entity) {
        return MediaType.valueOf(entity.getContentType().getValue());
    }

}
