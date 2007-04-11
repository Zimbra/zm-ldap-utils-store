/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Portions created by Zimbra are Copyright (C) 2005, 2006 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * The Original Code is: Zimbra Network
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.ldaputils;

import java.util.Map;

import org.dom4j.Namespace;
import org.dom4j.QName;

import com.zimbra.cs.service.account.AccountService;
import com.zimbra.cs.service.admin.AdminService;
import com.zimbra.soap.DocumentDispatcher;
import com.zimbra.soap.DocumentService;
import com.zimbra.soap.Element;

/**
 * @author Greg Solovyev
 */
public class ZimbraLDAPUtilsService implements DocumentService {

	public static final String NAMESPACE_STR = "urn:zimbraAdmin";
	public static final Namespace NAMESPACE = Namespace.get(NAMESPACE_STR);
		
    public static final QName GET_LDAP_ENTRIES_REQUEST = QName.get("GetLDAPEntrysRequest", NAMESPACE);
    public static final QName GET_LDAP_ENTRIES_RESPONSE = QName.get("GetLDAPEntrysResponse", NAMESPACE);
    
    public static final String E_LDAPEntry = "LDAPEntry";
    public static final String E_LDAPSEARCHBASE = "ldapSearchBase";    
    

    public void registerHandlers(DocumentDispatcher dispatcher) {
		dispatcher.registerHandler(GET_LDAP_ENTRIES_REQUEST, new GetLDAPEntries());
    }

    public static Element encodeLDAPEntry(Element parent, LDAPEntry ld) {
        Element LDAPEntryEl = parent.addElement(ZimbraLDAPUtilsService.E_LDAPEntry);
        LDAPEntryEl.addAttribute(AccountService.A_NAME, ld.getName());
        Map<String, Object> attrs = ld.getAttrs(false);
        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String[]) {
                String sv[] = (String[]) value;
                for (int i = 0; i < sv.length; i++)
                	LDAPEntryEl.addElement(AdminService.E_A).addAttribute(AdminService.A_N, name).setText(sv[i]);
            } else if (value instanceof String)
            	LDAPEntryEl.addElement(AdminService.E_A).addAttribute(AdminService.A_N, name).setText((String) value);
        }
        return LDAPEntryEl;
    }    
}
