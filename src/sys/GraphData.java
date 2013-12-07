package sys;

public class GraphData
{
    public boolean columnNamesInited = false;

    public String[] COLUMN_NAMES;
    public float[][] COLUMN_DATA;

    /**
     * How many array values in COLUMN_DATA is not empty.
     */
    int valuesFilled = 0;
    int elapsedTime = 0;

    int[] MILLIS_BETWEEN_PACK;

    //TODO: This is not good very good that GraphData knows about these values
    int lineSeparatorEvery = 1000; //1sec
    int timingOffset = 0;


    int[] MIN_VALUES;
    int[] MAX_VALUES;
}
