/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2016, 2019 Synacor, Inc.
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

import java.util.List;
import java.util.Map;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.LDAPUtilsConstants;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.ldap.entry.LdapDomain;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;



/**
 * @author Greg Solovyev
 */
public class GetLDAPEntries extends AdminDocumentHandler {
    public static final String C_LDAPEntry = "LDAPEntry";

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext lc = getZimbraSoapContext(context);
        OperationContext octxt = getOperationContext(lc, context);
        boolean allowAccess = LC.enable_delegated_admin_ldap_access.booleanValue();
        if(octxt.getAuthToken().isDelegatedAdmin() && !allowAccess) {
            throw ServiceException.PERM_DENIED("Delegated admin not can not access LDAP");
        }
        Element b = request.getElement(LDAPUtilsConstants.E_LDAPSEARCHBASE);
        String ldapSearchBase;
        if(isDomainAdminOnly(lc)) {
            ldapSearchBase = ((LdapDomain)getAuthTokenAccountDomain(lc)).getDN();
        } else {
            ldapSearchBase = b.getText();
        }
        String sortBy = request.getAttribute(AdminConstants.A_SORT_BY, null);
        boolean sortAscending = request.getAttributeBool(AdminConstants.A_SORT_ASCENDING, true);
        int limit = (int) request.getAttributeLong(AdminConstants.A_LIMIT, Integer.MAX_VALUE);
        if (limit == 0)
            limit = Integer.MAX_VALUE;

        int offset = (int) request.getAttributeLong(AdminConstants.A_OFFSET, 0);
        String query = request.getAttribute(AdminConstants.E_QUERY);

        List<NamedEntry> LDAPEntrys;
        LDAPEntrys = LDAPUtilsHelper.getInstance().searchObjects(query,ldapSearchBase,sortBy,sortAscending);

        Element response = lc.createElement(LDAPUtilsConstants.GET_LDAP_ENTRIES_RESPONSE);
        int i, limitMax = offset+limit;
        for (i=offset; i < limitMax && i < LDAPEntrys.size(); i++) {
            NamedEntry entry = LDAPEntrys.get(i);
            ZimbraLDAPUtilsService.encodeLDAPEntry(response,entry);
        }

        return response;
    }

    /** Returns whether domain admin auth is sufficient to run this command.
     *  This should be overriden only on admin commands that can be run in a
     *  restricted "domain admin" mode. */
    @Override
    public boolean domainAuthSufficient(Map<String, Object> context) {
        return true;
    }


}
