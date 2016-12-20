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
 *     Benjamin JALON <bjalon@nuxeo.com>
 */
package org.nuxeo.labs.operations.notification;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.mail.Composer;
import org.nuxeo.ecm.automation.core.mail.Mailer.Message;
import org.nuxeo.ecm.automation.core.operations.notification.MailTemplateHelper;
import org.nuxeo.ecm.automation.core.scripting.Scripting;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationServiceHelper;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bjalon@nuxeo.com">Benjamin JALON</a>
 * @since 5.7
 * Deprecated since 8.10
 */
@Deprecated
@Operation(id = AdvancedSendEmail.ID, category = Constants.CAT_NOTIFICATION, label = "AdvancedSendEmail", description = "Send Email according parameters set. <p> EMail Address resolution is as following if you check strict : <ul><li> if you set a string that starts with 'user:xxxx' will resolve email of username xxxx. </li><li> if you set a string that starts with 'group:xxxx, will resolve all users and add their email. </li><li> if not these 2 conditions, if the value contains an '@' then will concider it as an email directly. </li><li> If there is no match then throw an exception. </li></ul><p> if you uncheck the strict checkbox, you have the same behavior explain above, but the exception is not throw and will try to concider the value as a username <ul><li> if no match then will concider a group </li><li> and if no match again then empty string</li></ul>. <p>You can also put in values : <ul><li>a NuxeoPrincipal </li><li> or a list of NuxeoPrincipal </li><li> or a list of String.</li><li> or a list of mixed value.</li></ul> <p>For list of string, the resolution is the same as explain previously.<p> If you check 'rollbackOnError' then error will be catch if one is thrown. <p>For the file value just put the xpath value of field that stores the file (file:content, files:/files/0/content, myschema:myfield, myschema:/myfield/0/content)")
public class AdvancedSendEmail {

    public static final String ID = "AdvancedSendEmail";

    protected static final Log log = LogFactory.getLog(AdvancedSendEmail.class);

    private static final String LIST_ITEM_TYPE_ERROR_MESSAGE = "Field type \"%s\" store a list but an element in this list is not neither a String, nor a NuxeoPrincipal, please check the Studio Project Configuration: %s";

    private static final String TYPE_ERROR_MESSAGE = "Field type \"%s\" doesn't store neither String, nor a List, nor a NuxeoPrincipal, please check the Studio Project Configuration: %s";

    private static final String WARN_ON_SEND_EXCEPTION = "An error occured while trying to execute the %s operation, see complete stack trace below. Continuing chain since 'rollbackOnError' was set to false.";

    public static final Composer COMPOSER = new Composer();

    @Context
    protected OperationContext ctx;

    @Context
    protected UserManager umgr;

    @Param(name = "subject", required = false)
    protected String subject;

    @Param(name = "message", required = false, widget = Constants.W_MULTILINE_TEXT)
    protected String message;

    @Param(name = "HTML", required = false)
    protected boolean asHtml = false;

    @Param(name = "from")
    protected String from;

    @Param(name = "to")
    protected Object to;

    @Param(name = "cc", required = false)
    protected Object cc;

    @Param(name = "bcc", required = false)
    protected Object bcc;

    @Param(name = "replyto", required = false)
    protected Object replyto;

    @Param(name = "files", required = false)
    protected StringList blobXpath;

    @Param(name = "viewId", required = false, values = { "view_documents" })
    protected String viewId = "view_documents";

    @Param(name = "rollbackOnError", required = false, values = { "true" })
    protected boolean rollbackOnError = true;

