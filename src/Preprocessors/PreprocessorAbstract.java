package Preprocessors;

abstract public class PreprocessorAbstract implements PreprocessorI
{


    public String[] getColumnNames(String serialData)
    {
        return serialData.split(",");
    }
}
