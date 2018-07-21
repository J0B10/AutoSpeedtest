/**
 * MIT License
 * <p>
 * Copyright (c) 2018 Jonas Blocher
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.ungefroren.AutoSpeedtest.gui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import de.ungefroren.AutoSpeedtest.Main;

/**
 * Dialog in which the settings for the speedtest can be changed
 * <p>
 * Created on 16.07.2018.
 *
 * @author Jonas Blocher
 */
public class SetupFrame extends JDialog {

    private static final Font LABELS = new Font("Calibri", Font.BOLD, 14);
    private final JButton start;
    private final JTextArea serverIds;
    private final JTextField file_field;
    private final JTextField interval_field;
    private final JTextField timeout_field;
    private final JTextField delimiter_field;
    private final JTextField decimalSeparator_field;
    private File file = new File(Main.DEFAULT_FILE);
    private Set<Warning> currentWarnings = new HashSet<>();

    private Thread waitFor = null;


    public SetupFrame(Frame owner) {
        super(owner, "Setup", true);
        final Container contentPane = getContentPane();

        //serverIds
        String serverIds_tooltip = "<html>A list of ids of speedtest servers that the test should use.<br>" +
                "You can find a full list of all servers under:<br>" +
                "www.speedtestserver.com<br>" +
                "You can also leave it empty to use the best server available.</html>";
        JLabel serverIds_label = new JLabel("Server Ids:");
        serverIds_label.setFont(LABELS);
        serverIds_label.setToolTipText(serverIds_tooltip);
        contentPane.add(serverIds_label);
        serverIds = new JTextArea();
        serverIds.setToolTipText(serverIds_tooltip);
        JScrollPane serverIds_scroll = new JScrollPane(serverIds);
        serverIds_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        serverIds_scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        serverIds_scroll.setPreferredSize(new Dimension(150, 100));
        contentPane.add(serverIds_scroll);
        Warning serverIds_warning = new Warning("Server Ids must be a list of numeric ids!",
                                                (e) -> serverIds.getText().matches("(\\d+\\s*[\\s,;/]\\s*)*"));
        serverIds.getDocument().addDocumentListener(serverIds_warning);
        contentPane.add(serverIds_warning);

        //file
        String file_tooltip = "<html>A .csv file where the results of the tests should be logged.<br>" +
                "If the file already exists the results will be appended to the end of the file.</html>";
        JLabel file_label = new JLabel("Log file:");
        file_label.setFont(LABELS);
        file_label.setToolTipText(file_tooltip);
        contentPane.add(file_label);
        file_field = new JTextField(file.getName());
        file_field.setEditable(false);
        file_field.setToolTipText(file_tooltip);
        file_field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser(new File("."));
                chooser.setSelectedFile(new File(file_field.getText()));
                chooser.setMultiSelectionEnabled(false);
                chooser.setDialogTitle("Choose log file");
                chooser.setFileFilter(new FileNameExtensionFilter("csv files", "csv"));
                chooser.showSaveDialog(null);
                file = chooser.getSelectedFile();
                if (file != null) file_field.setText(file.getName());
            }
        });
        contentPane.add(file_field);

        //interval
        String interval_tooltip = "<html>Interval in seconds (s), minutes (m), hours (h) or days (d)<br>" +
                "at which the speedtest should run.<br>" +
                "<b>Example:</b> 250m</html>";
        JLabel interval_label = new JLabel("Interval:");
        interval_label.setFont(LABELS);
        interval_label.setToolTipText(interval_tooltip);
        contentPane.add(interval_label);
        interval_field = new JTextField(String.valueOf(Main.DEFAULT_INTERVAL));
        Warning interval_warning = new Warning("Interval must be a time interval!",
                                               (e) -> interval_field.getText().matches("\\d+[dhms]?"));
        interval_field.getDocument().addDocumentListener(interval_warning);
        interval_field.setToolTipText(interval_tooltip);
        contentPane.add(interval_warning);
        contentPane.add(interval_field);

        //timeout
        String timeout_tooltip = "<html>Http timeout of the test in seconds</html>";
        JLabel timeout_label = new JLabel("Timeout:");
        timeout_label.setFont(LABELS);
        timeout_label.setToolTipText(timeout_tooltip);
        contentPane.add(timeout_label);
        timeout_field = new JTextField(String.valueOf(Main.DEFAULT_TIMEOUT));
        Warning timeout_warning = new Warning("Timeout must be a positive Number!",
                                              (e) -> timeout_field.getText().matches("\\d+"));
        timeout_field.getDocument().addDocumentListener(timeout_warning);
        timeout_field.setToolTipText(timeout_tooltip);
        contentPane.add(timeout_warning);
        contentPane.add(timeout_field);

        //delimiter
        String delimiter_tooltip = "<html>Delimiter used in the .csv file to separate the columns<br>" +
                "<b>Using the same as for the decimal separator will cause issues loading the .csv!</b></html>";
        JLabel delimiter_label = new JLabel("Delimiter (.csv files):");
        delimiter_label.setFont(LABELS);
        delimiter_label.setToolTipText(delimiter_tooltip);
        contentPane.add(delimiter_label);
        delimiter_field = new JTextField(String.valueOf(Main.DEFAULT_DELIMETER));
        Warning delimiter_warning = new Warning("Delimiter must be a character!",
                                                (e) -> delimiter_field.getText().matches("."));
        delimiter_field.getDocument().addDocumentListener(delimiter_warning);
        delimiter_field.setToolTipText(delimiter_tooltip);
        contentPane.add(delimiter_warning);
        contentPane.add(delimiter_field);

        //decimal separator
        String decimalSeparator_tooltip = "<html>The decimal separator used for all numbers</html>";
        JLabel decimalSeparator_label = new JLabel("Decimal separator:");
        decimalSeparator_label.setFont(LABELS);
        decimalSeparator_label.setToolTipText(decimalSeparator_tooltip);
        contentPane.add(decimalSeparator_label);
        decimalSeparator_field = new JTextField(String.valueOf(DecimalFormatSymbols.getInstance().getDecimalSeparator()));
        Warning decimalSeparator_warning = new Warning("Decimal separator must be . or ,!",
                                                       (e) -> decimalSeparator_field.getText().matches("[,.]"));
        decimalSeparator_field.getDocument().addDocumentListener(decimalSeparator_warning);
        decimalSeparator_field.setToolTipText(decimalSeparator_tooltip);
        contentPane.add(decimalSeparator_field);
        contentPane.add(decimalSeparator_warning);

        //start button
        String start_tooltip = "<html>Click here to start the speedtest!</html>";
        start = new JButton("Run");
        start.setToolTipText(start_tooltip);
        start.addActionListener((e) -> {
            setVisible(false);
            dispose();
        });
        contentPane.add(start);


        //Set Layout
        final SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
        layout.putConstraint(SpringLayout.WEST, serverIds_warning, 5, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.NORTH, serverIds_warning, 5, SpringLayout.NORTH, contentPane);

        layout.putConstraint(SpringLayout.WEST, interval_warning, 0, SpringLayout.WEST, serverIds_warning);
        layout.putConstraint(SpringLayout.NORTH, interval_warning, 5, SpringLayout.NORTH, serverIds_warning);

        layout.putConstraint(SpringLayout.WEST, delimiter_warning, 0, SpringLayout.WEST, interval_warning);
        layout.putConstraint(SpringLayout.NORTH, delimiter_warning, 5, SpringLayout.SOUTH, interval_warning);

        layout.putConstraint(SpringLayout.WEST, timeout_warning, 0, SpringLayout.WEST, delimiter_warning);
        layout.putConstraint(SpringLayout.NORTH, timeout_warning, 5, SpringLayout.SOUTH, delimiter_warning);

        layout.putConstraint(SpringLayout.WEST, decimalSeparator_warning, 0, SpringLayout.WEST, timeout_warning);
        layout.putConstraint(SpringLayout.NORTH, decimalSeparator_warning, 5, SpringLayout.SOUTH, timeout_warning);

        layout.putConstraint(SpringLayout.WEST, serverIds_label, 0, SpringLayout.WEST, decimalSeparator_warning);
        layout.putConstraint(SpringLayout.WEST, serverIds_scroll, 80, SpringLayout.WEST, serverIds_label);
        layout.putConstraint(SpringLayout.NORTH, serverIds_label, 5, SpringLayout.SOUTH, decimalSeparator_warning);
        layout.putConstraint(SpringLayout.NORTH, serverIds_scroll, 5, SpringLayout.SOUTH, decimalSeparator_warning);

        layout.putConstraint(SpringLayout.WEST, file_label, 0, SpringLayout.WEST, serverIds_label);
        layout.putConstraint(SpringLayout.WEST, file_field, 0, SpringLayout.WEST, serverIds_scroll);
        layout.putConstraint(SpringLayout.EAST, file_field, 0, SpringLayout.EAST, serverIds_scroll);
        layout.putConstraint(SpringLayout.NORTH, file_label, 5, SpringLayout.SOUTH, serverIds_scroll);
        layout.putConstraint(SpringLayout.NORTH, file_field, 5, SpringLayout.SOUTH, serverIds_scroll);

        layout.putConstraint(SpringLayout.WEST, interval_label, 0, SpringLayout.WEST, file_label);
        layout.putConstraint(SpringLayout.WEST, interval_field, 0, SpringLayout.WEST, file_field);
        layout.putConstraint(SpringLayout.EAST, interval_field, 0, SpringLayout.EAST, file_field);
        layout.putConstraint(SpringLayout.NORTH, interval_label, 5, SpringLayout.SOUTH, file_field);
        layout.putConstraint(SpringLayout.NORTH, interval_field, 5, SpringLayout.SOUTH, file_field);

        layout.putConstraint(SpringLayout.WEST, timeout_label, 0, SpringLayout.WEST, interval_label);
        layout.putConstraint(SpringLayout.WEST, timeout_field, 0, SpringLayout.WEST, interval_field);
        layout.putConstraint(SpringLayout.EAST, timeout_field, 0, SpringLayout.EAST, interval_field);
        layout.putConstraint(SpringLayout.NORTH, timeout_label, 5, SpringLayout.SOUTH, interval_field);
        layout.putConstraint(SpringLayout.NORTH, timeout_field, 5, SpringLayout.SOUTH, interval_field);

        layout.putConstraint(SpringLayout.WEST, delimiter_label, 0, SpringLayout.WEST, timeout_label);
        layout.putConstraint(SpringLayout.EAST, delimiter_field, 0, SpringLayout.EAST, timeout_field);
        layout.putConstraint(SpringLayout.WEST, delimiter_field, -15, SpringLayout.EAST, delimiter_field);
        layout.putConstraint(SpringLayout.NORTH, delimiter_label, 5, SpringLayout.SOUTH, timeout_field);
        layout.putConstraint(SpringLayout.NORTH, delimiter_field, 5, SpringLayout.SOUTH, timeout_field);

        layout.putConstraint(SpringLayout.WEST, decimalSeparator_label, 0, SpringLayout.WEST, delimiter_label);
        layout.putConstraint(SpringLayout.WEST, decimalSeparator_field, 0, SpringLayout.WEST, delimiter_field);
        layout.putConstraint(SpringLayout.EAST, decimalSeparator_field, 0, SpringLayout.EAST, delimiter_field);
        layout.putConstraint(SpringLayout.NORTH, decimalSeparator_label, 5, SpringLayout.SOUTH, delimiter_field);
        layout.putConstraint(SpringLayout.NORTH, decimalSeparator_field, 5, SpringLayout.SOUTH, delimiter_field);

        layout.putConstraint(SpringLayout.NORTH, start, 5, SpringLayout.SOUTH, decimalSeparator_field);
        layout.putConstraint(SpringLayout.EAST, start, 0, SpringLayout.EAST, serverIds_scroll);

        layout.putConstraint(SpringLayout.EAST, contentPane, 5, SpringLayout.EAST, serverIds_scroll);
        layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, start);
        pack();


        //set window properties
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        //display in screen center
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2, getWidth(), getHeight());
        setVisible(true);
    }

    public List<Integer> getServerIDs() {
        if (serverIds.getText().isEmpty()) return new ArrayList<>();
        String[] ids = serverIds.getText().split("\\s*[\\s,;/]\\s*");
        List<Integer> list = new ArrayList<>(ids.length);
        for (String s : ids) {
            list.add(Integer.valueOf(s));
        }
        return list;
    }

    public File getFile() {
        return file;
    }

    public long getInterval() {
        String text = interval_field.getText();
        long multiplier = 1000;
        if (text.endsWith("d")) {
            multiplier *= 24 * 60 * 60;
            text = text.substring(0, text.length() - 1);
        } else if (text.endsWith("h")) {
            multiplier *= 60 * 60;
            text = text.substring(0, text.length() - 1);
        } else if (text.endsWith("m")) {
            multiplier *= 60;
            text = text.substring(0, text.length() - 1);
        } else if (text.endsWith("s")) {
            text = text.substring(0, text.length() - 1);
        }
        return Integer.valueOf(text) * multiplier;
    }

    public int getTimeout() {
        return Integer.valueOf(timeout_field.getText());
    }

    public char getDelimeter() {
        return delimiter_field.getText().charAt(0);
    }

    public void updateDecimalSeperator() {
        char decimalSeparator = decimalSeparator_field.getText().charAt(0);
        DecimalFormatSymbols.getInstance().setDecimalSeparator(decimalSeparator);
    }

    /**
     * Helper class that listens for changes on a document and displays warnings if document is invalid
     */
    private class Warning extends JLabel implements DocumentListener {

        private final Predicate<DocumentEvent> check;
        private final String warning_msg;

        public Warning(String warning_msg, Predicate<DocumentEvent> check) {
            this.check = check;
            this.warning_msg = warning_msg;
            setFont(LABELS);
            setForeground(Color.RED);
        }

        private void onEvent(DocumentEvent e) {
            final boolean test = check.test(e);
            if (test && !getText().isEmpty()) {
                //if text is valid and warning is displayed remove it
                setText("");
                repaint();
                currentWarnings.remove(this);
                if (currentWarnings.isEmpty()) {
                    start.setEnabled(true);
                }
            } else if (!test && getText().isEmpty()) {
                //if text is invalid and no warning is displayed show it
                setText(warning_msg);
                repaint();
                currentWarnings.add(this);
                if (!currentWarnings.isEmpty()) {
                    start.setEnabled(false);
                }
            }
        }

        public boolean isWarning() {
            return !warning_msg.isEmpty();
        }

        @Override
        public final void insertUpdate(DocumentEvent e) {
            onEvent(e);
        }

        @Override
        public final void removeUpdate(DocumentEvent e) {
            onEvent(e);
        }

        @Override
        public final void changedUpdate(DocumentEvent e) {
            onEvent(e);
        }
    }
}
