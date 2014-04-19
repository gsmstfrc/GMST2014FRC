/* This is a WebServer that allows us to modify variables stored
 * in the Variables Class using the HTTP protocol and a Web Browser
 * Note: The only browsers tested, are Chrome and Safari.
 *
 * @author John McDonough, Brandon Tuttle
 * Gwinnett School of Mathematics, Science, and Technology
 * GSMST FRC 2014
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author john
 */
public class WebServerClean implements Runnable
{

    static int PORT;
    static boolean DEBUG;
    static int BUFFER_SIZE;

    public boolean running = false;
    public static WebServerClean instance;
    public static Thread serverThread;

    //=-=-=-=-=-=-=-=-=-=-= STATIC METHODS AND CONSTRUCTORS -=-=-=-=-=-=-=-=-=-=-=
    public WebServerClean()
    {
        if (!Variables.exists("w_DebugLog"))
            Variables.setVariable("w_DebugLog", true);

        if (!Variables.exists("w_Port"))
            Variables.setVariable("w_Port", 80);

        if (!Variables.exists("w_NetworkBufferSize"))
            Variables.setVariable("w_NetworkBufferSize", 2048);

        PORT = Variables.getInt("w_Port");
        DEBUG = true;//Variables.getBoolean("w_DebugLog");
        BUFFER_SIZE = Variables.getInt("w_NetworkBufferSize");
    }
    //=-=-=-=-=-=-=-=-=- END STATIC METHODS AND CONSTRUCTORS -=-=-=-=-=-=-=-=-=-=-=

    public void run()
    {
        Utilities.debugLine("WebServer.run(): Runing Web Server", DEBUG);

        running = true;

        ServerSocketConnection ssc;
        try
        {
            ssc = (ServerSocketConnection) Connector.open("socket://:" + PORT);

            do
            {
                Utilities.debugLine("WebServer.run(): Socket Opened, Waiting for Connection...", DEBUG);
                SocketConnection sc = null;
                sc = (SocketConnection) ssc.acceptAndOpen();
                Thread t = new Thread(new ConnectionHandler(sc));
                t.setPriority(Thread.MIN_PRIORITY);
                t.start();
                
//--------------------------------------- DEBUG THREAD NUMBER ---------------------------------
                try
                {
                    System.out.println("Active Threads: " + Thread.activeCount());
                }
                catch (Exception e)
                {
                    System.out.println("There was an eror getting the thread count.");
                }
//--------------------------------------- DEBUG THREAD NUMBER ---------------------------------
                
            }
            while (running);
            ssc.close();
        }

        catch (Exception e)
        {
            Utilities.debugLine(e.toString() + e.getMessage(), true);
        }
    }

    //This class handles threading allowing for multiple connections.
    class ConnectionHandler implements Runnable
    {

        SocketConnection sc;
        InputStream in;
        PrintStream out;
        boolean closed = false;

        //Reading in data.
        public String header = "";
        public String content;
        public String method;
        public String file;
        public Hashtable headerFields;
        //Response data
        String responseHeader = "";
        String document = "";

        Timer t1 = new Timer();
        double lastTime = 0;

