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
package de.ungefroren.AutoSpeedtest;

import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ungefroren.AutoSpeedtest.gui.SetupFrame;
import de.ungefroren.AutoSpeedtest.gui.SpeedtestFrame;

/**
 * Created on 05.07.2018.
 *
 * @author Jonas Blocher
 */
public class Main {

    public static final String DEFAULT_FILE = "speedtest-log.csv";
    public static final int DEFAULT_INTERVAL = 3600;
    public static final int DEFAULT_TIMEOUT = 10;
    public static final char DEFAULT_DELIMETER = ';';

    private static boolean gui = false;
    private static List<Integer> serverIds = new ArrayList<>();
    private static File log = new File(DEFAULT_FILE);
    private static long interval = DEFAULT_INTERVAL * 1000L;
    private static char delimiter = DEFAULT_DELIMETER;

    private static SpeedtestFrame mainFrame = null;

    /**
     * Print the asci art from header.txt
     */
    private static void printHaeder() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/header.txt"),
                                                                             StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) System.out.println(line);
            reader.close();
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) throws Exception {
        //check if gui should be displayed
        if (System.console() == null && !GraphicsEnvironment.isHeadless()) {
            gui = true;
        } else {
            for (String arg : args) {
                if (arg.matches("-*gui:true")) {
                    gui = true;
                    break;
                }
            }
        }

        //setup gui
        if (gui) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            mainFrame = new SpeedtestFrame("Log:").useAsOutput();
        }

        printHaeder();

        checkforSpeedtestCLI();

        if (gui) {

            //show setting dialog
            SetupFrame setup = new SetupFrame(mainFrame);
            serverIds = setup.getServerIDs();
            log = setup.getFile();
            interval = setup.getInterval();
            Speedtest.TIMEOUT = setup.getTimeout();
            delimiter = setup.getDelimeter();
            setup.updateDecimalSeperator();

        } else {

            //handle arguments
            for (String arg : args) {
                Matcher m1 = Pattern.compile("-*timeout:(\\d+)").matcher(arg);
                Matcher m2 = Pattern.compile("-*servers:(\\d+(,\\d+)?)").matcher(arg);
                Matcher m3 = Pattern.compile("-*log:([^\\s]+)").matcher(arg);
                Matcher m4 = Pattern.compile("-*inteval:(\\d+)([smhd]?)").matcher(arg);
                Matcher m5 = Pattern.compile("-*decimalSeparator:([^\\s])").matcher(arg);
                Matcher m6 = Pattern.compile("-*delimiter:([^\\s])").matcher(arg);
                if (m1.matches()) {
                    Speedtest.TIMEOUT = Integer.parseInt(m1.group(1));
                } else if (m2.matches()) {
                    final List<Integer> finalServerIds = new ArrayList<>();
                    Arrays.stream(m2.group(1).split(",")).forEach(s -> finalServerIds.add(Integer.parseInt(s)));
                    serverIds = finalServerIds;
                } else if (m3.matches()) {
                    log = new File(m3.group(1));
                } else if (m4.matches()) {
                    interval = Integer.parseInt(m4.group(1));
                    switch (m4.group(2)) {
                        case "d":
                            interval *= 24;
                        case "h":
                            interval *= 60;
                        case "m":
                            interval *= 60;
                        default:
                            interval *= 1000;
                    }
                } else if (m5.matches()) {
                    DecimalFormatSymbols.getInstance().setDecimalSeparator(m5.group(1).charAt(0));
                } else if (m6.matches()) {
                    delimiter = m6.group(1).charAt(0);
                }
            }

        }

        new Log(log, delimiter);

        //start the timer
        Timer mainTimer = new Timer("Clock");
        mainTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runAllSpeedtests();
            }
        }, 2, interval);

        //notify
        System.out.println("Running Speedtest every " + interval / 1000 + " seconds.\n");
    }


    /**
     * Runs all speedtest and logs the results to file
     */
    private static void runAllSpeedtests() {
        final boolean empty = serverIds.isEmpty();
        Iterator<Integer> i = serverIds.iterator();
        while (empty || i.hasNext()) {
            try {
                int next = empty ? 0 : i.next();
                if (empty) {
                    System.out.println("Running speedtest...");
                    if (gui) mainFrame.indicateSpeedtest();
                } else {
                    System.out.println("Running speedtest #" + next + "...");
                    if (gui) mainFrame.indicateSpeedtest("#" + next);
                }
                Speedtest speedtest = empty ? new Speedtest() : new Speedtest(next);
                speedtest.run();
                Log.getInstance().log(speedtest);
                if (gui) mainFrame.endSpeedtest();
                if (empty) return;
            } catch (Exception e) {
                e.printStackTrace();
                if (empty) return;
            }
        }
    }

    /**
     * Check if speedtest-cli is installed, and try to install using pip if not
     */
    private static void checkforSpeedtestCLI() {
        try {
            Runtime.getRuntime().exec("speedtest-cli --help");
            System.out.println("Found speedtest cli!\n");
        } catch (IOException e) {
            if (e.getMessage().contains("error=2") || e.getMessage().contains("error=127")) {
                System.out.println("Installing speedtest-cli:\n");
                try {
                    System.out.println("x");
                    Process p = Runtime.getRuntime().exec("pip install speedtest-cli");
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) System.out.println("| " + line);
                    br.close();
                    System.out.println("x");
                    try {
                        Runtime.getRuntime().exec("speedtest-cli --help");
                        System.out.println("\nFound speedtest-cli!\n");
                    } catch (IOException e2) {
                        throw new RuntimeException("speedtest-cli command not working", e);
                    }
                } catch (IOException e1) {
                    if (e.getMessage().contains("error=2") || e.getMessage().contains("error=127")) {
                        System.out.println("Could not install speedtest-cli!\n\n" +
                                                   "MAKE SURE YOU HAVE INSTALLED PYTHON!\n" +
                                                   "www.python.org/downloads/");
                    } else throw new RuntimeException("could not install speedtest-cli", e1);
                }
            } else throw new RuntimeException("speedtest-cli command not working", e);
        }
    }
}
