package org.nuxeo.labs.operations.usermanagement;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.usermanager.UserManager;

@Operation(
        id= CreateOrUpdateGroupOp.ID,
        category= Constants.CAT_USERS_GROUPS,
        label="Create or Update Group",
        description="Create or Update Group")
public class CreateOrUpdateGroupOp {

    public static final String ID = "CreateOrUpdateGroup";

    @Context
    protected CoreSession session;

    @Context
    protected UserManager userManager;

    @Param(name = "groupname")
    protected String groupname;

    @Param(name = "grouplabel")
    protected String grouplabel;

    @Param(name = "tenantId", required = false)
    protected String tenantId;

    @OperationMethod
    public DocumentModel run() {
        boolean isUpdate = true;
        DocumentModel groupDoc = userManager.getGroupModel(getActualGroupName());
        if (groupDoc==null) {
            groupDoc = userManager.getBareGroupModel();
            groupDoc.setPropertyValue("group:groupname",groupname);
            isUpdate = false;
        }

        groupDoc.setPropertyValue("group:grouplabel",grouplabel);
        if (StringUtils.isNotBlank(tenantId)) {
            groupDoc.setPropertyValue("group:tenantId", tenantId);
        }
        if (!isUpdate) {
            return userManager.createGroup(groupDoc);
        } else {
            userManager.updateGroup(groupDoc);
            return userManager.getGroupModel(getActualGroupName());
        }
    }

    protected String getActualGroupName() {
        if (StringUtils.isNotBlank(tenantId)) {
            return "tenant_"+tenantId+"_"+groupname;
        } else {
            return groupname;
        }
    }

}
