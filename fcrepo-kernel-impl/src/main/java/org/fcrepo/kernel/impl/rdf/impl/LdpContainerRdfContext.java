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

import static com.hp.hpl.jena.graph.Triple.create;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static org.fcrepo.kernel.FedoraJcrTypes.LDP_HAS_MEMBER_RELATION;
import static org.fcrepo.kernel.FedoraJcrTypes.LDP_INSERTED_CONTENT_RELATION;
import static org.fcrepo.kernel.FedoraJcrTypes.LDP_MEMBER_RESOURCE;
import static org.fcrepo.kernel.RdfLexicon.LDP_MEMBER;
import static org.fcrepo.kernel.RdfLexicon.MEMBER_SUBJECT;
import static org.fcrepo.kernel.impl.identifiers.NodeResourceConverter.nodeConverter;
import static org.fcrepo.kernel.impl.rdf.converters.PropertyConverter.getPropertyNameFromPredicate;
import static org.fcrepo.kernel.impl.rdf.impl.LdpContainerTypes.asContainer;
import static org.fcrepo.kernel.impl.rdf.impl.LdpContainerTypes.isContainer;
import static org.fcrepo.kernel.impl.utils.Sequences.sequence;
import static org.fcrepo.kernel.impl.utils.Sequences.functions.toParent;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.fcrepo.kernel.identifiers.IdentifierConverter;
import org.fcrepo.kernel.impl.rdf.converters.ValueConverter;
import org.fcrepo.kernel.impl.rdf.impl.mappings.PropertyValues;
import org.fcrepo.kernel.models.FedoraResource;

import org.slf4j.Logger;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author cabeer
 * @author ajs6f
 * @since 9/25/14
 */
public class LdpContainerRdfContext extends NodeRdfContext {

    private final ValueConverter valueConverter;

    private static final Logger LOGGER = getLogger(ChildrenRdfContext.class);

    /**
     * Default constructor.
     *
     * @param resource
     * @param idTranslator
     * @throws javax.jcr.RepositoryException
     */
    public LdpContainerRdfContext(final FedoraResource resource,
            final IdentifierConverter<Resource, FedoraResource> idTranslator)
            throws RepositoryException {
        super(resource, idTranslator);
        valueConverter = new ValueConverter(session(), translator());
        final Iterator<Property> references = resource.getNode().getReferences(LDP_MEMBER_RESOURCE);
        final Sequence<Node> containers = sequence(references).map(toParent).filter(isContainer);
        join(containers.flatMap(new Function1<Node, Sequence<Triple>>() {

            @Override
            public Sequence<Triple> call(final Node node) throws RepositoryException {
                final FedoraResource resource = nodeConverter.convert(node);
                return relations(resource);
            }
        }));

    }

    /**
     * Get the member relations asserted on the subject by the given container
     *
     * @param container
     * @return the appropriate {@link Triple}s
     * @throws RepositoryException
     */
    private Sequence<Triple> relations(final FedoraResource container) throws RepositoryException {
        final LdpContainerTypes containerType = asContainer(container.getNode()).get();

        // determine the predicate for a member relation and calculate how indirect members, if any, are notated
        final com.hp.hpl.jena.rdf.model.Property memberRelation;
        final String insertedContainerProperty;
        switch (containerType) {
        case BASIC_CONTAINER:
            insertedContainerProperty = MEMBER_SUBJECT.getURI();
            memberRelation = LDP_MEMBER;
            break;
        case INDIRECT_CONTAINER:
            if (!container.hasProperty(LDP_INSERTED_CONTENT_RELATION)) {
                return sequence();
            }
            insertedContainerProperty = container.getProperty(LDP_INSERTED_CONTENT_RELATION).getString();
            memberRelation = createProperty(container.getProperty(LDP_HAS_MEMBER_RELATION).getString());
            break;
        case DIRECT_CONTAINER:
            insertedContainerProperty = MEMBER_SUBJECT.getURI();
            if (!container.hasProperty(LDP_HAS_MEMBER_RELATION)) {
                return sequence();
            }
            memberRelation = createProperty(container.getProperty(LDP_HAS_MEMBER_RELATION).getString());
            break;
        default:
            return sequence();
        }

        // produce triples for each of the container's childrens
        return sequence(container.getChildren()).flatMap(
                new Function1<FedoraResource, Sequence<Triple>>() {

                    @Override
                    public Sequence<Triple> call(final FedoraResource child) throws RepositoryException {

                        final com.hp.hpl.jena.graph.Node childSubject =
                                translator().reverse().convert(child.getDescribedResource())
                                        .asNode();
                        switch (containerType) {
                        case BASIC_CONTAINER:
                        case DIRECT_CONTAINER:
                            return sequence(create(subject(), memberRelation.asNode(), childSubject));
                        case INDIRECT_CONTAINER:
                            final String insertedContentProperty =
                                    getPropertyNameFromPredicate(resource().getNode(),
                                            createResource(insertedContainerProperty),
                                            null);

                            if (!child.hasProperty(insertedContentProperty)) {
                                return sequence();
                            }

                            return PropertyValues.forProperty(child.getProperty(insertedContentProperty))
                                    .map(new Function1<Value, Triple>() {

                                        @Override
                                        public Triple call(final Value value) {
                                            final RDFNode membershipResource = valueConverter.convert(value);
                                            return create(subject(), memberRelation.asNode(), membershipResource
                                                    .asNode());
                                        }
                                    });
                        default:
                            return sequence();
                        }
                    }
                });
    }
}
