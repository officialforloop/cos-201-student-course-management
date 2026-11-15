import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/** A simple graphical interface for the COS 201 course manager. */
@SuppressWarnings("serial")
public final class StudentCourseGui extends JFrame {
    private static final long serialVersionUID = 1L;
    private final CourseManager manager = new CourseManager();
    private final JTextField code = new JTextField();
    private final JTextField title = new JTextField();
    private final JTextField units = new JTextField();
    private final JTextField file = new JTextField("courses.tsv", 36);
    private final JLabel status = new JLabel("Enter course details, then click Add Course.");
    private final JLabel total = new JLabel("Total credit units: 0");
    private final DefaultTableModel rows = new DefaultTableModel(
            new String[] {"Course code", "Course title", "Credit units"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(rows);
    private boolean unsaved;

    public StudentCourseGui() {
        super("COS 201 - Student Course Management System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent event) { exit(); }
        });

        JPanel content = new JPanel(new BorderLayout(12, 16));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setContentPane(content);

        JPanel header = new JPanel(new BorderLayout(0, 14));
        JLabel heading = new JLabel("Student Course Management System");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 23f));
        header.add(heading, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridLayout(2, 3, 12, 6));
        fields.add(new JLabel("Course code (e.g. COS 201)"));
        fields.add(new JLabel("Course title"));
        fields.add(new JLabel("Credit units"));
        fields.add(code); fields.add(title); fields.add(units);
        header.add(fields, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(button("Add Course", this::addCourse));
        actions.add(button("Search by Code", this::searchCourse));
        actions.add(button("View All Courses", () -> {
            refresh();
            status.setText(manager.getCourses().isEmpty() ? "No courses recorded yet."
                    : "Displaying all " + manager.getCourses().size() + " courses.");
        }));
        header.add(actions, BorderLayout.SOUTH);
        content.add(header, BorderLayout.NORTH);

        table.setRowHeight(30);
        table.setFont(table.getFont().deriveFont(15f));
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(520);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.setFillsViewportHeight(true);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 14));
        JPanel files = new JPanel(new BorderLayout(8, 0));
        files.add(new JLabel("Course file"), BorderLayout.WEST);
        files.add(file, BorderLayout.CENTER);
        JPanel fileButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        fileButtons.add(button("Save to File", this::save));
        fileButtons.add(button("Load from File", this::load));
        files.add(fileButtons, BorderLayout.EAST);
        bottom.add(files, BorderLayout.NORTH);
        JPanel totals = new JPanel(new BorderLayout());
        total.setFont(total.getFont().deriveFont(Font.BOLD, 18f));
        totals.add(total, BorderLayout.WEST);
        JPanel finalButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        finalButtons.add(button("Compute Total Units", () -> {
            total.setText("Total credit units: " + manager.computeTotalUnits());
            status.setText("Total computed for " + manager.getCourses().size() + " courses.");
        }));
        finalButtons.add(button("Exit", this::exit));
        totals.add(finalButtons, BorderLayout.EAST);
        bottom.add(totals, BorderLayout.CENTER);
        bottom.add(status, BorderLayout.SOUTH);
        content.add(bottom, BorderLayout.SOUTH);

        setSize(980, 620);
        setLocationRelativeTo(null);
    }

    private JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        button.addActionListener(event -> {
            try {
                action.run();
            } catch (NumberFormatException error) {
                showError("Credit units must be a positive whole number within the integer range.");
            } catch (InvalidPathException error) {
                showError("Enter a valid file path.");
            } catch (IllegalArgumentException | SecurityException error) {
                showError(error.getMessage());
            }
        });
        return button;
    }

    private void addCourse() {
        manager.addCourse(new Course(code.getText(), title.getText(), Integer.parseInt(units.getText().trim())));
        unsaved = true;
        refresh();
        status.setText("Course added successfully.");
        code.setText(""); title.setText(""); units.setText("");
        code.requestFocusInWindow();
    }

    private void refresh() {
        rows.setRowCount(0);
        for (Course course : manager.getCourses()) {
            rows.addRow(new Object[] {course.getCode(), course.getTitle(), course.getUnits()});
        }
        total.setText("Total credit units: " + manager.computeTotalUnits());
    }

    private void searchCourse() {
        Course found = manager.searchByCode(code.getText());
        table.clearSelection();
        if (found == null) {
            status.setText("Course not found. Enter its code in the Course code field.");
            return;
        }
        int index = manager.getCourses().indexOf(found);
        table.setRowSelectionInterval(index, index);
        table.scrollRectToVisible(table.getCellRect(index, 0, true));
        status.setText("Found " + found.getCode() + ": " + found.getTitle() + ".");
    }

    private void save() {
        try {
            manager.saveToFile(Paths.get(file.getText().trim()));
            unsaved = false;
            status.setText("Saved " + manager.getCourses().size() + " courses to " + file.getText().trim());
        } catch (IOException error) { showError("Save failed: " + error.getMessage()); }
    }

    private void load() {
        if (unsaved && JOptionPane.showConfirmDialog(this,
                "Loading replaces the current list. Continue without saving?", "Load courses",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) { return; }
        try {
            manager.loadFromFile(Paths.get(file.getText().trim()));
            unsaved = false;
            refresh();
            status.setText("Loaded " + manager.getCourses().size() + " courses from " + file.getText().trim());
        } catch (IOException error) { showError("Load failed: " + error.getMessage()); }
    }

    private void showError(String message) {
        status.setText(message);
        JOptionPane.showMessageDialog(this, message, "Course manager", JOptionPane.ERROR_MESSAGE);
    }

    private void exit() {
        if (unsaved && JOptionPane.showConfirmDialog(this,
                "There are unsaved courses. Exit without saving?", "Exit",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) { return; }
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { /* Swing's default look remains available. */ }
            StudentCourseGui application = new StudentCourseGui();
            if (args.length > 0) { application.file.setText(args[0]); }
            application.setVisible(true);
        });
    }
}
