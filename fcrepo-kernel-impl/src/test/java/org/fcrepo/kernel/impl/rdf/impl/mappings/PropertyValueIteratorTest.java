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

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author cabeer
 * @author ajs6f
 */
public class PropertyValueIteratorTest {

    @Mock
    private Property mockProperty;

    @Mock
    private Property mockMultivaluedProperty;

    @Mock
    private Value value1;

    @Mock
    private Value value2;

    @Mock
    private Value value3;

    private Iterator<Property> propertyIterator;

    @Before
    public void setUp() throws RepositoryException {
        initMocks(this);
        when(mockProperty.getValue()).thenReturn(value1);
        when(mockMultivaluedProperty.isMultiple()).thenReturn(true);
        when(mockMultivaluedProperty.getValues()).thenReturn(new Value[] { value2, value3 });
        propertyIterator = asList(mockProperty, mockMultivaluedProperty).iterator();
    }

    @Test
    public void testSingleValueSingleProperty() {
        assertTrue(PropertyValues.forProperty(mockProperty).contains(value1));
    }

    @Test
    public void testMultiValueSingleProperty() {
        assertTrue(PropertyValues.forProperty(mockMultivaluedProperty).containsAll(of(value2, value3)));
    }

    @Test
    public void testSingleValuePropertyIterator() {
        assertTrue(PropertyValues.forProperties(propertyIterator).containsAll(of(value1, value2, value3)));
    }
}