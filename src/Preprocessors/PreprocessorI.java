package Preprocessors;


public interface PreprocessorI {
    float[] processValues(float[] incomingValues);
    boolean allowShiftData();
}
