package cecs.pkg323.java.project;

import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mimi Opkins with some tweaking from Dave Brown
 */
public class CECS323JavaProject {

    static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    static String DB_URL = "jdbc:derby://localhost:1527/";
    static Statement stmt;
    static ResultSet rs;
    static Connection conn; //initialize the connection
    static String sql = null;

    /**
     * Takes the input string and outputs "N/A" if the string is empty or null.
     *
     * @param input The string to be mapped.
     * @return Either the input string or "N/A" as appropriate.
     */
    public static String dispNull(String input) {
        //because of short circuiting, if it's null, it never checks the length.
        if (input == null || input.length() == 0) {
            return "N/A";
        } else {
            return input;
        }
    }

    /*
    * Checks whether the value in the given table is present in the database
     */
    public static boolean isFoundInDataBase(String tableName, String columnName, String input) throws SQLException {
        sql = "Select " + columnName + " From " + tableName + " where " + columnName + " = '" + input + "'";
        rs = stmt.executeQuery(sql);
        return rs.next();
    }

    /*
     * Checks the input of the string input
     */
    public static String checkInput(String input) {
        //check for valid varchar length
        if (input.length() > 50) {
            input = input.substring(0, 49);
        }
        return checkApos(input);
    }

    /*
     * replaces apostrophy for input
     */
    public static String checkApos(String input) {
        return input.replace("'", "''");
    }

    /*
     * checks whether input is valid in database
     */
    public static boolean isValid(String tableName, String input) throws SQLException {
        sql = "select * from " + tableName + " where " + input;
        rs = stmt.executeQuery(sql);
        return rs.next();
    }

