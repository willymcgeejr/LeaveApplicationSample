package willmcg;

/**
 * A class for storing information about an employee that can be pulled from Active Directory/LDAP
 * All methods are just getters/setters for the employee's information
 */
public class Employee {
  // Employee's common name, internal email, username, and manager
  private String name, email, username, manager;

  public String getManager() {
    return manager;
  }

  public void setManager(String manager) {
    this.manager = manager;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
