package willmcg;

public class LeaveCode {
  // Actual number value of the leave code
  private String code;

  // Leave code description
  private String description;

  // Flag to signal which hours bank is being modified; 0 for none, 1 for Sick, 2 for Vacation
  private int hourMod;

  public LeaveCode(String codeNum, String codeDescription) {
    description = codeDescription;
    code = codeNum;
    hourMod = 0;
  }

  public LeaveCode(String codeNum, String codeDescription, int flag) {
    description = codeDescription;
    code = codeNum;
    hourMod = flag;
  }

  public int getHourMod() {
    return hourMod;
  }

  @Override
  public String toString() {
    return code + ": " + description;
  }
}
