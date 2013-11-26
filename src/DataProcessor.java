import Preprocessors.PreprocessorAbstract;
import Preprocessors.PreprocessorI;

import java.util.ArrayList;

public class DataProcessor {

    int width;

    //Override
    int minValue = -40000;
    int maxValue = 40000;

    boolean deltaMillisCalc = false;
    int lastMillis = 0;

    GraphData graphData;

    ArrayList<PreprocessorAbstract> preprocessors = new ArrayList<PreprocessorAbstract>();

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

        String[] packMillis = serialData.split("\\|");
        if(packMillis.length == 2)
        {
            int arduinoMillis = Integer.parseInt(packMillis[packMillis.length - 1]);

            if(!deltaMillisCalc)
            {
                lastMillis = arduinoMillis;
            }
            int diff = arduinoMillis - lastMillis;
            /**
             * Чтобы не потерять время, если не была вызвана shiftData в препроцессорах
             */
            graphData.MILLIS_BETWEEN_PACK[width - 1] += diff;

            graphData.elapsedTime += diff;
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
        float[] values = new float[incomingValues.length];

        for(int val_num = 0; val_num < incomingValues.length; val_num++)
        {
            values[val_num] = Float.parseFloat(incomingValues[val_num]);
        }

        processValues(values, graphData.MILLIS_BETWEEN_PACK[width - 1]);
    }

    void processValues(float[] incomingValues, int millisBetweenPack)
    {
        if(!preprocessors.isEmpty())
        {
            int preprocessorNum = 0;
            preprocessorChain(incomingValues, millisBetweenPack, preprocessorNum);
        }
        else
        {
            addData(incomingValues);
        }
    }

    public void preprocessorChain(float[] incomingValues, int millisBetweenPack, int preprocessorNum)
    {
        PreprocessorAbstract Preprocessor = preprocessors.get(preprocessorNum);
        preprocessorNum++;
        /**
         * Записываем результат, если только разрешает allowShiftData,
         * т.к. препроцессору может потребоваться несколько пачек данных
         */
        float[] result = Preprocessor.processValues(incomingValues, millisBetweenPack);
        if(Preprocessor.allowShiftData())
        {
            if(preprocessorNum == preprocessors.size())
            {
                addData(result);
            }
            else
            {
                preprocessorChain(result, millisBetweenPack, preprocessorNum);
            }
        }

    }

    void addData(float[] processedValues)
    {
        shiftData();

        /**
         * Default preprocessing...
         */
        for(int val_num = 0; val_num < processedValues.length; val_num++)
        {
            this.graphData.COLUMN_DATA[val_num][width-1] = processedValues[val_num];
        }
    }

    protected void shiftData() {
        // put all data and related timings one array back
        for(int i = 1; i < width; i++)
        {
            for(int val_num = 0; val_num < this.graphData.COLUMN_DATA.length; val_num++)
            {
                this.graphData.COLUMN_DATA[val_num][i-1] = this.graphData.COLUMN_DATA[val_num][i];
            }

            graphData.MILLIS_BETWEEN_PACK[i-1] = graphData.MILLIS_BETWEEN_PACK[i];
        }

        graphData.MILLIS_BETWEEN_PACK[width - 1] = 0;
        if(graphData.valuesFilled < width)
        {
            graphData.valuesFilled++;
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
        this.graphData.COLUMN_NAMES = this.getColumnNames(serialData);

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
        this.graphData.COLUMN_DATA = new float[this.graphData.COLUMN_NAMES.length][width];
        graphData.MILLIS_BETWEEN_PACK = new int[width];

        graphData.columnNamesInited = true;

        return true;
    }

    public String[] getColumnNames(String serialData)
    {
        String[] result;

        //TODO: maybe make only one possible preprocessor available instead of list?
        if(!preprocessors.isEmpty()) //Supports only one preprocessor now..
        {


            return preprocessors.get(preprocessors.size()-1).getColumnNames(serialData);
        }

        result = serialData.split(",");

        return result;
    }

    void addPreprocessor(PreprocessorAbstract preprocessor)
    {

        preprocessors.add(preprocessor);
    }

}
