package willmcg;

import com.opencsv.bean.CsvBindByName;

/** A class for reading ticket entries into beans for .csv manipulation */
public class Ticket {
  // The number of the open ticket
  @CsvBindByName private int ticket;

  // The common name of the employee associated with the ticket
  @CsvBindByName private String employee;

  // The body of the ticket
  @CsvBindByName private String body;

  // The difference in sick hours (-6 would mean a loss of 6 hours)
  @CsvBindByName private double sickAdjust;

  // The difference in vacation hours (10 would mean a gain of 10 hours)
  @CsvBindByName private double vacAdjust;

  public int getTicket() {
    return ticket;
  }

  public void setTicket(int ticket) {
    this.ticket = ticket;
  }

  public String getEmployee() {
    return employee;
  }

  public void setEmployee(String employee) {
    this.employee = employee;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public double getSickAdjust() {
    return sickAdjust;
  }

  public void setSickAdjust(double sickAdjust) {
    this.sickAdjust = sickAdjust;
  }

  public double getVacAdjust() {
    return vacAdjust;
  }

  public void setVacAdjust(double vacAdjust) {
    this.vacAdjust = vacAdjust;
  }
}
