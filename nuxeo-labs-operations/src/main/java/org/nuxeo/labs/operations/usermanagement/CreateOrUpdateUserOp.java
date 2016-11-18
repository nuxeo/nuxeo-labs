package org.nuxeo.labs.operations.usermanagement;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.usermanager.UserManager;

import java.util.Map;

@Operation(
        id = CreateOrUpdateUserOp.ID,
        category = Constants.CAT_USERS_GROUPS,
        label = "Create or Update User",
        description = "Create or Update User.")
public class CreateOrUpdateUserOp {

    public static final String ID = "CreateOrUpdateUser";

    @Context
    protected CoreSession session;

    @Context
    protected UserManager userManager;

    @Param(name = "username")
    protected String username;

    @Param(name = "password", required = false)
    protected String password;

    @Param(name = "properties", required = false)
    protected Properties properties = new Properties();

    @Param(name = "groups", required = false)
    protected StringList groups = new StringList();


    @OperationMethod
    public DocumentModel run() {
        boolean isUpdate = true;
        DocumentModel userDoc = userManager.getUserModel(username);
        if (userDoc == null) {
            userDoc = userManager.getBareUserModel();
            userDoc.setPropertyValue("user:username", username);
            isUpdate = false;
        }

        if (StringUtils.isNotBlank(password)) {
            userDoc.setPropertyValue("user:password", password);
        }

        for (Map.Entry<String,String> entry: properties.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("user:")) {
                key = "user:"+key;
            }
            userDoc.setPropertyValue(key, entry.getValue());
        }

        if (groups.size()>0) {
            userDoc.setPropertyValue("user:groups",groups);
        }

        if (isUpdate) {
            userManager.updateUser(userDoc);
            return userManager.getUserModel(username);
        } else {
            return userManager.createUser(userDoc);
        }
    }
}
