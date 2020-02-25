package willmcg;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class ApplicationController {

  // List of actual leave codes to be referenced for code/flag values by the code
  private ObservableList<LeaveCode> leaveCodes =
      FXCollections.observableArrayList(
          new LeaveCode("110", "Vacation", 2),
          new LeaveCode("210", "Sick Leave Uncertified", 1),
          new LeaveCode("220", "Sick Leave Certified", 1),
          new LeaveCode("230", "Sick Leave w/o Pay"),
          new LeaveCode("410", "Family Related"),
          new LeaveCode("420", "Illness in Family"),
          new LeaveCode("510", "Bereavement"),
          new LeaveCode("610", "Court"),
          new LeaveCode("620", "Career Development"),
          new LeaveCode("630", "Union Business"),
          new LeaveCode("640", "Injury On Duty"),
          new LeaveCode("851", "Lieu Day"),
          new LeaveCode("910", "Union Business w/o Pay"),
          new LeaveCode("999", "Other Leave w/o Pay"));

  // List of leave codes as Strings to be seen by the user. Index is taken to translate back to
  // Leave Code value using leaveCodes
  private ObservableList<String> codeList =
      FXCollections.observableArrayList(
          new LeaveCode("110", "Vacation").toString(),
          new LeaveCode("210", "Sick Leave Uncertified").toString(),
          new LeaveCode("220", "Sick Leave Certified").toString(),
          new LeaveCode("230", "Sick Leave w/o Pay").toString(),
          new LeaveCode("410", "Family Related").toString(),
          new LeaveCode("420", "Illness in Family").toString(),
          new LeaveCode("510", "Bereavement").toString(),
          new LeaveCode("610", "Court").toString(),
          new LeaveCode("620", "Career Development").toString(),
          new LeaveCode("630", "Union Business").toString(),
          new LeaveCode("640", "Injury On Duty").toString(),
          new LeaveCode("851", "Lieu Day").toString(),
          new LeaveCode("910", "Union Business w/o Pay").toString(),
          new LeaveCode("999", "Other Leave w/o Pay").toString());

  // Start and end hour/min values, as well as hour/min totals for requested time
  private int hr, min, fromHH, toHH, fromMM, toMM;

  // The amount of hours the employee has in each bank, as well as the net change in each bank upon
  // submission
  private float vacationHr, sickHr, vacationDiff, sickDiff;

  // The email addresses of the employee and their manager, as well as strings to inform the
  // generated email/log entry
  private String username, managerEmail, userEmail, emailBody, notesBody, dates;

  // FileParser object for reading/writing hours info
  private FileParser fileIO = new FileParser();

  // Queues for all fields effected when entries are deleted
  private LinkedList<String> datesQueue, bodyQueue, notesQueue;

  // A queue for all hours entries, denoting what category each entry belongs to
  private LinkedList<SickHelper> hoursQueue = new LinkedList<>();

  // Groups of elements for easier enabling/disabling based on application selections
  @FXML private Group singleDayGroup, multiDayGroup;

  // Radio buttons that toggle the singleDay/multiDay groups
  @FXML private RadioButton singleRadio, multiRadio;

  // The dynamic labels in the FXML page
  @FXML
  private Label nameLabel,
      reportsToLabel,
      vacationHoursLabel,
      sickHoursLabel,
      formatErrorLabel,
      overdrawErrorLabel,
      datesLabel,
      codeLabel;

  // The text fields in the FXML page
  @FXML
  private TextField singleNotes,
      multiNotes,
      singleHH,
      singleMM,
      multiHH,
      multiMM,
      singleFromMM,
      singleToMM,
      singleFromHH,
      singleToHH,
      multiFromHH,
      multiToHH,
      multiFromMM,
      multiToMM;

  // The combo box populated with Leave Codes from codeList
  @FXML private ComboBox<String> codeCombo;

  // The date pickers in the FXML page (datePicker for single date, to and from for multi date)
  @FXML private DatePicker datePicker, toDatePicker, fromDatePicker;

  /** Initializes FXML page elements and values used for submission */
  @FXML
  private void initialize() {
    datesQueue = new LinkedList<>();
    bodyQueue = new LinkedList<>();
    notesQueue = new LinkedList<>();
    username = System.getProperty("user.name").toLowerCase(); // Fetch the username for LDAP lookup
    dates = "Requested Dates:\n";
    codeCombo.setItems(codeList); // Initialize the combo box
    LDAPSearcher searcher = new LDAPSearcher();
    Employee employee = searcher.search(username); // Create an Employee object to store user info
    nameLabel.setText(searcher.cnParser(employee)); // Get user's common name
    reportsToLabel.setText(searcher.managerCNParser(employee)); // Get manager's common name
    managerEmail = searcher.emailParser(searcher.cnSearch(searcher.managerCNParser(employee)));
    userEmail = searcher.emailParser(employee);
    emailBody = "";
    vacationHr =
        fileIO.getHoursDictionary().get(username).getVacationHours(); // Fetch initial hours values
    sickHr = fileIO.getHoursDictionary().get(username).getSickHours();
    vacationHoursLabel.setText(Float.toString(vacationHr));
    sickHoursLabel.setText(Float.toString(sickHr));
  }

  /**
   * Converts integer hour and minutes values into a float of hours
   *
   * @param hr Number of hours as an int
   * @param min Number of minutes as an int
   * @return Hours + mins as a float of hours
   */
  private float convertToHr(int hr, int min) {
    double num = (hr + (min / 60.0));
    num = Math.round(num * 100.0) / 100.0;
    System.out.println(num);
    return (float) num;
  }

  /** Generates the confirmation emails and logs all entries before closing the application */
  @FXML
  private void onSubmitClick() {
    if (datesQueue.isEmpty()) {
      return; // If there are no entries, do nothing!
    } else {
      sickDiff = Float.parseFloat(sickHoursLabel.getText()) - sickHr;
      vacationDiff = Float.parseFloat(vacationHoursLabel.getText()) - vacationHr;

      // Update hours in the user's bank
      fileIO.editSickHours(username, sickDiff);
      fileIO.editVacationHours(username, vacationDiff);

      // Log the changes and the generated ticket details
      String changeString =
          "Vacation Time Adjusted by " + vacationDiff + "; Sick Time Adjusted by " + sickDiff;
      int ticketNO = fileIO.addTicket(nameLabel.getText(), "UNRESOLVED", sickDiff, vacationDiff);
      try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("REDACTED"))) {
        String fileContent = emailBody + "</form>";
        bufferedWriter.write(fileContent);
      } catch (IOException e) {
        System.out.println("IOException in onSubmitClick of FileParser");
      }
      fileIO.addLog(
          nameLabel.getText(),
          changeString,
          "User Application",
          notesBody,
          Integer.toString(ticketNO));

      // Generate and send the confirmation emails
      emailBody =
          "<h4>"
              + nameLabel.getText()
              + " is requesting the following time off: </h4><form>"
              + emailBody
              + "</form>";
      Mailer mailer = new Mailer();
      mailer.buttonMail(
          managerEmail,
          reportsToLabel.getText(),
          "New Leave Request: " + nameLabel.getText() + " - Ticket #" + ticketNO,
          emailBody,
          "REDACTED");
      mailer.sendMail(
          userEmail,
          nameLabel.getText(),
          "Leave Request Submitted: Ticket #" + ticketNO,
          "<h3>What follows is a copy of the email sent to your manager on your behalf:</h3><br/>"
              + emailBody);

      // Generate the .bat file that will be launched when the link in the manager's email is
      // clicked
      try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("REDACTED"))) {
        String fileContent = "REDACTED";
        bufferedWriter.write(fileContent);
      } catch (IOException e) {
        System.out.println("IOException in onSubmitClick of ApplicationController");
      }

      // Close the application
      Stage stage = (Stage) multiRadio.getScene().getWindow();
      stage.close();
      System.exit(0);
    }
  }

  /**
   * Pushes an additional single-day entry onto the submission and updates the user's hours
   *
   * @throws NumberFormatException when a text field contains a non-integer value
   */
  @FXML
  private void onSingleDateClick() throws NumberFormatException {
    try { // Consume all of the values from the relevant text fields
      hr = Integer.parseInt(singleHH.getText());
      min = Integer.parseInt(singleMM.getText());
      fromMM = Integer.parseInt(singleFromMM.getText());
      toMM = Integer.parseInt(singleToMM.getText());
      fromHH = Integer.parseInt(singleFromHH.getText());
      toHH = Integer.parseInt(singleToHH.getText());
    } catch (NumberFormatException n) { // Display format error message
      formatErrorLabel.setVisible(true);
      overdrawErrorLabel.setVisible(false);
    }

    // Check if there are any other conditions that need to flash error messages
    if (datePicker.getValue() == null
        || codeCombo.getValue() == null
        || min > 59
        || convertToHr(hr, min) < 0.5
        || min < 0
        || hr < 0
        || hr > 23
        || fromMM > 59
        || toMM > 59
        || fromMM < 0
        || toMM < 0
        || toHH > 23
        || fromHH > 23
        || fromHH > toHH
        || (fromHH == toHH && toMM < fromMM)) {
      formatErrorLabel.setVisible(true);
      overdrawErrorLabel.setVisible(false);
    } else if (leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()).getHourMod() == 1
        && ((Float.parseFloat(sickHoursLabel.getText()) - convertToHr(hr, min)) < -10)) {
      formatErrorLabel.setVisible(false);
      overdrawErrorLabel.setVisible(true);
    } else if (leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()).getHourMod() == 2
        && ((Float.parseFloat(vacationHoursLabel.getText()) - convertToHr(hr, min)) < -10)) {
      formatErrorLabel.setVisible(false);
      overdrawErrorLabel.setVisible(true);
    } else { // No errors detected; complete the entry
      formatErrorLabel.setVisible(false);
      overdrawErrorLabel.setVisible(false);
      String fromTime = convertZeroes(fromHH) + ":" + convertZeroes(fromMM);
      String toTime = convertZeroes(toHH) + ":" + convertZeroes(toMM);
      datesQueue.addFirst(dates);
      bodyQueue.addFirst(emailBody);
      notesQueue.addFirst(notesBody);
      dates =
          dates
              + datePicker.getValue()
              + " from "
              + fromTime
              + " to "
              + toTime
              + "\n\t"
              + hr
              + "h and "
              + min
              + "m w/ code "
              + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
              + ";\n";

      // If there were notes in the field, add them to the email and log them
      if (singleNotes.getText().length() > 1) {
        notesBody =
            notesBody
                + hr
                + "h and "
                + min
                + "m w/ code "
                + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
                + " NOTES: "
                + singleNotes.getText().replaceAll(",", " ")
                + "; ";
        emailBody =
            emailBody
                + "<br/><li>"
                + datePicker.getValue()
                + " from "
                + fromTime
                + " to "
                + toTime
                + "<br/>"
                + hr
                + "h and "
                + min
                + "m w/ code "
                + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
                + "<br/>Notes: "
                + singleNotes.getText().replaceAll(",", " ")
                + ";</li><br/><br/>";
      } else {
        notesBody =
            notesBody
                + hr
                + "h and "
                + min
                + "m w/ code "
                + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
                + "; ";
        emailBody =
            emailBody
                + "<br/><li>"
                + datePicker.getValue()
                + " from "
                + fromTime
                + " to "
                + toTime
                + "<br/>"
                + hr
                + "h and "
                + min
                + "m w/ code "
                + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
                + ";</li><br/><br/>";
      }
      datesLabel.setVisible(true);
      datesLabel.setText(dates);
      singleNotes.setText("");

      // Reset all fields so you can make a new entry
      clearFields();

      // If the entry should modify the hours or sick bank, do so here
      if (leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()).getHourMod() == 1) {
        hoursQueue.addFirst(new SickHelper(true, sickHoursLabel.getText()));
        float hrsFloat = Float.parseFloat(sickHoursLabel.getText()) - convertToHr(hr, min);
        sickHoursLabel.setText(Float.toString(hrsFloat));
      } else if (leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()).getHourMod()
          == 2) {
        hoursQueue.addFirst(new SickHelper(false, vacationHoursLabel.getText()));
        float hrsFloat = Float.parseFloat(vacationHoursLabel.getText()) - convertToHr(hr, min);
        vacationHoursLabel.setText(Float.toString(hrsFloat));
      } else {
        hoursQueue.addFirst(new SickHelper(true, sickHoursLabel.getText()));
      }
    }
  }

  /**
   * Pushes an additional multi-day entry onto the submission and updates the user's hours
   *
   * @throws NumberFormatException when a text field contains a non-integer value
   */
  @FXML
  private void onMultiDateClick() throws NumberFormatException {
    try { // Consume all of the values from the relevant text fields
      hr = Integer.parseInt(multiHH.getText());
      min = Integer.parseInt(multiMM.getText());
      fromMM = Integer.parseInt(multiFromMM.getText());
      toMM = Integer.parseInt(multiToMM.getText());
      fromHH = Integer.parseInt(multiFromHH.getText());
      toHH = Integer.parseInt(multiToHH.getText());
    } catch (NumberFormatException n) { // Display format error message
      formatErrorLabel.setVisible(true);
      overdrawErrorLabel.setVisible(false);
    }

    // Check for any other error conditions
    if (toDatePicker.getValue() == null
        || fromDatePicker.getValue() == null
        || fromMM > 59
        || toMM > 59
        || fromMM < 0
        || toMM < 0
        || codeCombo.getValue() == null
        || convertToHr(hr, min) < 0.5
        || min > 59
        || min < 0
        || hr < 0
        || toDatePicker.getValue().isBefore(fromDatePicker.getValue())) {
      formatErrorLabel.setVisible(true);
      overdrawErrorLabel.setVisible(false);
    } else if (leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()).getHourMod() == 1
        && ((Float.parseFloat(sickHoursLabel.getText()) - convertToHr(hr, min)) < -10)) {
      formatErrorLabel.setVisible(false);
      overdrawErrorLabel.setVisible(true);
    } else if (leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()).getHourMod() == 2
        && ((Float.parseFloat(vacationHoursLabel.getText()) - convertToHr(hr, min)) < -10)) {
      formatErrorLabel.setVisible(false);
      overdrawErrorLabel.setVisible(true);
    } else { // No errors detected; complete the entry
      formatErrorLabel.setVisible(false);
      overdrawErrorLabel.setVisible(false);
      String fromTime = convertZeroes(fromHH) + ":" + convertZeroes(fromMM);
      String toTime = convertZeroes(toHH) + ":" + convertZeroes(toMM);
      datesQueue.addFirst(dates);
      bodyQueue.addFirst(emailBody);
      notesQueue.addFirst(notesBody);
      dates =
          dates
              + fromDatePicker.getValue()
              + " @ "
              + fromTime
              + " to "
              + toDatePicker.getValue()
              + " @ "
              + toTime
              + "\n\t"
              + hr
              + "h and "
              + min
              + "m w/ code "
              + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
              + ";\n";

      // If there were notes in the field, add them to the email and log them
      if (multiNotes.getText().length() > 1) {
        emailBody =
            emailBody
                + "<br/><li>"
                + fromDatePicker.getValue()
                + " @ "
                + fromTime
                + " to "
                + toDatePicker.getValue()
                + " @ "
                + toTime
                + "<br/>"
                + hr
                + "h and "
                + min
                + "m w/ code "
                + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
                + "<br/>Notes: "
                + multiNotes.getText().replaceAll(",", " ")
                + ";</li><br/><br/>";
        notesBody =
            notesBody
                + hr
                + "h and "
                + min
                + "m w/ code "
                + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
                + " NOTES: "
                + multiNotes.getText().replaceAll(",", " ")
                + "; ";
      } else {
        emailBody =
            emailBody
                + "<br/><li>"
                + fromDatePicker.getValue()
                + " @ "
                + fromTime
                + " to "
                + toDatePicker.getValue()
                + " @ "
                + toTime
                + "<br/>"
                + hr
                + "h and "
                + min
                + "m w/ code "
                + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
                + ";</li><br/><br/>";
        notesBody =
            notesBody
                + hr
                + "h and "
                + min
                + "m w/ code "
                + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex())
                + "; ";
      }
      datesLabel.setVisible(true);
      datesLabel.setText(dates);
      multiNotes.setText("");

      // Reset all fields so you can make a new entry
      clearFields();

      // If the entry should modify the hours or sick bank, do so here
      if (leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()).getHourMod() == 1) {
        hoursQueue.addFirst(new SickHelper(true, sickHoursLabel.getText()));
        float hrsFloat = Float.parseFloat(sickHoursLabel.getText()) - convertToHr(hr, min);
        sickHoursLabel.setText(Float.toString(hrsFloat));
      } else if (leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()).getHourMod()
          == 2) {
        hoursQueue.addFirst(new SickHelper(false, vacationHoursLabel.getText()));
        float hrsFloat = Float.parseFloat(vacationHoursLabel.getText()) - convertToHr(hr, min);
        vacationHoursLabel.setText(Float.toString(hrsFloat));
      } else {
        hoursQueue.addFirst(new SickHelper(true, sickHoursLabel.getText()));
      }
    }
  }

  /**
   * Clears all user-modifiable fields so a fresh entry can be made without any values carrying over
   */
  private void clearFields() {
    toDatePicker.setValue(null);
    fromDatePicker.setValue(null);
    multiHH.setText("00");
    multiMM.setText("00");
    multiFromHH.setText("00");
    multiToHH.setText("00");
    multiFromMM.setText("00");
    multiToMM.setText("00");
    multiDayGroup.setVisible(false);
    datePicker.setValue(null);
    singleNotes.setText("");
    singleHH.setText("00");
    singleMM.setText("00");
    singleFromHH.setText("00");
    singleToHH.setText("00");
    singleFromMM.setText("00");
    singleToMM.setText("00");
    singleDayGroup.setVisible(false);
    codeLabel.setVisible(false);
  }

  /**
   * Make appropriate fields in the submission pipeline visible based on the items selected on the
   * page
   */
  @FXML
  private void onNewDateClick() {
    int codeIndex = codeCombo.getSelectionModel().getSelectedIndex();
    if (codeIndex == -1) {
      formatErrorLabel.setVisible(true);
      overdrawErrorLabel.setVisible(false);
    } else if (singleRadio.isSelected()) { // Display the single-day options
      codeLabel.setText(
          "Chosen Code: " + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()));
      codeLabel.setVisible(true);
      singleDayGroup.setVisible(true);
      multiDayGroup.setVisible(false);
      formatErrorLabel.setVisible(false);
      overdrawErrorLabel.setVisible(false);
    } else { // Display the multi-day options
      codeLabel.setText(
          "Chosen Code: " + leaveCodes.get(codeCombo.getSelectionModel().getSelectedIndex()));
      codeLabel.setVisible(true);
      multiDayGroup.setVisible(true);
      singleDayGroup.setVisible(false);
      formatErrorLabel.setVisible(false);
      overdrawErrorLabel.setVisible(false);
    }
  }

  /**
   * Removes the most recent entry added to the submission if one exists; updates fields on the page
   * to reflect the change
   */
  @FXML
  private void onRemoveClick() {
    if (!datesQueue.isEmpty()) {
      String prevDate = datesQueue.pop();
      datesLabel.setText(prevDate);
      dates = prevDate;
      emailBody = bodyQueue.pop();
      notesBody = notesQueue.pop();
      SickHelper prevHrs = hoursQueue.pop();
      if (prevHrs.isSick()) {
        sickHoursLabel.setText(prevHrs.getHours());
      } else {
        vacationHoursLabel.setText(prevHrs.getHours());
      }
    }
  }

  /**
   * Opens a protected copy of the user's YTD entries into the leave application system as a .csv
   * file when the corresponding button is clicked
   */
  @FXML
  private void onYTDClick() {
    fileIO.openSafeCopy(username);
  }

  /**
   * Updates the "end date" date picker of a multi-day entry to be at least as late as the start
   * date. Called whenever the start date is updated in the page.
   */
  @FXML
  private void onDateSelected() {
    toDatePicker.setValue(fromDatePicker.getValue());
  }

  /**
   * Helper function that ensures the formatting of times is consistent through all emails and log
   * entries by converting the integer 0 to the string "00"
   *
   * @param num the integer value to be processed
   * @return the string "00" if num was 0, the standard string representation of num otherwise
   */
  private String convertZeroes(int num) {
    if (num == 0) {
      return "00";
    } else {
      return Integer.toString(num);
    }
  }
}
