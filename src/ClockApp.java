import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClockApp extends JFrame {
    private Clock clock;
    private List<Alarm> alarms;
    private JLabel timeLabel;
    private AlarmManagementWindow alarmWindow;
    private final Color NEON_PURPLE = new Color(187, 134, 252);
    private final Color NEON_BLUE = new Color(3, 218, 247);
    private final Color DARK_BG = new Color(18, 18, 18);
    private final Font DIGITAL_FONT = new Font("DS-Digital", Font.BOLD, 72);
    private final Font DEFAULT_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private JPanel timePanel; // Add this field

    public ClockApp() {
        clock = new Clock();
        alarms = new ArrayList<>();

        setupMainWindow();
        setupTimeDisplay();
        setupButtonPanel();
        startClockTimer();
    }

    private void setupMainWindow() {
        setTitle("Digital Clock");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(DARK_BG);
        getRootPane().setBorder(new EmptyBorder(20, 20, 20, 20));
        setLocationRelativeTo(null);
    }

    private void setupTimeDisplay() {
        timePanel = new JPanel(new BorderLayout());
        timePanel.setBackground(DARK_BG);

        timeLabel = new JLabel(clock.getCurrentTime(), SwingConstants.CENTER);
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts/DS-Digital.ttf")));
            timeLabel.setFont(DIGITAL_FONT);
        } catch (Exception e) {
            timeLabel.setFont(new Font("Monospaced", Font.BOLD, 72));
        }

        timeLabel.setForeground(NEON_PURPLE);
        timeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 2),
                new EmptyBorder(20, 40, 20, 40)
        ));

        // Add glow effect
        timePanel.add(createGlowEffect(), BorderLayout.NORTH);
        timePanel.add(timeLabel, BorderLayout.CENTER);
        add(timePanel, BorderLayout.CENTER);
    }

    private JPanel createGlowEffect() {
        JPanel glowPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Create gradient for glow effect
                GradientPaint glow = new GradientPaint(
                        width/2, 0, new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 50),
                        width/2, height, new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 0)
                );
                g2d.setPaint(glow);
                g2d.fillRect(0, 0, width, height);
            }
        };
        glowPanel.setPreferredSize(new Dimension(0, 20));
        glowPanel.setOpaque(false);
        return glowPanel;
    }

    private JButton createStyledButton(String text, Color mainColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, mainColor.darker(),
                        0, getHeight(), mainColor.darker().darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                // Glow effect
                g2d.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 20, 20);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(DEFAULT_FONT);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
            }
        };

        button.setPreferredSize(new Dimension(120, 40));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void setupButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(DARK_BG);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JButton settingsButton = createStyledButton("Settings", NEON_BLUE);
        settingsButton.addActionListener(e -> openSettingsWindow());

        JButton alarmsButton = createStyledButton("Alarms", NEON_PURPLE);
        alarmsButton.addActionListener(e -> openAlarmManagementWindow());

        buttonPanel.add(settingsButton);
        buttonPanel.add(alarmsButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void startClockTimer() {
        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();
    }

    private void updateTime() {
        timeLabel.setText(clock.getCurrentTime());
        checkAlarms();
    }

    // Rest of the methods remain the same
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

    public Color getClockTextColor(){
        return timeLabel.getForeground();
    }

    public void setBackgroundColor(Color color) {
        // Update all panel backgrounds
        getContentPane().setBackground(color);
        timePanel.setBackground(color);

        // Update all child components that need the background color
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                updatePanelBackground((JPanel)comp, color);
            }
        }

        // Repaint the entire frame
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    public Color getBackgroundColor(){
        return timePanel.getBackground();
    }

    private void updatePanelBackground(JPanel panel, Color color) {
        panel.setBackground(color);
        // Recursively update nested panels
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                updatePanelBackground((JPanel)comp, color);
            }
        }
    }

    public void setFrameWidth(int width) {
        setSize(width, getHeight());
    }

    public int getFrameWidth(){
        return this.getWidth();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new ClockApp().setVisible(true));
    }
}

