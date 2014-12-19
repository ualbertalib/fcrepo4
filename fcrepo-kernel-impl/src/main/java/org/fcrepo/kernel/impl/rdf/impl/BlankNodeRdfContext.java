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

import static com.google.common.collect.ImmutableList.of;
import static javax.jcr.PropertyType.PATH;
import static javax.jcr.PropertyType.REFERENCE;
import static javax.jcr.PropertyType.WEAKREFERENCE;
import static org.fcrepo.kernel.impl.identifiers.NodeResourceConverter.nodeConverter;
import static org.fcrepo.kernel.impl.rdf.impl.mappings.PropertyValues.toValues;
import static org.fcrepo.kernel.impl.utils.FedoraTypesUtils.isBlankNode;
import static org.fcrepo.kernel.impl.utils.Sequences.sequence;

import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.fcrepo.kernel.exception.RepositoryRuntimeException;
import org.fcrepo.kernel.identifiers.IdentifierConverter;
import org.fcrepo.kernel.models.FedoraResource;
import org.fcrepo.kernel.utils.iterators.RdfStream;

import com.google.common.collect.ImmutableList;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Embed all blank nodes in the RDF stream
 *
 * @author cabeer
 * @author ajs6f
 * @since 10/9/14
 */
public class BlankNodeRdfContext extends NodeRdfContext {

    private static final ImmutableList<Class<? extends NodeRdfContext>> BLANK_NODE_SOURCES = of(
            TypeRdfContext.class,
            PropertiesRdfContext.class,
            BlankNodeRdfContext.class);

    private static final List<Integer> REFERENCE_PROPERTY_TYPES = of(PATH, REFERENCE, WEAKREFERENCE);

    /**
     * Default constructor.
     *
     * @param resource
     * @param idTranslator
     * @throws javax.jcr.RepositoryException
     */
    public BlankNodeRdfContext(final FedoraResource resource,
            final IdentifierConverter<Resource, FedoraResource> idTranslator)
            throws RepositoryException {
        super(resource, idTranslator);

        final Function1<Node, RdfStream> blankNodesToTriples = new Function1<Node, RdfStream>() {

            @Override
            public RdfStream call(final Node node) {
                final FedoraResource resource = nodeConverter.convert(node);
                return resource.getTriples(translator(), BLANK_NODE_SOURCES);
            }
        };

        join(getBlankNodesIterator().flatMap(blankNodesToTriples));
    }

    private Sequence<Node> getBlankNodesIterator() throws RepositoryException {
        final Iterator<Property> properties = resource().getNode().getProperties();
        return sequence(properties).filter(filterReferenceProperties).flatMap(toValues).map(getNodesForValue).filter(
                isBlankNode);
    }

    private static final Predicate<Property> filterReferenceProperties = new Predicate<Property>() {

        @Override
        public boolean matches(final Property property) {
            try {
                return REFERENCE_PROPERTY_TYPES.contains(property.getType());
            } catch (final RepositoryException e) {
                throw new RepositoryRuntimeException(e);
            }
        }
    };

    private final Function1<Value, Node> getNodesForValue = new Function1<Value, Node>() {

        @Override
        public Node call(final Value v) throws RepositoryException {

            final Node refNode;
            if (v.getType() == PATH) {
                refNode = resource().getNode().getSession().getNode(v.getString());
            } else {
                refNode = resource().getNode().getSession().getNodeByIdentifier(v.getString());
            }
            return refNode;
        }
    };
}