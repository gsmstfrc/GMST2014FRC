/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import com.sun.squawk.microedition.io.FileConnection;
import edu.wpi.first.wpilibj.DriverStation;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author john
 */
public class WebServer implements Runnable
{

    public void sendMainPage(InputStream in, OutputStreamWriter out)
    {
        try
        {
            sendPage(in, out, "index.html");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String readFile(String fileName)
    {
        String s = "";
        //int b;
        //boolean test = true;
        int bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];
        try
        {
            System.out.println("Trying to read in file " + fileName);
            FileConnection connection = (FileConnection) Connector.open("file:///webserver" + fileName, Connector.READ);
            InputStream input = connection.openInputStream();
            //b = input.read();
            System.out.println(input.available());
            int read = 0;
            while ((read = input.read(buffer, 0, bufferSize)) != -1)
            {
                s = s.concat(new String(buffer));
                buffer = new byte[bufferSize];
            }
            //System.out.println("String representation of the file should be " + s);
        }
        catch (Exception error)
        {
            //test = false;
            System.out.println(error.toString());
        }
        finally
        {
            return s;
        }
    }

    public void sendPage(InputStream in, OutputStreamWriter out, String filePath)
    {
        try
        {
            out.write("HTTP/1.1 200 OK\r\n\r\n");
            //out.write("filePath = " + filePath);
            String toBeReplaced = readFile(filePath);

            if (!filePath.endsWith(".html"))
            {
                String replaced = replace(toBeReplaced, "{AUTONOMOUS}", generateAutonomousForm());
                replaced = replace(replaced, "{TELEOP}", generateTeleopForm());
                replaced = replace(replaced, "{VOLTAGE}", "" + DriverStation.getInstance().getBatteryVoltage());
                out.write(replaced);
            }
            else
            {
                out.write(toBeReplaced);
            }
            System.out.println("written out.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String generateAutonomousForm()
    {

        String form = "";//"<form method=\"POST\" action=\"/\">";
        Hashtable variables = Variables.getTable();
        if (variables.get("testMe") == null)
            variables.put("testMe", "Test1");
        if (variables.get("a_doShit") == null)
            variables.put("a_doShit", "10");
        if (variables.get("finalTest") == null)
            variables.put("finalTest", "asdf");

        Enumeration keys = variables.keys();

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            if (key.startsWith("a_"))
                form += "<tr>"
                        + "<td>" + key + ": </td>"
                        + "<td><input type=\"text\" name=\"" + key + "\" value=\"" + variables.get(key) + "\" /></td>\n"
                        + "</tr>";
        }
        //form += "<input type=\"submit\" />\n </form>";
        return form;
    }

    public String generateTeleopForm()
    {

        String form = "";//"<form method=\"POST\" action=\"/\">";
        Hashtable variables = Variables.getTable();

        Enumeration keys = variables.keys();

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            if (!key.startsWith("a_"))
                form += "<tr>"
                        + "<td>" + key + ": </td>"
                        + "<td><input type=\"text\" name=\"" + key + "\" value=\"" + variables.get(key) + "\" /></td>\n"
                        + "</tr>";
        }
        //form += "<input type=\"submit\" />\n </form>";
        return form;
    }

    final int bufferSize = 1024;

    public void readRequest(InputStream in, OutputStreamWriter out)
    {
        try
        {
            //InputStreamReader in = new InputStreamReader(sc.openInputStream());
            String line = "";

            char c;
            int toRead = -1;
            System.out.println("requestWasMade!");
            //header is done when \r\n\r\n is read.
            while (true)
            {
                //BROKEN CODE!!! IF IT HANGS, IT'S BECAUSE OF THIS!!!
                //IF IT'S FINISHED READING!!
                if (in.available() <= 0 && line.indexOf("\r\n\r\n") > -1)
                {
                    if (line.substring(0, "GET".length()).equalsIgnoreCase("GET"))
                    {
                        int startSubstring = line.indexOf("GET ") + "GET ".length();
                        int endSubstring = line.indexOf("HTTP", startSubstring);
                        String filePath = line.substring(startSubstring, endSubstring);
                        if (filePath.trim().equalsIgnoreCase("/"))
                            sendMainPage(in, out);
                        else
                            sendPage(in, out, filePath);
                    }
                    if (!line.substring(0, "POST".length()).equalsIgnoreCase("POST"))
                    {
                        break;
                    }
                    int startSubstring = line.indexOf("Content-Length: ") + "Content-Length: ".length();
                    int endSubstring = line.indexOf("\r", startSubstring);
                    System.out.println("Start at " + startSubstring + " and end at " + endSubstring + " and grabbed " + line.substring(startSubstring, endSubstring));
                    toRead = Integer.parseInt(line.substring(startSubstring, endSubstring));
                    String toParse = line.substring(line.indexOf("\r\n\r\n") + "\r\n\r\n".length());
                    System.out.println(toParse);
                    parseContent(toParse);
                    Variables.save();
                    //sendMainPage(in, out);

                    //THEORETICAL CODE THIS MAY NOT WORK!!!
                    if (line.indexOf("POST ") == -1)
                        sendMainPage(in, out);
                    int startSubstring2 = line.indexOf("POST ") + "POST ".length();
                    int endSubstring2 = line.indexOf("HTTP", startSubstring2);
                    String filePath = line.substring(startSubstring2, endSubstring2);
                    System.out.println("wants to send out file in post " + filePath);
                    if (filePath.trim().equalsIgnoreCase("/"))
                        sendMainPage(in, out);
                    else
                        sendPage(in, out, filePath);
                    System.out.println("breaking the loop");
                    break;
                }
                byte[] buffer = new byte[bufferSize];
                if (in.available() > 0)
                {
                    int read;
                    if (in.available() > bufferSize)
                    {
                        read = in.read(buffer, 0, bufferSize);
                    }
                    else
                    {
                        buffer = new byte[in.available()];
                        read = in.read(buffer, 0, buffer.length);
                    }
                    if (read == -1)
                    {
                        System.out.println("error/done");
                    }
                    else
                    {
                        line = line.concat(new String(buffer));
                        System.out.print(new String(buffer) + "!! buffer is " + read + " !!");
                    }
                }
                else
                {
                    //System.out.write("reader not ready!!!");
                }
            }
            //System.out.println("IT BROKE THE LOOP!!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void parseContent(String string)
    {
        System.out.println(string);
        String[] parametersToBeSplitAgain = split(string, "&");
        Hashtable table = new Hashtable();
        for (int i = 0; i < parametersToBeSplitAgain.length; i++)
        {
            String[] split = split(parametersToBeSplitAgain[i], "=");
            table.put(split[0], split[1]);
        }
        System.out.println(table);
        Variables.setTable(table);
    }

    public String[] split(String original, String separator)
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
                System.out.println(result[loop]);
            }

        }
        return result;
    }

    public String replace(String original, String searchString, String replaceString)
    {
        int beginning = -1;
        int end = -1;

        beginning = original.indexOf(searchString);
        end = beginning + searchString.length();
        if (beginning == -1 || end == -1)
            return original;

        String p1 = original.substring(0, beginning);
        String p2 = original.substring(end);
        return p1 + replaceString + p2;
    }

    public void run()
    {
        System.out.println("Runing Web Server");
        ServerSocketConnection ssc;

        try
        {
            ssc = (ServerSocketConnection) Connector.open("socket://:80");
            SocketConnection sc = null;
            while (true)
            {
                sc = (SocketConnection) ssc.acceptAndOpen();
                Thread t = new ThreadHandler(sc);
                t.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class ThreadHandler extends Thread
    {

        SocketConnection sc;

        public ThreadHandler(SocketConnection sc)
        {
            this.sc = sc;
        }

        public void run()
        {
            try
            {
                InputStream in = (sc.openInputStream());
                OutputStreamWriter out = new OutputStreamWriter(sc.openOutputStream());
                readRequest(in, out);
                //sendHTML(in,out);
                out.close();
                in.close();
                sc.close();
            }
            catch (Exception e)
            {
                System.out.println("exception");
            }
        }
    }

}
