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

import static com.hp.hpl.jena.graph.Node.ANY;
import static com.hp.hpl.jena.graph.NodeFactory.createURI;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static junit.framework.TestCase.assertFalse;
import static org.fcrepo.http.commons.test.util.TestHelpers.parseTriples;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.fcrepo.http.commons.domain.RDFMediaType;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.update.GraphStore;

public class FedoraDatastreamsIT extends AbstractResourceIT {

    @Test
    public void testMultipleDatastreams() throws Exception {

        String objPath = getTestObjectPath("FedoraDatastreamsTest7");
        final HttpPost createDS1Method =
                postDSMethod(objPath, "ds1",
                        "marbles for everyone");
        assertEquals(CREATED.getStatusCode(), getStatus(createDS1Method));
        final HttpPost createDS2Method =
                postDSMethod(objPath, "ds2",
                        "marbles for no one");
        assertEquals(CREATED.getStatusCode(), getStatus(createDS2Method));

        final HttpGet getDSesMethod =
                new HttpGet(serverAddress + objPath);
        getDSesMethod.addHeader(HttpHeaders.ACCEPT, RDFMediaType.N3_TEXT);
        HttpResponse response = client.execute(getDSesMethod);
        assertEquals(200, response.getStatusLine().getStatusCode());
        final GraphStore result =
            parseTriples(response.getEntity().getContent());
        logger.debug("Received triples: \n{}", result.toString());
        final String subjectURI = serverAddress + objPath;

        assertTrue("Didn't find the first datastream! ", result.contains(ANY,
                createURI(subjectURI), ANY, createURI(subjectURI + "/ds1")));
        assertTrue("Didn't find the second datastream! ", result.contains(ANY,
                createURI(subjectURI), ANY, createURI(subjectURI + "/ds2")));
    }

    @Test
    public void testModifyMultipleDatastreams() throws Exception {
        String objPath = getTestObjectPath("FedoraDatastreamsTest8");


        final HttpPost createDSVOIDMethod =
            postDSMethod(objPath, "ds_void",
                    "marbles for everyone");
        assertEquals(CREATED.getStatusCode(), getStatus(createDSVOIDMethod));

        final HttpPost post =
            new HttpPost(serverAddress + objPath
                    + "/fcr:datastreams?delete=ds_void");

        final MultipartEntity multiPartEntity = new MultipartEntity();
        multiPartEntity.addPart("ds1", new StringBody("asdfg"));
        multiPartEntity.addPart("ds2", new StringBody("qwerty"));

        post.setEntity(multiPartEntity);

        final HttpResponse postResponse = client.execute(post);

        assertEquals(CREATED.getStatusCode(), postResponse.getStatusLine().getStatusCode());

        final HttpGet getDSesMethod =
            new HttpGet(serverAddress + objPath);
        getDSesMethod.addHeader(HttpHeaders.ACCEPT, RDFMediaType.N3_TEXT);
        final HttpResponse response = client.execute(getDSesMethod);
        assertEquals(OK.getStatusCode(), response.getStatusLine().getStatusCode());

        final String subjectURI = serverAddress + objPath;
        final GraphStore result =
            parseTriples(response.getEntity().getContent());
        assertTrue("Didn't find the first datastream! ", result.contains(ANY,
                createURI(subjectURI), ANY, createURI(subjectURI + "/ds1")));
        assertTrue("Didn't find the second datastream! ", result.contains(ANY,
                createURI(subjectURI), ANY, createURI(subjectURI + "/ds2")));
        assertFalse("Found the deleted datastream! ", result.contains(Node.ANY,
                createURI(subjectURI), ANY, createURI(subjectURI + "/ds_void")));

    }

