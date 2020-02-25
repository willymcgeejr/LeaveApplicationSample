package willmcg;

/**
 * A helper class that stores and checks the nature of an entry to determine what bank of hours is
 * being altered
 */
public class SickHelper {
  boolean isSick;
  String hours;

  public SickHelper(boolean bool, String hrs) {
    isSick = bool;
    hours = hrs;
  }

  public boolean isSick() {
    return isSick;
  }

  public String getHours() {
    return hours;
  }
}
