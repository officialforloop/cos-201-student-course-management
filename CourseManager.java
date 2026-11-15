import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Stores courses and handles searching, totals, and file persistence. */
public class CourseManager {
    private final List<Course> courses = new ArrayList<>();

    public void addCourse(Course course) {
        if (searchByCode(course.getCode()) != null) {
            throw new IllegalArgumentException("That course code is already recorded.");
        }
        courses.add(course);
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public Course searchByCode(String code) {
        String normalized = Course.normalizeCode(code);
        for (Course course : courses) {
            if (course.getCode().equals(normalized)) {
                return course;
            }
        }
        return null;
    }

    public long computeTotalUnits() {
        return totalUnitsRecursive(0, courses.size());
    }

    // Base case: an empty range. Each recursive call divides the range in two.
    // This keeps recursion depth small even for a large course list.
    private long totalUnitsRecursive(int start, int end) {
        if (start == end) {
            return 0;
        }
        if (end - start == 1) {
            return courses.get(start).getUnits();
        }
        int middle = start + (end - start) / 2;
        return totalUnitsRecursive(start, middle) + totalUnitsRecursive(middle, end);
    }

    public void saveToFile(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Course course : courses) {
            lines.add(course.getCode() + "\t" + course.getTitle() + "\t" + course.getUnits());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public void loadFromFile(Path path) throws IOException {
        // Validate the complete file before replacing the current list.
        List<Course> loaded = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String[] fields = lines.get(i).split("\t", -1);
            try {
                if (fields.length != 3) {
                    throw new IllegalArgumentException("Expected code, title, and units separated by tabs.");
                }
                Course course = new Course(fields[0], fields[1], Integer.parseInt(fields[2].trim()));
                if (!codes.add(course.getCode())) {
                    throw new IllegalArgumentException("Duplicate course code: " + course.getCode());
                }
                loaded.add(course);
            } catch (IllegalArgumentException error) {
                throw new IOException("Invalid course data on line " + (i + 1) + ": "
                        + error.getMessage(), error);
            }
        }
        courses.clear();
        courses.addAll(loaded);
    }
}