    /*
     * adds a book with valid data into the database
     */
    public static void addBook(Statement stmt, Connection conn) {
        try {
            //STEP 2: Register JDBC driver
            Class.forName("org.apache.derby.jdbc.ClientDriver");

            //STEP 3: Open a connectionX
            conn = DriverManager.getConnection(DB_URL);

            //STEP 4: Execute a query
            stmt = conn.createStatement();
            String insertStatement;
            String writingGroupsStatement;

            int choice;
            Scanner scan = new Scanner(System.in);

            System.out.println("To add a book, please select from the existing writing groups: ");
            System.out.println();
            writingGroupsStatement = "Select GroupName From WritingGroup Order By groupName";
            String displayFormat = "%-21s\n";

            //displays available writing groups
            ResultSet rs = stmt.executeQuery(writingGroupsStatement);
            //STEP 5: Extract data from result set
            System.out.printf(displayFormat, "Group Names");
            int count = 1;
            while (rs.next()) {
                //Retrieve by column name
                String GName = rs.getString("GroupName");
                //Display values
                System.out.printf(displayFormat,
                        dispNull(Integer.toString(count) + ". " + GName));
                count++;
            }
            System.out.println();

            int rowNum = 0;
            Statement s = conn.createStatement(
                    rs.TYPE_SCROLL_INSENSITIVE, rs.CONCUR_READ_ONLY);

            rs = s.executeQuery("Select GroupName From WritingGroup order by GroupName");
            if (rs.last()) {
                rowNum = rs.getRow();
                rs.beforeFirst();
            }

            //user selects which writing group to use
            choice = selectFromMenuByNumber(rowNum);
            rs.absolute(choice);
            String GName = "";
            GName = rs.getString("GroupName");
            rs.beforeFirst();

            //displays available publishers
            System.out.println("To add a book, please select from the existing publishers: ");
            System.out.println();
            writingGroupsStatement = "Select PublisherName From Publisher Order By publisherName";
            String displayPubFormat = "%-21s\n";

            ResultSet pubResult = stmt.executeQuery(writingGroupsStatement);
            //STEP 5: Extract data from result set
            System.out.printf(displayPubFormat, "Publisher");
            int coun = 1;
            int pubChoice;
            while (pubResult.next()) {
                //Retrieve by column name
                String pName = pubResult.getString("PublisherName");
                //Display values
                System.out.printf(displayFormat,
                        dispNull(Integer.toString(coun) + ". " + pName));
                coun++;
            }
            System.out.println();

            int rowNumber = 0;
            Statement st = conn.createStatement(
                    pubResult.TYPE_SCROLL_INSENSITIVE, pubResult.CONCUR_READ_ONLY);

            pubResult = st.executeQuery("Select PublisherName From Publisher order by PublisherName");
            if (pubResult.last()) {
                rowNumber = pubResult.getRow();
                pubResult.beforeFirst();
            }

            //user selects which publisher to use
            pubChoice = selectFromMenuByNumber(rowNumber);
            pubResult.absolute(pubChoice);
            String pName = "";
            pName = pubResult.getString("PublisherName");
            pubResult.beforeFirst();

            //user enters the rest of the book information
            System.out.println("Enter the book title: ");
            String bookTitle = scan.nextLine();
            System.out.println("Enter the year: ");
            String year = scan.nextLine();
            System.out.println("Enter the number of pages: ");
            String numberPages = scan.nextLine();
            insertStatement = "insert into book (  PublisherName, GroupName, BookTitle, YearPublished, NumberPages) values ( '" + pName + "','" + GName + "','" + bookTitle + "','" + year + "','" + numberPages + "')";
            stmt.executeUpdate(insertStatement);

            //STEP 6: Clean-up environment;
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println();

    }

    /*
    * gets a valid number input from the user from a given range starting from 1
     */
    public static int selectFromMenuByNumber(int choices) {

        Scanner console = new Scanner(System.in);
        String input;
        int number;
        boolean isValid;
        do {
            isValid = true; // reset the validity
            System.out.print("Enter a valid number: ");
            input = console.nextLine();

            try {
                number = Integer.parseInt(input);
                if (number < 1 || number > choices) {
                    isValid = false;
                    System.out.println("Invalid. Try again.");
                }
            } catch (NumberFormatException e) {
                isValid = false;
                System.out.println("Invalid. Not a number. Try again.");
            }

        } while (!isValid);

        return Integer.parseInt(input);
    }

    public static void main(String[] args) {
        //Prompt the user for the database name, and the credentials.
        //If your database has no credentials, you can update this code to 
        //remove that from the connection string.
        Scanner in = new Scanner(System.in);
        int choice = 0;
        String userInput;
        stmt = null;
        rs = null;
        conn = null;
        //System.out.print("Name of the database (not the user account): ");
        //DBNAME = in.nextLine();
        //System.out.print("Database user name: ");
        //USER = in.nextLine();
        //System.out.print("Database password: ");
        //PASS = in.nextLine();
        //Constructing the database URL connection string
        DB_URL = DB_URL + "Lab1" + ";user=" + "Ronald" + ";password=" + "Casasola";

        //initialize displayFormats to display database values
        String displayFormatWritingGroups = "%-25s%-25s%-25s%-25s\n";
        String displayFormatPublishers = "%-25s%-25s%-25s%-25s%-25s%-25s%-25s%-25s%-25s%-25s%-25s\n";
        String displayFormatAllPublishers = "%-25s%-50s%-25s%-50s\n";
        String displayFormatAllBooks = "%-25s%-25s%-25s%-25s%-25s\n";
        String displayFormatBook = "%-25s%-25s%-25s%-25s%-25s%-25s%-25s%-25s%-25s%-25s%-25s\n";

        //initialize strings
        String bookTitle = "BookTitle";
        String groupName = "GroupName";
        String publisherName = "PublisherName";
        String yearPublished = "YearPublished";
        String numberPages = "NumberPages";
        String publisherAddress = "PublisherAddress";
        String publisherPhone = "PublisherPhone";
        String publisherEmail = "PublisherEmail";
        String headWriter = "HeadWriter";
        String yearFormed = "YearFormed";
        String subject = "Subject";

        //initialize preparedStatements 
        PreparedStatement displayAllWritingGroups;
        PreparedStatement displayWritingGroup;
        PreparedStatement displayPublisher;
        PreparedStatement displayAllPublishers;
        PreparedStatement displayAllBooks;
        PreparedStatement displayBook;
        PreparedStatement removeBook;
        PreparedStatement addBook;
        PreparedStatement addPublisher;
        PreparedStatement updatePublisher;
        PreparedStatement updateBookPublisher;
        PreparedStatement addWritingGroup;

        //initialize SQL statements
        String sqlDisplayAllWritingGroups = " Select GroupName, HeadWriter, YearFormed, Subject From WritingGroup Order By GroupName";
        String sqlDisplayWritingGroup = "Select * From WritingGroup where groupName = ?";
        String sqlDisplayPublisher = "Select * From Publisher  Where PublisherName = ?";
        String sqlDisplayAllPublishers = "Select PublisherName, PublisherAddress, PublisherPhone, PublisherEmail From Publisher Order By PublisherName";
        String sqlDisplayAllBooks = "Select BookTitle, GroupName, PublisherName, YearPublished, NumberPages From Book Order By BookTitle";
        String sqlDisplayBook = "Select * From Book Natural Join Publisher Natural Join WritingGroup Where BookTitle = ? And GroupName = ?";
        String sqlRemoveBook = "Delete From Book Where BookTitle = ? And GroupName = ?";
        String sqlAddBook = "Insert Into Book (BookTitle, GroupName, PublisherName, YearPublished, NumberPages) Values (?, ?, ?, ?, ?)";
        String sqlAddPublisher = "Insert Into Publisher(PublisherName, PublisherAddress, PublisherPhone, PublisherEmail) Values(?, ?, ?, ?)";
        String sqlUpdatePublisher = "Update Publisher Set PublisherName = ?, PublisherAddress = ?, PublisherEmail = ? Where PublisherName = ?";
        String sqlUpdateBookPublisher = "Update book set PublisherName = ? Where PublisherName = ?";
        String sqlAddWritingGroup = "Insert Into WritingGroup(GroupName, HeadWriter, YearFormed, Subject) Values (?, ?, ?, ?)";

        try {
            //STEP 2: Register JDBC driver
            Class.forName("org.apache.derby.jdbc.ClientDriver");

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL);

            //Step 4:
            stmt = conn.createStatement(); /////creates statement object

            try {
                displayAllWritingGroups = conn.prepareStatement(sqlDisplayAllWritingGroups);
                displayWritingGroup = conn.prepareStatement(sqlDisplayWritingGroup);
                displayPublisher = conn.prepareStatement(sqlDisplayPublisher);
                displayAllPublishers = conn.prepareStatement(sqlDisplayAllPublishers);
                displayAllBooks = conn.prepareStatement(sqlDisplayAllBooks);
                displayBook = conn.prepareStatement(sqlDisplayBook);
                removeBook = conn.prepareStatement(sqlRemoveBook);
                addBook = conn.prepareStatement(sqlAddBook);
                addPublisher = conn.prepareStatement(sqlAddPublisher);
                updatePublisher = conn.prepareStatement(sqlUpdatePublisher);
                updateBookPublisher = conn.prepareStatement(sqlUpdateBookPublisher);
                addWritingGroup = conn.prepareStatement(sqlAddWritingGroup);

                do {

                    try {
                        System.out.println("Make a selection: ");
                        System.out.println("1) Search the database");
                        System.out.println("2) Add a book");
                        System.out.println("3) Insert a new publisher and update it from an existing publisher");
                        System.out.println("4) Remove a specified book");
                        System.out.println("5) Exit");
                        choice = selectFromMenuByNumber(5);
                        System.out.println();

                        switch (choice) {
                            case 1:
                                int listChoice;

                                do {
                                    System.out.println("Enter a database search option: ");
                                    System.out.println("1) All writing groups");
                                    System.out.println("2) Group info specified by the user");
                                    System.out.println("3) All publishers");
                                    System.out.println("4) Publisher info specified by the user");
                                    System.out.println("5) All titles");
                                    System.out.println("6) Book info");
                                    System.out.println("7) Go back");
                                    System.out.println("Enter valid number: ");
                                    listChoice = selectFromMenuByNumber(7);
                                    System.out.println();

                                    switch (listChoice) {
                                        //Display all writing groups
                                        case 1:
                                            rs = displayAllWritingGroups.executeQuery();
                                            System.out.printf(displayFormatWritingGroups,
                                                    "Writing Groups",
                                                    "Head Writer",
                                                    "Year Formed",
                                                    "Subject"
                                            );
                                            while (rs.next()) {
                                                System.out.printf(displayFormatWritingGroups,
                                                        dispNull(rs.getString(groupName)),
                                                        dispNull(rs.getString(headWriter)),
                                                        dispNull(rs.getString(yearFormed)),
                                                        dispNull(rs.getString(subject)));
                                            }
                                            rs.close();
                                            System.out.println();
                                            break;

                                        //Display writing group specified by the user
                                        case 2:
                                            String str;

                                            while (true) {
                                                sql = "Select * From Book Natural Join WritingGroup ";
                                                rs = stmt.executeQuery(sql);
                                                System.out.print("Enter a writing group to get more information on: ");
                                                str = in.nextLine();
                                                boolean end = false;
                                                System.out.println();
                                                while (rs.next()) {
                                                    if (rs.getString("groupName").equals(str)) {
                                                        ResultSetMetaData metadata = rs.getMetaData();
                                                        int columnCount = metadata.getColumnCount();
                                                        for (int i = 1; i <= columnCount; i++) {
                                                            String columnName = metadata.getColumnName(i);
                                                            System.out.println(columnName + '\t' + rs.getString(columnName));
                                                        }
                                                        end = true;
                                                        System.out.println();
                                                    }

                                                }
                                                if (end) {
                                                    break;
                                                }
                                            }

                                        //Display all publishers    
                                        case 3:
                                            rs = displayAllPublishers.executeQuery();
                                            System.out.printf(displayFormatAllPublishers,
                                                    "Publisher",
                                                    "Address",
                                                    "Phone",
                                                    "Email"
                                            );
                                            while (rs.next()) {
                                                System.out.printf(displayFormatAllPublishers,
                                                        dispNull(rs.getString(publisherName)),
                                                        dispNull(rs.getString(publisherAddress)),
                                                        dispNull(rs.getString(publisherPhone)),
                                                        dispNull(rs.getString(publisherEmail))
                                                );
                                            }
                                            rs.close();
                                            System.out.println();
                                            break;
                                            //Display Publisher specified by the user
                                        case 4:
                                           String stri;

                                            while (true) {
                                                sql = "Select * From Publisher Natural Join Book";
                                                rs = stmt.executeQuery(sql);
                                                System.out.print("Enter a publisher to get more information on: ");
                                                stri = in.nextLine();
                                                boolean end = false;
                                                System.out.println();
                                                while (rs.next()) {
                                                    if (rs.getString("publisherName").equals(stri)) {
                                                        ResultSetMetaData metadata = rs.getMetaData();
                                                        int columnCount = metadata.getColumnCount();
                                                        for (int i = 1; i <= columnCount; i++) {
                                                            String columnName = metadata.getColumnName(i);
                                                            System.out.println(columnName + '\t' + rs.getString(columnName));
                                                        }
                                                        end = true;
                                                        System.out.println();
                                                    }

                                                }
                                                if (end) {
                                                    break;
                                                }
                                            }

                                            //Display All Books
                                        case 5:
                                            rs = displayAllBooks.executeQuery();
                                            System.out.printf(displayFormatAllBooks,
                                                    "List of Book Titles",
                                                    "Writing Group",
                                                    "Publisher",
                                                    "Year Published",
                                                    "Number of Pages"
                                            );

                                            while (rs.next()) {
                                                System.out.printf(displayFormatAllBooks,
                                                        dispNull(rs.getString(bookTitle)),
                                                        dispNull(rs.getString(groupName)),
                                                        dispNull(rs.getString(publisherName)),
                                                        dispNull(rs.getString(yearPublished)),
                                                        rs.getString(numberPages)
                                                );
                                            }
                                            rs.close();
                                            System.out.println();
                                            break;

                                            //Display Book specified by the user
                                        case 6:
                                            String strin;

                                            while (true) {
                                                sql = "Select * From Book ";
                                                rs = stmt.executeQuery(sql);
                                                System.out.print("Enter a book to get more information on: ");
                                                strin = in.nextLine();
                                                boolean end = false;
                                                System.out.println();
                                                while (rs.next()) {
                                                    if (rs.getString("BookTitle").equals(strin)) {
                                                        ResultSetMetaData metadata = rs.getMetaData();
                                                        int columnCount = metadata.getColumnCount();
                                                        for (int i = 1; i <= columnCount; i++) {
                                                            String columnName = metadata.getColumnName(i);
                                                            System.out.println(columnName + '\t' + rs.getString(columnName));
                                                        }
                                                        end = true;
                                                        System.out.println();
                                                    }

                                                }
                                                if (end) {
                                                    break;
                                                }
                                            }
                                    }

                                } while (listChoice != 7);
                                break;

                            case 2: //INSERT BOOK
                                addBook(stmt, conn);

                                break;
                            case 3://REPLACE PREVIOUS PUBLISHER WITH NEW PUBLISHER                          
                                System.out.print("Name of publisher to be replaced: ");

                                if (!isFoundInDataBase("publisher", publisherName, userInput = checkInput(in.nextLine()))) {
                                    System.out.println(userInput);
                                    System.out.println("Publisher not found!");
                                    break;
                                }

                                updateBookPublisher.setString(2, userInput);

                                System.out.print("Enter updated publisher name: ");
                                userInput = checkInput(in.nextLine());
                                addBook.setString(3, userInput);

                                if (!isFoundInDataBase("publisher", publisherName, userInput)) {
                                    addPublisher.setString(1, userInput);
                                    System.out.print("Address: ");
                                    addPublisher.setString(2, checkInput(in.nextLine()));
                                    System.out.print("Phone: ");
                                    addPublisher.setString(3, checkInput(in.nextLine()));
                                    System.out.print("Email: ");
                                    addPublisher.setString(4, checkInput(in.nextLine()));
                                } else {
                                    System.out.println("Publisher already in Database!");
                                    break;
                                }

                                System.out.println();

                                updateBookPublisher.setString(1, userInput);
                                addPublisher.executeUpdate();
                                updateBookPublisher.executeUpdate();
                                break;

                            //remove book
                            case 4:
                                System.out.print("Enter to be removed book title: ");
                                userInput = checkInput(in.nextLine());
                                if (!isFoundInDataBase("book", bookTitle, userInput)) {
                                    System.out.println("Book title '" + userInput
                                            + "' not found in database.");
                                    break;
                                }
                                removeBook.setString(1, userInput);

                                System.out.print("Enter writing group: ");
                                String tempInput = "booktitle = '" + userInput
                                        + "' AND groupname = '"
                                        + (userInput = checkInput(in.nextLine()))
                                        + "'";
                                if (!isValid("book", tempInput)) {
                                    System.out.println("Title by this writing group not found!");
                                    break;
                                }
                                removeBook.setString(2, userInput);

                                if (0 < removeBook.executeUpdate()) {
                                    System.out.println("Title successfully removed from database.");
                                } else {
                                    System.out.println("Error: Something went wrong with title removal.");
                                }
                                System.out.println();
                        }
                    } catch (SQLDataException de) //CATCH DATA INPUT ERROR, SUCH AS ALPHA CHARS TO INT
                    {
                        System.out.print("Something was wrong with the data that was entered1.("
                                + de + ")\nPress enter to continue: ");
                        in.nextLine();
                    } catch (SQLException e) {
                        System.out.print("Something was wrong with the data that was entered.("
                                + e + ")\nPress enter to continue: ");
                        in.nextLine();
                    }

                } while (choice != 5);

                //clean up
                in.close();
                if (!stmt.isClosed()) {
                    stmt.close();
                }
                if (!conn.isClosed()) {
                    conn.close();
                }
                if (!displayWritingGroup.isClosed()) {
                    displayWritingGroup.close();
                }
                if (!displayPublisher.isClosed()) {
                    displayPublisher.close();
                }
                if (!displayAllPublishers.isClosed()) {
                    displayAllPublishers.close();
                }
                if (!displayAllBooks.isClosed()) {
                    displayAllBooks.close();
                }
                if (!displayBook.isClosed()) {
                    displayBook.close();
                }
                if (!removeBook.isClosed()) {
                    removeBook.close();
                }
                if (!addBook.isClosed()) {
                    addBook.close();
                }
                if (!addPublisher.isClosed()) {
                    addPublisher.close();
                }
                if (!updatePublisher.isClosed()) {
                    updatePublisher.close();
                }
                if (!updateBookPublisher.isClosed()) {
                    updateBookPublisher.close();
                }
                if (!addWritingGroup.isClosed()) {
                    addWritingGroup.close();
                }

            } catch (SQLException ex) {
                Logger.getLogger(CECS323JavaProject.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
    }
}
