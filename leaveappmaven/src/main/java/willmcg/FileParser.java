package willmcg;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/** This class handles all necessary file IO for the application */
public class FileParser {
  /**
   * Reads the employee hours into a dictionary
   *
   * @return a HashMap of hours Entries, keyed by employee username
   */
  public HashMap<String, Entry> getHoursDictionary() {
    try {
      List<Entry> beans =
          new CsvToBeanBuilder(new FileReader("REDCATED")).withType(Entry.class).build().parse();
      HashMap<String, Entry> staffDict = new HashMap<>();
      for (Entry i : beans) staffDict.put(i.getUsername(), i);
      return staffDict;
    } catch (FileNotFoundException e) {
      System.out.println("File could not be found");
      return null;
    }
  }

  /**
   * Reads the unresolved/open tickets into a dictionary
   *
   * @return a HashMap of Tickets, keyed by ticket number
   */
  public HashMap<Integer, Ticket> getTicketDictionary() {
    try {
      List<Ticket> beans =
          new CsvToBeanBuilder(new FileReader("REDACTED")).withType(Ticket.class).build().parse();
      HashMap<Integer, Ticket> dict = new HashMap<>();
      for (Ticket i : beans) dict.put(i.getTicket(), i);
      return dict;
    } catch (FileNotFoundException e) {
      System.out.println("File could not be found");
      return null;
    }
  }

  /**
   * Creates a protected copy of the user's personal YTD changelog, and then attempts to open it as
   * a .csv file
   *
   * @param name the username of the employee's changelog entries are being fetched
   */
  public void openSafeCopy(String name) {
    try {
      // Create a list with the proper csvbeans format, then copy over only the needed entries for
      // the new file
      List<Change> newbeans =
          new CsvToBeanBuilder(new FileReader("REDACTED")).withType(Change.class).build().parse();
      newbeans.clear();
      LDAPSearcher searcher = new LDAPSearcher();
      String commonname = searcher.cnParser(searcher.search(name));
      List<Change> beans =
          new CsvToBeanBuilder(new FileReader("REDACTED")).withType(Change.class).build().parse();
      ListIterator<Change> iter = beans.listIterator();
      while (iter.hasNext()) {
        Change next = iter.next();
        if (next.getEmployee().equals(commonname)) {
          if (next.getDate().contains(Integer.toString(LocalDate.now().getYear()))) {
            newbeans.add(next);
          }
        }
      }

      // Create the new file with opencsv
      try (BufferedWriter writer =
          Files.newBufferedWriter(Paths.get("REDACTED"), StandardCharsets.UTF_8)) {
        StatefulBeanToCsv<Change> beanToCsv =
            new StatefulBeanToCsvBuilder(writer)
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .build();
        beanToCsv.write(newbeans);
        File file = new File("REDACTED");

        // Open the newly created file
        Desktop desktop = Desktop.getDesktop();
        if (file.exists()) {
          desktop.open(file);
        }
      } catch (CsvDataTypeMismatchException ex) {
        System.out.println("Data Type Mismatch Exception Thrown (OpenCSV Error in addLog)");
      } catch (CsvRequiredFieldEmptyException ex) {
        System.out.println("Required Field Empty Exception Thrown (OpenCSV Error in addLog)");
      } catch (IOException ex) {
        System.out.println("Java IO Exception Thrown (java.io Error in addLog");
      }
    } catch (Exception e) {
      System.out.println("Error trying to create Safe Copy");
    }
  }

  /**
   * Adds a new Change to the changelog
   *
   * @param employee the username of the affected employee
   * @param change the string describing the change made
   * @param admin the administrator of the change
   * @param notes the notes accompanying the change
   * @param ticket the ticket number associated with the change
   */
  public void addLog(String employee, String change, String admin, String notes, String ticket) {
    try {
      String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
      String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
      Change newLog = new Change(date, time, employee, change, admin, notes, ticket);
      List<Change> beans =
          new CsvToBeanBuilder(new FileReader("REDACTED")).withType(Change.class).build().parse();
      beans.add(newLog);
      try (BufferedWriter writer =
          Files.newBufferedWriter(Paths.get("REDACTED"), StandardCharsets.UTF_8)) {
        StatefulBeanToCsv<Change> beanToCsv =
            new StatefulBeanToCsvBuilder(writer)
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .build();
        beanToCsv.write(beans);
      } catch (CsvDataTypeMismatchException ex) {
        System.out.println("Data Type Mismatch Exception Thrown (OpenCSV Error in addLog)");
      } catch (CsvRequiredFieldEmptyException ex) {
        System.out.println("Required Field Empty Exception Thrown (OpenCSV Error in addLog)");
      } catch (IOException ex) {
        System.out.println("Java IO Exception Thrown (java.io Error in addLog");
      }
    } catch (FileNotFoundException e) {
      System.out.println("File could not be found");
    }
  }

