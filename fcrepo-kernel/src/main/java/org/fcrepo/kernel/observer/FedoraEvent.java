/**
 * Copyright 2015 DuraSpace, Inc.
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
package org.fcrepo.kernel.observer;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Sets.union;
import static java.util.Collections.singleton;
import static javax.jcr.observation.Event.PROPERTY_ADDED;
import static javax.jcr.observation.Event.PROPERTY_CHANGED;
import static javax.jcr.observation.Event.PROPERTY_REMOVED;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;

import org.fcrepo.kernel.utils.EventType;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * A very simple abstraction to prevent event-driven machinery downstream from the repository from relying directly
 * on a JCR interface {@link Event}. Can represent either a single JCR event or several.
 *
 * @author ajs6f
 * @since Feb 19, 2013
 */
public class FedoraEvent {

    private Event e;

    private Set<Integer> eventTypes = new HashSet<>();
    private Set<String> eventProperties = new HashSet<>();

    /**
     * Wrap a JCR Event with our FedoraEvent decorators
     *
     * @param e
     */
    public FedoraEvent(final Event e) {
        checkArgument(e != null, "null cannot support a FedoraEvent!");
        this.e = e;
    }

    /**
     * Create a FedoraEvent from an existing FedoraEvent object
     * Note: Only the wrapped JCR event is passed on to the new object.
     *
     * @param e
     */
    public FedoraEvent(final FedoraEvent e) {
        checkArgument(e != null, "null cannot support a FedoraEvent!");
        this.e = e.e;
    }

    /**
     * @return the event types of the underlying JCR {@link Event}s
     */
    public Set<Integer> getTypes() {
        return eventTypes != null ? union(singleton(e.getType()), eventTypes) : singleton(e.getType());
    }

    /**
     * @param type
     * @return this object for continued use
     */
    public FedoraEvent addType(final Integer type) {
        eventTypes.add(type);
        return this;
    }

    /**
     * @return the property names of the underlying JCR property {@link Event}s
    **/
    public Set<String> getProperties() {
        return eventProperties;
    }

    /**
     * Add a property name to this event
     * @param property property name
     * @return this object for continued use
    **/
    public FedoraEvent addProperty( final String property ) {
        eventProperties.add(property);
        return this;
    }

    /**
     * @return the path of the underlying JCR {@link Event}s
     */
    public String getPath() throws RepositoryException {
        if (e.getType() == PROPERTY_ADDED   ||
            e.getType() == PROPERTY_CHANGED ||
            e.getType() == PROPERTY_REMOVED) {
            return e.getPath().substring(0, e.getPath().lastIndexOf("/"));
        }
        return e.getPath();
    }

    /**
     * @return the user ID of the underlying JCR {@link Event}s
     */
    public String getUserID() {
        return e.getUserID();
    }

    /**
     * @return the node identifer of the underlying JCR {@link Event}s
     */
    public String getIdentifier() throws RepositoryException {
        return e.getIdentifier();
    }

    /**
     * @return the info map of the underlying JCR {@link Event}s
     */
    public Map<Object, Object> getInfo() throws RepositoryException {
        return new HashMap<>(e.getInfo());
    }

    /**
     * @return the user data of the underlying JCR {@link Event}s
     */
    public String getUserData() throws RepositoryException {
        return e.getUserData();
    }

    /**
     * @return the date of the underlying JCR {@link Event}s
     */
    public long getDate() throws RepositoryException {
        return e.getDate();
    }

    @Override
    public String toString() {
        try {
            return toStringHelper(this).add("Event types:",
                    Joiner.on(',').join(Iterables.transform(getTypes(), new Function<Integer, String>() {

                        @Override
                        public String apply(final Integer type) {
                            return EventType.valueOf(type).getName();
                        }
                    }))).add("Event properties:",
                    Joiner.on(',').join(eventProperties)).add("Path:", getPath()).add("Date: ",
                    getDate()).add("Info:", getInfo()).toString();
        } catch (final RepositoryException e) {
            throw propagate(e);
        }
    }
}