        public ConnectionHandler(SocketConnection sc)
        {
            this.sc = sc;
            try
            {
                this.in = sc.openInputStream();
                this.out = new PrintStream(sc.openOutputStream());
            }
            catch (Exception e)
            {
                Utilities.debugLine("ConnectionHandler Constructor: Unable to open streams.", DEBUG);
            }
            Utilities.debugLine("ConnectionHandler Constructor: Connection Made.", DEBUG);
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

            for (int i = 0; i < Variables.sortedKeys.length; i++)
            {
                String key = (String) Variables.sortedKeys[i];
                if (key.startsWith("a_") && key.length() > 0)
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
            try
            {
                for (int i = 0; i < Variables.sortedKeys.length; i++)
                {
                    String key = (String) Variables.sortedKeys[i];
                    if (!key.startsWith("a_") && !key.startsWith("w_") && key.length() > 0)
                        form += "<tr>"
                                + "<td>" + key + ": </td>"
                                + "<td><input type=\"text\" name=\"" + key + "\" value=\"" + variables.get(key) + "\" /></td>\n"
                                + "</tr>";
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            //form += "<input type=\"submit\" />\n </form>";
            return form;
        }

        public String generateSystemForm()
        {

            String form = "";//"<form method=\"POST\" action=\"/\">";
            Hashtable variables = Variables.getTable();
            try
            {
                for (int i = 0; i < Variables.sortedKeys.length; i++)
                {
                    String key = (String) Variables.sortedKeys[i];
                    if (key.startsWith("w_") && key.length() > 0)
                        form += "<tr>"
                                + "<td>" + key + ": </td>"
                                + "<td><input type=\"text\" name=\"" + key + "\" value=\"" + variables.get(key) + "\" /></td>\n"
                                + "</tr>";
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            //form += "<input type=\"submit\" />\n </form>";
            return form;
        }

        public void run()
        {
            //This thread is to allow the header to be constantly updated.
            //UPDATE: USING WHILE TRUE CAUSES HEADERS TO GET MESSED UP. AVOID DOING THIS.
            //------- ALLOW THE CLIENT TO MAKE AN ADDITIONAL CONNECTION, RATHER THATN JUST USING ONE.
 
            //while(true)
            //{
            readRequest();
            //}

        }

        //4th step in making the connection
        public void process()
        {

            if (method.equalsIgnoreCase("POST"))
            {
                parsePost();
            }

            if(headerFields.containsKey("Upgrade") && ((String)headerFields.get("Upgrade")).equalsIgnoreCase("websocket"))
            {
                if(!headerFields.containsKey("Sec-WebSocket-Key"))
                    Utilities.debug("There key Sec-WebSocket-Key does not exist. can not create web socket.", DEBUG);
                try
                {
                    WebSocket ws = new WebSocket(this,sc,in,out,(String)headerFields.get("Sec-WebSocket-Key"));
                }
                catch(Exception e){e.printStackTrace();}    
                return;
            }
            
            sendDocument();

        }

        public void sendDocument()
        {
            try
            {
                Utilities.debugLine("WebServer.sendDocument(): The file is " + file, DEBUG);
                responseHeader += "HTTP/1.1 200 OK\r\n";

                if (file.equalsIgnoreCase("/"))
                {
                    Utilities.debugLine("WebServer.sendDocument(): CASE / The file is " + "/index.html", DEBUG);
                    file = "/index.html";
                    document = Utilities.stringFromFile(file);
                }

                else if (file.equalsIgnoreCase("/debug.txt"))
                {
                    Utilities.debugLine("WebServer.sendDocument(): CASE DEBUG.txt The file is " + "/debug.txt", DEBUG);
                    document = Utilities.stringReplace(Utilities.debug.toString(), "\n", "\n<br>");
                }
                else if (file.equalsIgnoreCase("/cleardebug.txt"))
                {
                    Utilities.clearDebug();
                    Utilities.debugLine("WebServer.sendDocument(): CASE DEBUG.txt The file is " + "/debug.txt", DEBUG);
                    document = Utilities.stringReplace(Utilities.debug.toString(), "\n", "\n<br>");
                }

                else
                {
                    Utilities.debugLine("WebServer.sendDocument(): CASE ELSE The file read is " + file, DEBUG);
                    document = Utilities.stringFromFile(file);
                }

                if (file.endsWith(".html"))
                {
                    document = parseDocument(Utilities.stringFromFile("/header.html") + document);
                }

                if (file.endsWith(".js"))
                {
                    responseHeader += "Cache-Control: public" + "\r\n";
                }

                responseHeader += "Content-Length: " + document.length() + "\r\n\r\n";

                responseHeader += document;
                out.print(responseHeader);
                out.flush();
                //responseHeader = "";
                close();
            }
            catch (Exception e)
            {
                sendError();
            }
        }

        public void close()
        {
            out.flush();
            out.close();
            try
            {
                in.close();
                sc.close();
            }
            catch (Exception e)
            {
                Utilities.debugLine("WebServer.close(): Killing connection.", DEBUG);
            }
        }

        public void sendError()
        {
            responseHeader = "HTTP/1.1 400 Bad Request\r\n\r\n";
            out.print(responseHeader);
            closed = true;
        }

        public String parseDocument(String document)
        {
            document = Utilities.stringReplace(document, "{AUTONOMOUS}", generateAutonomousForm());
            document = Utilities.stringReplace(document, "{TELEOP}", generateTeleopForm());
            document = Utilities.stringReplace(document, "{SYSTEM}", generateSystemForm());
            document = Utilities.stringReplace(document, "{VOLTAGE}", "" + DriverStation.getInstance().getBatteryVoltage());

            return document;
        }

        //5th step in making the connection
        public void parsePost()
        {
            Hashtable table = new Hashtable();
            try
            {

                String[] pairs = Utilities.splitString(content, "&");
                for (int i = 0; i < pairs.length; i++)
                {
                    String[] pair = Utilities.splitString(pairs[i], "=");
                    table.put(pair[0], pair[1]);
                }
                Variables.updateTableWithTable(table);
                Variables.save();
            }
            catch (Exception e)
            {
                Utilities.debugLine("WebServer.parsePost(): ERROR" + content, DEBUG);
            }
            Utilities.debugLine("WebServer.parsePost(): Variables were " + content, DEBUG);
            Utilities.debugLine("WebServer.parsePost(): HASHTABLE = " + table.toString(), DEBUG);

        }

        //3rd step in making the connection
        public void readContent()
        {
            if (headerFields.containsKey("Content-Length") && !headerFields.get("Content-Length").equals("0"))
            {
                Utilities.debugLine("WebServer.readContent(): it did contain Content-Length and it wasn't equal to 0.", DEBUG);
                int contentLength = Integer.parseInt((String) headerFields.get("Content-Length"));
                Utilities.debugLine("WebServer.readContent(): content-Length = " + contentLength, DEBUG);
                byte[] contentBytes = new byte[contentLength];
                try
                {
                    int read = in.read(contentBytes);
                    while (read < contentLength)
                    {
                        read += in.read(contentBytes, read, contentLength - read);
                    }
                    content = new String(contentBytes);
                }
                catch (Exception e)
                {
                    Utilities.debugLine("ConnectionHandler.readContent():"
                            + " there was a problem reading the content of length " + contentLength, DEBUG);
                    sendError();
                }
            }
            else
            {
                Utilities.debugLine("WebServer.readContent(): contains Content-Length =  "
                        + headerFields.containsKey("Content-Length")
                        + "\nContent-Length = " + headerFields.get("Content-Length"), DEBUG);
            }
        }

        //2nd step in making the connection
        public void parseHeader()
        {

            Hashtable table = new Hashtable();
            String[] lines = Utilities.splitString(header, "\r\n"); //Break everything into lines
            String[] line1 = Utilities.splitString(header, " ");    //Break the 1st header line Ex: GET / HTTP/1.1
            method = line1[0].trim();
            file = line1[1].trim();
            Utilities.debugLine("WebServer.parseHeader(): " + lines[0], DEBUG);

            //For the remainder of the headers, parse the requestFields.
            for (int i = 1; i < lines.length - 1; i++)
            {
                String[] tempLine = Utilities.splitStringOnce(lines[i], ":");
                table.put(tempLine[0].trim(), tempLine[1].trim());
            }
            headerFields = table;
        }

        //1st step in making the connection
        public void readRequest()
        {
            t1.start();
            StringBuffer sb = new StringBuffer(BUFFER_SIZE);
            char c;
            int sequentialBreaks = 0;
            while (true)
            {
                try
                {
                    if (in.available() > 0)
                    {
                        c = (char) in.read();

                        //keep track of the number of \r or \n s read in a row.
                        if (c != '\n' && c != '\r')
                            sequentialBreaks = 0;
                        else
                            sequentialBreaks++;

                        //If there is an error, or we read the \r\n\r\n EOF, break the read loop.
                        //We don't want to read too far.
                        if (c == -1 || sequentialBreaks == 4)
                            break;
                        else
                            sb.append(c);
                    }
                }
                catch (Exception e)
                {
                    sendError();
                }
            }
            header += sb.toString().trim();
            Utilities.debugLine("WebServer.readRequest(): Header was \n" + header, DEBUG);

            Utilities.debugLine("WebServer read bytes completed in " + t1.get(), running);
            lastTime = t1.get();

            parseHeader();

            Utilities.debugLine("WebServer parse header completed in " + (t1.get() - lastTime), running);
            lastTime = t1.get();

            readContent();

            Utilities.debugLine("WebServer parseHeader completed in " + (t1.get() - lastTime), running);
            lastTime = t1.get();

            process();

            Utilities.debugLine("WebServer processing and reply completed in " + (t1.get() - lastTime), running);
        }
    }

}
