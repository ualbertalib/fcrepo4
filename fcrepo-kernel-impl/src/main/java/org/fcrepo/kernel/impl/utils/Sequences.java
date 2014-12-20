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

package org.fcrepo.kernel.impl.utils;

import com.googlecode.totallylazy.Unchecked;

import java.util.Iterator;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.googlecode.totallylazy.ForwardOnlySequence;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;

/**
 * @author ajs6f
 *
 */
public class Sequences {

    private Sequences() {
    }

    /**
     * @param elements
     * @return
     */
    public static <S extends T, T> Sequence<S> sequence(final Iterator<S> elements) {
        return new ForwardOnlySequence<>(Unchecked.<Iterator<S>>cast(elements));
    }

    /**
     * @param iterable
     * @return
     */
    public static <T> Sequence<T> sequence(final Iterable<? extends T> iterable) {
        return com.googlecode.totallylazy.Sequences.sequence(iterable);
    }

    /**
     * @param element
     * @return
     */
    @SafeVarargs
    public static <T> Sequence<T> sequence(final T... element) {
        return com.googlecode.totallylazy.Sequences.sequence(element);
    }

    /**
     * @return
     */
    public static <T> Sequence<T> sequence() {
        return com.googlecode.totallylazy.Sequences.sequence();
    }

    /**
     * @author ajs6f
     *
     */
    public static class functions {

        /**
         * Converts any {@link Item} to its parent {@link Node}
         */
        public static Function1<Item, Node> toParent = new Function1<Item, Node>() {

            @Override
            public Node call(final Item child) throws RepositoryException {
                return child.getParent();
            }
        };
    }
}
