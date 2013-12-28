package sys;

import Preprocessors.PreprocessorAbstract;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataProcessor {

    int width;

    //Override
    int minValue = -40000;
    int maxValue = 40000;

    boolean deltaMillisCalc = false;
    int lastMillis = 0;

    boolean accumulateMillisBetweenPack = true;

    public GraphData graphData;

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

            if(accumulateMillisBetweenPack)
            {
                /**
                 * Чтобы не потерять время, если не была вызвана shiftData в препроцессорах
                 * Используется в тех препроцессорах, которые выдают одно значение на много пакетов с МК
                 */
                graphData.MILLIS_BETWEEN_PACK[0] += diff;
            }
            else
            {
                /**
                 * Для тех прероцессоров, для которых не нужно это значение
                 * В основном: один пакет с МК - одно значение на выход.
                 * Но, также может аккумулировать несколько значений для расчета средней ошибки
                 */
                graphData.MILLIS_BETWEEN_PACK[0] = diff;
            }

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

        processValues(values, graphData.MILLIS_BETWEEN_PACK[0]);
    }

    protected void processValues(float[] incomingValues, int millisBetweenPack)
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

    protected void addData(float[] processedValues)
    {
        shiftData();

        /**
         * Default preprocessing...
         */
        for(int val_num = 0; val_num < processedValues.length; val_num++)
        {
            this.graphData.COLUMN_DATA[val_num][0] = processedValues[val_num];
        }
    }

    protected void shiftData() {
        // put all data and related timings one array back
        for(int i = this.graphData.showGraphTime - 1; i > 0; i--)
        {
            for(int val_num = 0; val_num < this.graphData.COLUMN_DATA.length; val_num++)
            {
                this.graphData.COLUMN_DATA[val_num][i] = this.graphData.COLUMN_DATA[val_num][i-1];
            }

            graphData.MILLIS_BETWEEN_PACK[i] = graphData.MILLIS_BETWEEN_PACK[i-1];
        }

        graphData.MILLIS_BETWEEN_PACK[0] = 0;
        if(graphData.valuesFilled < graphData.showGraphTime)
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



        boolean needInitGroups = false;
        if(graphData.groups == null)
        {
            needInitGroups = true;
            graphData.groups = new int[this.graphData.COLUMN_NAMES.length][1];
            System.out.println("Init single groups");
        }
        else
        {
            System.out.println("Using existed groups");
        }

        for(int i=0; i < this.graphData.COLUMN_NAMES.length; i++)
        {
            if(needInitGroups)
            {
                this.graphData.groups[i] = new int[]{i};
            }
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

        if(!needInitGroups)
        {
            for(int[] groupArray : graphData.groups)
            {
                for(int val_num : groupArray)
                {
                    if(val_num > graphData.COLUMN_NAMES.length - 1)
                    {
                        System.out.println("ERROR. Groups count mismatch. Check groups!");
                    }
                }
            }
        }

        //Making buffer
        this.graphData.COLUMN_DATA = new float[this.graphData.COLUMN_NAMES.length][graphData.showGraphTime];
        graphData.MILLIS_BETWEEN_PACK = new int[graphData.showGraphTime];

        graphData.columnNamesInited = true;

        return true;
    }

    public String[] getColumnNames(String serialData)
    {
        String[] result;

        AppProperties appSettings = new AppProperties(this.getClass().getSimpleName());
        String columnHeaderFilename = appSettings.getProperty("ColumnHeaders");

        if(columnHeaderFilename != null)
        {
            System.out.println("Cleared serialData headers. Try to fill from header file");
            serialData = "";
            try {
                BufferedReader br = new BufferedReader(new FileReader(columnHeaderFilename));

                String line = br.readLine();

                while (line != null) {
                    String pattern = "#define (.*) (\\d+)(.*?)//(.*)";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(line);
                    if (m.find( )) {
                        System.out.println("Found column headers value: " + m.group(1) + " as " + m.group(2) + ". Real header: " + m.group(4) );
                        serialData += m.group(4) + ",";
                    }

                    line = br.readLine();
                }

                /**
                 * Remove last comma
                 */
                serialData = serialData.substring(0, serialData.length() - 1);

                br.close();
            } catch (IOException e)
            {
                System.out.println("Parse columns from header file error: " + e.getMessage());
            }
        }

        //TODO: maybe make only one possible preprocessor available instead of list?
        if(!preprocessors.isEmpty()) //Supports only one preprocessor now..
        {


            return preprocessors.get(preprocessors.size()-1).getColumnNames(serialData);
        }

        result = serialData.split(",");

        return result;
    }

    public void addPreprocessor(PreprocessorAbstract preprocessor)
    {

        preprocessors.add(preprocessor);
    }

    public void setAccumulateMillisBetweenPack(boolean accumulateMillisBetweenPack) {
        this.accumulateMillisBetweenPack = accumulateMillisBetweenPack;
    }
}
