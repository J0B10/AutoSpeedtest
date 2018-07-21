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

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Window displaying the progress of the speedtest
 * <p>
 * Created on 08.07.2018.
 *
 * @author Jonas Blocher
 */
public class SpeedtestFrame extends JFrame {

    private final JTextArea content;
    private final JProgressBar progressBar;

    public SpeedtestFrame(String title) throws HeadlessException {
        //set frame properties
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(575, 300));
        setBounds((screen.width - 716) / 2, (screen.height - 489) / 2, 716, 489);
        setTitle("AutoSpeedtest");
        //Set layout
        final BorderLayout layout = new BorderLayout();
        setLayout(layout);
        //add conetent text area with scroll bars
        content = new JTextArea();
        content.setEditable(false);
        final JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        add(scrollPane, BorderLayout.CENTER);
        //add speedtest progress bar
        JPanel bottom = new JPanel();
        JLabel description = new JLabel("Speedtest: ");
        description.setFont(new Font("Calibri", Font.BOLD, 14));
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setPreferredSize(new Dimension(getWidth() - 250, 20));
        progressBar.setStringPainted(true);
        progressBar.setString("");
        bottom.add(description);
        bottom.add(progressBar);
        bottom.setBorder(BorderFactory.createLoweredBevelBorder());
        add(bottom, BorderLayout.SOUTH);
        //set visible
        setVisible(true);
    }


    /**
     * @return a PrintStream that writes all output to this frame
     */
    public PrintStream printer() {
        return new PrintStream(new Out());
    }

    /**
     * Set the PrintStream returned by {@link #printer()} as {@link System#out} and {@link System#err}
     *
     * @return the SpeedtestFrame
     */
    public SpeedtestFrame useAsOutput() {
        PrintStream print = printer();
        System.setOut(print);
        System.setErr(print);
        return this;
    }

    /**
     * Indicates that a speedtest is running
     * <p>
     * This results in an animation in the progress bar
     */
    public void indicateSpeedtest() {
        SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(true));
    }

    /**
     * Indicates that a speedtest is running
     * <p>
     * This results in an animation in the progress bar
     *
     * @param label label that should be displayed in the progress bar while the test is running
     */
    public void indicateSpeedtest(String label) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(true);
            progressBar.setString(label);
        });
    }

    /**
     * Stops the indication of any speedtests running
     * <p>
     * No animation will be displayed in the progress bar, instead it will be empty
     */
    public void endSpeedtest() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setString("");
        });
    }

    public class Out extends OutputStream {

        private StringBuilder builder = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            SwingUtilities.invokeLater(() -> {
                byte[] bytes = {(byte) b};
                content.append(new String(bytes));
                content.setCaretPosition(content.getDocument().getLength());
            });
        }
    }
}
