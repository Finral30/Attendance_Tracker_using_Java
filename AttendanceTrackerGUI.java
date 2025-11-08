import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceTrackerGUI extends JFrame {
    private ArrayList<Student> students = new ArrayList<>();
    private static final String FILE_NAME = "attendance_master.txt";

    private static final String FONT_FAMILY = "Poppins";
    private static final int FONT_SIZE_LABEL = 16;
    private static final int FONT_SIZE_FIELD = 18;
    private static final int FONT_SIZE_BUTTON = 15;
    private static final int FONT_SIZE_TEXTAREA = 18;

    private JTextArea textArea;
    private JTextField nameField, rollField;
    private String currentDate;

    public AttendanceTrackerGUI() {
        setTitle("Attendance Tracker");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        applyGlobalFont();
        loadFromFile();
        resetDailyStatus(); // ✅ reset flags daily
        setupUI();
    }

    private void applyGlobalFont() {
        Font labelFont = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_LABEL);
        Font fieldFont = new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_FIELD);
        Font buttonFont = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_BUTTON);
        Font textAreaFont = new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_TEXTAREA);
        Font tabFont = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_LABEL - 2);

        UIManager.put("Label.font", labelFont);
        UIManager.put("TextField.font", fieldFont);
        UIManager.put("Button.font", buttonFont);
        UIManager.put("TextArea.font", textAreaFont);
        UIManager.put("TabbedPane.font", tabFont);
        UIManager.put("OptionPane.font", labelFont);
    }

    private void setupUI() {
        currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        JTabbedPane tabs = new JTabbedPane();

        // --- Add Student Tab ---
        JPanel addPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(250, 35));

        JLabel rollLabel = new JLabel("Roll Number:");
        rollField = new JTextField(20);
        rollField.setPreferredSize(new Dimension(250, 35));

        JButton addBtn = new JButton("Add Student");
        addBtn.setPreferredSize(new Dimension(200, 40));
        addBtn.addActionListener(e -> addStudent());

        gbc.gridx = 0; gbc.gridy = 0;
        addPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        addPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        addPanel.add(rollLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        addPanel.add(rollField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        addPanel.add(addBtn, gbc);

        // --- Mark Attendance Tab ---
        JPanel markPanel = new JPanel(new BorderLayout());
        JButton markBtn = new JButton("Mark Attendance");
        markPanel.add(markBtn, BorderLayout.NORTH);
        textArea = new JTextArea();
        textArea.setEditable(false);
        markPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        markBtn.addActionListener(e -> markAttendance());

        // --- View Attendance Tab ---
        JPanel viewPanel = new JPanel(new BorderLayout());
        JTextArea viewArea = new JTextArea();
        viewArea.setEditable(false);
        JButton refreshBtn = new JButton("Refresh");
        viewPanel.add(new JScrollPane(viewArea), BorderLayout.CENTER);
        viewPanel.add(refreshBtn, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> {
            viewArea.setText("");
            int srNo = 1;
            int totalStudents = students.size();
            int totalPresent = 0;
            int totalAbsent = 0;

            for (Student s : students) {
                viewArea.append(String.format(
                        "%-3d | Roll: %-3d | Name: %-20s | Total: %-2d | Present: %-2d | Attendance: %-6.2f%% | Marked Today: %s%n",
                        srNo++, s.getRollNumber(), s.getName(), s.getTotalDays(), s.getPresentDays(),
                        s.getAttendancePercentage(), s.isMarkedToday() ? "✅" : "❌"));

                totalPresent += s.getPresentDays();
                totalAbsent += (s.getTotalDays() - s.getPresentDays());
            }

            viewArea.append("\n---------------------------------------------------------\n");
            viewArea.append(String.format("Total Students: %d | Present: %d | Absent: %d%n",
                    totalStudents, totalPresent, totalAbsent));
        });

        // --- Settings Tab ---
        JPanel settingsPanel = new JPanel(new GridLayout(1, 1, 10, 10));
        JButton clearBtn = new JButton("Clear All Records");
        settingsPanel.add(clearBtn);
        clearBtn.addActionListener(e -> clearAllRecords());

        tabs.add("Add Student", addPanel);
        tabs.add("Mark Attendance", markPanel);
        tabs.add("View Attendance", viewPanel);
        tabs.add("Settings", settingsPanel);

        add(tabs);
    }

    private void addStudent() {
        try {
            String name = nameField.getText().trim();
            int roll = Integer.parseInt(rollField.getText().trim());
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a valid name!");
                return;
            }

            for (Student s : students) {
                if (s.getRollNumber() == roll) {
                    JOptionPane.showMessageDialog(this, "Student with this roll number already exists!");
                    return;
                }
            }

            students.add(new Student(roll, name, 0, 0));
            saveToFile();
            JOptionPane.showMessageDialog(this, "Student added successfully!");
            rollField.setText("");
            nameField.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid roll number!");
        }
    }

    private void markAttendance() {
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students available!");
            return;
        }

        boolean someoneMarked = false;
        for (Student s : students) {
            if (s.isMarkedToday()) continue;
            int option = JOptionPane.showConfirmDialog(this,
                    "Is " + s.getName() + " present?",
                    "Mark Attendance",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION)
                s.markPresent();
            else
                s.markAbsent();
            someoneMarked = true;
        }

        if (!someoneMarked)
            JOptionPane.showMessageDialog(this, "All students are already marked today!");
        else
            JOptionPane.showMessageDialog(this, "Attendance marked successfully!");

        saveToFile();
        saveDailyAttendance();
    }

    private void saveToFile() {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            for (Student s : students) {
                writer.write(s.toString() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                students.add(Student.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No existing file found. Starting fresh.");
        }
    }

    private void resetDailyStatus() {
        for (Student s : students) {
            s.resetForNewDay();
        }
    }

    private void saveDailyAttendance() {
        String fileName = "attendance_" + currentDate + ".txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            int totalPresent = 0;
            int totalAbsent = 0;

            writer.write("Date: " + currentDate + "\n");
            writer.write("------------------------------------------------------\n");
            writer.write(String.format("%-10s %-25s %-10s%n", "Roll No", "Name", "Status"));
            writer.write("------------------------------------------------------\n");

            for (Student s : students) {
                String status;
                if (s.isMarkedToday()) {
                    status = s.isPresentToday() ? "Present" : "Absent";
                } else {
                    status = "Absent";
                }

                if (status.equals("Present"))
                    totalPresent++;
                else
                    totalAbsent++;

                writer.write(String.format("%-10d %-25s %-10s%n",
                        s.getRollNumber(),
                        s.getName(),
                        status));
            }

            writer.write("------------------------------------------------------\n");
            writer.write(String.format("Total Students: %d%n", students.size()));
            writer.write(String.format("Total Present:  %d%n", totalPresent));
            writer.write(String.format("Total Absent:   %d%n", totalAbsent));
            writer.write("------------------------------------------------------\n");
            writer.write("File generated successfully.\n");

            JOptionPane.showMessageDialog(this,
                    "Attendance saved successfully as " + fileName,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            try {
                Desktop.getDesktop().open(new File(fileName));
            } catch (Exception ex) {
                System.out.println("Could not open file automatically: " + ex.getMessage());
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving daily file: " + e.getMessage());
        }
    }

    private void clearAllRecords() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all student records?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            students.clear();
            try (FileWriter writer = new FileWriter(FILE_NAME)) {
                writer.write("");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error clearing file: " + e.getMessage());
                return;
            }
            JOptionPane.showMessageDialog(this, "All records cleared successfully!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AttendanceTrackerGUI().setVisible(true));
    }
}