  /**
   * Adds a new ticket to the list of open tickets
   *
   * @param employee the common name of the employee the ticket belongs to
   * @param body the body of the ticket entry
   * @param sick the change in sick hours (negative values mean the time is being used)
   * @param vacation the change in vacation hours (negative values mean the time is being used)
   * @return the ticket number of the newly added entry, or 0 if a problem occurs
   */
  public int addTicket(String employee, String body, double sick, double vacation) {
    // Get current tickets so the new ticket can be appended
    HashMap<Integer, Ticket> tempDict = getTicketDictionary();
    Ticket newEntry = new Ticket();
    int ticketNum = 0;

    // Get the new ticket number
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader("REDACTED"))) {
      String line = bufferedReader.readLine();
      ticketNum = Integer.parseInt(line);
    } catch (FileNotFoundException e) {
      System.out.println("FileNotFound in addTicket of FileParser");
    } catch (IOException e) {
      System.out.println("IOException in addTicket of FileParser");
    }

    // Increment the ticket number
    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("REDACTED"))) {
      String fileContent = Integer.toString(ticketNum + 1);
      bufferedWriter.write(fileContent);
    } catch (IOException e) {
      System.out.println("IOException in addTicket of FileParser");
    }

    // Create the new ticket entry, then append to existing list
    newEntry.setTicket(ticketNum);
    newEntry.setEmployee(employee);
    newEntry.setBody(body);
    newEntry.setSickAdjust(sick);
    newEntry.setVacAdjust(vacation);
    tempDict.put(ticketNum, newEntry);
    List<Ticket> newList = new ArrayList<>(tempDict.values());

    // Write the updated list of open tickets back to the file
    try (BufferedWriter writer =
        Files.newBufferedWriter(Paths.get("REDACTED"), StandardCharsets.UTF_8)) {
      StatefulBeanToCsv<Ticket> beanToCsv =
          new StatefulBeanToCsvBuilder(writer).withQuotechar(CSVWriter.NO_QUOTE_CHARACTER).build();
      beanToCsv.write(newList);
      return ticketNum;
    } catch (CsvDataTypeMismatchException ex) {
      System.out.println("Data Type Mismatch Exception Thrown (OpenCSV Error in addEntry)");
    } catch (CsvRequiredFieldEmptyException ex) {
      System.out.println("Required Field Empty Exception Thrown (OpenCSV Error in addEntry)");
    } catch (IOException ex) {
      System.out.println("Java IO Exception Thrown (java.io Error in addEntry");
    }
    return 0;
  }

  /**
   * Modifies the banked sick time of an employee
   *
   * @param username the username of the employee being modified
   * @param hours the hours to be added/removed (ex. hours = -6 will remove 6 hours from their
   *     current sick total)
   */
  public void editSickHours(String username, double hours) {
    // Get the current hours for the employee
    HashMap<String, Entry> tempDict = getHoursDictionary();
    if (tempDict.containsKey(username)) {
      Entry tempEntry = tempDict.get(username);
      tempEntry.setSickHours(tempEntry.getSickHours() + Float.parseFloat(Double.toString(hours)));
      tempDict.replace(username, tempEntry);
      List<Entry> newList = new ArrayList<Entry>(tempDict.values());

      // Write the updated hours
      try (BufferedWriter writer =
          Files.newBufferedWriter(Paths.get("REDACTED"), StandardCharsets.UTF_8)) {
        StatefulBeanToCsv<Entry> beanToCsv =
            new StatefulBeanToCsvBuilder(writer)
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .build();
        beanToCsv.write(newList);
      } catch (CsvDataTypeMismatchException ex) {
        System.out.println("Data Type Mismatch Exception Thrown (OpenCSV Error in editSickHours)");
      } catch (CsvRequiredFieldEmptyException ex) {
        System.out.println(
            "Required Field Empty Exception Thrown (OpenCSV Error in editSickHours)");
      } catch (IOException ex) {
        System.out.println("Java IO Exception Thrown (java.io Error in editSickHours");
        System.out.println(ex.toString());
        System.out.println(ex.getMessage());
      }
    } else {
      System.out.println("Value cannot be found");
    }
  }

  /**
   * Modifies the banked vacation time of an employee
   *
   * @param username the username of the employee being modified
   * @param hours the hours to be added/removed (ex. hours = -6 will remove 6 hours from their
   *     current vacation total)
   */
  public void editVacationHours(String username, double hours) {
    // Get the current hours for th employee
    HashMap<String, Entry> tempDict = getHoursDictionary();
    if (tempDict.containsKey(username)) {
      Entry tempEntry = tempDict.get(username);
      tempEntry.setVacationHours(
          tempEntry.getVacationHours() + Float.parseFloat(Double.toString(hours)));
      tempDict.replace(username, tempEntry);
      List<Entry> newList = new ArrayList<Entry>(tempDict.values());

      // Write the updated hours
      try (BufferedWriter writer =
          Files.newBufferedWriter(Paths.get("REDACTED"), StandardCharsets.UTF_8)) {
        StatefulBeanToCsv<Entry> beanToCsv =
            new StatefulBeanToCsvBuilder(writer)
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .build();
        beanToCsv.write(newList);
      } catch (CsvDataTypeMismatchException ex) {
        System.out.println(
            "Data Type Mismatch Exception Thrown (OpenCSV Error in editVacationHours)");
      } catch (CsvRequiredFieldEmptyException ex) {
        System.out.println(
            "Required Field Empty Exception Thrown (OpenCSV Error in editVacationHours)");
      } catch (IOException ex) {
        System.out.println("Java IO Exception Thrown (java.io Error in editVacationHours");
        System.out.println(ex.toString());
        System.out.println(ex.getMessage());
      }
    } else {
      System.out.println("Value cannot be found");
    }
  }
}