    @Test
    public void testRetrieveMultipartDatastreams() throws Exception {

        String objPath = getTestObjectPath("FedoraDatastreamsTest9");
        String datastreamsUri = serverAddress + objPath + "/fcr:datastreams";
        final HttpPost post =
            new HttpPost(datastreamsUri.concat("/"));

        final MultipartEntity multiPartEntity = new MultipartEntity();
        multiPartEntity.addPart("ds1", new StringBody("asdfg"));
        multiPartEntity.addPart("ds2", new StringBody("qwerty"));

        post.setEntity(multiPartEntity);

        final HttpResponse postResponse = client.execute(post);
        assertEquals(CREATED.getStatusCode(), postResponse.getStatusLine().getStatusCode());

        // TODO: we should actually evaluate the multipart response for the
        // things we're expecting
        final HttpGet getDSesMethod =
            new HttpGet(datastreamsUri);
        final HttpResponse response = client.execute(getDSesMethod);
        assertEquals(OK.getStatusCode(), response.getStatusLine().getStatusCode());
        final String content = EntityUtils.toString(response.getEntity());

        assertTrue("Didn't find the first datastream!",
                compile("asdfg", DOTALL).matcher(content).find());
        assertTrue("Didn't find the second datastream!", compile("qwerty",
                DOTALL).matcher(content).find());

    }

    @Test
    public void testRetrieveFilteredMultipartDatastreams() throws Exception {

        String objPath = getTestObjectPath("FedoraDatastreamsTest10");
        String datastreamsUri = serverAddress + objPath + "/fcr:datastreams";
        final HttpPost post =
            new HttpPost(datastreamsUri.concat("/"));

        final MultipartEntity multiPartEntity = new MultipartEntity();
        multiPartEntity.addPart("ds1", new StringBody("asdfg"));
        multiPartEntity.addPart("ds2", new StringBody("qwerty"));

        post.setEntity(multiPartEntity);

        final HttpResponse postResponse = client.execute(post);
        assertEquals(CREATED.getStatusCode(), postResponse.getStatusLine().getStatusCode());

        // TODO: we should actually evaluate the multipart response for the
        // things we're expecting
        final HttpGet getDSesMethod =
            new HttpGet(datastreamsUri.concat("?dsid=ds1"));
        final HttpResponse response = client.execute(getDSesMethod);
        assertEquals(OK.getStatusCode(), response.getStatusLine().getStatusCode());
        final String content = EntityUtils.toString(response.getEntity());

        assertTrue("Didn't find the first datastream!",
                compile("asdfg", DOTALL).matcher(content).find());
        assertFalse("Didn't expect to find the second datastream!", compile(
                "qwerty", DOTALL).matcher(content).find());

    }

    @Test
    public void testBatchDeleteDatastream() throws Exception {
        String objPath = getTestObjectPath("FedoraDatastreamsTest11");
        final HttpPost method1 =
            postDSMethod(objPath, "ds1", "foo1");
        assertEquals(CREATED.getStatusCode(), getStatus(method1));
        final HttpPost method2 =
            postDSMethod(objPath, "ds2", "foo2");
        assertEquals(CREATED.getStatusCode(), getStatus(method2));

        final HttpDelete dmethod =
            new HttpDelete(
                    serverAddress + objPath
                            + "/fcr:datastreams?dsid=ds1&dsid=ds2");
        assertEquals(204, getStatus(dmethod));

        final HttpGet method_test_get1 =
            new HttpGet(serverAddress + objPath + "/ds1");
        assertEquals(404, getStatus(method_test_get1));
        final HttpGet method_test_get2 =
                new HttpGet(serverAddress + objPath + "/ds2");
        assertEquals(404, getStatus(method_test_get2));
    }
    
    public String getTestObjectPath(String parent) throws ClientProtocolException, IOException {
        final HttpPost createObjMethod =
                postObjMethod(parent);
        HttpResponse response = execute(createObjMethod);
        assertEquals(201, response.getStatusLine().getStatusCode());
        String objPath = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
        return objPath.substring(serverAddress.length());
    }
}
