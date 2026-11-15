# Student Course Management System

A Java Swing application for the COS 201 Programming I lab exercise. It records semester courses, searches by course code, computes total credit units, and saves or loads course lists. A text-based console interface is also available.

## Requirements

JDK 8 or newer. No external libraries are needed.

## Start the graphical interface

```sh
javac Course.java CourseManager.java StudentCourseGui.java
java StudentCourseGui
```

Enter a course code, title, and positive whole number of credit units, then click **Add Course**. Use the other buttons to view records, search by code, calculate totals, save, load, and exit. Enter a code in the Course code field before searching.

To use the included sample, enter `sample-courses.tsv` in the Course file field and click **Load from File**. The two records total **5 credit units**.

Saving overwrites the selected file. Loading validates the complete file before replacing the list. Invalid loads leave existing records intact. The GUI asks for confirmation before loading or exiting with unsaved changes.

## Console alternative

```sh
javac Course.java CourseManager.java StudentCourseApp.java
java StudentCourseApp
```

## Source structure

| File | Purpose |
| --- | --- |
| `Course.java` | Course data, string normalization, and validation |
| `CourseManager.java` | ArrayList storage, searching, recursive totals, and UTF-8 files |
| `StudentCourseGui.java` | Swing fields, buttons, course table, and error dialogs |
| `StudentCourseApp.java` | Seven-option text-based menu |
| `tests/CourseManagerTest.java` | Dependency-free verification checks |

The total-unit calculation uses recursion: an empty range returns zero, a single-course range returns its units, and larger ranges split into two halves. The file format is tab-separated text with code, title, and units on each line.

## Run checks

Compile:

```sh
javac Course.java CourseManager.java StudentCourseGui.java StudentCourseApp.java tests/CourseManagerTest.java
```

Windows PowerShell:

```powershell
java -cp '.;tests' CourseManagerTest
```

macOS or Linux:

```sh
java -cp '.:tests' CourseManagerTest
```

## Development assistance

AI assistance was used in developing this project.
