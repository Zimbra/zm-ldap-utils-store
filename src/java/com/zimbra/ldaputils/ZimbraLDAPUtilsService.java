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

import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.DocumentDispatcher;
import com.zimbra.soap.DocumentService;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.LDAPUtilsConstants;

/**
 * @author Greg Solovyev
 */
public class ZimbraLDAPUtilsService implements DocumentService {

    public void registerHandlers(DocumentDispatcher dispatcher) {
        dispatcher.registerHandler(LDAPUtilsConstants.GET_LDAP_ENTRIES_REQUEST, new GetLDAPEntries());
        dispatcher.registerHandler(LDAPUtilsConstants.CREATE_LDAP_ENTRIY_REQUEST, new CreateLDAPEntry());
        dispatcher.registerHandler(LDAPUtilsConstants.MODIFY_LDAP_ENTRIY_REQUEST, new ModifyLDAPEntry());
        dispatcher.registerHandler(LDAPUtilsConstants.RENAME_LDAP_ENTRIY_REQUEST, new RenameLDAPEntry());
        dispatcher.registerHandler(LDAPUtilsConstants.DELETE_LDAP_ENTRIY_REQUEST, new DeleteLDAPEntry());
    }

    public static Element encodeLDAPEntry(Element parent, NamedEntry ld) {
        Element LDAPEntryEl = parent.addElement(LDAPUtilsConstants.E_LDAPEntry);
        LDAPEntryEl.addAttribute(AdminConstants.A_NAME, ld.getName());
        Map<String, Object> attrs = ld.getAttrs(false);
        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            String name = entry.getKey();
            Object value;
            if (name.equals(Provisioning.A_userPassword)) {
                value = "VALUE-BLOCKED";
            } else {
                value = entry.getValue();
            }
            if (value instanceof String[]) {
                String sv[] = (String[]) value;
                for (int i = 0; i < sv.length; i++)
                    LDAPEntryEl.addElement(AdminConstants.E_A).addAttribute(AdminConstants.A_N, name).setText(sv[i]);
            } else if (value instanceof String)
                LDAPEntryEl.addElement(AdminConstants.E_A).addAttribute(AdminConstants.A_N, name).setText((String) value);
        }
        return LDAPEntryEl;
    }
}
