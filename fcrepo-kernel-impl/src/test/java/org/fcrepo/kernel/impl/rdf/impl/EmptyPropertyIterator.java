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

package org.fcrepo.kernel.impl.rdf.impl;

import java.util.NoSuchElementException;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;

public class EmptyPropertyIterator implements PropertyIterator {

    public static final EmptyPropertyIterator EMPTY_PROPERTY_ITERATOR = new EmptyPropertyIterator();

    @Override
    public void skip(final long skipNum) {
        // NOOP
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public long getPosition() {
        return 0;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        throw new NoSuchElementException();
    }

    @Override
    public Property nextProperty() {
        throw new NoSuchElementException();
    }
}