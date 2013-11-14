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

package org.modeshape.jcr;

import org.modeshape.jcr.api.Session;


public abstract class ReadOnlySessions {

    /**
     * A proxy for MODE internals to get a read-only session
     * @param context
     * @return
     */
    public static Session readOnlySessionUnder(JcrSession context) {
        return context.spawnSession(true);
    }
}
