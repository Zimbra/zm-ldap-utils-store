package com.zimbra.ldaputils;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributeIdentifierException;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.InvalidAttributesException;
import javax.naming.directory.SchemaViolationException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapUtil;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.cs.service.admin.AdminService;
import com.zimbra.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

public class ModifyLDAPEntry extends AdminDocumentHandler {

	public Element handle(Element request, Map<String, Object> context)
			throws ServiceException {

		ZimbraSoapContext lc = getZimbraSoapContext(context);
		DirContext ctxt = null;
		ctxt = LdapUtil.getDirContext(true);

		Provisioning prov = Provisioning.getInstance();

		String dn = request.getAttribute(ZimbraLDAPUtilsService.E_DN);
		Map<String, Object> attrs = AdminService.getAttrs(request, true);

		NamedEntry ne = GetLDAPEntries.getObjectByDN(dn, ctxt);
		prov.modifyAttrs(ne, attrs, true);

		ZimbraLog.security.info(ZimbraLog.encodeAttrs(new String[] { "cmd",
				"SaveLDAPEntry", "dn", dn }, attrs));

		Element response = lc
				.createElement(ZimbraLDAPUtilsService.MODIFY_LDAP_ENTRY_RESPONSE);
		ZimbraLDAPUtilsService.encodeLDAPEntry(response, ne);

		return response;
	}
}
