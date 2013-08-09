/**
 * Copyright 2013 DuraSpace, Inc.
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

import javax.jcr.observation.Event;

/**
 * Simple EventFilter that does no filtering.
 *
 * @author eddies
 * @date Feb 7, 2013
 */
public class NOOPFilter implements EventFilter {

    /**
     * A no-op filter that passes every Event through.
     * @param event
     * @return true under all circumstances
     */
    @Override
    public boolean apply(final Event event) {
        return true;
    }
}