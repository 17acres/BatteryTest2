package com.seventeen_acres_development;

import javafx.scene.input.KeyCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dan on 8/12/2016.
 */
public class BattTest {
    private JPanel panel;
    private static JFrame frame;
    private JTextField voltageTextField;
    private JTextField currentTextField;
    private JSpinner cutoffVoltageSpinner;
    private JTextField energyTextField;
    private JTextField powerTextField;
    private JButton startTestButton;
    private JPanel loadEnabled;
    private JCheckBox enableCutoffCheckBox;

    public static FileWriter writer;
    public static long startTime;

    private static BattTest instance;
    public static BattTest getInstance(){return instance;}

    public static void main(String[] args) {
        try {
            ArduinoInterface.getInstance().setComPort(Integer.parseInt(JOptionPane.showInputDialog(frame,"Enter COM Port",null)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String filename =
                    System.getProperty("user.home") +
                            "\\Desktop\\BattTest\\" +
                            JOptionPane.showInputDialog(frame,"Enter Battery Name","")+
                            " Batt Test"+
                            ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".csv";
            filename = filename.replace(":", "-");
            filename = filename.replaceFirst("-", ":");
            File file = new File(filename);
            file.createNewFile();
            writer = new FileWriter(file);
            writer.write("Elapsed Time (ms), Voltage (V), Current (A), Power (W), Battery Energy(W*h)\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame = new JFrame("BattTest");
        instance = new BattTest();

        frame.setContentPane(instance.panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE)
                    System.exit(1);
            }
        });
        instance.cutoffVoltageSpinner.setModel(new SpinnerNumberModel(8.5, 1, 16, 0.1));

        instance.startTestButton.addActionListener((actionEvent)->ArduinoInterface.getInstance().startTest());

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                ()->{
                    if(ArduinoInterface.getInstance().isEnabled)
                        Integrator.addPoint(((double)System.currentTimeMillis())/3600000,ArduinoInterface.getInstance().getPower());
                },10,10, TimeUnit.MILLISECONDS);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                if(ArduinoInterface.getInstance().isEnabled) {
                    writer.write(String.valueOf(((System.currentTimeMillis() - startTime) - (System.currentTimeMillis() - startTime) % 10) + ","));
                    writer.write(String.valueOf(ArduinoInterface.getInstance().getVoltage()) + ",");
                    writer.write(String.valueOf(ArduinoInterface.getInstance().getCurrent()) + ",");
                    writer.write(String.valueOf(ArduinoInterface.getInstance().getPower()) + ",");
                    writer.write(String.valueOf(Integrator.getArea()) + "\n");
                    writer.flush();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }, 100 - (System.currentTimeMillis() % 100), 100, TimeUnit.MILLISECONDS);
        ArduinoInterface.getInstance().startReading();
        new Timer(1000 / 60, (e) -> {
            if(instance.enableCutoffCheckBox.isSelected()) {
                if (ArduinoInterface.getInstance().getVoltage() > 0 && ArduinoInterface.getInstance().getVoltage() < (Double) instance.cutoffVoltageSpinner.getValue()) {
                    ArduinoInterface.getInstance().endTest();
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane optionPane = new JOptionPane("Test Complete. Energy used: " + Integrator.getArea() + " Watt-hours", JOptionPane.INFORMATION_MESSAGE);
                    JDialog dialog = optionPane.createDialog("Success");
                    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    dialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowDeactivated(WindowEvent e) {
                            try {
                                writer.close();
                                System.exit(0);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            System.exit(0);
                        }
                    });
                    dialog.setAlwaysOnTop(true);
                    dialog.setVisible(true);
                }
            }
            instance.voltageTextField.setText(String.format("%1$.3f", ArduinoInterface.getInstance().getVoltage()) + "V");
            instance.currentTextField.setText(String.format("%1$.3f", ArduinoInterface.getInstance().getCurrent()) + "A");
            instance.powerTextField.setText(String.format("%1$.3f", ArduinoInterface.getInstance().getPower()) + "W");
            instance.energyTextField.setText(String.format("%1$.3f", Integrator.getArea()) + "W*h");
            if (ArduinoInterface.getInstance().isLoadEnable())
                instance.loadEnabled.setBackground(Color.RED);
            else
                instance.loadEnabled.setBackground(Color.BLACK);
        }).start();
    }

    private void createUIComponents() {
    }
}
