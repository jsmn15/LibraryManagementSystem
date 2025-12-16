import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LibraryManagementGUI {
    // ----- Data Storage -----
    static class Book {
        int id;
        String title, author;
        boolean available;
        String borrower;  // NEW: Track who borrowed the book (username or null)

        Book(int id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.available = true;
            this.borrower = null;  // NEW: Initially no borrower
        }
    }

    // UPDATED: User is now an abstract class (superclass for inheritance)
    static abstract class User {
        String username, password, role;

        User(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        // NEW: Abstract method for polymorphism and override
        abstract void performAction();
    }

    // UPDATED: Admin is now an inner class (not static) to access outer class methods
    class Admin extends User {
        Admin(String username, String password) {
            super(username, password, "admin");
        }

        // NEW: Override performAction to show admin panel (demonstrates override and polymorphism)
        @Override
        void performAction() {
            showAdminPanel();  // Now works because Admin is an inner class
        }
    }

    // UPDATED: Student is now an inner class (not static) to access outer class methods
    class Student extends User {
        Student(String username, String password) {
            super(username, password, "student");
        }

        // NEW: Override performAction to show student panel (demonstrates override and polymorphism)
        @Override
        void performAction() {
            showStudentPanel();  // Now works because Student is an inner class
        }
    }

    static ArrayList<Book> books = new ArrayList<>();
    static ArrayList<Book> borrowedBooks = new ArrayList<>();
    static ArrayList<User> users = new ArrayList<>();
    static User currentUser = null;

    // ----- GUI Components -----
    JFrame frame = new JFrame("Library Management System");

    JPanel loginPanel = new JPanel();
    JTextField usernameField = new JTextField(15);
    JPasswordField passwordField = new JPasswordField(15);
    JButton loginButton = new JButton("Login");

    JPanel adminPanel = new JPanel(new BorderLayout());
    JTable adminTable;
    DefaultTableModel adminTableModel;
    JButton addBookButton = new JButton("Add Book");
    JButton logoutButton1 = new JButton("Logout");

    JPanel studentPanel = new JPanel(new BorderLayout());
    JTable studentTable;
    DefaultTableModel studentTableModel;
    JButton borrowButton = new JButton("Borrow Selected Book");  // NEW: Button for borrowing
    JButton returnButton = new JButton("Return Selected Book");  // NEW: Button for returning
    JButton logoutButton2 = new JButton("Logout");

    // ----- Constructor -----
    public LibraryManagementGUI() {
        // UPDATED: Use Admin and Student subclasses (demonstrates inheritance)
        users.add(new Admin("admin", "admin123"));
        users.add(new Student("student", "student123"));

        // Sample Books
        books.add(new Book(1, "Book One", "Author A"));
        books.add(new Book(2, "Book Two", "Author B"));

        // ----- Login Panel -----
        loginPanel.setLayout(new GridLayout(3,2,5,5));
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel(""));
        loginPanel.add(loginButton);

        loginButton.addActionListener(e -> login());

        frame.add(loginPanel);
        frame.setSize(400,200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // ----- Login Function -----
    void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        for (User user : users) {
            if (user.username.equals(username) && user.password.equals(password)) {
                currentUser = user;
                loginPanel.setVisible(false);
                // UPDATED: Use polymorphism - call performAction() which is overridden in subclasses
                currentUser.performAction();
                return;
            }
        }
        JOptionPane.showMessageDialog(frame, "Invalid credentials");
    }

    // ----- Admin Panel -----
    void showAdminPanel() {
        String[] columns = {"ID","Title","Author","Available","Borrower"};  // UPDATED: Added "Borrower" column
        adminTableModel = new DefaultTableModel(columns,0);
        adminTable = new JTable(adminTableModel);
        refreshAdminTable();

        JPanel topPanel = new JPanel();
        topPanel.add(addBookButton);
        topPanel.add(logoutButton1);

        addBookButton.addActionListener(e -> addBook());
        logoutButton1.addActionListener(e -> logout());

        adminPanel.add(topPanel, BorderLayout.NORTH);
        adminPanel.add(new JScrollPane(adminTable), BorderLayout.CENTER);

        // Right-click menu for edit/delete
        JPopupMenu popup = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit Book");
        JMenuItem deleteItem = new JMenuItem("Delete Book");
        popup.add(editItem);
        popup.add(deleteItem);

        adminTable.setComponentPopupMenu(popup);
        adminTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) popup.show(adminTable, e.getX(), e.getY()); }
            public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) popup.show(adminTable, e.getX(), e.getY()); }
        });

        editItem.addActionListener(e -> editBook());
        deleteItem.addActionListener(e -> deleteBook());

        frame.add(adminPanel);
        frame.setSize(700,400);  // UPDATED: Slightly wider for new column
        adminPanel.setVisible(true);
    }

    void refreshAdminTable() {
        adminTableModel.setRowCount(0);
        for (Book b : books) {
            String borrowerDisplay = (b.borrower == null) ? "" : b.borrower;  // NEW: Display borrower or blank
            adminTableModel.addRow(new Object[]{b.id, b.title, b.author, b.available ? "Yes":"No", borrowerDisplay});
        }
    }

    void addBook() {
        String title = JOptionPane.showInputDialog("Enter Book Title:");
        if (title == null || title.isEmpty()) return;
        String author = JOptionPane.showInputDialog("Enter Author:");
        if (author == null || author.isEmpty()) return;
        int id = books.size()>0 ? books.get(books.size()-1).id + 1 : 1;
        books.add(new Book(id, title, author));
        refreshAdminTable();
    }

    void editBook() {
        int row = adminTable.getSelectedRow();
        if (row == -1) return;
        Book b = books.get(row);
        String title = JOptionPane.showInputDialog("Edit Title:", b.title);
        if (title == null || title.isEmpty()) return;
        String author = JOptionPane.showInputDialog("Edit Author:", b.author);
        if (author == null || author.isEmpty()) return;
        b.title = title;
        b.author = author;
        refreshAdminTable();
    }

    void deleteBook() {
        int row = adminTable.getSelectedRow();
        if (row == -1) return;
        if (JOptionPane.showConfirmDialog(frame, "Delete this book?")==JOptionPane.YES_OPTION) {
            books.remove(row);
            refreshAdminTable();
        }
    }

    // ----- Student Panel -----
    void showStudentPanel() {
        String[] columns = {"ID","Title","Author","Status","Action"};
        studentTableModel = new DefaultTableModel(columns,0);
        studentTable = new JTable(studentTableModel);
        refreshStudentTable();

        JPanel topPanel = new JPanel();
        topPanel.add(borrowButton);  // NEW: Borrow button
        topPanel.add(returnButton);  // NEW: Return button
        topPanel.add(logoutButton2);
        logoutButton2.addActionListener(e -> logout());

        // NEW: Action listeners for borrow/return buttons
        borrowButton.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a book to borrow.");
                return;
            }
            Book b = books.get(row);
            borrowBook(b);
        });

        returnButton.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a book to return.");
                return;
            }
            Book b = books.get(row);
            returnBook(b);
        });

        studentPanel.add(topPanel, BorderLayout.NORTH);
        studentPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // UPDATED: Kept existing click functionality as fallback, but buttons are primary
        studentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                int col = studentTable.columnAtPoint(e.getPoint());
                if (col == 4) { // Action column
                    Book b = books.get(row);
                    if (b.available) borrowBook(b);
                    else if (borrowedBooks.contains(b)) returnBook(b);
                }
            }
        });

        frame.add(studentPanel);
        frame.setSize(700,400);  // UPDATED: Slightly wider for buttons
        studentPanel.setVisible(true);
    }

    void refreshStudentTable() {
        studentTableModel.setRowCount(0);
        for (Book b : books) {
            String status = b.available ? "Available" : "Borrowed";
            String action = b.available ? "Borrow" : (borrowedBooks.contains(b) ? "Return" : "");
            studentTableModel.addRow(new Object[]{b.id, b.title, b.author, status, action});
        }
    }

    void borrowBook(Book b) {
        if (!b.available) {
            JOptionPane.showMessageDialog(frame, "Book not available or already borrowed by another user.");
            return;
        }
        b.available = false;
        b.borrower = currentUser.username;  // NEW: Set borrower
        borrowedBooks.add(b);
        refreshStudentTable();
        JOptionPane.showMessageDialog(frame, "Book borrowed successfully!");
    }

    void returnBook(Book b) {
        if (!borrowedBooks.contains(b)) {
            JOptionPane.showMessageDialog(frame, "You haven't borrowed this book.");
            return;
        }
        b.available = true;
        b.borrower = null;  // NEW: Clear borrower
        borrowedBooks.remove(b);
        refreshStudentTable();
        JOptionPane.showMessageDialog(frame, "Book returned successfully!");
    }

    // ----- Logout -----
    void logout() {
        currentUser = null;
        adminPanel.setVisible(false);
        studentPanel.setVisible(false);
        loginPanel.setVisible(true);
    }

    // ----- Main -----
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryManagementGUI());
    }
}
