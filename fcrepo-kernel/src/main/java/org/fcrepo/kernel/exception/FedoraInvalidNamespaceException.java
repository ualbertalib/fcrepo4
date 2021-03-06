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
package org.fcrepo.kernel.exception;


/**
 * Indicates a namespace used in a CRUD request has not been registered in the repository
 *
 * @author whikloj
 * @since September 12, 2014
 */
public class FedoraInvalidNamespaceException extends RepositoryRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Ordinary constructor
     *
     * @param msg
     */
    public FedoraInvalidNamespaceException(final String msg) {
        super(msg);
    }

    /**
     * Ordinary constructor
     *
     * @param msg
     * @param rootCause
     */
    public FedoraInvalidNamespaceException(final String msg, final Throwable rootCause) {
        super(msg, rootCause);
    }

}
