ProcessingSerialGraph
=====================

Processing (java-like, processing.org). Draws graph with data from serial port. Useful with arduino.

![Draws graph with data from serial port](/screenshots/specialDelayInArduinoSketch.png)

Code used in Arduino sketch
-------------

    void setup()
    {
        ...
        Serial.println("Columns:gyro.x{-32768;32767},gyro.y{-32768;32767},gyro.z{-32768;32767},acc.x{-2048;2047},acc.y{-2048;2047},acc.z{-2048;2047}");
    }

    
    void loop()
    {
        ...sersor read
    
        Serial.print(gyro.g.x);
        Serial.print(",");
        Serial.print(gyro.g.y);
        Serial.print(",");
        Serial.print(gyro.g.z);
        Serial.print(",");
        Serial.print(compass.a.x);
        Serial.print(",");
        Serial.print(compass.a.y);
        Serial.print(",");
        Serial.print(compass.a.z);
        Serial.print("|");
        Serial.println(millis());
    }

Graph preprocessors
=====================
###Recursive filter preprocessor
![Recursive filter preprocessor](/screenshots/Preprocessor-RecursiveFilter.png)
###Recursive filter vs. raw data from accelerometer
![Recursive filter preprocessor](/screenshots/Recursive.vs.WithoutFilter.png)
###Gyroscope angles
![Gyroscope angles data processing](/screenshots/GyroDrift.jpg)