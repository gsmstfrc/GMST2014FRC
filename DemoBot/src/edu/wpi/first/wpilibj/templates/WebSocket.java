package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import java.io.IOException;
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

    boolean connected = true;

    SocketConnection s;
    InputStream is;
    OutputStream os;
    String key;
    WebServer.ConnectionHandler conHan;

    Timer writeTimer = new Timer();
    double lastWriteTime = 0;
    double updateTime = .5;

    public WebSocket(WebServer.ConnectionHandler conHan, SocketConnection s, InputStream is, PrintStream os, String key)
    {
        this.conHan = conHan;
        this.s = s;
        this.is = is;
        this.os = os;
        this.key = key;
        handshake();
        run();
        updateTime = Variables.getDouble("w_websocketUpdateInterval") == 0 ? .5 : Variables.getDouble("w_websocketUpdateInterval");
    }

    public void handshake()
    {
        try
        {
            String toWrite = "HTTP/1.1 101 Switching Protocols \r\n"
                    + "Connection: Upgrade\r\nUpgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: " + SHA1.encode(key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11") + "\r\n"
                    + "\r\n";
            os.write(toWrite.getBytes());
            connected = true;
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
                //while (is.available() == 0);
                b[0] = (byte) is.read();
                if (b[0] == -1)
                {
                    connected = false;
                    return null;
                }
                b[1] = (byte) is.read();
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
                ////System.out.println("" + data);
            }
        }
        catch (IOException e)
        {
            System.out.println("There was an error reading a frame.");
            connected = false;
        }

        return data;
    }

    public void writeData(String toWrite)
    {
        try
        {
            int payloadLength = toWrite.length();
            //byte b1 = (byte) ((byte) 0x81);//we are going to assume that the entire message fits into a single frame.
            byte b1 = (byte) 0x81;
            //System.out.println(Integer.toBinaryString(b1));
            byte b2;
            int secondByte = 0;
            byte[] extendedLength = null;

            if (payloadLength <= 125) //size in bytes.
            {
                secondByte = payloadLength;
                extendedLength = null;
                //System.out.println("length is " + payloadLength);
            }

            else if (payloadLength <= 65535) //size in bytes. just under 64 kilobyte max size.
            {
                //System.out.println("length is " + payloadLength);
                secondByte = 126;
                short data = (short) payloadLength;
                extendedLength = new byte[]
                {
                    (byte) ((data >> 8) & 0xFF), (byte) (data & 0xFF)
                };
            }
            else                            //
            {
                //System.out.println("length is " + payloadLength);
                secondByte = 127;
                long data = (long) payloadLength;
                extendedLength = new byte[]
                {
                    (byte) ((data >> 48) & 0xFF),
                    (byte) ((data >> 40) & 0xFF),
                    (byte) ((data >> 32) & 0xFF),
                    (byte) ((data >> 24) & 0xFF),
                    (byte) ((data >> 16) & 0xFF),
                    (byte) ((data >> 8) & 0xFF),
                    (byte) (data & 0xFF)
                };
            }
            b2 = (byte) ((byte) secondByte);
            //System.out.println("byte 2 = " + Integer.toBinaryString(b2));
            //System.out.println("byte 1 = " + Integer.toBinaryString(b1));
            byte[] arr;
            if (extendedLength != null)
            {
                arr = new byte[2 + extendedLength.length + toWrite.length()];
                arr[0] = (byte) (b1);
                arr[1] = (byte) (b2);
                if (extendedLength.length == 2)
                {
                    arr[2] = (byte) extendedLength[0];
                    arr[3] = (byte) extendedLength[1];
                    byte[] toWriteBytes = toWrite.getBytes();
                    for (int i = 0; i < toWriteBytes.length; i++)
                    {
                        arr[i + 4] = toWriteBytes[i];
                        //System.out.println(Integer.toBinaryString(arr[i + 4] & 0xFF));
                    }
                }
                else
                {
                    arr[2] = (byte) extendedLength[0];
                    arr[3] = (byte) extendedLength[1];
                    arr[4] = (byte) extendedLength[2];
                    arr[5] = (byte) extendedLength[3];
                    byte[] toWriteBytes = toWrite.getBytes();
                    for (int i = 6; i < toWriteBytes.length; i++)
                    {
                        arr[i + 4] = toWriteBytes[i];
                        //System.out.println(Integer.toBinaryString(arr[i + 4] & 0xFF));
                    }
                }
            }
            else
            {
                arr = new byte[2 + toWrite.length()];
                arr[0] = (byte) (b1);
                arr[1] = (byte) (b2);
                byte[] toWriteBytes = toWrite.getBytes();
                //System.out.println();
                //System.out.println(Integer.toBinaryString(arr[0] & 0xFF));
                //System.out.println(Integer.toBinaryString(arr[1] & 0xFF));
                //System.out.println();
                for (int i = 0; i < toWriteBytes.length; i++)
                {
                    arr[i + 2] = toWriteBytes[i];
                    //System.out.println(Integer.toBinaryString(arr[i + 2] & 0xFF));
                }
            }
            os.write(arr);
            //os.write(new byte[]{(byte)0b10000001,(byte)0b00000001,(byte)0b00000001});
            //System.out.println("writing out!!");
            os.flush();
            //System.out.println();
            //os.write((byte) (b1 & 0xFF));
            //os.write((byte) (b2 & 0xFF));
            //if (extendedLength != null)
            //{
            //    //System.out.println("writing extended bytes.");
            //    os.write(extendedLength);
            //}
            ////System.out.println(Integer.toBinaryString(b1 & 0xFF) + " " + Integer.toBinaryString(b2 & 0xFF));
            //os.write(toWrite.getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("bullshiznit happened that with writing thread yo.");
            connected = false;
        }
    }

    public void run()
    {
        try
        {
            //STATUS THREAD
            if (conHan.file.equalsIgnoreCase("/websockets/status"))
            {
                Thread writeThread = new Thread()
                {
                    public void run()
                    {
                        writeTimer.start();
                        Thread waitForEnd = new Thread()
                        {
                            public void run()
                            {
                                try
                                {
                                    while (is.read() != -1);
                                    connected = false;
                                }
                                catch (Exception e)
                                {
                                    connected = false;
                                }
                            }
                        };
                        waitForEnd.start();
                        while (connected)
                            processStatusWrite();
                        System.out.println("Closing write Thread");
                    }
                };
                writeThread.start();
            }
            //STATUS THREAD
            //VISION THREAD
            if (conHan.file.equalsIgnoreCase("/websockets/vision"))
            {

                Thread readThread = new Thread()
                {
                    public void run()
                    {
                        while (connected)
                            processVisionRead();
                        System.out.println("Closing Read Thread");
                    }
                };
                readThread.start();
            }
            //VISION THREAD
            //DEBUG THREAD
            if (conHan.file.equalsIgnoreCase("/websockets/debug"))
            {

                Thread debugThread = new Thread()
                {
                    public void run()
                    {
                        while (connected)
                            processDebugWrite();
                        System.out.println("Closing Read Thread");
                    }
                };
                debugThread.start();
            }
            //DEBUG THREAD
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void processVisionRead()
    {
        String message = readNextData();
        if (message != null)
        {
            if (message.equalsIgnoreCase("true"))
                RobotTemplate.GoalDetected = true;
            if (message.equalsIgnoreCase("false"))
                RobotTemplate.GoalDetected = false;
        }
    }

    public void processStatusWrite()
    {
        if (writeTimer.get() > lastWriteTime + updateTime)
        {
            lastWriteTime = writeTimer.get();
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            sb.append("\"frontLeftMotor\": ").append(RobotTemplate.frontLeft.get());
            sb.append(",\"frontRightMotor\": ").append(RobotTemplate.frontRight.get());
            sb.append(",\"backLeftMotor\": ").append(RobotTemplate.backLeft.get());
            sb.append(",\"backRightMotor\": ").append(RobotTemplate.backRight.get());
            sb.append(",\"intake\": ").append(RobotTemplate.intake2.get());
            sb.append(",\"tilt\": ").append(RobotTemplate.tilted);
            sb.append(",\"leftSolenoid\": ").append(RobotTemplate.sol3.get().value);
            sb.append(",\"middleSolenoid\": ").append(RobotTemplate.sol2.get().value);
            sb.append(",\"rightSolenoid\": ").append(RobotTemplate.sol1.get().value);
            sb.append(",\"voltage\": ").append(DriverStation.getInstance().getBatteryVoltage());
            sb.append(",\"threadCount\": ").append(Thread.activeCount());
            sb.append(",\"memory\": ").append(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            sb.append(",\"autonomousSetting\": ").append(RobotTemplate.autonomousSetting);
            sb.append(",\"outtakeSpeed\": ").append(RobotTemplate.outtakeSpeed);
            sb.append(",\"driveReversed\": ").append(RobotTemplate.multiplier);
            sb.append(",\"time\": ").append(DriverStation.getInstance().getMatchTime());
            sb.append(",\"alliance\": \"").append(DriverStation.getInstance().getAlliance().name).append("\"");
            sb.append(",\"position\": \"").append(DriverStation.getInstance().getLocation()).append("\"");
            sb.append(",\"compressor\": \"").append(RobotTemplate.comp.enabled()).append("\"");
            sb.append("}");
            writeData(sb.toString());
        }
    }

    public void processDebugWrite()
    {
        if (writeTimer.get() > lastWriteTime + updateTime)
        {
            lastWriteTime = writeTimer.get();
            StringBuffer sb = new StringBuffer();
            writeData(sb.toString());
        }
    }
}
