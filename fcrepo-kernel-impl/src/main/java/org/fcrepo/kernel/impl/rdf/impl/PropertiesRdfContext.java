/**
 * Copyright 2014 DuraSpace, Inc.
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

package org.fcrepo.kernel.impl.rdf.impl;

import static com.googlecode.totallylazy.Predicates.not;
import static org.fcrepo.kernel.impl.utils.FedoraTypesUtils.isInternalProperty;
import static org.slf4j.LoggerFactory.getLogger;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import com.googlecode.totallylazy.ForwardOnlySequence;
import com.googlecode.totallylazy.Sequence;
import com.hp.hpl.jena.rdf.model.Resource;

import org.fcrepo.kernel.models.FedoraResource;
import org.fcrepo.kernel.identifiers.IdentifierConverter;
import org.fcrepo.kernel.impl.rdf.impl.mappings.PropertyToTriple;
import org.slf4j.Logger;

import com.hp.hpl.jena.graph.Triple;

/**
 * {@link NodeRdfContext} for RDF that derives from JCR properties on a Resource
 *
 * @author ajs6f
 * @since Oct 10, 2013
 */
public class PropertiesRdfContext extends NodeRdfContext {

    private final PropertyToTriple property2triple;

    private static final Logger LOGGER = getLogger(PropertiesRdfContext.class);

    /**
     * Default constructor.
     *
     * @param resource
     * @throws RepositoryException
     */

    public PropertiesRdfContext(final FedoraResource resource,
            final IdentifierConverter<Resource, FedoraResource> idTranslator)
            throws RepositoryException {
        super(resource, idTranslator);
        property2triple = new PropertyToTriple(resource.getNode().getSession(), idTranslator);
        join(triplesFromProperties(resource()));
    }

    private Sequence<Triple> triplesFromProperties(final FedoraResource n) throws RepositoryException {
        LOGGER.trace("Creating triples for node: {}", n);
        return new ForwardOnlySequence<Property>(n.getNode().getProperties()).filter(not(isInternalProperty))
                .flatMap(property2triple);
    }
}
