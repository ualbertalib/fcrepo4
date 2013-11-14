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

package org.fcrepo.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.VersionException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A dynamic proxy that wraps JCR sessions. It is aware of fcrepo transactions,
 * and turns mutating methods (e.g. logout, session) into no-ops. Those no-op'ed
 * methods should be called from the Transaction level instead.
 */
public class TxAwareSession implements TxSession {

    private final String txId;

    private Session session;

    /**
     * @param session a JCR session
     * @param txID the transaction identifier
     */
    public TxAwareSession(final Session session, final String txID) {
        this.session = session;
        this.txId = txID;
    }

    /**
     * {@inheritDoc}
     */
    public Repository getRepository() {
        return session.getRepository();
    }

    /**
     * {@inheritDoc}
     */
    public String getUserID() {
        return session.getUserID();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAttributeNames() {
        return session.getAttributeNames();
    }

    /**
     * {@inheritDoc}
     */
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    public Workspace getWorkspace() {
        return session.getWorkspace();
    }

    /**
     * {@inheritDoc}
     */
    public Node getRootNode() throws RepositoryException {
        return session.getRootNode();
    }

    /**
     * {@inheritDoc}
     */
    public Session impersonate(Credentials credentials) throws LoginException,
        RepositoryException {
        return new TxAwareSession(session.impersonate(credentials), txId);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public Node getNodeByUUID(String uuid) throws ItemNotFoundException,
        RepositoryException {
        return session.getNodeByUUID(uuid);
    }

    /**
     * {@inheritDoc}
     */
    public Node getNodeByIdentifier(String id) throws ItemNotFoundException,
        RepositoryException {
        return session.getNodeByIdentifier(id);
    }

    /**
     * {@inheritDoc}
     */
    public Item getItem(String absPath) throws PathNotFoundException,
        RepositoryException {
        return session.getItem(absPath);
    }

    /**
     * {@inheritDoc}
     */
    public Node getNode(String absPath) throws PathNotFoundException,
            RepositoryException {
        return session.getNode(absPath);
    }

    /**
     * {@inheritDoc}
     */
    public Property getProperty(String absPath) throws PathNotFoundException,
            RepositoryException {
        return session.getProperty(absPath);
    }

    /**
     * {@inheritDoc}
     */
    public boolean itemExists(String absPath) throws RepositoryException {
        return session.itemExists(absPath);
    }

    /**
     * {@inheritDoc}
     */
    public boolean nodeExists(String absPath) throws RepositoryException {
        return session.nodeExists(absPath);
    }

    /**
     * {@inheritDoc}
     */
    public boolean propertyExists(String absPath) throws RepositoryException {
        return session.propertyExists(absPath);
    }

    /**
     * {@inheritDoc}
     */
    public void move(String srcAbsPath, String destAbsPath)
        throws ItemExistsException, PathNotFoundException,
        VersionException, ConstraintViolationException, LockException,
        RepositoryException {
        session.move(srcAbsPath, destAbsPath);
    }

    /**
     * {@inheritDoc}
     */
    public void removeItem(String absPath) throws VersionException,
        LockException, ConstraintViolationException, AccessDeniedException,
        RepositoryException {
        session.removeItem(absPath);
    }

    /**
     * {@inheritDoc}
     */
    public void save() throws AccessDeniedException, ItemExistsException,
        ReferentialIntegrityException, ConstraintViolationException,
        InvalidItemStateException, VersionException, LockException,
        NoSuchNodeTypeException, RepositoryException {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        session.refresh(keepChanges);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPendingChanges() throws RepositoryException {
        return session.hasPendingChanges();
    }

    /**
     * {@inheritDoc}
     */
    public ValueFactory getValueFactory()
        throws UnsupportedRepositoryOperationException, RepositoryException {
        return session.getValueFactory();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String absPath, String actions)
        throws RepositoryException {
        return session.hasPermission(absPath, actions);
    }

    /**
     * {@inheritDoc}
     */
    public void checkPermission(String absPath, String actions)
        throws AccessControlException, RepositoryException {
        session.checkPermission(absPath, actions);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasCapability(String methodName, Object target,
            Object[] arguments) throws RepositoryException {
        return session.hasCapability(methodName, target, arguments);
    }

    /**
     * {@inheritDoc}
     */
    public ContentHandler getImportContentHandler(String parentAbsPath,
            int uuidBehavior)
        throws PathNotFoundException, ConstraintViolationException,
        VersionException, LockException, RepositoryException {
        return session.getImportContentHandler(parentAbsPath, uuidBehavior);
    }

    /**
     * {@inheritDoc}
     */
    public void importXML(
            String parentAbsPath, InputStream in, int uuidBehavior)
        throws IOException, PathNotFoundException, ItemExistsException,
        ConstraintViolationException, VersionException,
        InvalidSerializedDataException, LockException, RepositoryException {
        session.importXML(parentAbsPath, in, uuidBehavior);
    }

    /**
     * {@inheritDoc}
     */
    public void exportSystemView(String absPath, ContentHandler contentHandler,
            boolean skipBinary, boolean noRecurse)
        throws PathNotFoundException, SAXException, RepositoryException {
        session.exportSystemView(absPath, contentHandler, skipBinary, noRecurse);
    }

    /**
     * {@inheritDoc}
     */
    public void exportSystemView(String absPath, OutputStream out,
            boolean skipBinary, boolean noRecurse)
        throws IOException, PathNotFoundException, RepositoryException {
        session.exportSystemView(absPath, out, skipBinary, noRecurse);
    }

    /**
     * {@inheritDoc}
     */
    public void exportDocumentView(String absPath,
            ContentHandler contentHandler,
            boolean skipBinary, boolean noRecurse)
        throws PathNotFoundException, SAXException, RepositoryException {
        session.exportDocumentView(absPath, contentHandler, skipBinary,
                noRecurse);
    }

    /**
     * {@inheritDoc}
     */
    public void exportDocumentView(String absPath, OutputStream out,
            boolean skipBinary, boolean noRecurse) throws IOException,
            PathNotFoundException, RepositoryException {
        session.exportDocumentView(absPath, out, skipBinary, noRecurse);
    }

    /**
     * {@inheritDoc}
     */
    public void setNamespacePrefix(String prefix, String uri)
        throws NamespaceException, RepositoryException {
        session.setNamespacePrefix(prefix, uri);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getNamespacePrefixes() throws RepositoryException {
        return session.getNamespacePrefixes();
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespaceURI(String prefix)
        throws NamespaceException, RepositoryException {
        return session.getNamespaceURI(prefix);
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespacePrefix(String uri)
        throws NamespaceException, RepositoryException {
        return session.getNamespacePrefix(uri);
    }

    /**
     * {@inheritDoc}
     */
    public void logout() {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLive() {
        return session.isLive();
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public void addLockToken(String lt) {
        session.addLockToken(lt);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public String[] getLockTokens() {
        return session.getLockTokens();
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public void removeLockToken(String lt) {
        session.removeLockToken(lt);
    }

    /**
     * {@inheritDoc}
     */
    public AccessControlManager getAccessControlManager()
        throws UnsupportedRepositoryOperationException, RepositoryException {
        return session.getAccessControlManager();
    }

    /**
     * {@inheritDoc}
     */
    public RetentionManager getRetentionManager()
        throws UnsupportedRepositoryOperationException, RepositoryException {
        return session.getRetentionManager();
    }

    /**
     * {@inheritDoc}
     */
    public String getTxId() {
        return txId;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "TxAwareSession(" + super.toString() + ")";
    }

}
