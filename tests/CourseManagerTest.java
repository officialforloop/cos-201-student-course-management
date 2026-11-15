import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/** Small dependency-free checks. Run using the README commands. */
public class CourseManagerTest {
    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }

    public static void main(String[] args) throws IOException {
        CourseManager manager = new CourseManager();
        check(manager.computeTotalUnits() == 0, "Empty total");
        manager.addCourse(new Course(" cos   201 ", " Programming I ", 3));
        manager.addCourse(new Course("MTH 201", "Mathematical Methods", 2));
        check(manager.computeTotalUnits() == 5, "Recursive total");
        check(manager.searchByCode(" cos 201 ").getTitle().equals("Programming I"), "Normalized search");
        check(manager.searchByCode("CSC 999") == null, "Missing course");
        try {
            manager.addCourse(new Course("COS 201", "Duplicate", 3));
            throw new AssertionError("Duplicate accepted");
        } catch (IllegalArgumentException expected) { /* Expected. */ }
        for (int units : new int[] {0, -1}) {
            try {
                new Course("TEST 101", "Test", units);
                throw new AssertionError("Invalid units accepted");
            } catch (IllegalArgumentException expected) { /* Expected. */ }
        }
        for (String title : new String[] {" ", "Title\tfield", "Title\nline"}) {
            try {
                new Course("TEST 101", title, 1);
                throw new AssertionError("Invalid title accepted");
            } catch (IllegalArgumentException expected) { /* Expected. */ }
        }
        Path file = Files.createTempFile("cos201-test-", ".tsv");
        try {
            manager.addCourse(new Course("FRE 101", "Fran\u00e7ais", 1));
            manager.saveToFile(file);
            CourseManager restored = new CourseManager();
            restored.loadFromFile(file);
            check(restored.getCourses().size() == 3, "Round-trip course count");
            check(restored.computeTotalUnits() == 6, "Round-trip units");
            check(restored.searchByCode("FRE 101").getTitle().equals("Fran\u00e7ais"), "UTF-8 round trip");
            for (String invalid : new String[] {
                    "BAD\tBroken\tnot-a-number", "BAD\tBroken\t0", "missing fields",
                    "COS 201\tDuplicate\t2"}) {
                Files.write(file, Arrays.asList("COS 201\tProgramming I\t3", invalid), StandardCharsets.UTF_8);
                try {
                    restored.loadFromFile(file);
                    throw new AssertionError("Invalid file accepted");
                } catch (IOException expected) { /* Expected. */ }
                check(restored.getCourses().size() == 3 && restored.computeTotalUnits() == 6,
                        "Failed load changed current courses");
            }
            Files.write(file, new byte[0]);
            restored.loadFromFile(file);
            check(restored.getCourses().isEmpty(), "Empty file");
        } finally {
            Files.deleteIfExists(file);
        }
        CourseManager large = new CourseManager();
        large.addCourse(new Course("A", "First", Integer.MAX_VALUE));
        large.addCourse(new Course("B", "Second", Integer.MAX_VALUE));
        check(large.computeTotalUnits() == 4294967294L, "Total overflow");
        System.out.println("All CourseManager checks passed.");
    }
}
