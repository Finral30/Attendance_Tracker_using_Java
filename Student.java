public class Student {
    private int rollNumber;
    private String name;
    private int totalDays;
    private int presentDays;
    private boolean markedToday;    // whether attendance is recorded today
    private boolean presentToday;   // true = present, false = absent

    public Student(int rollNumber, String name, int totalDays, int presentDays) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.totalDays = totalDays;
        this.presentDays = presentDays;
        this.markedToday = false;
        this.presentToday = false;
    }

    public int getRollNumber() { return rollNumber; }
    public String getName() { return name; }
    public int getTotalDays() { return totalDays; }
    public int getPresentDays() { return presentDays; }

    public boolean isMarkedToday() { return markedToday; }
    public void setMarkedToday(boolean markedToday) { this.markedToday = markedToday; }

    public boolean isPresentToday() { return presentToday; }
    public void setPresentToday(boolean presentToday) { this.presentToday = presentToday; }

    // ✅ Mark present
    public void markPresent() {
        totalDays++;
        presentDays++;
        markedToday = true;
        presentToday = true;
    }

    // ✅ Mark absent
    public void markAbsent() {
        totalDays++;
        markedToday = true;
        presentToday = false;
    }

    public double getAttendancePercentage() {
        if (totalDays == 0) return 0;
        return (presentDays * 100.0) / totalDays;
    }

    // ✅ Include both flags when saving
    @Override
    public String toString() {
        return rollNumber + "," + name + "," + totalDays + "," + presentDays + "," + markedToday + "," + presentToday;
    }

    // ✅ Parse both old and new file formats
    public static Student fromString(String line) {
        String[] parts = line.split(",");
        int roll = Integer.parseInt(parts[0]);
        String name = parts[1];
        int total = Integer.parseInt(parts[2]);
        int present = Integer.parseInt(parts[3]);
        boolean marked = false;
        boolean presentToday = false;

        if (parts.length > 4)
            marked = Boolean.parseBoolean(parts[4]);
        if (parts.length > 5)
            presentToday = Boolean.parseBoolean(parts[5]);

        Student s = new Student(roll, name, total, present);
        s.setMarkedToday(marked);
        s.setPresentToday(presentToday);
        return s;
    }

    // ✅ Reset flags for new day
    public void resetForNewDay() {
        this.markedToday = false;
        this.presentToday = false;
    }
}
