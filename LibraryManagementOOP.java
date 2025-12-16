import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LibraryManagementOOP {
    // ----- Data Storage -----
    static class Book {
        int id;
        String title, author;
        boolean available;

        Book(int id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.available = true;
        }
    }

    static class User {
        String username, password, role;

        User(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
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
    JButton logoutButton2 = new JButton("Logout");

    // ----- Constructor -----
    public LibraryManagementOOP() {
        // Sample Users
        users.add(new User("admin", "admin123", "admin"));
        users.add(new User("student", "student123", "student"));

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
                if (user.role.equals("admin")) showAdminPanel();
                else showStudentPanel();
                return;
            }
        }
        JOptionPane.showMessageDialog(frame, "Invalid credentials");
    }

    // ----- Admin Panel -----
    void showAdminPanel() {
        String[] columns = {"ID","Title","Author","Available"};
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
        frame.setSize(600,400);
        adminPanel.setVisible(true);
    }

    void refreshAdminTable() {
        adminTableModel.setRowCount(0);
        for (Book b : books) {
            adminTableModel.addRow(new Object[]{b.id, b.title, b.author, b.available ? "Yes":"No"});
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
        topPanel.add(logoutButton2);
        logoutButton2.addActionListener(e -> logout());

        studentPanel.add(topPanel, BorderLayout.NORTH);
        studentPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

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
        frame.setSize(600,400);
        studentPanel.setVisible(true);
    }

    void refreshStudentTable() {
        studentTableModel.setRowCount(0);
        for (Book b : books) {
            String status = b.available ? "Available" : "Borrowed";
            String action = b.available ? "Borrow" : borrowedBooks.contains(b) ? "Return" : "";
            studentTableModel.addRow(new Object[]{b.id, b.title, b.author, status, action});
        }
    }

    void borrowBook(Book b) {
        if (!b.available) { JOptionPane.showMessageDialog(frame,"Book not available"); return; }
        b.available = false;
        borrowedBooks.add(b);
        refreshStudentTable();
    }

    void returnBook(Book b) {
        b.available = true;
        borrowedBooks.remove(b);
        refreshStudentTable();
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
        SwingUtilities.invokeLater(() -> new LibraryManagementOOP());
    }
}   