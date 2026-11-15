import java.util.Locale;

/** A course and its validated details. */
public class Course {
    private final String code;
    private final String title;
    private final int units;

    public Course(String code, String title, int units) {
        this.code = normalizeCode(code);
        this.title = title.trim();
        if (this.code.isEmpty() || this.title.isEmpty()) {
            throw new IllegalArgumentException("Course code and title cannot be blank.");
        }
        if (this.code.contains("\t") || this.title.contains("\t")
                || this.code.contains("\n") || this.title.contains("\n")
                || this.code.contains("\r") || this.title.contains("\r")) {
            throw new IllegalArgumentException("Course details cannot contain tabs or line breaks.");
        }
        if (units <= 0) {
            throw new IllegalArgumentException("Units must be a positive whole number.");
        }
        this.units = units;
    }

    public static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replaceAll(" +", " ");
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getUnits() { return units; }
}