// Create new AlarmManagementWindow class
class AlarmManagementWindow extends JFrame {
    private ClockApp mainApp;
    private JPanel alarmsPanel;
    private final Color NEON_PURPLE = new Color(187, 134, 252);
    private final Color NEON_BLUE = new Color(3, 218, 247);
    private final Color DARK_BG = new Color(18, 18, 18);
    private final Font WINDOW_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public AlarmManagementWindow(ClockApp app) {
        this.mainApp = app;

        setTitle("Alarm Management");
        setSize(400, 500);
        setLocationRelativeTo(app);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(DARK_BG);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 15));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Create header
        JLabel headerLabel = new JLabel("Your Alarms", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Create alarms panel with custom styling
        alarmsPanel = new JPanel();
        alarmsPanel.setLayout(new BoxLayout(alarmsPanel, BoxLayout.Y_AXIS));
        alarmsPanel.setBackground(DARK_BG);

        // Style the scroll pane
        JScrollPane scrollPane = new JScrollPane(alarmsPanel);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.setBackground(DARK_BG);
        scrollPane.getViewport().setBackground(DARK_BG);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(DARK_BG);

        JButton addAlarmButton = createStyledButton("Add New Alarm");
        addAlarmButton.addActionListener(e -> openAddAlarmWindow());
        buttonPanel.add(addAlarmButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        updateAlarmsList();
    }

    public void updateAlarmsList() {
        alarmsPanel.removeAll();
        for (Alarm alarm : mainApp.getAlarms()) {
            JPanel alarmPanel = createStyledAlarmPanel(alarm);
            alarmsPanel.add(alarmPanel);
            alarmsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between alarms
        }
        alarmsPanel.revalidate();
        alarmsPanel.repaint();
    }

    private JPanel createStyledAlarmPanel(Alarm alarm) {
        JPanel alarmPanel = new JPanel();
        alarmPanel.setLayout(new BorderLayout(10, 0));
        alarmPanel.setBackground(DARK_BG.brighter());
        alarmPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_PURPLE, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        alarmPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Time and tune panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setBackground(DARK_BG.brighter());

        JLabel timeLabel = new JLabel("⏰ " + alarm.getAlarmTime());
        timeLabel.setFont(WINDOW_FONT);
        timeLabel.setForeground(Color.WHITE);

        JLabel tuneLabel = new JLabel("🎵 " + new File(alarm.getAlarmTune()).getName());
        tuneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tuneLabel.setForeground(Color.LIGHT_GRAY);

        infoPanel.add(timeLabel);
        infoPanel.add(tuneLabel);
        alarmPanel.add(infoPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.setBackground(DARK_BG.brighter());

        JButton editButton = createStyledButton("Edit");
        editButton.addActionListener(e -> openEditAlarmWindow(alarm));

        JButton deleteButton = createStyledButton("Delete");
        deleteButton.setBackground(new Color(255, 69, 58).darker());
        deleteButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this alarm?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                mainApp.removeAlarm(alarm);
                updateAlarmsList();
            }
        });

        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        alarmPanel.add(buttonsPanel, BorderLayout.EAST);

        return alarmPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);

        button.setForeground(Color.BLACK);
        button.setBackground(NEON_BLUE.darker());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add these to ensure button styling is consistent
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // Ensure text stays black in all states
        button.addChangeListener(e -> {
            button.setForeground(Color.BLACK);
        });

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE);
                button.setForeground(Color.BLACK);  // Keep text black on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE.darker());
                button.setForeground(Color.BLACK);  // Keep text black when not hovering
            }
        });

        return button;
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
    private Clip clip;
    Timer snoozeTimer;
    private boolean isSnoozing = false;

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
        if (snoozedCount < noOfSnoozes) {
            snoozedCount++;
            String[] timeParts = alarmTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]) + snoozeTime;

            // Handle minute overflow
            if (minute >= 60) {
                hour += minute / 60;
                minute = minute % 60;
            }

            // Handle hour overflow (keep in 24-hour format)
            hour = hour % 24;

            // Set the new alarm time after snoozing
            alarmTime = String.format("%02d:%02d:00", hour, minute);
            isSnoozing = true;
            System.out.println("Alarm snoozed for " + snoozeTime + " minutes. New time: " + alarmTime);
        } else {
            System.out.println("No more snoozes allowed.");
        }
    }

    // Add these methods to Alarm class
    public void cancelSnooze() {
        if (snoozeTimer != null) {
            snoozeTimer.stop();
            snoozeTimer = null;
        }
        isSnoozing = false;
    }

    public boolean isSnoozing() {
        return isSnoozing;
    }

    public void playAlarmTune() {
        try {
            File audioFile = new File(alarmTune);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Make it loop continuously
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add this method to stop the alarm
    public void stopAlarmTune() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

}


class ClockSettings extends JFrame {
    private Color textColor;
    private Color backgroundColor;
    private int frameWidth;
    private final ClockApp app;
    private final Clock clock;
    private final Color NEON_PURPLE = new Color(187, 134, 252);
    private final Color NEON_BLUE = new Color(3, 218, 247);
    private final Color DARK_BG = new Color(18, 18, 18);
    private final Font SETTINGS_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private JPanel previewPanel;
    private JLabel previewLabel;

    // Store initial settings for comparison
    private Color initialTextColor;
    private Color initialBackgroundColor;
    private int initialFrameWidth;

    public ClockSettings(Clock clock, ClockApp app) {
        this.clock = clock;
        this.app = app;
        this.textColor = app.getClockTextColor(); // Default text color
        this.backgroundColor = app.getBackgroundColor() ; // Default background color
        this.frameWidth = app.getFrameWidth();

        // Save initial settings
        initialTextColor = textColor;
        initialBackgroundColor = backgroundColor;
        initialFrameWidth = frameWidth;

        setupSettingsWindow();
    }

    private void setupSettingsWindow() {
        setTitle("Clock Settings");
        setSize(400, 500);
        setLocationRelativeTo(app);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(DARK_BG);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Main settings panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Preview Panel
        setupPreviewPanel();

        // Settings Options
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(4, 1, 10, 15));
        settingsPanel.setBackground(DARK_BG);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Time Format Setting
        JPanel formatPanel = createSettingPanel("Time Format:");
        String[] formats = {"12-Hour", "24-Hour"};
        JComboBox<String> formatComboBox = createStyledComboBox(formats);
        formatComboBox.addActionListener(e -> {
            clock.setDisplayFormat((String) formatComboBox.getSelectedItem());
            updatePreview();
        });
        formatPanel.add(formatComboBox);
        settingsPanel.add(formatPanel);

        // Text Color Setting
        JPanel textColorPanel = createSettingPanel("Text Color:");
        JButton textColorButton = createStyledButton("Choose Color");
        textColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Text Color", textColor);
            if (newColor != null) {
                textColor = newColor;
                previewLabel.setForeground(textColor);
                updatePreview();
            }
        });
        textColorPanel.add(textColorButton);
        settingsPanel.add(textColorPanel);

        // Background Color Setting
        JPanel bgColorPanel = createSettingPanel("Background Color:");
        JButton bgColorButton = createStyledButton("Choose Color");
        bgColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Background Color", backgroundColor);
            if (newColor != null) {
                backgroundColor = newColor;
                previewPanel.setBackground(backgroundColor);
                updatePreview();
            }
        });
        bgColorPanel.add(bgColorButton);
        settingsPanel.add(bgColorPanel);

        // Frame Width Setting
        JPanel widthPanel = createSettingPanel("Frame Width:");
        JSlider widthSlider = new JSlider(JSlider.HORIZONTAL, 400, 800, frameWidth);
        widthSlider.setBackground(DARK_BG);
        widthSlider.setForeground(Color.WHITE);
        widthSlider.setMajorTickSpacing(100);
        widthSlider.setMinorTickSpacing(50);
        widthSlider.setPaintTicks(true);
        widthSlider.setPaintLabels(true);
        widthSlider.addChangeListener(e -> {
            frameWidth = widthSlider.getValue();
            updatePreview();
        });
        widthPanel.add(widthSlider);
        settingsPanel.add(widthPanel);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(DARK_BG);

        JButton saveButton = createStyledButton("Save Changes");
        saveButton.addActionListener(e -> {
            applySettings();
            dispose();
        });

        JButton cancelButton = createStyledButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(previewPanel);
        mainPanel.add(settingsPanel);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupPreviewPanel() {
        previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(backgroundColor);
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        previewPanel.setPreferredSize(new Dimension(0, 100));

        previewLabel = new JLabel(clock.getCurrentTime(), SwingConstants.CENTER);
        previewLabel.setFont(new Font("DS-Digital", Font.BOLD, 48));
        previewLabel.setForeground(textColor);

        previewPanel.add(previewLabel, BorderLayout.CENTER);

        // Start timer to update preview
        Timer previewTimer = new Timer(1000, e -> updatePreview());
        previewTimer.start();
    }

    private JPanel createSettingPanel(String labelText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(DARK_BG);

        JLabel label = new JLabel(labelText);
        label.setFont(SETTINGS_FONT);
        label.setForeground(Color.WHITE);
        panel.add(label);

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(SETTINGS_FONT);
        button.setForeground(Color.BLACK); // Change text color to black
        button.setBackground(NEON_BLUE.darker());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE.darker());
            }
        });

        return button;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(SETTINGS_FONT);
        comboBox.setForeground(Color.BLACK);  // Change text color to black
        comboBox.setBackground(NEON_BLUE);   // Change background color for contrast
        comboBox.setBorder(BorderFactory.createLineBorder(NEON_BLUE, 1));

        // Style the dropdown
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? NEON_BLUE.darker() : DARK_BG);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });

        return comboBox;
    }

    private void updatePreview() {
        previewLabel.setText(clock.getCurrentTime());
    }

    private void applySettings() {
        // Apply settings only if they have changed
        if (!textColor.equals(initialTextColor)) {
            app.setClockTextColor(textColor);
        }

        if (!backgroundColor.equals(initialBackgroundColor)) {
            app.setBackgroundColor(backgroundColor);
        }

        if (frameWidth != initialFrameWidth) {
            app.setFrameWidth(frameWidth);
        }

        // Update the app window
        app.revalidate();
        app.repaint();
    }
}


