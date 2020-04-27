# RPi-1.5-OLED
Raspberry Pi library to control Waveshare 1.5" OLED SPI module using Pi4J

## Build Environment

### Raspberry Pi model 3B

#### OS

Raspbian Buster Lite (Version: February 2020)  
*https://www.raspberrypi.org/downloads/raspbian/*

#### Installed software

**OpenJDK 11**

*https://openjdk.java.net/projects/jdk/11/*

`sudo apt-get install openjdk-11`

**Wiringpi**

*http://wiringpi.com/download-and-install/*

`sudo apt-get install wiringpi`

**Pi4J**

*https://pi4j.com/1.2/install.html*

`curl -sSL https://pi4j.com/install | sudo bash`

**SSH**

Enable ssh by adding an empty file named "*ssh*" in the boot partition of the RPi SD card.
SSH is needed for remote debugging.

### Hardware

*TODO*

### Quick test

You can check if your setup works by simply cloning this repo directly to your RPi and run "*OLEDDemo.sh*".
This will run the pre-compiled OLED demo.

`git clone https://github.com/DrMarcel/RPi-1.5-OLED.git`  
`cd RPi-1.5-OLED`  
`./OLEDDemo.sh`  

### IDE

#### IntelliJ

*IntelliJ IDEA 2020.1 (Community Edition)*  
*Build #IC-201.6668.121, built on April 8, 2020*  
*Runtime version: 11.0.7+10-b875.1 amd64*  
https://www.jetbrains.com/de-de/idea/  

#### Plugins

**Choose Runtime**

Useful to download the correct SDK version. 
After installing the plugin you can download SDK *build 11.0.7*.
You can find the *Choose Runtime* dialog by searching "*Choose Runtime*" under *Help->Find action...*. 
The downloaded Runtime can be added under *File->Project Structure...->SDKs*. 
The SDK path may be something like */home/username/.var/app/com.jetbrains.IntelliJ-IDEA-Community/config/JetBrains/IdeaIC2020.1/jdks/jbrsdk-11_0_7-linux-x64-b875.1/jbrsdk*.

If you are using a higher SDK version it may be possible to change the *Language Level* under *File->Project Structure...->Project* to 11. 

**Embedded Linux JVM Debugger**

Build Tool for remote debugging on the RPi.  
After installing the plugin you get a new Template under *Run->Edit Configurations...*. 
To create a new build configuration for remote debugging just copy that Template and insert the data for your Pi.  