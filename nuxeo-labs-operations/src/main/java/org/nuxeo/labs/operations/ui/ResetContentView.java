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
 *     jfletcher
 */

package org.nuxeo.labs.operations.ui;

import org.jboss.seam.contexts.Contexts;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.platform.contentview.seam.ContentViewActions;

/**
 * 
 */
@Operation(id = ResetContentView.ID, category = Constants.CAT_UI, requires = Constants.SEAM_CONTEXT, label = "Reset Content View", description = "")
public class ResetContentView {

    public static final String ID = "ResetContentView";

    @Param(name = "Content View Name", required = true)
    protected String contentViewName;

    @OperationMethod
    public void run() {
        ((ContentViewActions) Contexts.getConversationContext().get("contentViewActions")).reset(contentViewName);

    }

}
