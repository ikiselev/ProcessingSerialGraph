package sys;

import processing.core.PApplet;

import java.io.*;
import java.lang.reflect.Method;

public class CardReader extends MockReader
{
    String filename;
    Method serialEventMethod;

    String UNIQUE_DELIMITER = "\\$\\$";
    String END_DELIMITER = "\n";
    String COLUMNS_HEADER = "Columns:";


    int SECTOR_SIZE = 512;
    int MAX_LINE_LENGTH = 1024;
    int startBlock = 1;

    int uniqueNumber;
    boolean uniqueNumberInited = false;

    String fileContents = "";

    private final Thread t;



    public CardReader(PApplet parent, String filename)
    {
        super(parent, filename);

        try
        {
            File diskRoot = new File(this.getFilename());
            RandomAccessFile diskAccess = new RandomAccessFile (diskRoot, "r");
            byte[] content = new byte[SECTOR_SIZE];


            String bufString = "";
            boolean validData = true;

            while(validData)
            {
                diskAccess.seek(SECTOR_SIZE * startBlock);
                diskAccess.readFully(content);

                bufString += new String(content, 0, SECTOR_SIZE);
                if(bufString.length() > MAX_LINE_LENGTH)
                {
                    /**
                     * Reached max bound
                     */
                    break;
                }
                String[] messages = bufString.split(END_DELIMITER);
                boolean lastMessageInBufferIsFull = bufString.endsWith(END_DELIMITER);

                for(int i=0; i < messages.length; i++)
                {
                    String message = messages[i];


                    if(i == messages.length - 1 && !lastMessageInBufferIsFull)
                    {
                        /**
                         * If last message is not fully in block
                         */

                        bufString = message;
                        startBlock++;
                    }
                    else
                    {
                        String[] dataStruct = message.split(UNIQUE_DELIMITER);
                        if(dataStruct.length == 2)
                        {
                            if(!uniqueNumberInited)
                            {
                                uniqueNumberInited = true;
                                uniqueNumber = Integer.parseInt(dataStruct[0]);
                            }
                            else
                            {
                                uniqueNumber++;
                                if(uniqueNumber != Integer.parseInt(dataStruct[0]))
                                {
                                    //TODO: Error!
                                    System.out.println("END?!");
                                    validData = false;
                                    break;
                                }
                            }

                            fileContents += dataStruct[1] + END_DELIMITER;
                            bufString = "";
                        }
                    }



                }
            }



        } catch ( IOException e)
        {
            System.out.println("File read error: " + e.getMessage());
        }

        fileContents = COLUMNS_HEADER + fileContents;

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


    public void run() {
        boolean deltaMillisCalc = false;
        int lastMillis = 0;

        String[] lines = fileContents.split(END_DELIMITER);

        for(String line : lines) {
            if (serialEventMethod != null) {
                try {
                    //TODO: Refactor dataProcessor, move method.
                    //TODO: using void processData from dataProcessor
                    String[] packMillis = line.split("\\|");
                    if(packMillis.length == 2)
                    {
                        int arduinoMillis = Integer.parseInt(packMillis[packMillis.length - 1]);

                        if(!deltaMillisCalc)
                        {
                            lastMillis = arduinoMillis;
                        }
                        int diff = arduinoMillis - lastMillis;

                        //Delay to view graph partially
                        parent.delay(diff);

                        lastMillis = arduinoMillis;
                        deltaMillisCalc = true;
                    }


                    serialEventMethod.invoke(parent, new Object[]{line});
                } catch (Exception e) {
                    String msg = "error, disabling fileEvent() for " + filename;
                    System.err.println(msg);
                    e.printStackTrace();
                    serialEventMethod = null;
                }
            }
        }

    }
}
