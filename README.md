```
 __________________________________________________________________
/_____/_____/_____/_____/_____/_____/_____/_____/_____/_____/_____/
   /   | __  __/ /_____
  / /| |/ / / / __/ __ \
 / ___ / /_/ / /_/ /_/ /
/_/  |_\__,_/\__/\____/             ____            __
          _________  ___  ___  ____/ / /____  _____/ /_
         / ___/ __ \/ _ \/ _ \/ __  / __/ _ \/ ___/ __/
        (__  ) /_/ /  __/  __/ /_/ / /_/  __(__  ) /_
       /____/ .___/\___/\___/\__,_/\__/\___/____/\__/
 __________///_____________________________________________________
/_____/_____/_____/_____/_____/_____/_____/_____/_____/_____/_____/

```
I have problems with my ISP (they are not delivering the bandwidth that they sold me),
so I created this small command line tool to do automated speedtests and log the results into a csv file.


The tool uses [speedtest-cli by sivel](https://github.com/sivel/speedtest-cli) to do the actual speedtests.

[**-> Zur deutschen Anleitung**]()

## Prerequisites
To run this program you need to have Python and Java installed on your computer.

* Python download: https://www.python.org/downloads/
* Java download: https://www.java.com

The program should install speedtest-cli by itself, but if you encounter any errors you can try downloading it manually:

* speedtest-cli download: https://github.com/sivel/speedtest-cli#installation

## Installation
It's pretty simple:
Just download the latest release from https://github.com/joblo2213/AutoSpeedtest/releases.  
Make sure that you downloaded the file named `AutoSpeedtest.jar`.

You can then place the jar file anywhere on your pc and run it by double clicking.

If you wan't to run it without gui, open the console, naviagte to the directory of the jar file and use the following command:

```
java -jar Autospeedtest.jar -gui:false
```

## Setup
If you run the program by double clicking the following window should open:

![setup](https://raw.githubusercontent.com/wiki/joblo2213/AutoSpeedtest/images/setup.png)

Here you can tweak the following settings:

* **Server Ids:**  
  A list of ids of servers that should be used for the speedtest.  
  You can find a full list containing the ids of all available servers under https://speedtestserver.com/  
  **Example:**
  ```
  11547,
  12082,
  11123,
  
  ```
* **Log file:**  
  Click on this field and a "save file" dialog will open.  
  Select the file where you want your results to be saved.  
  If the file already exists the results will be appended to the end of the file.
* **Interval:**  
  Set how often the program should run speedtests.  
  You can add `s` to the end of the number and it will be interpreted as seconds, `m` as minutes , `h` as hours and `d` as days.  
  Default setting is in seconds.  
  **Example:** `220m` (test will run every 220 minutes)
* **Timout:**  
  Time after which the http connection times out.  
  Only change this if you know what you are doing. :wink:
* **Delimiter:**  
  A character that separates the columns in the csv file where your results get saved.  
  To avoid errors I suggest to use one of the following: `;` `|` `^`  
  Or `,`, but then make sure it is not used as decimal separator!
* **Decimal separator:**  
  Character that is used to separate the integer part from the fractional part of a number.  
  Setting it to `.` Will display numbers as `3.45`, while `,` will display it as `3,45`.

When you configured everything click on `Run`.

If you run the program from command line you can change those settings by adding command line arguments:

```
java -jar AutoSpeedtest.jar -gui:false -servers:[list of servers separated by ,] -log:[path to file] -interval:[time in seconds] -timeout:[timout in seconds] -delimiter:[delimiter char] -decimalSeparator:[decimal separator char]
```
Of course you have to replace the square brackets with your values

## Evaluating results in [Microsoft Excel](https://products.office.com/en-us/excel)

You can use the template [results.xltx](https://github.com/joblo2213/AutoSpeedtest/blob/master/results.xltx?raw=true) to evaluate your resluts and display nice graphs.  
Just download the template and open it.

**On opening you will get informed about a data source error which is intentionally, you can just ignore it.**


![excel1_en.png](https://raw.githubusercontent.com/wiki/joblo2213/AutoSpeedtest/images/excel1_en.png)

Then click inside the preset table and on `Query` and there on `Edit` to configure the data source settings.  

![excel2_en.png](https://raw.githubusercontent.com/wiki/joblo2213/AutoSpeedtest/images/excel2_en.png)

Click on `Quelle` on the right side in the `Applied Steps` section.  
Then hit `Edit settings`.  
Now a dialog opens where you can change the file path to your csv file which was generated by AutoSpeedtest.  
Hit `Ok` and `Close & Load`.  
Then you will have all your results loaded and displayed in the graphs on the right side.  
Now you can also just click on `Refresh` whenever you want the latest results to be loaded into your table.