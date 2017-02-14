/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.SoapServlet;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.qa.unittest.TestLDAPUtilsHelper;
import com.zimbra.qa.unittest.ZimbraSuite;
/**
 * @author Greg Solovyev
 */
public class ZimbraLDAPUtilsExtension implements ZimbraExtension {
    public static final String EXTENSION_NAME_ZIMBRASAMBA = "zimbrasamba";
    
    public void init() throws ServiceException {
    	AttributeManager.getInstance().makeDomainAdminModifiable("isSpecialNTAccount");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaSID");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaAcctFlags");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaBadPasswordCount");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaBadPasswordTime");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaDomainName");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaDomainSID");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaHomeDrive");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaHomePath");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaKickoffTime");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaLMPassword");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaLogoffTime");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaLogonHours");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaLogonScript");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaLogonTime");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaMungedDial");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaNTPassword");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaPasswordHistory");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaPrimaryGroupSID");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaProfilePath");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaPwdCanChange");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaPwdLastSet");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaPwdMustChange");
    	AttributeManager.getInstance().makeDomainAdminModifiable("sambaUserWorkstations");

    	AttributeManager.getInstance().makeDomainAdminModifiable("gidNumber");
    	AttributeManager.getInstance().makeDomainAdminModifiable("homeDirectory");
    	AttributeManager.getInstance().makeDomainAdminModifiable("uidNumber");
    	AttributeManager.getInstance().makeDomainAdminModifiable("gecos");
    	AttributeManager.getInstance().makeDomainAdminModifiable("loginShell");
    	AttributeManager.getInstance().makeDomainAdminModifiable("userPassword");   	
    	
        SoapServlet.addService("AdminServlet", new ZimbraLDAPUtilsService());
        try {
            ZimbraSuite.addTest(TestLDAPUtilsHelper.class);
        } catch (NoClassDefFoundError e) {
            // Expected in production, because JUnit is not available.
            ZimbraLog.test.debug("Unable to load ZimbraLDAPUtils SOAP tests.", e);
        }
    }

    public void destroy() {
        
    }
    
    public String getName() {
        return EXTENSION_NAME_ZIMBRASAMBA;
    }

}