class EditAlarmWindow extends JFrame {
    private final Alarm alarm;
    private final ClockApp app;
    private final AlarmManagementWindow parentWindow;
    private final Color NEON_PURPLE = new Color(187, 134, 252);
    private final Color NEON_BLUE = new Color(3, 218, 247);
    private final Color DARK_BG = new Color(18, 18, 18);
    private final Font SETTINGS_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public EditAlarmWindow(Alarm alarm, ClockApp app, AlarmManagementWindow parentWindow) {
        this.alarm = alarm;
        this.app = app;
        this.parentWindow = parentWindow;

        setupWindow();
    }

    private void setupWindow() {
        setTitle("Edit Alarm");
        setSize(600, 450);
        setLocationRelativeTo(app);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(DARK_BG);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Settings Panel
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(6, 1, 10, 15));
        settingsPanel.setBackground(DARK_BG);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Time Settings
        JPanel timePanel = createSettingPanel("Time Settings");
        JPanel hourMinutePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hourMinutePanel.setBackground(DARK_BG);

        // Hour Selector
        Integer[] hours = new Integer[24];
        for (int i = 0; i < 24; i++) hours[i] = i;
        JComboBox<Integer> hourComboBox = createStyledComboBox(hours);
        hourComboBox.setSelectedItem(Integer.parseInt(alarm.getAlarmTime().substring(0, 2)));
        JLabel hourLabel = createStyledLabel("Hour:");
        hourMinutePanel.add(hourLabel);
        hourMinutePanel.add(hourComboBox);

