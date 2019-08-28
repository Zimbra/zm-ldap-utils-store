/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2016, 2019 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.ldaputils;

import java.util.Map;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.cs.service.admin.AdminService;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.LDAPUtilsConstants;
import com.zimbra.soap.ZimbraSoapContext;
/**
 * @author Greg Solovyev
 */
public class ModifyLDAPEntry extends AdminDocumentHandler {

    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException {

        ZimbraSoapContext lc = getZimbraSoapContext(context);
        OperationContext octxt = getOperationContext(lc, context);
        AuthToken authToken = octxt.getAuthToken();
        boolean allowAccess = LC.enable_delegated_admin_ldap_access.booleanValue();
        if(octxt.getAuthToken().isDelegatedAdmin() && !allowAccess) {
            throw ServiceException.PERM_DENIED("Delegated admin not can not modify LDAP");
        }
        String dn = request.getAttribute(LDAPUtilsConstants.E_DN);
        if(dn==null)
            throw ServiceException.INVALID_REQUEST("Missing request parameter: "+LDAPUtilsConstants.E_DN, null);

        Map<String, Object> attrs = AdminService.getAttrs(request);
        if (attrs.containsKey("zimbraIsAdminAccount") && !authToken.isAdmin()) {
            throw ServiceException.PERM_DENIED("Can not modify attribute 'zimbraIsAdminAccount'");
        }

        NamedEntry newNe = LDAPUtilsHelper.getInstance().modifyLDAPEntry(dn,  attrs);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(new String[] { "cmd",
                    "SaveLDAPEntry", "dn", dn }, attrs));

        Element response = lc.createElement(LDAPUtilsConstants.MODIFY_LDAP_ENTRY_RESPONSE);
        ZimbraLDAPUtilsService.encodeLDAPEntry(response, newNe);

        return response;

    }
}
