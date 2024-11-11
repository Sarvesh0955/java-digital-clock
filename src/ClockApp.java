import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Modify the ClockApp class to be simpler
public class ClockApp extends JFrame {
    private Clock clock;
    private List<Alarm> alarms;
    private JLabel timeLabel;
    private AlarmManagementWindow alarmWindow;

    public ClockApp() {
        clock = new Clock();
        alarms = new ArrayList<>();

        setTitle("Digital Clock");
        setSize(400, 200); // Reduced height since we have fewer components
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Display current time
        timeLabel = new JLabel(clock.getCurrentTime(), SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(timeLabel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Settings button
        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(e -> openSettingsWindow());
        buttonPanel.add(settingsButton);

        // Alarms button
        JButton alarmsButton = new JButton("Alarms");
        alarmsButton.addActionListener(e -> openAlarmManagementWindow());
        buttonPanel.add(alarmsButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Timer to update the clock display every second
        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();
    }

    private void updateTime() {
        timeLabel.setText(clock.getCurrentTime());
        checkAlarms();
    }

    private void checkAlarms() {
        for (Alarm alarm : alarms) {
            if (clock.checkAlarmHelper().equals(alarm.getAlarmTime())) {
                new AlarmRingWindow(alarm, this);
            }
        }
    }

    private void openSettingsWindow() {
        new ClockSettings(clock, this).setVisible(true);
    }

    private void openAlarmManagementWindow() {
        if (alarmWindow == null || !alarmWindow.isVisible()) {
            alarmWindow = new AlarmManagementWindow(this);
        }
        alarmWindow.setVisible(true);
    }

    public void addAlarm(Alarm alarm) {
        alarms.add(alarm);
        if (alarmWindow != null) {
            alarmWindow.updateAlarmsList();
        }
    }

    public void removeAlarm(Alarm alarm) {
        alarms.remove(alarm);
        if (alarmWindow != null) {
            alarmWindow.updateAlarmsList();
        }
    }

    public List<Alarm> getAlarms() {
        return alarms;
    }

    public void setClockTextColor(Color color) {
        timeLabel.setForeground(color);
    }

    public void setBackgroundColor(Color color) {
        getContentPane().setBackground(color);
    }

    public void setFrameWidth(int width) {
        setSize(width, getHeight());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClockApp().setVisible(true));
    }
}

// Create new AlarmManagementWindow class
class AlarmManagementWindow extends JFrame {
    private ClockApp mainApp;
    private JPanel alarmsPanel;

    public AlarmManagementWindow(ClockApp app) {
        this.mainApp = app;

        setTitle("Alarm Management");
        setSize(400, 400);
        setLayout(new BorderLayout());

        // Create alarms panel
        alarmsPanel = new JPanel();
        alarmsPanel.setLayout(new BoxLayout(alarmsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(alarmsPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Add Alarm button
        JButton addAlarmButton = new JButton("Add Alarm");
        addAlarmButton.addActionListener(e -> openAddAlarmWindow());
        add(addAlarmButton, BorderLayout.SOUTH);

        updateAlarmsList();
    }

    public void updateAlarmsList() {
        alarmsPanel.removeAll();
        for (Alarm alarm : mainApp.getAlarms()) {
            JPanel alarmPanel = new JPanel();
            alarmPanel.setLayout(new FlowLayout());

            alarmPanel.add(new JLabel("Alarm at " + alarm.getAlarmTime()));

            JButton editButton = new JButton("Edit");
            editButton.addActionListener(e -> openEditAlarmWindow(alarm));
            alarmPanel.add(editButton);

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> {
                mainApp.removeAlarm(alarm);
                updateAlarmsList();
            });
            alarmPanel.add(deleteButton);

            alarmsPanel.add(alarmPanel);
        }
        alarmsPanel.revalidate();
        alarmsPanel.repaint();
    }

    private void openAddAlarmWindow() {
        new AddAlarmWindow(mainApp).setVisible(true);
    }

    private void openEditAlarmWindow(Alarm alarm) {
        new EditAlarmWindow(alarm, mainApp, this).setVisible(true);
    }
}

class Clock {
    private String displayFormat = "HH:mm:ss"; // Default to 24-hour format

    public String getCurrentTime() {
        return new SimpleDateFormat(displayFormat).format(new Date());
    }

    public String checkAlarmHelper() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public void setDisplayFormat(String format) {
        if (format.equals("12-Hour")) {
            displayFormat = "hh:mm:ss a";
        } else {
            displayFormat = "HH:mm:ss";
        }
    }

}

class Alarm {
    private String alarmTime;
    private String alarmTune;
    private int snoozeTime;
    private int noOfSnoozes;
    protected int snoozedCount;

    public Alarm(String time, String tune, int snooze, int noOfSnoozes) {
        this.alarmTime = time;
        this.alarmTune = tune;
        this.snoozeTime = snooze;
        this.noOfSnoozes = noOfSnoozes;
        this.snoozedCount = 0;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public String getAlarmTune() {
        return alarmTune;
    }

    public int getSnoozeTime() {
        return snoozeTime;
    }

    public int getNoOfSnoozes() {
        return noOfSnoozes;
    }

    public void setAlarmTime(String time) {
        this.alarmTime = time;
    }

    public void setAlarmTune(String tune) {
        this.alarmTune = tune;
    }

    public void setSnoozeTime(int snooze) {
        this.snoozeTime = snooze;
    }

    public void setNoOfSnoozes(int snoozes) {
        this.noOfSnoozes = snoozes;
    }

    public void snooze() {
        if (snoozedCount < noOfSnoozes) { // Check if snooze limit is not reached
            snoozedCount++; // Increment the snoozed count
            String[] timeParts = alarmTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]) + snoozeTime; // Add snooze time

            // Handle minute overflow
            if (minute >= 60) {
                hour += minute / 60;
                minute = minute % 60;
            }

            // Handle hour overflow (keep in 24-hour format)
            hour = hour % 24;

            // Set the new alarm time after snoozing
            alarmTime = String.format("%02d:%02d:00", hour, minute);
            System.out.println("Alarm snoozed for " + snoozeTime + " minutes. New time: " + alarmTime);
        } else {
            System.out.println("No more snoozes allowed.");
        }
    }

    public void playAlarmTune() {
        try {
            File audioFile = new File(alarmTune); // Use the alarmTune path
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


class ClockSettings extends JFrame {
    private Color textColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;
    private int frameWidth = 400;

    public ClockSettings(Clock clock, ClockApp app) {
        setTitle("Clock Settings");
        setSize(300, 300);
        setLayout(new GridLayout(5, 2));

        // Time format setting
        String[] formats = {"12-Hour", "24-Hour"};
        JComboBox<String> formatComboBox = new JComboBox<>(formats);
        formatComboBox.addActionListener(e -> {
            clock.setDisplayFormat((String) formatComboBox.getSelectedItem());
        });

        add(new JLabel("Time Format:"));
        add(formatComboBox);

        // Text color setting
        JButton textColorButton = new JButton("Choose Text Color");
        textColorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(this, "Choose Text Color", textColor);
            if (chosenColor != null) {
                textColor = chosenColor;
            }
        });

        add(new JLabel("Text Color:"));
        add(textColorButton);

        // Background color setting
        JButton bgColorButton = new JButton("Choose Background Color");
        bgColorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(this, "Choose Background Color", backgroundColor);
            if (chosenColor != null) {
                backgroundColor = chosenColor;
            }
        });

        add(new JLabel("Background Color:"));
        add(bgColorButton);

        // Frame width setting
        JSpinner frameWidthSpinner = new JSpinner(new SpinnerNumberModel(400, 300, 800, 50));
        frameWidthSpinner.addChangeListener(e -> frameWidth = (int) frameWidthSpinner.getValue());

        add(new JLabel("Frame Width:"));
        add(frameWidthSpinner);

        // Save button to apply settings
        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(e -> {
            app.setClockTextColor(textColor);
            app.setBackgroundColor(backgroundColor);
            app.setFrameWidth(frameWidth);
            dispose();
        });

        add(new JLabel());
        add(saveButton);
    }
}

class EditAlarmWindow extends JFrame {
    private final Alarm alarm;
    private final ClockApp app;
    private final AlarmManagementWindow parentWindow;

    public EditAlarmWindow(Alarm alarm, ClockApp app, AlarmManagementWindow parentWindow) {
        this.alarm = alarm;
        this.app = app;
        this.parentWindow = parentWindow;

        setTitle("Edit Alarm");
        setSize(400, 350);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Hour selection
        JLabel hourLabel = new JLabel("Hour:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(hourLabel, gbc);

        Integer[] hours = new Integer[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = i;
        }
        JComboBox<Integer> hourComboBox = new JComboBox<>(hours);
        hourComboBox.setSelectedItem(Integer.parseInt(alarm.getAlarmTime().substring(0, 2)));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(hourComboBox, gbc);

        // Minute selection
        JLabel minuteLabel = new JLabel("Minute:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(minuteLabel, gbc);

        Integer[] minutes = new Integer[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = i;
        }
        JComboBox<Integer> minuteComboBox = new JComboBox<>(minutes);
        minuteComboBox.setSelectedItem(Integer.parseInt(alarm.getAlarmTime().substring(3, 5)));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(minuteComboBox, gbc);

        // Alarm Tune
        JLabel tuneLabel = new JLabel("Alarm Tune:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(tuneLabel, gbc);

        JTextField tuneField = new JTextField(alarm.getAlarmTune(), 15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(tuneField, gbc);

        // Button to choose alarm tune
        JButton tuneButton = new JButton("Choose Tune");
        tuneButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                tuneField.setText(selectedFile.getAbsolutePath());
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        add(tuneButton, gbc);

        // Snooze Time
        JLabel snoozeLabel = new JLabel("Snooze Time (min):");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        add(snoozeLabel, gbc);

        JTextField snoozeField = new JTextField(String.valueOf(alarm.getSnoozeTime()), 5);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        add(snoozeField, gbc);

        // Number of Snoozes
        JLabel noOfSnoozesLabel = new JLabel("No. of Snoozes:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        add(noOfSnoozesLabel, gbc);

        JTextField noOfSnoozesField = new JTextField(String.valueOf(alarm.getNoOfSnoozes()), 5);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        add(noOfSnoozesField, gbc);

        // Save Button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String time = String.format("%02d:%02d:00", hourComboBox.getSelectedItem(), minuteComboBox.getSelectedItem());
            alarm.setAlarmTime(time);
            alarm.setAlarmTune(tuneField.getText());
            alarm.setSnoozeTime(Integer.parseInt(snoozeField.getText()));
            alarm.setNoOfSnoozes(Integer.parseInt(noOfSnoozesField.getText()));
            parentWindow.updateAlarmsList();
            dispose();
        });
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);

        // Delete Button
        JButton deleteButton = new JButton("Delete Alarm");
        deleteButton.addActionListener(e -> {
            app.removeAlarm(alarm);
            dispose();
        });
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(deleteButton, gbc);
    }
}

class AddAlarmWindow extends JFrame {
    public AddAlarmWindow(ClockApp app) {
        setTitle("Add Alarm");
        setSize(400, 350);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Hour selection
        JLabel hourLabel = new JLabel("Hour:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(hourLabel, gbc);

        Integer[] hours = new Integer[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = i;
        }
        JComboBox<Integer> hourComboBox = new JComboBox<>(hours);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(hourComboBox, gbc);

        // Minute selection
        JLabel minuteLabel = new JLabel("Minute:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(minuteLabel, gbc);

        Integer[] minutes = new Integer[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = i;
        }
        JComboBox<Integer> minuteComboBox = new JComboBox<>(minutes);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(minuteComboBox, gbc);

        // Alarm Tune
        JLabel tuneLabel = new JLabel("Alarm Tune:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(tuneLabel, gbc);

        JTextField tuneField = new JTextField("D:\\Java\\clock\\AlarmSound\\default_alarm.WAV", 15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(tuneField, gbc);

        JButton tuneButton = new JButton("Choose Tune");
        tuneButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                tuneField.setText(selectedFile.getAbsolutePath());
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        add(tuneButton, gbc);

        // Snooze Time
        JLabel snoozeLabel = new JLabel("Snooze Time (min):");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        add(snoozeLabel, gbc);

        JTextField snoozeField = new JTextField("1", 5);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        add(snoozeField, gbc);

        // Number of Snoozes
        JLabel noOfSnoozesLabel = new JLabel("No. of Snoozes:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        add(noOfSnoozesLabel, gbc);

        JTextField noOfSnoozesField = new JTextField("1", 5);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        add(noOfSnoozesField, gbc);

        // Add Alarm Button
        JButton saveButton = new JButton("Add Alarm");
        saveButton.addActionListener(e -> {
            int hour = (int) hourComboBox.getSelectedItem();
            int minute = (int) minuteComboBox.getSelectedItem();
            String time = String.format("%02d:%02d:00", hour, minute);
            String tune = tuneField.getText();
            int snoozeTime = Integer.parseInt(snoozeField.getText());
            int noOfSnoozes = Integer.parseInt(noOfSnoozesField.getText());

            Alarm newAlarm = new Alarm(time, tune, snoozeTime, noOfSnoozes);
            app.addAlarm(newAlarm);
            dispose();
        });
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);
    }
}

class AlarmRingWindow extends JFrame {
    private final ClockApp app;
    private final Alarm alarm;

    public AlarmRingWindow(Alarm alarm, ClockApp app) {
        this.app = app;
        this.alarm = alarm;

        alarm.playAlarmTune();

        setTitle("Alarm Ringing!");
        setSize(300, 150);
        setLayout(new BorderLayout(10, 10));

        // Center panel for alarm info
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel timeLabel = new JLabel("Time: " + alarm.getAlarmTime());
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(timeLabel);

        JLabel snoozeLabel = new JLabel("Snoozes remaining: " +
                (alarm.getNoOfSnoozes() - alarm.snoozedCount));
        snoozeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(snoozeLabel);

        add(centerPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> {
            app.removeAlarm(alarm);
            dispose();
        });
        buttonPanel.add(stopButton);

        JButton snoozeButton = new JButton("Snooze");
        snoozeButton.addActionListener(e -> {
            if (alarm.getNoOfSnoozes() > alarm.snoozedCount) {
                alarm.snooze();
                dispose();
                Timer snoozeTimer = new Timer(alarm.getSnoozeTime() * 60 * 1000, ev -> {
                    new AlarmRingWindow(alarm, app);
                });
                snoozeTimer.setRepeats(false);
                snoozeTimer.start();
            } else {
                JOptionPane.showMessageDialog(this, "No more snoozes remaining!");
                app.removeAlarm(alarm);
                dispose();
            }
        });
        buttonPanel.add(snoozeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Center the window on screen
        setLocationRelativeTo(null);
        setVisible(true);
        setAlwaysOnTop(true);
    }
}