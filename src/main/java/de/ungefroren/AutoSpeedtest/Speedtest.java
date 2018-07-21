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

import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.StringJoiner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Class representing a speedtest that can be run and whose results can be logged
 * <p>
 * Created on 05.07.2018.
 *
 * @author Jonas Blocher
 */
public class Speedtest implements Runnable {

    private static final String COMMAND = "speedtest-cli --json --server %d --secure --timeout %d";
    private static final String COMMAND_UNSPECIFIC_SERVER = "speedtest-cli --json --secure --timeout %d";

    static int TIMEOUT = Main.DEFAULT_TIMEOUT;


    /**
     * Server to connect to
     */
    private final Optional<Integer> serverID;

    /**
     * Ping in milliseconds
     */
    private double ping = -1;

    /**
     * Average download speed in bits
     */
    private double download = -1;

    /**
     * Average upload speed in bits
     */
    private double upload = -1;

    /**
     * Name of the host server to test against
     */
    private String serverName = null;

    /**
     * Location of the host server
     */
    private String location = null;

    /**
     * Url of the host server
     */
    private String url = null;

    /**
     * Time at which the test was done in UTC
     */
    private ZonedDateTime timestamp = null;

    public Speedtest(int id) {
        this(Optional.of(id));
    }

    public Speedtest(Optional<Integer> id) {
        serverID = id;
    }

    public Speedtest() {
        serverID = Optional.empty();
    }

    @Override
    public void run() {
        try {
            final Runtime runtime = Runtime.getRuntime();
            String command;
            if (serverID.isPresent()) command = String.format(COMMAND, serverID.get(), TIMEOUT);
            else command = String.format(COMMAND_UNSPECIFIC_SERVER, TIMEOUT);
            Process process = runtime.exec(command);
            JSONObject object = (JSONObject) new JSONParser().parse(new InputStreamReader(process.getInputStream()));
            ping = (double) object.get("ping");
            download = (double) object.get("download");
            upload = (double) object.get("upload");
            JSONObject server = (JSONObject) object.get("server");
            serverName = (String) server.get("sponsor");
            location = (String) server.get("name");
            url = (String) server.get("url");
            timestamp = LocalDateTime.parse(((String) object.get("timestamp")).split("\\.")[0]).atZone(ZoneId.of("UTC"));
            System.out.printf("Speedtest done! Ping: %.3fms Download: %.3fMbit/s Upload:% .3fMbit/s Server: %s (%s)\n",
                              ping,
                              download / 1000000,
                              upload / 1000000,
                              serverName,
                              location);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return If the test has been completed and results have been cached
     */
    public boolean completed() {
        return ping >= 0
                && download >= 0
                && upload >= 0
                && serverName != null
                && location != null
                && url != null
                && timestamp != null;
    }

    /**
     * Returns a string containing the result of the speedtest, formatted to be exported as csv
     *
     * @param delimiter the delimiter to use in csv output
     * @return the formatted result
     * @throws RuntimeException if the speedtest hasn't been executed yet
     */
    public String valuesAsCsv(final char delimiter) throws RuntimeException {
        if (!completed()) throw new RuntimeException("Speedtest must be run before values can be got");
        StringJoiner joiner = new StringJoiner(String.valueOf(delimiter));
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        joiner
                .add(timestamp.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .add(decimalFormat.format(ping))
                .add(decimalFormat.format(download / 1000000)) //in Mbit/s
                .add(decimalFormat.format(upload / 1000000)) //in Mbit/s
                .add(serverName)
                .add(location)
                .add(url);
        return joiner.toString();
    }

    @Override
    public String toString() {
        if (completed()) {
            return "Speedtest{" +
                    "serverName='" + serverName + '\'' +
                    ", location='" + location + '\'' +
                    ", url='" + url + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    ", ping=" + ping +
                    ", download=" + download +
                    ", upload=" + upload +
                    '}';
        } else {
            return "Speedtest{server=" + serverID + '}';
        }
    }
}
