package edu.wpi.first.wpilibj.templates;

import java.io.InputStream;
import java.io.OutputStream;
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
                    + "Sec-WebSocket-Accept: " + SHA1.encode(key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11") + "\r\n"
                    + "\r\n";
            os.write(toWrite);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String readNextData()
    {
        String data = "";
        try
        {
            boolean isFinished = false;
            while (!isFinished)
            {
                //SETUP
                boolean extended = false;
                int payloadLength = 0;
                byte[] b = new byte[2];
                while (is.available() == 0);
                is.read(b);
                int maskp1 = (b[0] < 0 ? 256 + b[0] : b[0]); //convert to unsigned
                if (maskp1 > 128)                            //if it's bigger than 128, then the first byte MUST BE 1. That means, the first bit is 1.
                    isFinished = true;
                int maskp2 = (b[1] < 0 ? 256 + b[1] : b[1]); //convert to unsigned.

                if ((maskp2 - 128) == 126) //we subtract 128, because from client to server, it's always encrypted.
                {
                    byte[] b2 = new byte[2]; //16 bit integer.
                    is.read(b2);
                    payloadLength = ((0xFF & b2[0]) << 8) | (0xFF & b2[1]);
                }
                else if ((maskp2 - 128) <= 125)
                {
                    payloadLength = maskp2 - 128;
                }
                else
                {
                    byte[] b2 = new byte[8];
                    is.read(b2);
                    long value = 0;
                    for (int i = 0; i < b2.length; i++)
                    {
                        value = (value << 8) + (b2[i] & 0xff);
                    }
                    payloadLength = (int) value;//THIS WILL ONLY WORK IF THE LENGTH OF THE PAYLOAD IS LESS THAN 2147483647 BYTES!!!!! OTHERWISE OVERFLOW!
                }
                byte[] key = new byte[4];
                is.read(key);

                //SETUP
                byte[] payload = new byte[payloadLength];
                is.read(payload);

                for (int i = 0; i < payload.length; i++)
                {
                    payload[i] = (byte) (payload[i] ^ key[i % 4]);
                }
                data = new String(payload);
                //System.out.println("" + data);
            }
        }
        catch (Exception e)
        {
            System.out.println("There was an error reading a frame.");
        }

        return data;
    }

    public void writeData(String toWrite)
    {
        int payloadLength = toWrite.length();
        int firstByte = 129; //we are going to assume that the entire message fits into a single frame.
        byte b1 = (byte) firstByte;

        int secondByte;
        int extendedByte;
        if (payloadLength <= 125) //size in bytes.
        {
            secondByte = payloadLength;
        }

        else if (payloadLength <= 65535) //size in bytes. just under 64 kilobyte max size.
        {

        }
        else                            //
        {

        }

    }

    public void run()
    {
        try
        {
            while (true)
            {
                String information = readNextData();
                if (information.equalsIgnoreCase("true"))
                {
                    RobotTemplate.GoalDetected = true;
                }
                if (information.equalsIgnoreCase("false"))
                {
                    RobotTemplate.GoalDetected = false;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
