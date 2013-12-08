package sys;

import java.io.*;
import java.util.Properties;

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
            InputStream stream = new FileInputStream(System.getProperty("user.dir") + "/app.cfg");
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            appSettings.load(reader);
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

    public String getProperty(String key)
    {
        return appSettings.getProperty(key);
    }

    public boolean getClassPropertyBoolean(String key)
    {
        String data = appSettings.getProperty(getClassKey(key));
        return (data != null && data.equals("true"));
    }

    public String getClassProperty(String key) {
        return appSettings.getProperty(getClassKey(key));
    }

    public String[] getClassProperties(String key) {

        return appSettings.getProperty(getClassKey(key)).split(",");
    }

    private String getClassKey(String key)
    {
        return className + "." + key;
    }

}
