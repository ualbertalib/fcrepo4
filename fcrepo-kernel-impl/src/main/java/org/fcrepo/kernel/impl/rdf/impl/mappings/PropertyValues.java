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

package org.fcrepo.kernel.impl.rdf.impl.mappings;

import static com.googlecode.totallylazy.Sequences.sequence;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;

import com.googlecode.totallylazy.ForwardOnlySequence;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.slf4j.Logger;

/**
 * Iterate over all the values in a property or list of properties
 *
 * @author cabeer
 * @author ajs6f
 */
public class PropertyValues {

    private PropertyValues() {
    }

    private static final Logger log = getLogger(PropertyValues.class);

    /**
     * @param properties
     * @return
     */
    public static Sequence<Value> forProperties(final Iterator<Property> properties) {
        return new ForwardOnlySequence<>(properties).flatMap(toValues);
    }

    /**
     * @param property
     * @return
     */
    public static Sequence<Value> forProperty(final Property property) {
        return toValues.apply(property);
    }

    /**
     * Converts any {@link Property} to its {@link Value}s
     */
    public static Function1<Property, Sequence<Value>> toValues = new Function1<Property, Sequence<Value>>() {

        @Override
        public Sequence<Value> call(final Property property) throws RepositoryException {
            if (property.isMultiple()) {
                log.trace("Iterating over multiple-value property values.");
                return sequence(asList(property.getValues()));
            }
            log.trace("Iterating over single-value property value.");
            return sequence(property.getValue());
        }
    };
}
