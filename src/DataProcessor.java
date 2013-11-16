public class DataProcessor {

    int width;


    //Override
    int minValue = -40000;
    int maxValue = 40000;



    boolean deltaMillisCalc = false;
    int lastMillis = 0;

    GraphData graphData;


    public DataProcessor(int width) {
        this.graphData = new GraphData();
        this.width = width;
    }

    public void processData(String serialData)
    {
        if(!graphData.columnNamesInited)
        {
            initColumnNames(serialData);
            return;
        }

        // put all data and related timings one array back
        for(int i = 1; i < width; i++)
        {
            for(int val_num = 0; val_num < this.graphData.COLUMN_DATA.length; val_num++)
            {
                this.graphData.COLUMN_DATA[val_num][i-1] = this.graphData.COLUMN_DATA[val_num][i];
            }

            graphData.MILLIS_BETWEEN_PACK[i-1] = graphData.MILLIS_BETWEEN_PACK[i];
        }


        String[] packMillis = serialData.split("\\|");
        if(packMillis.length == 2)
        {
            int arduinoMillis = Integer.parseInt(packMillis[packMillis.length - 1]);

            if(!deltaMillisCalc)
            {
                lastMillis = 0;
            }
            int diff = arduinoMillis - lastMillis;
            graphData.MILLIS_BETWEEN_PACK[width - 1] = diff;
            graphData.timingOffset += diff;
            if(graphData.timingOffset > graphData.lineSeparatorEvery)
            {
                graphData.timingOffset = graphData.timingOffset % graphData.lineSeparatorEvery;
            }


            lastMillis = arduinoMillis;
            deltaMillisCalc = true;


            serialData = packMillis[0];
        }


        String[] incomingValues = serialData.split(",");

        for(int val_num = 0; val_num < incomingValues.length; val_num++)
        {
            this.graphData.COLUMN_DATA[val_num][width-1] = Integer.parseInt(incomingValues[val_num]);
        }

    }


    public boolean initColumnNames(String serialData)
    {
        int startColumnsIndex = serialData.indexOf(':');
        if(startColumnsIndex == -1)
        {

            //If Arduino already writes in serial port, we've got garbage in serialData.
            //So wait untill arduino resets
            return false;
        }

        System.out.print("Init string from arduino: ");
        System.out.println(serialData);

        serialData = serialData.substring(startColumnsIndex + 1);
        this.graphData.COLUMN_NAMES = serialData.split(",");

        //Init min and max array length
        graphData.MIN_VALUES = new int[this.graphData.COLUMN_NAMES.length];
        graphData.MAX_VALUES = new int[this.graphData.COLUMN_NAMES.length];



        for(int i=0; i < this.graphData.COLUMN_NAMES.length; i++)
        {
            int additionalDataIndex = this.graphData.COLUMN_NAMES[i].indexOf('{');
            if(additionalDataIndex != -1)
            {
                //Есть дополнительные данные
                //Обрезаем название колонки, а за ней фигурные скобки с дополнительными данными
                String AdditionalData = graphData.COLUMN_NAMES[i].substring(additionalDataIndex + 1, this.graphData.COLUMN_NAMES[i].length() - 1);
                //Самой колонке оставляем только имя
                this.graphData.COLUMN_NAMES[i] = this.graphData.COLUMN_NAMES[i].substring(0, additionalDataIndex);


                String[] aAdditionalData = AdditionalData.split(";");

                for(int minMaxIndex = 0; minMaxIndex < aAdditionalData.length; minMaxIndex++) {
                    if(minMaxIndex == 0)
                    {
                        graphData.MIN_VALUES[i] = Integer.parseInt(aAdditionalData[minMaxIndex]);
                    }
                    else if(minMaxIndex == 1)
                    {
                        graphData.MAX_VALUES[i] = Integer.parseInt(aAdditionalData[minMaxIndex]);
                    }
                }


            }
            else
            {
                graphData.MIN_VALUES[i] = minValue;
                graphData.MAX_VALUES[i] = maxValue;
            }


        }


        //Making buffer
        this.graphData.COLUMN_DATA = new int[this.graphData.COLUMN_NAMES.length][width];
        graphData.MILLIS_BETWEEN_PACK = new int[width];

        graphData.columnNamesInited = true;

        return true;
    }

}
