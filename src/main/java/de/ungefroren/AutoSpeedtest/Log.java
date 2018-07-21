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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper class that logs the results of speedtests to a .csv file
 * <p>
 * Created on 08.07.2018.
 *
 * @author Jonas Blocher
 */
public class Log {

    private static Log instance;

    private final File file;
    private final char delimiter;

    public Log(File file, char delimiter) {
        this.file = file;
        this.delimiter = delimiter;
        //if log file doesn't exist create new one with header
        if (!file.exists()) {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
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
        instance = this;
    }

    public static Log getInstance() {
        return instance;
    }

    /**
     * Log a line of text to the .csv
     *
     * @param string a new line
     */
    public void log(String string) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.newLine();
            bw.write(string);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log the results of the speedtest
     *
     * @param speedtest a speedtest
     */
    public void log(Speedtest speedtest) {
        log(speedtest.valuesAsCsv(delimiter));
    }
}
