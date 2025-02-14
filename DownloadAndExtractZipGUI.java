import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.*;
import javax.swing.*;

public class DownloadAndExtractZipGUI extends JFrame {

    private JTextField urlField;
    private JTextField directoryField;
    private JButton chooseDirButton;
    private JButton downloadButton;

    public DownloadAndExtractZipGUI() {
        setTitle("Download and Extract Zip");
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // URL input
        JLabel urlLabel = new JLabel("URL:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(urlLabel, gbc);

        urlField = new JTextField(30);
        gbc.gridx = 1;
        add(urlField, gbc);

        // Directory chooser
        JLabel directoryLabel = new JLabel("Directory:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(directoryLabel, gbc);

        directoryField = new JTextField(30);
        directoryField.setEditable(false);
        gbc.gridx = 1;
        add(directoryField, gbc);

        chooseDirButton = new JButton("Choose...");
        chooseDirButton.addActionListener(new ChooseDirAction());
        gbc.gridx = 2;
        add(chooseDirButton, gbc);

        // Download button
        downloadButton = new JButton("Download and Extract");
        downloadButton.addActionListener(new DownloadAndExtractAction());
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(downloadButton, gbc);

        pack();
        setLocationRelativeTo(null); // Center the frame
    }

    private class ChooseDirAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(DownloadAndExtractZipGUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File chosenDirectory = chooser.getSelectedFile();
                directoryField.setText(chosenDirectory.getAbsolutePath());
            }
        }
    }

    private class DownloadAndExtractAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String downloadUrl = urlField.getText();
            File chosenDirectory = new File(directoryField.getText());

            if (downloadUrl.isEmpty() || chosenDirectory.getAbsolutePath().isEmpty()) {
                JOptionPane.showMessageDialog(DownloadAndExtractZipGUI.this, 
                        "Please provide both URL and directory.", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Download the zip file
                downloadZip(downloadUrl, chosenDirectory);
                
                // Extract the zip file
                extractZip(chosenDirectory);

                JOptionPane.showMessageDialog(DownloadAndExtractZipGUI.this, 
                        "Download and extraction completed successfully.", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(DownloadAndExtractZipGUI.this, 
                        "An error occurred: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void downloadZip(String urlStr, File directory) throws IOException {
        URL url = new URL(urlStr);
        String fileName = "downloaded.zip"; // You can customize the file name
        File zipFile = new File(directory, fileName);

        try (InputStream in = url.openStream(); 
             OutputStream out = new FileOutputStream(zipFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("Download completed: " + zipFile.getAbsolutePath());
        }
    }

    private static void extractZip(File directory) throws IOException {
        File zipFile = new File(directory, "downloaded.zip");
        Path zipPath = zipFile.toPath();
        Path extractDir = directory.toPath();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = extractDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (OutputStream out = Files.newOutputStream(entryPath)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zis.closeEntry();
            }
            System.out.println("Extraction completed: " + extractDir);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DownloadAndExtractZipGUI().setVisible(true);
        });
    }
}