        // Minute Selector
        Integer[] minutes = new Integer[60];
        for (int i = 0; i < 60; i++) minutes[i] = i;
        JComboBox<Integer> minuteComboBox = createStyledComboBox(minutes);
        minuteComboBox.setSelectedItem(Integer.parseInt(alarm.getAlarmTime().substring(3, 5)));
        JLabel minuteLabel = createStyledLabel("Minute:");
        hourMinutePanel.add(Box.createHorizontalStrut(20));
        hourMinutePanel.add(minuteLabel);
        hourMinutePanel.add(minuteComboBox);

        timePanel.add(hourMinutePanel);
        settingsPanel.add(timePanel);

        // Alarm Tune Setting
        JPanel tunePanel = createSettingPanel("Alarm Tune");
        JTextField tuneField = createStyledTextField(alarm.getAlarmTune());
        JButton tuneButton = createStyledButton("Choose Tune");
        tuneButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                tuneField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        tunePanel.add(tuneField);
        tunePanel.add(tuneButton);
        settingsPanel.add(tunePanel);

        // Snooze Time Setting
        JPanel snoozePanel = createSettingPanel("Snooze Time (minutes)");
        JTextField snoozeField = createStyledTextField(String.valueOf(alarm.getSnoozeTime()));
        snoozePanel.add(snoozeField);
        settingsPanel.add(snoozePanel);

