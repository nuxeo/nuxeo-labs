/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     Thibaud Arguillere
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
