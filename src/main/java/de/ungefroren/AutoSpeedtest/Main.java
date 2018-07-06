/**
 * MIT License
 *
 * Copyright (c) 2018 Jonas Blocher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.ungefroren.AutoSpeedtest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 05.07.2018.
 *
 * @author Jonas Blocher
 */
public class Main {

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

    public static void main(String[] args) {
        printHaeder();
        List<Integer> serverIds = new ArrayList<>();
        File log = new File("speedtest-log.csv");
        long interval = /*36000000L*/1000 * 60;
        char delimiter = ';';
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
        if (!log.exists()) {
            if (log.getParentFile() != null) log.getParentFile().mkdirs();
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(log, true));
                bw
                        .append("Time").append(delimiter)
                        .append("Ping (ms)").append(delimiter)
                        .append("Download rate (Mbit/s)").append(delimiter)
                        .append("Upload rate (Mbit/s)").append(delimiter)
                        .append("Server").append(delimiter)
                        .append("Location").append(delimiter)
                        .append("URL");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Timer mainTimer = new Timer("Clock");
        if (serverIds.isEmpty()) {
            final File finalLog = log;
            Speedtest speedtest = new Speedtest(Optional.empty());
            char finalDelimiter = delimiter;
            mainTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        System.out.println("Running speedtest...");
                        speedtest.run();
                        BufferedWriter bw = new BufferedWriter(new FileWriter(finalLog, true));
                        bw.newLine();
                        bw.write(speedtest.valuesAsCsv(finalDelimiter));
                        bw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 2, interval);
        } else {
            for (int id : serverIds) {
                final File finalLog = log;
                Speedtest speedtest = new Speedtest(Optional.empty());
                char finalDelimiter = delimiter;
                mainTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("Running speedtest...");
                            speedtest.run();
                            BufferedWriter bw = new BufferedWriter(new FileWriter(finalLog, true));
                            bw.newLine();
                            bw.write(speedtest.valuesAsCsv(finalDelimiter));
                            bw.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 2, interval);
            }
        }
        System.out.println("Running Speedtest every " + interval / 1000 + " seconds.\n");
    }
}
