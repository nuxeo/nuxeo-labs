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
 *     
 */

package org.nuxeo.labs.operations.ui;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.platform.ui.web.auth.NXAuthConstants;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author fvadon
 */
@Operation(id = NavigateToUrl.ID, category = Constants.CAT_UI, label = "Navigate To Url", description = "Redirect to the a nuxeo URL passed as a parameter, for instance the parameter can be: /nuxeo/site/automation/doc")
public class NavigateToUrl {

    public static final String ID = "NavigateToUrl";

    @Param(name = "URL")
    protected String URL;

    @Context
    protected OperationContext ctx;

    @OperationMethod
    public void run() throws OperationException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
                                                                         .getExternalContext()
                                                                         .getResponse();
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                                                                      .getExternalContext()
                                                                      .getRequest();

        String targetURL = URL;

        try {
            // Operation was probably triggered by a POST
            // so we need to de-activate the ResponseWrapper that would
            // rewrite the URL
            request.setAttribute(NXAuthConstants.DISABLE_REDIRECT_REQUEST_KEY, new Boolean(true));
            // send the redirect
            response.sendRedirect(targetURL);
            // mark all JSF processing as completed
            response.flushBuffer();
            FacesContext.getCurrentInstance().responseComplete();
            // set Outcome to null (just in case)
            ctx.getVars().put("Outcome", null);
        } catch (IOException e) {
            throw new OperationException("cannot redirect");
        }

    }

}