        // Number of Snoozes Setting
        JPanel snoozesPanel = createSettingPanel("Number of Snoozes");
        JTextField noOfSnoozesField = createStyledTextField(String.valueOf(alarm.getNoOfSnoozes()));
        snoozesPanel.add(noOfSnoozesField);
        settingsPanel.add(snoozesPanel);

        mainPanel.add(settingsPanel);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(DARK_BG);

        JButton saveButton = createStyledButton("Save Changes");
        saveButton.addActionListener(e -> {
            String time = String.format("%02d:%02d:00",
                    hourComboBox.getSelectedItem(),
                    minuteComboBox.getSelectedItem());
            alarm.setAlarmTime(time);
            alarm.setAlarmTune(tuneField.getText());
            alarm.setSnoozeTime(Integer.parseInt(snoozeField.getText()));
            alarm.setNoOfSnoozes(Integer.parseInt(noOfSnoozesField.getText()));
            parentWindow.updateAlarmsList();
            dispose();
        });

        JButton deleteButton = createStyledButton("Delete Alarm");
        deleteButton.setBackground(new Color(255, 69, 58));
        deleteButton.addActionListener(e -> {
            app.removeAlarm(alarm);
            dispose();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSettingPanel(String labelText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(DARK_BG);

        JLabel label = createStyledLabel(labelText);
        panel.add(label);

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SETTINGS_FONT);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JTextField createStyledTextField(String text) {
        JTextField textField = new JTextField(text, 20);
        textField.setFont(SETTINGS_FONT);
        textField.setForeground(Color.BLACK);
        textField.setBackground(Color.WHITE.brighter());
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(SETTINGS_FONT);
        button.setForeground(Color.BLACK);
        button.setBackground(NEON_BLUE.darker());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE.darker());
            }
        });

        return button;
    }

    private JComboBox<Integer> createStyledComboBox(Integer[] items) {
        JComboBox<Integer> comboBox = new JComboBox<>(items);
        comboBox.setFont(SETTINGS_FONT);
        comboBox.setForeground(Color.BLACK);
        comboBox.setBackground(DARK_BG.brighter());
        comboBox.setBorder(BorderFactory.createLineBorder(NEON_BLUE, 1));

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? NEON_BLUE.darker() : DARK_BG);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });

        return comboBox;
    }
}


class AddAlarmWindow extends JFrame {
    private final ClockApp app;
    private final Color NEON_PURPLE = new Color(187, 134, 252);
    private final Color NEON_BLUE = new Color(3, 218, 247);
    private final Color DARK_BG = new Color(18, 18, 18);
    private final Font SETTINGS_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public AddAlarmWindow(ClockApp app) {
        this.app = app;
        setupWindow();
    }

