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
package org.fcrepo.kernel.impl.rdf.impl.mappings;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import java.util.ArrayList;
import java.util.List;
import static com.google.common.collect.ImmutableList.of;
import static java.util.stream.Collectors.toCollection;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author cabeer
 * @author ajs6f
 */
public class PropertyValueStreamTest {

    private PropertyValueStream testObj;

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

    @Before
    public void setUp() throws RepositoryException {
        initMocks(this);
        when(mockProperty.getValue()).thenReturn(value1);
        when(mockMultivaluedProperty.isMultiple()).thenReturn(true);
        when(mockMultivaluedProperty.getValues()).thenReturn(new Value[] { value2, value3 });
        of(mockProperty, mockMultivaluedProperty).iterator();
    }

    @Test
    public void testSingleValueSingleProperty() {
        testObj = new PropertyValueStream(mockProperty);
        assertTrue(testObj.anyMatch(value1::equals));
    }

    @Test
    public void testMultiValueSingleProperty() {
        testObj = new PropertyValueStream(mockMultivaluedProperty);
        final List<Value> values = testObj.collect(toCollection(ArrayList::new));
        assertTrue(values.containsAll(of(value2, value3)));
    }
}