package willmcg;

import com.opencsv.bean.CsvBindByName;

/** A class for reading employee hours bank entries into beans for .csv manipulation */
public class Entry {
  // Employee's username
  @CsvBindByName private String username;

  // The amount of remaining sick hours given as a float
  @CsvBindByName private float sickHours;

  // The amount of remaining vacation hours given as a float
  @CsvBindByName private float vacationHours;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public float getSickHours() {
    return sickHours;
  }

  public void setSickHours(float sickHours) {
    this.sickHours = sickHours;
  }

  public float getVacationHours() {
    return vacationHours;
  }

  public void setVacationHours(float vacationHours) {
    this.vacationHours = vacationHours;
  }
}
