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
 *     Frederic Vadon
 */
package org.nuxeo.labs;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublisherService;
import org.nuxeo.ecm.platform.rendition.publisher.RenditionPublicationFactory;
import org.nuxeo.template.adapters.doc.TemplateBindings;

/**
 * @author fvadon
 */
@Operation(id = PublishTemplateRenditionOperation.ID, category = Constants.CAT_DOCUMENT, label = "Publish Template Rendition", description = "Operation to publish a rendition of a document, the parameters are the target section and the template name: delivery for example. "
        + "Gives back the original document for now (should give the published proxy of the document in the next version).")
public class PublishTemplateRenditionOperation {

    public static final String ID = "PublishTemplateRendition";

    @Context
    protected CoreSession session;

    @Context
    protected PublisherService service;

    @Param(name = "templateName")
    protected String templateName = TemplateBindings.DEFAULT_BINDING;

    @Param(name = "target")
    protected DocumentModel target;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws NuxeoException {
        Map<String, String> params = new HashMap<String, String>();
        PublicationTree tree = service.getPublicationTreeFor(target, session);
        params.put(RenditionPublicationFactory.RENDITION_NAME_PARAMETER_KEY, templateName);
        service.publish(doc, tree.getNodeByPath(target.getPathAsString()), params);

        return doc;
    }

}
