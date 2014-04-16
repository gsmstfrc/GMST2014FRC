package edu.wpi.first.wpilibj.templates;

import java.io.InputStream;
import java.io.PrintStream;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author john
 */
public class WebSocket
{

    boolean DEBUG = true;

    SocketConnection s;
    InputStream is;
    PrintStream os;
    String key;
    WebServerClean.ConnectionHandler conHan;

    public WebSocket(WebServerClean.ConnectionHandler conHan, SocketConnection s, InputStream is, PrintStream os, String key)
    {
        this.conHan = conHan;
        this.s = s;
        this.is = is;
        this.os = os;
        this.key = key;
        handshake();
        run();
    }

    public void handshake()
    {
        try
        {
            String toWrite = "HTTP/1.1 101 Switching Protocols \r\n"
                    + "Connection: Upgrade\r\nUpgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: " + SHA1.encode(new String(key.getBytes("UTF8")) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11") + "\r\n"
                    + "\r\n";
            os.println(toWrite);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public String readNextData()
    {
        try
        {
            byte[] header = new byte[2];
            is.read(header);
           
        }
        catch(Exception e){System.out.println("There was an error reading a frame.");}
        
        return "";
    }

    public void run()
    {
        try
        {
            while (true)
            {
                
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
