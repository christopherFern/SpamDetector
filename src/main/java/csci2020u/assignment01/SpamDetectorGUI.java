package csci2020u.assignment01;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;

public class SpamDetectorGUI {
    public static void main(String[] args) {
        // Creating the frame
        JFrame frame = new JFrame("Spam Detector");
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Creating the table
        String[] columnNames = {"File", "Spam Detector’s Categorization", "Actual Category"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Creating accuracy & precision labels
        JLabel accuracyLabel = new JLabel("Accuracy: ");
        JLabel precisionLabel = new JLabel("Precision: ");

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new FlowLayout());
        statsPanel.add(accuracyLabel);
        statsPanel.add(precisionLabel);

        // File chooser
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JButton selectDirButton = new JButton("Select Directory");
        selectDirButton.addActionListener(e -> {
            int returnValue = directoryChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File mainDirectory = directoryChooser.getSelectedFile();
                runTrainingAndTesting(mainDirectory, tableModel, accuracyLabel, precisionLabel);
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(selectDirButton);

        // Adding components to the frame
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(statsPanel, BorderLayout.SOUTH);

        // Show the window
        frame.setVisible(true);
    }

    private static void runTrainingAndTesting(File mainDirectory, DefaultTableModel tableModel, JLabel accuracyLabel, JLabel precisionLabel) {
        SpamDetector spamDetector = new SpamDetector();

        ClassLoader classLoader = SpamDetectorGUI.class.getClassLoader();

        //getting the directories based on the one picked by the user
        URL testHamUrl = classLoader.getResource(mainDirectory.getName() + "/test/ham");
        URL testSpamUrl = classLoader.getResource(mainDirectory.getName() + "/test/spam");

        //checking if the directory has a test ham and a test spam directory if not it send an error
        if (testHamUrl == null || testSpamUrl == null) {
            JOptionPane.showMessageDialog(null, "Test folder structure not found! Please select the correct 'data' folder.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get the paths for test data
        File testHamFolder = new File(Objects.requireNonNull(testHamUrl).getFile());
        File testSpamFolder = new File(Objects.requireNonNull(testSpamUrl).getFile());

        // Test the model
        List<TestFile> results = spamDetector.test(testHamFolder, testSpamFolder, spamDetector.getWordProbabilities());

        // Populate JTable
        for (TestFile file : results) {
            tableModel.addRow(new Object[]{file.getFilename(), file.getSpamProbability() > 0.9999999999999999 ? "Spam" : "Ham", file.getActualClass()});
        }

        // Compute Accuracy & Precision
        double accuracy = spamDetector.calculateAccuracy(results);
        double precision = spamDetector.calculatePrecision(results);
        accuracyLabel.setText("Accuracy: " + String.format("%.5f", accuracy));
        precisionLabel.setText("Precision: " + String.format("%.5f", precision));
    }
}