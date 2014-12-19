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

import static com.googlecode.totallylazy.Unchecked.cast;
import static com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel;
import static java.util.Arrays.asList;
import static java.util.Objects.hash;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Session;

import com.googlecode.totallylazy.ForwardOnlySequence;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Iterators;
import com.googlecode.totallylazy.Sequence;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * A stream of RDF triples along with some useful context.
 *
 * @author ajs6f
 * @since Oct 9, 2013
 */
public class RdfStream extends Sequence<Triple> {

    private final Map<String, String> namespaces = new HashMap<>();

    protected Iterator<Triple> triples;

    protected Session context;

    protected Node topic;

    /**
     * Constructor that begins the stream with proffered triples.
     *
     * @param triples
     */
    public <Tr extends Triple, T extends Iterator<Tr>> RdfStream(final T triples) {
        this.triples = cast(triples);
    }

    /**
     * Constructor that begins the stream with proffered triples.
     *
     * @param triples
     */
    public <Tr extends Triple, T extends Iterable<Tr>> RdfStream(final T triples) {
        this(triples.iterator());
    }

    /**
     * Constructor that begins the stream with proffered triples.
     *
     * @param triples
     */
    @SafeVarargs
    public <T extends Triple> RdfStream(final T... triples) {
        this(asList(triples));
    }

    /**
     * Returns the proffered {@link Triple}s with the context of this RdfStream.
     *
     * @param stream
     * @return proffered Triples with the context of this RDFStream
     */
    public <Tr extends Triple, T extends Iterator<Tr>> RdfStream withThisContext(final T stream) {
        return new RdfStream(stream).namespaces(namespaces()).topic(topic());
    }

    /**
     * Returns the proffered {@link Triple}s with the context of this RdfStream.
     *
     * @param stream
     * @return proffered Triples with the context of this RDFStream
     */
    public <Tr extends Triple, T extends Iterable<Tr>> RdfStream withThisContext(final T stream) {
        return new RdfStream(stream).namespaces(namespaces()).topic(topic());
    }

    /**
     * @return the {@link Triple}s in this stream
     */
    public Iterator<Triple> triples() {
        return triples;
    }

    /**
     * RdfStream
     *
     * @param prefix
     * @param uri
     * @return This object for continued use.
     */
    public RdfStream namespace(final String prefix, final String uri) {
        namespaces.put(prefix, uri);
        return this;
    }

    /**
     * @param nses
     * @return This object for continued use.
     */
    public RdfStream namespaces(final Map<String, String> nses) {
        namespaces.putAll(nses);
        return this;
    }

    /**
     * @return The {@link Session} in context
     */
    public Session session() {
        return this.context;
    }

    /**
     * Sets the JCR context of this stream
     *
     * @param session The {@link Session} in context
     */
    public RdfStream session(final Session session) {
        this.context = session;
        return this;
    }

    /**
     * @return The {@link Node} topic in context
     */
    public Node topic() {
        return this.topic;
    }

    /**
     * Sets the topic of this stream
     *
     * @param topic The {@link Node} topic in context
     */
    public RdfStream topic(final Node topic) {
        this.topic = topic;
        return this;
    }

    /**
     * WARNING! This method exhausts the RdfStream on which it is called!
     *
     * @return A {@link Model} containing the prefix mappings and triples in this stream of RDF
     */
    public Model asModel() {
        final Model model = createDefaultModel();
        model.setNsPrefixes(namespaces());
        while (triples().hasNext()) {
            model.add(model.asStatement(triples.next()));
        }
        return model;
    }

    /**
     * @param model A {@link Model} containing the prefix mappings and triples to be put into this stream of RDF
     * @return RDFStream
     */
    public static RdfStream from(final Model model) {
        return new RdfStream(new ForwardOnlySequence<>(model.listStatements()).map(statement2triple))
                .namespaces(model.getNsPrefixMap());
    }

    /**
     * @param triples triples to be put into this stream of RDF
     * @return RDFStream
     */
    public static RdfStream from(final Sequence<Triple> triples) {
        return new RdfStream(triples);
    }

    public static Function1<Statement, Triple> statement2triple = new Function1<Statement, Triple>() {

        @Override
        public Triple call(final Statement s) {
            return s.asTriple();
        }

    };

    /**
     * @return Namespaces in scope for this stream.
     */
    public Map<String, String> namespaces() {
        return namespaces;
    }

    /*
     * We ignore duplicated triples for equality. (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof RdfStream)) {
            return false;
        }
        final RdfStream rdfo = (RdfStream) o;
        return rdfo.triples().equals(this.triples());
    }

    @Override
    public int hashCode() {
        return hash(namespaces(), triples(), topic());
    }

    @Override
    public Iterator<Triple> iterator() {
        return triples();
    }

    @Override
    public Sequence<Triple> append(final Triple t) {
        triples = Iterators.add(triples, t);
        return this;
    }

    @Override
    public Sequence<Triple> join(final Iterable<? extends Triple> ts) {
        triples = Iterators.join(triples, ts.iterator());
        return this;
    }

}
