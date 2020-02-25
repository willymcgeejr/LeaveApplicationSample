package willmcg;

import com.opencsv.bean.CsvBindByName;

/** A class for reading changelog entries into beans for .csv manipulation */
public class Change {
  // Date given in a MM-DD-YYYY format
  @CsvBindByName private String date;

  // Time given in HH:MM:SS format (24h)
  @CsvBindByName private String time;

  // Employee's internal username
  @CsvBindByName private String employee;

  // A string describing the changes that were made to their hours (for logging purposes)
  @CsvBindByName private String change;

  // The user who administrated the change (the administrator's common name for changes made in the
  // Hours Administration
  // application, "User Application" otherwise
  @CsvBindByName private String admin;

  // An optional notes field
  @CsvBindByName private String notes;

  // The ticket number associated with the change
  @CsvBindByName private String ticket;

  public Change(
      String date,
      String time,
      String employee,
      String change,
      String admin,
      String notes,
      String ticket) {
    this.date = date;
    this.time = time;
    this.employee = employee;
    this.change = change;
    this.admin = admin;
    this.notes = notes;
    this.ticket = ticket;
  }

  public Change() {}

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getEmployee() {
    return employee;
  }

  public String getTicket() {
    return ticket;
  }

  public void setTicket(String ticket) {
    this.ticket = ticket;
  }

  public void setEmployee(String employee) {
    this.employee = employee;
  }

  public String getChange() {
    return change;
  }

  public void setChange(String change) {
    this.change = change;
  }

  public String getAdmin() {
    return admin;
  }

  public void setAdmin(String admin) {
    this.admin = admin;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
