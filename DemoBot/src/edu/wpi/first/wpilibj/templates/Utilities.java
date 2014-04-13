/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import com.sun.squawk.microedition.io.FileConnection;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;
import javax.microedition.io.Connector;

/**
 *
 * @author john
 */
public class Utilities
{

    static boolean debugFileInitialized = false;
    static boolean DEBUG = true;
    static StringBuffer debug = new StringBuffer(256);
    static boolean dontSpamLargeLog = true;
    static int BUFFER_SIZE = 4096;

    public static void saveStringToFile(String fileName, String toSave)
    {
        //WARNING!! THERE WAS A PROBLEM WITH THIS CODE DON'T USE IT.
//        PrintStream out;
//        DataOutputStream theFile;
//        FileConnection fc;
//
//        try
//        {
//            fc = (FileConnection) Connector.open("file:///webserver" + fileName, Connector.READ_WRITE);
//            fc.create();
//            theFile = fc.openDataOutputStream();
//            out = new PrintStream(theFile);
//            out.print(toSave);
//            out.flush();
//            out.close();
//            fc.close();
//        }
//        catch (Exception e)
//        {
//            Utilities.debugLine("Utilities.saveStringToFile(X,X): " + e.toString(), true);
//        }
    }

    public static String stringFromFile(String fileName2)
    {
        int b = 0;
        StringBuffer sb = new StringBuffer(BUFFER_SIZE);
        FileConnection connection = null;
        try
        {
            connection = (FileConnection) Connector.open("file:///webserver" + fileName2, Connector.READ);
            if (!connection.exists())
            {
                connection.close();
            }
            else
            {
                InputStream input = connection.openDataInputStream();
                while (b != -1)
                {
                    //System.out.print((char) b);
                    b = input.read();
                    if (b != -1)
                        sb.append((char) b);
                }
                input.close();
                connection.close();
            }
        }
        catch (Exception error)
        {
            Utilities.debugLine("Utilities.stringFromFile(" + fileName2 + "): " + error.toString(), DEBUG);
        }
        finally
        {
            try
            {
                connection.close();
            }
            catch(Exception e){Utilities.debugLine("Utilities.stringFromFile(" + fileName2 + "): SHIT IS BROKEN!!! FILE CAAN'T CLOSE", DEBUG);}
        }
        Utilities.debugLine("Utilities.stringFromFile(" + fileName2 + "): Finished reading the file!", DEBUG);
        return sb.toString().trim();
    }

    public static String stringReplace(String original, String searchString, String replaceString)
    {
        if (replaceString == null)
            replaceString = "";

        int beginning = -1;
        int end = -1;

        beginning = original.indexOf(searchString);
        end = beginning + searchString.length();
        if (beginning == -1 || end == -1)
            return original;

        String p1 = original.substring(0, beginning);
        String p2 = original.substring(end);

        String toReturn;

        if (!replaceString.equals(""))
            toReturn = p1.concat(replaceString);

        else
            toReturn = p1;

        toReturn = toReturn.concat(p2);
        return toReturn;
    }

    public static String[] splitString(String original, String separator)
    {
        Vector nodes = new Vector();
        // Parse nodes into vector
        int index = original.indexOf(separator);
        while (index >= 0)
        {
            nodes.addElement(original.substring(0, index));
            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
        }
        // Get the last node
        nodes.addElement(original);

        // Create split string array
        String[] result = new String[nodes.size()];
        if (nodes.size() > 0)
        {
            for (int loop = 0; loop < nodes.size(); loop++)
            {
                result[loop] = (String) nodes.elementAt(loop);
                //System.out.println(result[loop]);
            }

        }
        return result;
    }

    public static String[] splitStringOnce(String original, String separator)
    {
        String[] result = new String[2];
        int index = original.indexOf(separator);
        //Utilities.debugLine("Utilities.splitStringOnce(" + original + "," + separator + "): Index = " + index, DEBUG);
        result[0] = original.substring(0, index).trim();
        result[1] = original.substring(index + 1).trim();
        //Utilities.debugLine(result[0], DEBUG);
        //Utilities.debugLine(" " + result[1], DEBUG);
        return result;
    }

    public static void debug(byte[] byteArray, boolean doPrint)
    {
        if (doPrint)
            debug(new String(byteArray), doPrint);
    }

    public static void debugLine(byte[] byteArray, boolean doPrint)
    {
        if (doPrint)
            debugLine(new String(byteArray), doPrint);
    }

    public static void debug(char[] charArray, boolean doPrint)
    {
        if (doPrint)
            debug(new String(charArray), doPrint);
    }

    public static void debugLine(char[] charArray, boolean doPrint)
    {
        if (doPrint)
            debugLine(new String(charArray), doPrint);
    }

    public static void debugLine(String message, boolean doPrint)
    {
        if (doPrint)
            debug(message + "\n", doPrint);
    }

    public static void debug(String message, boolean doPrint)
    {
        if (doPrint)
        {
            if (!dontSpamLargeLog)
                System.out.print(message);
            else if (message.length() <= 50)
                System.out.print(message);
            message = message + "----SB SIZE = " + debug.capacity() + "---";
            debug.append(message);
        }
    }

    public static void saveDebug()
    {
        saveStringToFile("debug.txt", debug.toString());
    }

    public static void clearDebug()
    {
        debug = new StringBuffer(256);
    }

}
