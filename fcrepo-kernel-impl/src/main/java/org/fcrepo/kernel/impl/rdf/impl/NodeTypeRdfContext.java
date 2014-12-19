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
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.hp.hpl.jena.graph.NodeFactory.createLiteral;
import static com.hp.hpl.jena.graph.Triple.create;
import static com.hp.hpl.jena.vocabulary.RDF.type;
import static com.hp.hpl.jena.vocabulary.RDFS.Class;
import static com.hp.hpl.jena.vocabulary.RDFS.label;
import static com.hp.hpl.jena.vocabulary.RDFS.subClassOf;
import static org.fcrepo.kernel.impl.rdf.impl.mappings.ItemDefinitionToTriples.getResource;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.List;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Predicate;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import org.fcrepo.kernel.impl.rdf.impl.mappings.NodeDefinitionToTriples;
import org.fcrepo.kernel.impl.rdf.impl.mappings.PropertyDefinitionToTriples;
import org.fcrepo.kernel.utils.iterators.RdfStream;

import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * Assemble {@link Triple}s derived from the {@link NodeType}s in a repository.
 *
 * @author cbeer
 */
public class NodeTypeRdfContext extends RdfStream {

    private static final Logger LOGGER = getLogger(NodeTypeRdfContext.class);

    private static final Predicate<ItemDefinition> isWildcardResidualDefinition =
        new Predicate<ItemDefinition>() {

            @Override
            public boolean matches(final ItemDefinition input) {
                return input.getName().equals("*");
            }
        };

    /**
     * Convert the NodeTypeManager to an RDF stream, including both primary and
     * mixin node types.
     *
     * @param nodeTypeManager
     * @throws RepositoryException
     */
    public NodeTypeRdfContext(final NodeTypeManager nodeTypeManager)
        throws RepositoryException {
        super();

        final Iterator<NodeType> primaryNodeTypes = nodeTypeManager.getPrimaryNodeTypes();
        join(new NodeTypeRdfContext(primaryNodeTypes));
        final Iterator<NodeType> mixinNodeTypes = nodeTypeManager.getMixinNodeTypes();
        join(new NodeTypeRdfContext(mixinNodeTypes));

    }

    /**
     * Convert a NodeType iterator into an RDF stream
     *
     * @param nodeTypeIterator
     * @throws RepositoryException
     */
    public NodeTypeRdfContext(final Iterator<? extends NodeType> nodeTypeIterator)
        throws RepositoryException {
        while (nodeTypeIterator.hasNext()) {
            join(new NodeTypeRdfContext(nodeTypeIterator.next()));
        }
    }

    /**
     * Convert a NodeType into an RDF stream by capturing the supertypes, node
     * definitions, and property definitions of the type as RDFS triples.
     *
     * @param nodeType
     * @throws RepositoryException
     */
    public NodeTypeRdfContext(final NodeType nodeType) throws RepositoryException {
        final Node nodeTypeResource = getResource(nodeType).asNode();
        final String nodeTypeName = nodeType.getName();

        LOGGER.trace("Adding triples for nodeType: {} with URI: {}",
                nodeTypeName, nodeTypeResource.getURI());
        // add triples pointing to any supertypes of this nodetype
        join(sequence(nodeType.getDeclaredSupertypes()).map(
                new Function1<NodeType, Triple>() {

                    @Override
                    public Triple call(final NodeType input) throws RepositoryException {
                        final Node supertypeRdfNode = getResource(input).asNode();
                        LOGGER.trace("Adding triple for nodeType: {} with subclass: {}",
                                nodeTypeName, supertypeRdfNode.getURI());
                        return create(nodeTypeResource, subClassOf.asNode(), supertypeRdfNode);
                    }
                }));

        // include RDFS for any child node definitions
        join(sequence(nodeType.getDeclaredChildNodeDefinitions()).filter(not(isWildcardResidualDefinition)).flatMap(
                new NodeDefinitionToTriples(nodeTypeResource)));
        // include RDFS for any property definitions
        final List<PropertyDefinition> propertyDefs = sequence(nodeType.getDeclaredPropertyDefinitions()).toList();
        LOGGER.trace("Found property defs: {}", propertyDefs);
        join(sequence(propertyDefs).filter(not(isWildcardResidualDefinition)).flatMap(
                new PropertyDefinitionToTriples(nodeTypeResource)));
        // declare this to be a rdfs:Class
        append(create(nodeTypeResource, type.asNode(), Class.asNode()));
        // provide it a rdfs:label
        append(create(nodeTypeResource, label.asNode(), createLiteral(nodeTypeName)));
    }
}
