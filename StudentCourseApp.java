import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/** Entry point for the COS 201 console application. */
public class StudentCourseApp {
    private final CourseManager manager = new CourseManager();
    private final Scanner input = new Scanner(System.in);
    private boolean running = true;

    public static void main(String[] args) {
        new StudentCourseApp().run();
    }

    private void run() {
        while (running) {
            showMenu();
            String choice = readLine("Choose an option: ");
            if (!running) {
                break;
            }
            try {
                switch (choice) {
                    case "1": addCourse(); break;
                    case "2": viewAllCourses(); break;
                    case "3": searchCourse(); break;
                    case "4":
                        System.out.println("Total credit units: " + manager.computeTotalUnits());
                        break;
                    case "5": saveCourses(); break;
                    case "6": loadCourses(); break;
                    case "7":
                        running = false;
                        System.out.println("Goodbye. Remember to save before exiting.");
                        break;
                    default: System.out.println("Invalid menu option. Enter a number from 1 to 7.");
                }
            } catch (NumberFormatException error) {
                System.out.println("Invalid units. Enter a positive whole number within the integer range.");
            } catch (InvalidPathException error) {
                System.out.println("Invalid file path. Please try again.");
            } catch (IllegalArgumentException error) {
                System.out.println(error.getMessage());
            } catch (IOException | SecurityException error) {
                System.out.println("File operation failed: " + error.getMessage());
            }
        }
    }

    private void showMenu() {
        System.out.println("\n=== STUDENT COURSE MANAGEMENT SYSTEM ===");
        System.out.println("1. Add Course");
        System.out.println("2. View All Courses");
        System.out.println("3. Search Course by Code");
        System.out.println("4. Compute Total Units");
        System.out.println("5. Save to File");
        System.out.println("6. Load from File (replaces current list)");
        System.out.println("7. Exit Program (save first to keep changes)");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) {
            running = false;
            System.out.println("\nInput closed. Program stopped.");
            return "";
        }
        return input.nextLine().trim();
    }

    private void addCourse() {
        String code = readLine("Course code (e.g. COS 201): ");
        if (!running) { return; }
        String title = readLine("Course title: ");
        if (!running) { return; }
        String units = readLine("Credit units: ");
        if (!running) { return; }
        manager.addCourse(new Course(code, title, Integer.parseInt(units)));
        System.out.println("Course added successfully.");
    }

    private void printCourse(Course course) {
        System.out.printf("%-15s %-45s %5d%n", course.getCode(), course.getTitle(), course.getUnits());
    }

    private void printHeading() {
        System.out.printf("%-15s %-45s %5s%n", "CODE", "TITLE", "UNITS");
        System.out.println("-------------------------------------------------------------------");
    }

    private void viewAllCourses() {
        if (manager.getCourses().isEmpty()) {
            System.out.println("No courses recorded yet.");
            return;
        }
        printHeading();
        for (Course course : manager.getCourses()) {
            printCourse(course);
        }
    }

    private void searchCourse() {
        String code = readLine("Enter course code: ");
        if (!running) { return; }
        Course course = manager.searchByCode(code);
        if (course == null) {
            System.out.println("Course not found.");
        } else {
            printHeading();
            printCourse(course);
        }
    }

    private Path readPath() {
        String filename = readLine("File path (Enter for courses.tsv): ");
        return Paths.get(filename.isEmpty() ? "courses.tsv" : filename);
    }

    private void saveCourses() throws IOException {
        Path path = readPath();
        if (!running) { return; }
        manager.saveToFile(path);
        System.out.println("Saved " + manager.getCourses().size() + " course(s) to " + path.toAbsolutePath());
    }

    private void loadCourses() throws IOException {
        Path path = readPath();
        if (!running) { return; }
        manager.loadFromFile(path);
        System.out.println("Loaded " + manager.getCourses().size() + " course(s).");
    }
}
