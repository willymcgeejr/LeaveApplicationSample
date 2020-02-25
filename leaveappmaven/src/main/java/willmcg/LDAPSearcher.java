package willmcg;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.Hashtable;

/** This class provides all methods for LDAP/Active Directory interactions and queries */
public class LDAPSearcher {

  protected String ldapURL = "REDACTED";
  protected String ldapPrincipal = "REDACTED";
  protected String ldapCredentials = "REDACTED";

  /**
   * Lookup details about an employee based on their username
   *
   * @param username the username to query for in AD (ex. smithj)
   * @return an Employee object containing the employee's details
   */
  public Employee search(String username) {
    DirContext ldapContext;
    Employee employee = new Employee();
    try {
      Hashtable<String, String> ldapEnv = new Hashtable<String, String>(11);
      ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      ldapEnv.put(Context.PROVIDER_URL, ldapURL);
      ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
      ldapEnv.put(Context.SECURITY_PRINCIPAL, ldapPrincipal);
      ldapEnv.put(Context.SECURITY_CREDENTIALS, ldapCredentials);
      ldapEnv.put(Context.SECURITY_PROTOCOL, "simple");
      ldapContext = new InitialDirContext(ldapEnv);

      // Create the search controls
      SearchControls searchCtls = new SearchControls();

      // Specify the attributes to return
      String returnedAtts[] = {"cn", "givenName", "samAccountName", "mail", "manager"};
      searchCtls.setReturningAttributes(returnedAtts);

      // Specify the search scope
      searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

      // specify the LDAP search filter
      String searchFilter = "(&(objectClass=user))";

      // Specify the Base for the search
      String searchBase = "dc=saa,dc=local";

      // Search for objects using the filter
      NamingEnumeration<SearchResult> answer =
          ldapContext.search(searchBase, "sAMAccountName=" + username, searchCtls);

      // Loop through the search results
      while (answer.hasMoreElements()) {
        SearchResult sr = answer.next();
        Attributes attrs = sr.getAttributes();
        employee.setUsername(attrs.get("samAccountName").toString());
        employee.setEmail(attrs.get("mail").toString());
        employee.setName(attrs.get("cn").toString());
        employee.setManager(attrs.get("manager").toString());
      }
      ldapContext.close();
    } catch (Exception e) {
      System.out.println(" Search error: " + e);
      e.printStackTrace();
      System.exit(-1);
    }
    return employee;
  }

  /**
   * Lookup details about an employee by their common name in AD
   *
   * @param cn the common name of the employee (ex. Joe Smith)
   * @return an Employee object containing the employee's details
   */
  public Employee cnSearch(String cn) {
    DirContext ldapContext;
    Employee employee = new Employee();
    try {
      Hashtable<String, String> ldapEnv = new Hashtable<String, String>(11);
      ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      ldapEnv.put(Context.PROVIDER_URL, ldapURL);
      ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
      ldapEnv.put(Context.SECURITY_PRINCIPAL, ldapPrincipal);
      ldapEnv.put(Context.SECURITY_CREDENTIALS, ldapCredentials);
      ldapEnv.put(Context.SECURITY_PROTOCOL, "simple");
      ldapContext = new InitialDirContext(ldapEnv);

      // Create the search controls
      SearchControls searchCtls = new SearchControls();

      // Specify the attributes to return
      String returnedAtts[] = {"cn", "givenName", "samAccountName", "mail", "manager"};
      searchCtls.setReturningAttributes(returnedAtts);

      // Specify the search scope
      searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

      // specify the LDAP search filter
      String searchFilter = "(&(objectClass=user))";

      // Specify the Base for the search
      String searchBase = "dc=saa,dc=local";

      // Search for objects using the filter
      NamingEnumeration<SearchResult> answer =
          ldapContext.search(searchBase, "cn=" + cn, searchCtls);

      // Loop through the search results
      while (answer.hasMoreElements()) {
        SearchResult sr = answer.next();
        Attributes attrs = sr.getAttributes();
        employee.setUsername(attrs.get("samAccountName").toString());
        employee.setEmail(attrs.get("mail").toString());
        employee.setName(attrs.get("cn").toString());
      }
      ldapContext.close();
    } catch (Exception e) {
      System.out.println(" Search error: " + e);
      e.printStackTrace();
      System.exit(-1);
    }
    return employee;
  }

  /**
   * Returns the common name of an employee's manager
   *
   * @param employee the Employee object to be queried
   * @return the common name of the employee's manager
   */
  public String managerCNParser(Employee employee) {
    String managerString = employee.getManager();
    managerString = managerString.substring(12);
    managerString = managerString.split(",")[0];
    return managerString;
  }

  /**
   * Return the common name of an employee
   *
   * @param employee the Employee object to be queried
   * @return the common name of employee
   */
  public String cnParser(Employee employee) {
    String cnString = employee.getName();
    cnString = cnString.substring(4);
    return cnString;
  }

  /**
   * Return the internal email of an employee
   *
   * @param employee the Employee object to be queried
   * @return the internal email address of employee
   */
  public String emailParser(Employee employee) {
    String emailString = employee.getEmail();
    emailString = emailString.substring(6);
    return emailString;
  }
}
