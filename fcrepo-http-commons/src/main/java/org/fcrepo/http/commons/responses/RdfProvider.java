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

package org.fcrepo.http.commons.responses;

import static java.util.Collections.singletonList;
import static org.apache.jena.riot.WebContent.contentTypeToLang;
import static org.fcrepo.http.commons.responses.RdfSerializationUtils.setCachingHeaders;
/**
import static org.fcrepo.http.commons.domain.RDFMediaType.TURTLE;
import static org.fcrepo.http.commons.domain.RDFMediaType.RDF_XML;
import static org.fcrepo.http.commons.domain.RDFMediaType.NTRIPLES;
import static org.fcrepo.http.commons.domain.RDFMediaType.RDF_JSON;
import static org.fcrepo.http.commons.domain.RDFMediaType.N3_TEXT_RDF;
import static org.fcrepo.http.commons.domain.RDFMediaType.N3_APPLICATION;
import static org.fcrepo.http.commons.domain.RDFMediaType.N3_TEXT;
import static org.fcrepo.http.commons.domain.RDFMediaType.TRI_G;
import static org.fcrepo.http.commons.domain.RDFMediaType.NQUADS;
*/
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.fcrepo.http.commons.domain.RDFMediaType;
import org.slf4j.Logger;

import com.hp.hpl.jena.query.Dataset;

/**
 * Helper for writing Datasets out in a variety of serialization formats
 * with cache headers.
 */
@Provider
@Produces(MediaType.WILDCARD)
//@Produces(value = {TURTLE, RDF_XML, NTRIPLES, RDF_JSON, N3_TEXT_RDF,
//        N3_APPLICATION, N3_TEXT, TRI_G, NQUADS})
public class RdfProvider implements MessageBodyWriter<Dataset> {

    private static final Logger logger = getLogger(RdfProvider.class);

    @Override
    public void writeTo(final Dataset rdf, final Class<?> type,
            final Type genericType, final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException,
        WebApplicationException {

        logger.debug("Writing a response for: {} with MIMEtype: {}", rdf,
                mediaType);
        MediaType realType = (mediaType == null ||
                mediaType.equals(RDFMediaType.WILDCARD_TYPE)) ?
                        RDFMediaType.TURTLE_TYPE :
                        mediaType;
        // add standard headers
        httpHeaders.put("Content-type", singletonList((Object) realType.toString()));

        setCachingHeaders(httpHeaders, rdf);

        new GraphStoreStreamingOutput(rdf, realType).write(entityStream);
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType) {

        // we can return a result for any MIME type that Jena can serialize
        final Boolean appropriateMimeType =
                mediaType == null ||
                mediaType.equals(MediaType.WILDCARD_TYPE) ||
                mediaType.equals(RDFMediaType.RDF_XML_TYPE) ||
                contentTypeToLang(mediaType.toString()) != null;
        if (!appropriateMimeType) {
            logger.info("rejecting {} on grounds of appropriateness", mediaType);
        }
        return appropriateMimeType &&
                (Dataset.class.isAssignableFrom(type) || Dataset.class
                        .isAssignableFrom(genericType.getClass()));
    }

    @Override
    public long getSize(final Dataset rdf, final Class<?> type,
            final Type genericType, final Annotation[] annotations,
            final MediaType mediaType) {
        // we don't know in advance how large the result might be
        return -1;
    }

}
