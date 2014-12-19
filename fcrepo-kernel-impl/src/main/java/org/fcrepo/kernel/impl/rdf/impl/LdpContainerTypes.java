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

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static org.fcrepo.kernel.FedoraJcrTypes.LDP_BASIC_CONTAINER;
import static org.fcrepo.kernel.FedoraJcrTypes.LDP_DIRECT_CONTAINER;
import static org.fcrepo.kernel.FedoraJcrTypes.LDP_INDIRECT_CONTAINER;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.fcrepo.kernel.exception.RepositoryRuntimeException;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;

/**
 * @author ajs6f
 */
public enum LdpContainerTypes {
    DIRECT_CONTAINER(LDP_DIRECT_CONTAINER),
    INDIRECT_CONTAINER(LDP_INDIRECT_CONTAINER),
    BASIC_CONTAINER(LDP_BASIC_CONTAINER);

    /**
     * @param mixinType
     */
    LdpContainerTypes(final String mixinType) {
        this.mixinType = mixinType;
    }

    public final String mixinType;

    /**
     * @param resource
     * @return
     * @throws RepositoryException
     */
    public boolean hasInstance(final Node resource) throws RepositoryException {
        return resource.isNodeType(mixinType);
    }

    /**
     * @param resource
     * @return
     * @throws RepositoryException
     */
    public static Option<LdpContainerTypes> asContainer(final Node resource) throws RepositoryException {
        for (final LdpContainerTypes type : values()) {
            if (type.hasInstance(resource)) {
                return some(type);
            }
        }
        return none();
    }

    /**
     * Determines whether a given {@link Node} qualifies as a potential LDP container.
     */
    public static Predicate<Node> isContainer = new Predicate<Node>() {

        @Override
        public boolean matches(final Node node) {
            try {
                return asContainer(node).isDefined();
            } catch (final RepositoryException e) {
                throw new RepositoryRuntimeException(e);
            }
        }
    };
}