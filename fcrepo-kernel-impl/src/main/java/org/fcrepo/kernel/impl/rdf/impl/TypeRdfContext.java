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

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;

import org.fcrepo.kernel.models.FedoraResource;
import org.fcrepo.kernel.identifiers.IdentifierConverter;

import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.hp.hpl.jena.graph.NodeFactory.createURI;
import static com.hp.hpl.jena.graph.Triple.create;
import static com.hp.hpl.jena.vocabulary.RDF.type;
import static org.fcrepo.kernel.impl.rdf.JcrRdfTools.getRDFNamespaceForJcrNamespace;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author cabeer
 * @author ajs6f
 * @since 10/1/14
 */
public class TypeRdfContext extends NodeRdfContext {

    private static final Logger LOGGER = getLogger(TypeRdfContext.class);

    /**
     * Default constructor.
     *
     * @param resource
     * @param idTranslator
     * @throws javax.jcr.RepositoryException
     */
    public TypeRdfContext(final FedoraResource resource,
            final IdentifierConverter<Resource, FedoraResource> idTranslator)
            throws RepositoryException {
        super(resource, idTranslator);
        LOGGER.trace("Creating type RDF for resource: {}", resource);

        // include rdf:type for primaryType, mixins, and their supertypes
        concatRdfTypes();
    }

    private void concatRdfTypes() throws RepositoryException {
        final Node node = resource().getNode();
        final NodeType primaryNodeType = node.getPrimaryNodeType();
        final Sequence<NodeType> nodeTypes = sequence(node.getMixinNodeTypes()).append(primaryNodeType);
        join(sequence(nodeTypes.flatMap(superTypes).unique()).map(nodetype2triple));
    }

    private final Function1<NodeType, Sequence<NodeType>> superTypes = new Function1<NodeType, Sequence<NodeType>>() {

        @Override
        public Sequence<NodeType> call(final NodeType type) {
            LOGGER.trace("Retrieving supertypes for type: {}", type);
            return sequence(type.getSupertypes()).append(type);
        }
    };

    private final Function1<NodeType, Triple> nodetype2triple = new Function1<NodeType, Triple>() {

        @Override
        public Triple call(final NodeType nodeType) throws RepositoryException {
            final String fullTypeName = nodeType.getName();
            LOGGER.trace("Translating JCR mixin name: {}", fullTypeName);
            final String prefix = fullTypeName.split(":")[0];
            final String typeName = fullTypeName.split(":")[1];
            final String namespace = getJcrUri(prefix);
            LOGGER.trace("with JCR namespace: {}", namespace);
            final com.hp.hpl.jena.graph.Node rdfType =
                    createURI(getRDFNamespaceForJcrNamespace(namespace)
                            + typeName);
            LOGGER.trace("into RDF resource: {}", rdfType);
            return create(subject(), type.asNode(), rdfType);
        }

    };

    private String getJcrUri(final String prefix) throws RepositoryException {
        return resource().getNode().getSession().getWorkspace().getNamespaceRegistry()
                .getURI(prefix);
    }

}