    private void setupWindow() {
        setTitle("Add Alarm");
        setSize(600, 450);
        setLocationRelativeTo(app);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(DARK_BG);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Settings Panel
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(6, 1, 10, 15));
        settingsPanel.setBackground(DARK_BG);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Time Settings
        JPanel timePanel = createSettingPanel("Time Settings");
        JPanel hourMinutePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hourMinutePanel.setBackground(DARK_BG);

        // Hour Selector
        Integer[] hours = new Integer[24];
        for (int i = 0; i < 24; i++) hours[i] = i;
        JComboBox<Integer> hourComboBox = createStyledComboBox(hours);
        JLabel hourLabel = createStyledLabel("Hour:");
        hourMinutePanel.add(hourLabel);
        hourMinutePanel.add(hourComboBox);

        // Minute Selector
        Integer[] minutes = new Integer[60];
        for (int i = 0; i < 60; i++) minutes[i] = i;
        JComboBox<Integer> minuteComboBox = createStyledComboBox(minutes);
        JLabel minuteLabel = createStyledLabel("Minute:");
        hourMinutePanel.add(Box.createHorizontalStrut(20));
        hourMinutePanel.add(minuteLabel);
        hourMinutePanel.add(minuteComboBox);

        timePanel.add(hourMinutePanel);
        settingsPanel.add(timePanel);

        // Alarm Tune Setting
        JPanel tunePanel = createSettingPanel("Alarm Tune");
        JTextField tuneField = createStyledTextField("D:\\Java\\clock\\AlarmSound\\default_alarm.WAV");
        JButton tuneButton = createStyledButton("Choose Tune");
        tuneButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                tuneField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        tunePanel.add(tuneField);
        tunePanel.add(tuneButton);
        settingsPanel.add(tunePanel);

        // Snooze Time Setting
        JPanel snoozePanel = createSettingPanel("Snooze Time (minutes)");
        JTextField snoozeField = createStyledTextField("1");
        snoozePanel.add(snoozeField);
        settingsPanel.add(snoozePanel);

        // Number of Snoozes Setting
        JPanel snoozesPanel = createSettingPanel("Number of Snoozes");
        JTextField noOfSnoozesField = createStyledTextField("1");
        snoozesPanel.add(noOfSnoozesField);
        settingsPanel.add(snoozesPanel);

        mainPanel.add(settingsPanel);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(DARK_BG);

        JButton saveButton = createStyledButton("Add Alarm");
        saveButton.addActionListener(e -> {
            String time = String.format("%02d:%02d:00",
                    hourComboBox.getSelectedItem(),
                    minuteComboBox.getSelectedItem());
            Alarm newAlarm = new Alarm(
                    time,
                    tuneField.getText(),
                    Integer.parseInt(snoozeField.getText()),
                    Integer.parseInt(noOfSnoozesField.getText())
            );
            app.addAlarm(newAlarm);
            dispose();
        });

        JButton cancelButton = createStyledButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSettingPanel(String labelText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(DARK_BG);

        JLabel label = createStyledLabel(labelText);
        panel.add(label);

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SETTINGS_FONT);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JTextField createStyledTextField(String text) {
        JTextField textField = new JTextField(text, 20);
        textField.setFont(SETTINGS_FONT);
        textField.setForeground(Color.BLACK);
        textField.setBackground(Color.WHITE.brighter());
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(SETTINGS_FONT);
        button.setForeground(Color.BLACK);
        button.setBackground(NEON_BLUE.darker());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add these to ensure button styling is consistent
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // Ensure text stays black in all states
        button.addChangeListener(e -> {
            button.setForeground(Color.BLACK);
        });

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE);
                button.setForeground(Color.BLACK);  // Keep text black on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE.darker());
                button.setForeground(Color.BLACK);  // Keep text black when not hovering
            }
        });

        return button;
    }

    private JComboBox<Integer> createStyledComboBox(Integer[] items) {
        JComboBox<Integer> comboBox = new JComboBox<>(items);
        comboBox.setFont(SETTINGS_FONT);
        comboBox.setForeground(Color.BLACK);
        comboBox.setBackground(DARK_BG.brighter());
        comboBox.setBorder(BorderFactory.createLineBorder(NEON_BLUE, 1));

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? NEON_BLUE.darker() : DARK_BG);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });

        return comboBox;
    }
}

