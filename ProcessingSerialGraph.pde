import processing.serial.*;

Serial arduino;

String serialData;

boolean showAllGraphsOnOneAxis = true;

String[] COLUMN_NAMES;
float[][] COLUMN_DATA;


int[] MIN_VALUES;
int[] MAX_VALUES;

//Override 
int minValue = -40000;
int maxValue = 40000;

//Graph colors
color[] graphColors = { #FF0000, #0000FF };
//Default graph color
color defaultGraphColor = #cccccc;

//This array is actually used for performance reasons
color[] initedColorsArray;

boolean columnNamesInited = false;

void setup() {  
  size(1600, 600);
  //println(arduino.list()); // Use this to print connected serial devices
  arduino = new Serial(this, Serial.list()[1], 115200);
  arduino.clear();
  arduino.bufferUntil('\n'); // Buffer until line feed
  
}

void draw()
{ 
  // Draw graphPaper
  background(255); // white
  for (int i = 0; i<=width/10; i++) {
    stroke(200); // gray
    line((-frameCount%10)+i*10, 0, (-frameCount%10)+i*10, height);
    line(0, i*10, width, i*10);
  }

  stroke(0); // black
  for (int i = 1; i <= 3; i++)
    line(0, height/4*i, width, height/4*i); // Draw line, indicating 90 deg, 180 deg, and 270 deg



  noFill();
  
  
  if(columnNamesInited)
  {
    // redraw each graph
    for(int val_num = 0; val_num < COLUMN_DATA.length; val_num++)
    {
      
      stroke(initedColorsArray[val_num]);
      
      beginShape();
      for(int i = 0; i < COLUMN_DATA[val_num].length; i++)
      {
        float ypos;
        if(showAllGraphsOnOneAxis)
        {
          ypos = map(COLUMN_DATA[val_num][i], MIN_VALUES[val_num], MAX_VALUES[val_num], 0, height);
        }
        else
        {
          ypos = map(COLUMN_DATA[val_num][i], MIN_VALUES[val_num], MAX_VALUES[val_num], 0, height/COLUMN_DATA.length);
          float graphBottom = val_num * height/COLUMN_DATA.length;
          ypos = ypos + graphBottom;
        }
        
        vertex(i,ypos);
      }
      endShape();
      
      // put all data one array back
      for(int i = 1; i < COLUMN_DATA[val_num].length; i++)
      {
          COLUMN_DATA[val_num][i-1] = COLUMN_DATA[val_num][i];
      }
    }
  }
  
  
}

void serialEvent (Serial arduino) {
  float[] incomingValues;
  
  serialData = arduino.readStringUntil('\n');
  serialData = trim(serialData);
  if (serialData != null) {
    
    if(!columnNamesInited)
    {
      print("Init string from arduino: ");
      println(serialData);
      

      int startColumnsIndex = serialData.indexOf(':');
      if(startColumnsIndex == -1)
      {
        println("Error!");
      }
      
      String[] COLUMN_NAMES = split(serialData.substring(startColumnsIndex + 1), ",");
      
      //Init min and max array length
      MIN_VALUES = new int[COLUMN_NAMES.length];
      MAX_VALUES = new int[COLUMN_NAMES.length];
      
      initedColorsArray = new color[COLUMN_NAMES.length];
      
      for(int i=0; i < COLUMN_NAMES.length; i++)
      {
        int additionalDataIndex = COLUMN_NAMES[i].indexOf('{');
        if(additionalDataIndex != -1)
        {
          //Есть дополнительные данные
          //Обрезаем название колонки, а за ней фигурные скобки с дополнительными данными
          String AdditionalData = COLUMN_NAMES[i].substring(additionalDataIndex + 1, COLUMN_NAMES[i].length() - 1);
          //Самой колонке оставляем только имя
          COLUMN_NAMES[i] = COLUMN_NAMES[i].substring(0, additionalDataIndex);
         
         
          int[] minAndMax = int(split(AdditionalData, ";"));
          MIN_VALUES[i] = minAndMax[0];
          MAX_VALUES[i] = minAndMax[1];
        }
        else
        {
          MIN_VALUES[i] = minValue;
          MAX_VALUES[i] = maxValue;
        }
        
        //Init colors
        if(graphColors.length > i)
        {
          initedColorsArray[i] = graphColors[i];
        }
        else
        {
          initedColorsArray[i] = defaultGraphColor;
        }
        
      }

      
      //Making buffer
      COLUMN_DATA = new float[COLUMN_NAMES.length][width];
      
      
      columnNamesInited = true;
      
    }
    
    else
    {
      incomingValues = float(split(serialData, ","));
      
    
      for(int val_num = 0; val_num < incomingValues.length; val_num++)
      {
        COLUMN_DATA[val_num][width-1] = incomingValues[val_num];
      }
    }
  }
  
  arduino.clear();
  
}
