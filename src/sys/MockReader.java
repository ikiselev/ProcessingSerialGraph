package sys;


import processing.core.PApplet;

import java.lang.reflect.Method;

abstract public class MockReader implements Runnable
{
    PApplet parent;
    String filename;

    private final Thread t;
    Method serialEventMethod;



    public MockReader(PApplet parent, String filename)  {
        this.parent = parent;
        this.filename = filename;



        try {
            serialEventMethod = parent.getClass().getMethod("fileEvent", new Class[] { String.class });
        } catch (Exception e) {
            // no such method, or an error.. which is fine, just ignore
        }

        t = new Thread(this, "File Data Thread");
    }

    public void start() {
        t.start();
    }

    public String getFilename() {
        return filename;
    }
}
