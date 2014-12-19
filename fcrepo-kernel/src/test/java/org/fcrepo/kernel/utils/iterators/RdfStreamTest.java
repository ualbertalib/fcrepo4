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
package org.fcrepo.kernel.utils.iterators;

import static com.hp.hpl.jena.graph.NodeFactory.createAnon;
import static com.hp.hpl.jena.graph.Triple.create;
import static com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.fcrepo.kernel.utils.iterators.RdfStream.from;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * <p>RdfStreamTest class.</p>
 *
 * @author ajs6f
 */
public class RdfStreamTest {

    private RdfStream testStream;

    @Mock
    private Iterator<Triple> mockIterator;

    @Mock
    private Iterable<Triple> mockIterable;

    @Mock
    private Triple triple1;

    @Mock
    private Triple triple2;

    @Mock
    private Triple triple3;

    private static final Triple triple = create(createAnon(), createAnon(),
            createAnon());

    private final static String prefix1 = "testNS";

    private final static String uri1 = "http://testNS";

    private final static String prefix2 = "testNS2";

    private final static String uri2 = "http://testNS2";

    private final Map<String, String> testNamespaces = ImmutableMap.of(prefix1,
            uri1, prefix2, uri2);

    @Before
    public void setUp() {
        initMocks(this);
        when(mockIterable.iterator()).thenReturn(mockIterator);
        testStream = new RdfStream(mockIterable);
    }

    @Test
    public void testIterator() {
        assertEquals(testStream, testStream);
    }

    @Test(expected = NoSuchElementException.class)
    public void testDefaultConstructor() {
        new RdfStream().first();
    }

    @Test
    public void testIteratorConstructor() {
        testStream = new RdfStream(ImmutableSet.of(triple1, triple2).iterator());
        assertEquals(triple1, testStream.first());
    }

    @Test
    public void testCollectionConstructor() {
        testStream = new RdfStream(ImmutableSet.of(triple1, triple2));
        assertEquals(triple1, testStream.first());
    }

    @Test
    public void testVarargsConstructor() {
        testStream = new RdfStream(triple1, triple2);
        assertEquals(triple1, testStream.first());
    }

    @Test
    public void testWithThisContextIterator() {
        testStream.namespace(prefix1, uri1);
        testStream.topic(NodeFactory.createAnon());
        final RdfStream testStream2 =
            testStream.withThisContext(new RdfStream().iterator());
        assertEquals(testStream.namespaces(), testStream2.namespaces());
        assertEquals(testStream.topic(), testStream2.topic());
    }

    @Test
    public void testWithThisContextIterable() {
        testStream.namespace(prefix1, uri1);
        testStream.topic(createAnon());
        final RdfStream testStream2 = testStream.withThisContext(new RdfStream());
        assertEquals(testStream.namespaces(), testStream2.namespaces());
        assertEquals(testStream.topic(), testStream2.topic());
    }

    @Test
    public void testAddNamespace() {
        testStream.namespace(prefix1, uri1);
        assertTrue(testStream.namespaces().containsKey(prefix1));
        assertTrue(testStream.namespaces().containsValue(uri1));
    }

    @Test
    public void testAddNamespaces() {
        testStream.namespaces(testNamespaces);
        assertTrue(testStream.namespaces().containsKey(prefix1));
        assertTrue(testStream.namespaces().containsValue(uri1));
        assertTrue(testStream.namespaces().containsKey(prefix2));
        assertTrue(testStream.namespaces().containsValue(uri2));
    }

    @Test
    public void testAsModel() {
        testStream = new RdfStream(singletonList(triple));
        testStream.namespaces(testNamespaces);

        final Model testModel = testStream.asModel();
        assertEquals(testModel.getNsPrefixMap(), testNamespaces);
        assertTrue(testModel.contains(testModel.asStatement(triple)));
    }

    @Test
    public void testFromModel() {
        final Model model = createDefaultModel();
        model.setNsPrefix(prefix1, uri1);
        testStream = from(model.add(model.asStatement(triple)));
        assertEquals("Didn't find triple in stream from Model!", triple,
                testStream.first());
        assertEquals("Didn't find namespace mapping in stream from Model!",
                singletonMap(prefix1, uri1), testStream.namespaces());
    }

    @Test
    public void testCanContinue() {
        when(mockIterator.hasNext()).thenReturn(true).thenThrow(
                new RuntimeException("Expected.")).thenReturn(true);
        assertTrue(mockIterator.hasNext());
        try {
            mockIterator.hasNext();
        } catch (final RuntimeException ex) {
        }
        assertTrue("Couldn't continue with iteration!",mockIterator.hasNext());
    }

    @Test
    public void testEquals() {
        assertNotEquals(testStream,new Object());
        RdfStream testStreamToCompare = new RdfStream(mockIterator);
        assertEquals(testStream, testStreamToCompare);
        testStreamToCompare.namespace(prefix1, uri1);
        assertEquals(testStream, testStreamToCompare);
        when(mockIterator.hasNext()).thenReturn(false);
        final Iterator<Triple> differentIterator = asList(triple).iterator();
        testStreamToCompare = new RdfStream(differentIterator);
        assertNotEquals(testStream, testStreamToCompare);
    }

    @Test
    public void testHashCode() {
        final RdfStream testStreamToCompare = new RdfStream(mockIterator);
        testStreamToCompare.namespace(prefix1, uri1);
        assertNotEquals(testStream.hashCode(), testStreamToCompare.hashCode());
    }

    @Test
    public void testContext() {
        final Session mockSession = mock(Session.class);
        assertEquals("Didn't retrieve the session we stored!", mockSession,
                testStream.session(mockSession).session());
    }

    @Test
    public void testTopic() {
        final Node mockNode = mock(Node.class);
        assertEquals("Didn't retrieve the session we stored!", mockNode,
                testStream.topic(mockNode).topic());
    }
}