    @Param(name = "Strict User Resolution", required = false)
    protected boolean isStrict = false;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        send(doc);
        return doc;
    }

    protected void send(DocumentModel doc) throws Exception {
        try {
            Message msg = createContentMessage(doc);
            addToEmails(msg);
            addFromEmails(msg);
            addCcEmails(msg);
            addBccEmails(msg);
            addReplyToEmails(msg);
            msg.setSubject(subject);
            msg.send();
        } catch (Exception e) {
            if (rollbackOnError) {
                throw e;
            } else {
                log.warn(String.format(WARN_ON_SEND_EXCEPTION, ID), e);
            }
        }
    }

    protected Message createContentMessage(DocumentModel doc) throws Exception {
        Map<String, Object> ctx = createContext(doc);

        String content = StringEscapeUtils.unescapeHtml(message);

        if (blobXpath == null) {
            if (asHtml) {
                return COMPOSER.newHtmlMessage(content, ctx);
            } else {
                return COMPOSER.newTextMessage(content, ctx);
            }
        } else {
            ArrayList<Blob> blobs = new ArrayList<Blob>();
            for (String xpath : blobXpath) {
                Property p = doc.getProperty(xpath);
                if (p instanceof ListProperty) {
                    for (Property pp : p) {
                        Object o = pp.getValue();
                        if (o instanceof Blob) {
                            blobs.add((Blob) o);
                        }
                    }
                } else {
                    Object o = p.getValue();
                    if (o instanceof Blob) {
                        blobs.add((Blob) o);
                    }
                }
            }
            return COMPOSER.newMixedMessage(content, ctx, asHtml ? "html" : "plain", blobs);
        }
    }

    private Map<String, Object> createContext(DocumentModel doc) throws Exception {
        Map<String, Object> map = Scripting.initBindings(ctx);
        map.put("Document", doc);
        map.put("docUrl", MailTemplateHelper.getDocumentUrl(doc, viewId));
        map.put("subject", subject);
        map.put("to", to);
        map.put("from", from);
        map.put("cc", cc);
        map.put("bcc", bcc);
        map.put("replyTo", replyto);
        map.put("viewId", viewId);
        map.put("baseUrl", NotificationServiceHelper.getNotificationService().getServerUrlPrefix());
        map.put("Runtime", Framework.getRuntime());
        return map;
    }

    protected void addToEmails(Message msg) throws NuxeoException, MessagingException {
        for (String email : getEmails(to, "TO")) {
            msg.addTo(email);
        }
    }

    protected void addFromEmails(Message msg) throws NuxeoException, MessagingException {
        for (String email : getEmails(from, "FROM")) {
            msg.addFrom(email);
        }
    }

    protected void addCcEmails(Message msg) throws NuxeoException, MessagingException {
        for (String email : getEmails(cc, "CC")) {
            msg.addCc(email);
        }
    }

    protected void addBccEmails(Message msg) throws NuxeoException, MessagingException {
        for (String email : getEmails(bcc, "BCC")) {
            msg.addBcc(email);
        }
    }

    protected void addReplyToEmails(Message msg) throws NuxeoException, MessagingException {

        if (replyto == null) {
            return;
        }

        if ("".equals(replyto)) {
            return;
        }

        List<String> emails = getEmails(replyto, "ReplyTo");
        Address[] replyToValue = new InternetAddress[emails.size()];
        for (int i = 0; i < emails.size(); i++) {
            replyToValue[i] = new InternetAddress(emails.get(i));
        }
        msg.setReplyTo(replyToValue);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getEmails(Object value, String fieldType) throws NuxeoException {
        List<String> result = new ArrayList<String>();

        if (value == null) {
            return result;
        }

        if ("".equals(value)) {
            return result;
        }

        if (value instanceof String) {
            result.addAll(getEmailFromString((String) value));
            return result;
        }

        if (value instanceof List) {
            for (Object user : (List<Object>) value) {
                if (user instanceof String) {
                    result.addAll(getEmailFromString((String) user));
                } else {
                    if (user instanceof NuxeoPrincipal) {
                        result.add(((NuxeoPrincipal) user).getEmail());
                    } else {
                        throw new NuxeoException(String.format(LIST_ITEM_TYPE_ERROR_MESSAGE, fieldType, user.getClass()
                                                                                                            .getName()));
                    }

                }
            }
            return result;
        }

        if (value instanceof NuxeoPrincipal) {
            result.add(((NuxeoPrincipal) value).getEmail());
            return result;
        }

        throw new NuxeoException(String.format(TYPE_ERROR_MESSAGE, fieldType, value.getClass().getName()));

    }

    protected List<String> getEmailFromString(String value) throws NuxeoException {
        List<String> result = null;

        if (value.startsWith("user:")) {
            String userId = value.substring("user:".length());
            result = getEmailUserFromUserId(userId);
            log.debug("User email found from (username) " + value + " was " + result.get(0));
            return result;
        }

        if (value.startsWith("group:")) {
            String groupId = value.substring("group:".length());
            result = getEmailsFromGroupId(groupId);
            log.debug("User emails found from (groupId) " + value + " was [" + StringUtils.join(result, ",") + "]");
            return result;
        }

        if (!isStrict) {
            result = getEmailUserFromUserId(value);
            if (result != null && !result.isEmpty()) {
                log.debug("User email found from (username) " + value + " was " + result.get(0));
                return result;
            }

            result = getEmailsFromGroupId(value);
            if (result != null && !result.isEmpty()) {
                log.debug("User emails found from (groupId) " + value + " was [" + StringUtils.join(result, ",") + "]");
                return result;
            }
        }

        if (value.contains("@")) {
            log.debug("User email (esasily) found from (email) " + value + " was " + value);
            return Collections.singletonList(value);
        }

        if (isStrict) {
            throw new NuxeoException("User or group not found and not an email " + value);
        }

        log.debug("User emails found from (groupId) " + value + " was [" + StringUtils.join(result, ",") + "]");
        return null;
    }

    private List<String> getEmailUserFromUserId(String userId) throws NuxeoException, PropertyException {
        DocumentModel user = umgr.getUserModel(userId);
        if (user != null && user.getPropertyValue("email") != null
                && ((String) user.getPropertyValue("email")).contains("@")) {

            return Collections.singletonList(((String) user.getPropertyValue("email")));
        }
        return null;
    }

    private List<String> getEmailsFromGroupId(String groupId) throws NuxeoException, PropertyException {
        if (umgr.getGroup(groupId) == null) {
            return null;
        }

        List<String> users = umgr.getUsersInGroup(groupId);
        if (users != null) {
            List<String> result = new ArrayList<String>();
            for (String userId : users) {
                DocumentModel user = umgr.getUserModel(userId);
                if (user != null && user.getPropertyValue("email") != null
                        && ((String) user.getPropertyValue("email")).contains("@")) {
                    result.add((String) user.getPropertyValue("email"));
                }

            }
            return result;
        }
        return null;
    }

}