class AlarmRingWindow extends JFrame {
    private final ClockApp app;
    private final Alarm alarm;
    private final Color NEON_PURPLE = new Color(187, 134, 252);
    private final Color NEON_BLUE = new Color(3, 218, 247);
    private final Color DARK_BG = new Color(18, 18, 18);
    private final Font SETTINGS_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public AlarmRingWindow(Alarm alarm, ClockApp app) {
        this.app = app;
        this.alarm = alarm;

        if (alarm.isSnoozing()) {
            return;
        }

        alarm.playAlarmTune();
        setupWindow();
    }

    private void setupWindow() {
        setTitle("Alarm Ringing!");
        setSize(400, 300);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        setAlwaysOnTop(true);
        getContentPane().setBackground(DARK_BG);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Center panel for alarm info
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBackground(DARK_BG);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        // Time Label
        JLabel timeLabel = createStyledLabel("Time: " + alarm.getAlarmTime());
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(timeLabel);

        // Snooze Label
        JLabel snoozeLabel = createStyledLabel("Snoozes remaining: " +
                (alarm.getNoOfSnoozes() - alarm.snoozedCount));
        snoozeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(snoozeLabel);

        add(centerPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(DARK_BG);

        JButton stopButton = createStyledButton("Stop");
        stopButton.addActionListener(e -> {
            alarm.stopAlarmTune();
            app.removeAlarm(alarm);
            dispose();
        });


        JButton snoozeButton = createStyledButton("Snooze");
        snoozeButton.addActionListener(e -> {
            if (alarm.getNoOfSnoozes() > alarm.snoozedCount) {
                // Cancel any existing snooze timer
                alarm.cancelSnooze();

                // Start new snooze timer
                alarm.snooze();
                alarm.stopAlarmTune(); // Stop current alarm sound
                dispose();

                Timer snoozeTimer = new Timer(alarm.getSnoozeTime() * 60 * 1000, ev -> {
                    // Check if window is already open or alarm is already ringing
                    if (!alarm.isSnoozing()) {
                        return;
                    }
                    SwingUtilities.invokeLater(() -> {
                        new AlarmRingWindow(alarm, app).setVisible(true);
                    });
                    alarm.cancelSnooze(); // Reset snooze state after alarm rings
                });
                alarm.snoozeTimer = snoozeTimer; // Store reference to timer
                snoozeTimer.setRepeats(false);
                snoozeTimer.start();
            } else {
                showStyledErrorDialog("No more snoozes remaining!");
                app.removeAlarm(alarm);
                alarm.stopAlarmTune();
                dispose();
            }
        });


        buttonPanel.add(snoozeButton);
        buttonPanel.add(stopButton);

        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SETTINGS_FONT);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(SETTINGS_FONT);
        button.setForeground(Color.BLACK);
        button.setBackground(NEON_BLUE.darker());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_BLUE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add these to ensure button styling is consistent
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // Ensure text stays black in all states
        button.addChangeListener(e -> {
            button.setForeground(Color.BLACK);
        });

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE);
                button.setForeground(Color.BLACK);  // Keep text black on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(NEON_BLUE.darker());
                button.setForeground(Color.BLACK);  // Keep text black when not hovering
            }
        });

        return button;
    }

    private void showStyledErrorDialog(String message) {
        JDialog dialog = new JDialog(this, "Error", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(DARK_BG);

        JLabel messageLabel = createStyledLabel(message);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton okButton = createStyledButton("OK");
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(DARK_BG);
        buttonPanel.add(okButton);

        dialog.add(messageLabel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}