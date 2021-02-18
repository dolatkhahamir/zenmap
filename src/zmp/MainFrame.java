package zmp;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class MainFrame extends JFrame implements Runnable {

    private JTextField commandTextField;
    private ResultPane scanResultPane;
    private Thread scanThread;

    public MainFrame() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setLocationByPlatform(false);
        setTitle("Zenmap Like Application");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(150, 100, 750, 700);

        initComponents();
    }

    private void initComponents() {
        commandTextField = new JTextField();
        commandTextField.setOpaque(true);
        commandTextField.setBackground(Color.DARK_GRAY);
        commandTextField.setForeground(Color.WHITE);
        commandTextField.setFont(new Font("serif", Font.BOLD, 18));

        scanResultPane = new ResultPane();

        commandTextField.addActionListener(e -> doScan());

        var scanButton = new JButton("Scan");
        var stopButton = new JButton("Stop");

        scanButton.addActionListener(e -> doScan());
        stopButton.addActionListener(e -> stopScan());

        var target = new JTextField();
        target.setOpaque(true);
        target.setBackground(Color.DARK_GRAY);
        target.setForeground(Color.WHITE);
        var profile = new JComboBox<String>();
        profile.setForeground(Color.WHITE);
        profile.addItem("Intense scan");
        profile.setOpaque(true);
        profile.setBackground(Color.DARK_GRAY);
        profile.addItem("Intense scan plus UDP");
        profile.addItem("Intense scan, all TCP ports");
        profile.addItem("Intense scan, no ping");
        profile.addItem("Ping scan");
        profile.addItem("Quick scan");
        profile.addItem("Quick scan plus");
        profile.addItem("Quick traceroute");
        profile.addItem("Regular scan");
        profile.addItem("Slow comprehensive scan");
        profile.addActionListener(e -> {
            switch ((String) Objects.requireNonNull(profile.getSelectedItem())) {
                case "Intense scan": commandTextField.setText("nmap -T4 -A -v " + target.getText()); break;
                case "Intense scan plus UDP": commandTextField.setText("nmap -sS -sU -T4 -A -v " + target.getText()); break;
                case "Intense scan, all TCP ports": commandTextField.setText("nmap -p 1-65535 -T4 -A -v " + target.getText()); break;
                case "Intense scan, no ping": commandTextField.setText("nmap -T4 -A -v -Pn " + target.getText()); break;
                case "Ping scan": commandTextField.setText("nmap -sn " + target.getText()); break;
                case "Quick scan": commandTextField.setText("nmap -T4 -F " + target.getText()); break;
                case "Quick scan plus": commandTextField.setText("nmap -sV -T4 -O -F --version-light " + target.getText()); break;
                case "Quick traceroute": commandTextField.setText("nmap -sn --traceroute " + target.getText()); break;
                case "Regular scan": commandTextField.setText("nmap " + target.getText()); break;
                case "Slow comprehensive scan": commandTextField.setText("nmap -sS -sU -T4 -A -v -PE -PP -PS80,443 -PA3389 -PU40125 -PY -g 53 --script \"default or (discovery and safe)\" " + target.getText()); break;
            }
        });

        var northPanel1 = new JPanel(new GridLayout(1, 2));
        var northPanel2 = new JPanel(new GridLayout(1, 2));
        var northPanel3 = new JPanel(new BorderLayout());
        northPanel1.add(target);
        northPanel1.add(profile);
        northPanel3.add(northPanel1, BorderLayout.CENTER);
        northPanel2.add(scanButton);
        northPanel2.add(stopButton);
        northPanel3.add(northPanel2, BorderLayout.EAST);

        var northPanel = new JPanel(new GridLayout(2, 1));
        northPanel.add(northPanel3);
        northPanel.add(commandTextField);

        add(northPanel, BorderLayout.NORTH);

        var tabbedPane = new JTabbedPane();
//        tabbedPane.add("Nmap Output", new JScrollPane(scanResultPane));
//        tabbedPane.add("Ports / Hosts", );
        add(new JScrollPane(scanResultPane), BorderLayout.CENTER);
    }

    private void doScan() {
        stopScan();
        scanThread = new Thread(() -> {
            try {
                var proc = Runtime.getRuntime().exec(".\\lib\\nmap-7.91\\nmap.exe " + commandTextField.getText());
                var stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                var stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                scanResultPane.append("out>\n");
                String s;
                while ((s = stdInput.readLine()) != null)
                    scanResultPane.append(s).append("\n");
                scanResultPane.append("err>\n");
                while ((s = stdError.readLine()) != null)
                    scanResultPane.append(s).append("\n");
                stdError.close();
                stdInput.close();
            } catch (IOException e) {
                System.err.println("Error in scanning thread: " + e.getMessage());
            }
        });
        scanThread.start();
    }

    private void stopScan() {
        if (scanThread == null)
            return;
        try {
            scanThread.stop();
            scanThread.interrupt();
            scanThread.join();
        } catch (Exception ignore) {
        } finally {
            scanThread = null;
        }
        scanResultPane.setText("");
    }

    @Override
    public void run() {
        setVisible(true);
    }

    private static final class ResultPane extends JTextPane {
        public ResultPane() {
            setBackground(Color.DARK_GRAY.darker());
            setOpaque(true);
            setForeground(Color.WHITE);
            setEditable(false);
        }

        public ResultPane append(Object obj) {
            setText(getText() + obj);
            this.repaint();
            return this;
        }
    }
}
