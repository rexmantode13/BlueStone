package com.bluestone.attendance.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.bluestone.attendance.data.dao.AttendanceDao;
import com.bluestone.attendance.data.dao.SettingsDao;
import com.bluestone.attendance.data.dao.StudentDao;
import com.bluestone.attendance.data.dao.SubjectDao;
import com.bluestone.attendance.data.dao.SyllabusDao;
import com.bluestone.attendance.data.dao.TimetableDao;
import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.data.entities.Student;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.entities.SyllabusUnit;
import com.bluestone.attendance.data.entities.Topic;
import com.bluestone.attendance.data.entities.TimetableSession;
import com.bluestone.attendance.data.entities.UserSettings;
import com.bluestone.attendance.data.repository.AppExecutor;
import com.bluestone.attendance.domain.model.AttendanceStatus;
import com.bluestone.attendance.utils.PasswordHasher;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Room database for BlueStone.
 * Note: A single local student is assumed for this local-first offline application.
 */
@Database(
    entities = {
        Student.class,
        Subject.class,
        AttendanceRecord.class,
        SyllabusUnit.class,
        Topic.class,
        TimetableSession.class,
        UserSettings.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "bluestone_attendance.db";
    private static volatile AppDatabase INSTANCE;

    public abstract StudentDao studentDao();
    public abstract SubjectDao subjectDao();
    public abstract AttendanceDao attendanceDao();
    public abstract SyllabusDao syllabusDao();
    public abstract TimetableDao timetableDao();
    public abstract SettingsDao settingsDao();

    // Migration scaffold: placeholder for future schema migrations
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Future schema changes go here
        }
    };

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            AppExecutor.getInstance().diskIO().execute(() -> {
                                if (INSTANCE != null) {
                                    INSTANCE.seedDatabaseSync();
                                }
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Seeds initial sample demo data.
     * Can also be called during Settings "Reset Demo Data".
     */
    public void seedDatabaseSync() {
        runInTransaction(() -> {
            attendanceDao().deleteAll();
            timetableDao().deleteAll();
            syllabusDao().deleteAllUnits();
            subjectDao().deleteAll();
            studentDao().deleteAll();

            // 1. Seed 3 Demo Students (2 Boys, 1 Girl): CS2026001, CS2026002, CS2026003 (Password: Blue@123)
            byte[] salt1 = PasswordHasher.generateSalt();
            byte[] hash1 = PasswordHasher.hashPassword("Blue@123".toCharArray(), salt1);
            Student demoBoy1 = new Student(
                    "CS2026001",
                    "Rohan Malhotra",
                    "rohan.cs@bluestone.edu",
                    "B.Tech Computer Science & Engineering",
                    2,
                    "2025–2026",
                    "Computer Science Engineering",
                    PasswordHasher.toHex(hash1),
                    PasswordHasher.toHex(salt1)
            );
            studentDao().insert(demoBoy1);

            byte[] salt2 = PasswordHasher.generateSalt();
            byte[] hash2 = PasswordHasher.hashPassword("Blue@123".toCharArray(), salt2);
            Student demoBoy2 = new Student(
                    "CS2026002",
                    "Aryan Sharma",
                    "aryan.cs@bluestone.edu",
                    "B.Tech Computer Science & Engineering",
                    2,
                    "2025–2026",
                    "Computer Science Engineering",
                    PasswordHasher.toHex(hash2),
                    PasswordHasher.toHex(salt2)
            );
            studentDao().insert(demoBoy2);

            byte[] salt3 = PasswordHasher.generateSalt();
            byte[] hash3 = PasswordHasher.hashPassword("Blue@123".toCharArray(), salt3);
            Student demoGirl1 = new Student(
                    "CS2026003",
                    "Ananya Patel",
                    "ananya.cs@bluestone.edu",
                    "B.Tech Computer Science & Engineering",
                    2,
                    "2025–2026",
                    "Computer Science Engineering",
                    PasswordHasher.toHex(hash3),
                    PasswordHasher.toHex(salt3)
            );
            studentDao().insert(demoGirl1);

            // 2. Seed User Settings with active student Rohan
            settingsDao().insert(new UserSettings(75.0, 0, "CS2026001"));

            // 3. Seed Allocated Semester 2 Subjects (6 Subjects: 5 to 7 allocated subjects)
            List<Subject> subjects = new ArrayList<>();
            subjects.add(new Subject("Object-Oriented Programming", "CS201", 2, 4, "Prof. Bjarne Stroustrup", 75.0, "Core Curriculum", false));
            subjects.add(new Subject("Data Structures and Algorithms", "CS202", 2, 4, "Prof. Donald Knuth", 75.0, "Core Curriculum", false));
            subjects.add(new Subject("Database Management Systems", "CS203", 2, 3, "Dr. Edgar Codd", 75.0, "Core Curriculum", false));
            subjects.add(new Subject("Computer Organization & Architecture", "CS204", 2, 3, "Prof. David Patterson", 75.0, "Core Curriculum", false));
            subjects.add(new Subject("Software Engineering & Design", "CS205", 2, 3, "Dr. Margaret Hamilton", 75.0, "Core Curriculum", false));
            subjects.add(new Subject("Discrete Mathematics", "MA201", 2, 4, "Prof. George Boole", 75.0, "Core Curriculum", false));

            for (Subject s : subjects) {
                long insertedId = subjectDao().insert(s);
                s.setId(insertedId);
            }

            // Find subjects
            Subject cs201 = findSubjectByCode(subjects, "CS201");
            Subject cs202 = findSubjectByCode(subjects, "CS202");
            Subject cs203 = findSubjectByCode(subjects, "CS203");
            Subject cs204 = findSubjectByCode(subjects, "CS204");
            Subject cs205 = findSubjectByCode(subjects, "CS205");
            Subject ma201 = findSubjectByCode(subjects, "MA201");

            // 4. Seed Syllabus for CS202 (5 units)
            if (cs202 != null) {
                long cs202Id = cs202.getId();
                syllabusDao().insertUnit(new SyllabusUnit(cs202Id, 1, "Introduction & Asymptotic Complexity",
                        "Time & Space complexity, Big-O, Omega, Theta notation, Recurrences",
                        "Analyze worst, average and best case complexity of algorithms", "Covered in Week 1-2", true));

                syllabusDao().insertUnit(new SyllabusUnit(cs202Id, 2, "Linked Lists & Node Pointers",
                        "Singly, Doubly, Circular Linked Lists, Pointer manipulations, Skip lists",
                        "Implement dynamic lists and understand node memory allocations", "Covered in Week 3-4", true));

                syllabusDao().insertUnit(new SyllabusUnit(cs202Id, 3, "Stacks & Queues",
                        "Array and linked representations, Expression evaluation (Infix to Postfix), Deque",
                        "Apply stack in recursion and queue in BFS and buffering", "Covered in Week 5-6", true));

                syllabusDao().insertUnit(new SyllabusUnit(cs202Id, 4, "Trees & Binary Search Trees",
                        "Binary Trees, Traversals, BST operations, AVL Trees, Heap data structure",
                        "Understand hierarchical data structures and balanced tree rotations", "In progress", false));

                syllabusDao().insertUnit(new SyllabusUnit(cs202Id, 5, "Graphs, Sorting & Searching",
                        "BFS, DFS, Dijkstra's algorithm, QuickSort, MergeSort, Hash tables",
                        "Master graph traversals and optimal comparison sorting algorithms", "Upcoming", false));
            }

            // Syllabus units for other subjects
            for (Subject s : subjects) {
                if (cs202 != null && s.getId() == cs202.getId()) continue;
                syllabusDao().insertUnit(new SyllabusUnit(s.getId(), 1, "Foundational Principles",
                        "Core concepts and foundational paradigms", "Grasp basic domain definitions", "Initial unit", true));
                syllabusDao().insertUnit(new SyllabusUnit(s.getId(), 2, "Core Methodologies & Architecture",
                        "Key mechanisms and practical implementations", "Apply fundamentals to design problems", "Active unit", false));
            }

            // 5. Seed Exactly 5 Classes per day for Monday to Friday (Showcase 5 classes)
            if (cs201 != null && cs202 != null && cs203 != null && cs204 != null && cs205 != null && ma201 != null) {
                for (int day = 1; day <= 5; day++) {
                    // Pick 5 varied subjects for each day of the week
                    Subject s1 = (day == 1) ? cs202 : (day == 2 ? cs201 : (day == 3 ? ma201 : (day == 4 ? cs203 : cs205)));
                    Subject s2 = (day == 1) ? cs201 : (day == 2 ? cs204 : (day == 3 ? cs202 : (day == 4 ? cs205 : cs203)));
                    Subject s3 = (day == 1) ? cs203 : (day == 2 ? cs205 : (day == 3 ? cs204 : (day == 4 ? cs201 : cs202)));
                    Subject s4 = (day == 1) ? cs204 : (day == 2 ? cs203 : (day == 3 ? cs201 : (day == 4 ? ma201 : cs204)));
                    Subject s5 = (day == 1) ? cs205 : (day == 2 ? ma201 : (day == 3 ? cs205 : (day == 4 ? cs202 : cs201)));

                    timetableDao().insert(new TimetableSession(s1.getId(), day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Lab 201", s1.getFaculty(), false));
                    timetableDao().insert(new TimetableSession(s2.getId(), day, LocalTime.of(10, 15), LocalTime.of(11, 15), "Room 302", s2.getFaculty(), false));
                    timetableDao().insert(new TimetableSession(s3.getId(), day, LocalTime.of(11, 30), LocalTime.of(12, 30), "Room 105", s3.getFaculty(), false));
                    timetableDao().insert(new TimetableSession(s4.getId(), day, LocalTime.of(13, 30), LocalTime.of(14, 30), "Hall B", s4.getFaculty(), false));
                    timetableDao().insert(new TimetableSession(s5.getId(), day, LocalTime.of(14, 45), LocalTime.of(15, 45), "Lab 102", s5.getFaculty(), false));
                }
            }

            // 6. Seed Rich Attendance History for ALL 6 Subjects across past 7 weeks (160+ records)
            LocalDate today = LocalDate.now();
            List<AttendanceRecord> initialRecords = new ArrayList<>();

            for (Subject s : subjects) {
                for (int i = 35; i >= 1; i--) {
                    LocalDate recordDate = today.minusDays(i);
                    int dayValue = recordDate.getDayOfWeek().getValue();
                    if (dayValue >= 6) continue; // Skip weekends

                    AttendanceStatus status;
                    // CS204 is deliberately kept below threshold (~64%) so exactly 1 subject is below target!
                    if ("CS204".equals(s.getCode())) {
                        status = (i % 3 == 0) ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT;
                    } else if ("CS203".equals(s.getCode())) {
                        status = (i % 5 == 0) ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT;
                    } else if ("CS201".equals(s.getCode())) {
                        status = (i % 6 == 0) ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT;
                    } else if ("CS205".equals(s.getCode())) {
                        status = (i % 8 == 0) ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT;
                    } else if ("CS202".equals(s.getCode())) {
                        status = (i % 10 == 0) ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT;
                    } else { // MA201
                        status = (i % 12 == 0) ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT;
                    }

                    initialRecords.add(new AttendanceRecord(
                            s.getId(),
                            recordDate,
                            1,
                            status,
                            "Regular session",
                            false
                    ));
                }
            }
            if (!initialRecords.isEmpty()) {
                attendanceDao().insertAll(initialRecords);
            }
        });
    }

    private static Subject findSubjectByCode(List<Subject> subjects, String code) {
        for (Subject s : subjects) {
            if (code.equals(s.getCode())) return s;
        }
        return null;
    }
}
