public class GraphData
{
    public boolean columnNamesInited = false;

    String[] COLUMN_NAMES;
    int[][] COLUMN_DATA;

    int[] MILLIS_BETWEEN_PACK;

    //TODO: This is not good very good that GraphData knows about these values
    int lineSeparatorEvery = 1000; //1sec
    int timingOffset = 0;


    int[] MIN_VALUES;
    int[] MAX_VALUES;
}
