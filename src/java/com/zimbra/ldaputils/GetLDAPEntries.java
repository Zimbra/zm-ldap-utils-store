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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidSearchFilterException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.ldap.LdapUtil;
import com.zimbra.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.cs.service.admin.AdminService;

/**
 * @author Greg Solovyev
 */
public class GetLDAPEntries extends AdminDocumentHandler {
	public static final String C_LDAPEntry = "LDAPEntry";
	
	private static final SearchControls sObjectSC = new SearchControls(SearchControls.OBJECT_SCOPE, 0, 0, null, false, false);
	
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    	ZimbraSoapContext lc = getZimbraSoapContext(context);
        
    	Element b = request.getElement(ZimbraLDAPUtilsService.E_LDAPSEARCHBASE);
        String ldapSearchBase = b.getText();
        
        String query = request.getAttribute(AdminService.E_QUERY);
        List LDAPEntrys;
        LDAPEntrys = searchObjects(query,ldapSearchBase);
        
    	Element response = lc.createElement(ZimbraLDAPUtilsService.GET_LDAP_ENTRIES_RESPONSE);
    	Iterator it = LDAPEntrys.iterator();
    	while(it.hasNext()) {
    		ZimbraLDAPUtilsService.encodeLDAPEntry(response,(LDAPEntry)it.next());
    	}
    	
    	return response;
    }

    public static NamedEntry getObjectByDN(String dn, DirContext initCtxt) throws ServiceException {
        DirContext ctxt = initCtxt;
        try {
            if (ctxt == null)
                ctxt = LdapUtil.getDirContext();
               
            Attributes attrs = ctxt.getAttributes(dn);
            NamedEntry ne = new LDAPEntry(dn, attrs,null);
            return ne;
            
        } catch (NameNotFoundException e) {
            return null;
        } catch (InvalidNameException e) {
            return null;                        
        } catch (NamingException e) {
            throw ServiceException.FAILURE("unable to find dn: "+dn+" message: "+e.getMessage(), e);
        } finally {
            if (initCtxt == null)
                LdapUtil.closeContext(ctxt);
        }
    }
    
    public List<NamedEntry> searchObjects(String query,String base)
    throws ServiceException {
        final List<NamedEntry> result = new ArrayList<NamedEntry>();
        
        NamedEntry.Visitor visitor = new NamedEntry.Visitor() {
            public void visit(NamedEntry entry) {
                result.add(entry);
            }
        };
        
        searchObjects(query,  base,  visitor);

       
        return result;
    }
    

    void searchObjects(String query,  String base, NamedEntry.Visitor visitor)
        throws ServiceException
    {
        DirContext ctxt = null;
        try {
            ctxt = LdapUtil.getDirContext();
            
            SearchControls searchControls = 
                new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 0, null, false, false);

            //Set the page size and initialize the cookie that we pass back in subsequent pages
            int pageSize = 1000;
            byte[] cookie = null;
 
            LdapContext lctxt = (LdapContext)ctxt; 
 
            // we don't want to ever cache any of these, since they might not have all their attributes

            NamingEnumeration ne = null;

            try {
                do {
                    lctxt.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
                    
                    ne = ctxt.search(base, query, searchControls);
                    while (ne != null && ne.hasMore()) {
                        SearchResult sr = (SearchResult) ne.nextElement();
                        String dn = sr.getNameInNamespace();
                        // skip admin accounts
                        if (dn.endsWith("cn=zimbra")) continue;
                        Attributes attrs = sr.getAttributes();
                        visitor.visit(new LDAPEntry(dn, attrs,null));
                    }
                    cookie = getCookie(lctxt);
                } while (cookie != null);
            } finally {
                if (ne != null) ne.close();
            }
        } catch (InvalidSearchFilterException e) {
            throw ServiceException.INVALID_REQUEST("invalid search filter "+e.getMessage(), e);
        } catch (NameNotFoundException e) {
            // happens when base doesn't exist
            ZimbraLog.extensions.warn("unable to list all objects", e);
        } catch (SizeLimitExceededException e) {
            throw AccountServiceException.TOO_MANY_SEARCH_RESULTS("too many search results returned", e);
        } catch (NamingException e) {
            throw ServiceException.FAILURE("unable to list all objects", e);
        } catch (IOException e) {
            throw ServiceException.FAILURE("unable to list all objects", e);            
        } finally {
            LdapUtil.closeContext(ctxt);
        }   
    }
    
    private byte[] getCookie(LdapContext lctxt) throws NamingException {
        Control[] controls = lctxt.getResponseControls();
        if (controls != null) {
            for (int i = 0; i < controls.length; i++) {
                if (controls[i] instanceof PagedResultsResponseControl) {
                    PagedResultsResponseControl prrc =
                        (PagedResultsResponseControl)controls[i];
                    return prrc.getCookie();
                }
            }
        }
        return null;
    }    
}
