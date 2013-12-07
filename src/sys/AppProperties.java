package sys;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AppProperties
{
    public static String PREPROCESSORS = "preprocessors";
    public static String ACCUMULATE_MILLIS_BETWEEN_PACK = "accumulateMillisBetweenPack";

    Properties appSettings;
    String className;

    public AppProperties(String className) {
        this.className = className;

        try
        {
            appSettings = new java.util.Properties();
            appSettings.load(new FileInputStream(System.getProperty("user.dir") + "/app.cfg"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Config file was not found: " + e.getMessage());
        }
        catch (IOException e)
        {
            System.out.println("Config file IOException: " + e.getMessage());
        }
    }

    public boolean getClassPropertyBoolean(String key)
    {
        String data = appSettings.getProperty(getClassKey(key));
        return data.equals("true");
    }

    public String getClassProperty(String key) {
        return appSettings.getProperty(getClassKey(key));
    }

    public String[] getClassProperties(String key) {

        return appSettings.getProperty(getClassKey(key)).split(",");
    }

    protected String getClassKey(String key)
    {
        return className + "." + key;
    }

}
