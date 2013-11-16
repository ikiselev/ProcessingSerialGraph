package Preprocessors;


public interface PreprocessorI {
    int[] processValues(int[] incomingValues);
    boolean allowShiftData();
}
