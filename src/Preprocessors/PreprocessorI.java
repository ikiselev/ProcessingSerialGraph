package Preprocessors;


public interface PreprocessorI {
    float[] processValues(float[] incomingValues, int millisBetweenPack);
    boolean allowShiftData();
}
