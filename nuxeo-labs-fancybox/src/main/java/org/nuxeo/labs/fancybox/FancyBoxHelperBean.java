/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     
 */

package org.nuxeo.labs.fancybox;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.SimpleDocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.runtime.api.Framework;

@Name("fancyBoxHelper")
@Scope(CONVERSATION)
public class FancyBoxHelperBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(FancyBoxHelperBean.class);

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected Map<String, String> messages;

    protected DocumentModel fictiveDocumentModel;

    public DocumentModel getDocumentModel() {
        if (fictiveDocumentModel == null) {
            fictiveDocumentModel = new SimpleDocumentModel();
        }
        return fictiveDocumentModel;
    }

    public String process(String automationChain) throws NuxeoException {
        if (fictiveDocumentModel != null) {

            // Input setting
            OperationContext ctx = new OperationContext(documentManager);

            DocumentModel doc = navigationContext.getCurrentDocument();

            ctx.setInput(doc);
            // Context Variable setting
            ctx.put("data", fictiveDocumentModel);

            try {
                AutomationService service = Framework.getLocalService(AutomationService.class);
                service.run(ctx, automationChain);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                log.error("Could not run the chain: " + automationChain, e);
            }

            fictiveDocumentModel = null;
        }

        return null;
    }

    public void cancel() {
        fictiveDocumentModel = null;
    }

}
