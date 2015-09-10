/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     thibaud
 */

package org.nuxeo.labs.operations.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.directory.api.DirectoryService;

/**
 * Adds a new entry ion a vocabulary.
 * <p>
 * Notice: This is for a nuxeo Vocabulary, which is a specific kind of Directory. This code expects the following:
 * <ul>
 * <li>The vocabulary schema <i>must</i> have <code>id</code>, <code>label</code>, <code>obsolete</code> and
 * <code>ordering</code> fields</li>
 * <li>If it is hierarchical, it must also have the <code>parent</code> field</li>
 * </ul>
 */
@Operation(id = VocabularyAddEntryOp.ID, category = Constants.CAT_SERVICES, label = "Vocabulary: Add Entry", description = "Add a new entry in the <i>name</i> directory only if <i>id</i> is not found (an existing entry is not updated). If <i>label</i> is empty, it is set to the id. WARNING: Current user must have enough rights to write in a directory.")
public class VocabularyAddEntryOp {

    public static final String ID = "Vocabulary.AddEntry";

    // Caching infos about vocabularies, to avoid getting the schema and testing the "parent" field at every call.
    // This is because passing a Map with "parent" to a simple vocabulary (non hierarchical) throws an error
    protected static HashMap<String, Boolean> vocabularyAndHasParent = new HashMap<String, Boolean>();

    @Context
    protected DirectoryService directoryService;

    @Context
    protected SchemaManager schemaManager;

    @Param(name = "name", required = true)
    String name;

    @Param(name = "id", required = true)
    String id;

    @Param(name = "label", required = false)
    String label;

    @Param(name = "parent", required = false)
    String parent = "";

    @Param(name = "obsolete", required = false)
    long obsolete = 0;

    @Param(name = "ordering", required = false)
    long ordering = 0;

    @OperationMethod
    public void run() {

        if (StringUtils.isNotBlank(id)) {
            boolean hasParent;
            if (vocabularyAndHasParent.get(name) == null) {
                String dirSchema = directoryService.getDirectorySchema(name);
                Schema schema = schemaManager.getSchema(dirSchema);
                hasParent = schema.hasField("parent");

                vocabularyAndHasParent.put(name, hasParent);
            } else {
                hasParent = vocabularyAndHasParent.get(name);
            }

            org.nuxeo.ecm.directory.Session directorySession = directoryService.open(name);
            if (!directorySession.hasEntry(id)) {

                Map<String, Object> entry = new HashMap<String, Object>();
                entry.put("id", id);
                if (label == null || label.isEmpty()) {
                    label = id;
                }
                entry.put("label", label);
                if (hasParent) {
                    entry.put("parent", parent);
                }
                entry.put("obsolete", obsolete);
                entry.put("ordering", ordering);

                directorySession.createEntry(entry);
            }
            directorySession.close();
        }

    }

}
