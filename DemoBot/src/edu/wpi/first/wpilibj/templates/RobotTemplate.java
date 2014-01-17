package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

public class RobotTemplate extends SimpleRobot
{

    //front left is 4
    //front right is 1
    //back left is 3
    //back right is 2
    Joystick joystick;
    Victor frontLeft;
    Victor frontRight;
    Victor backLeft;
    Victor backRight;
    Watchdog watchdog;

    public RobotTemplate()
    {
        joystick = new Joystick(1);

        frontLeft = new Victor(3);
        frontRight = new Victor(9);
        backLeft = new Victor(4);
        backRight = new Victor(2);
        
        watchdog = Watchdog.getInstance();
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl()
    {
        while (true && isOperatorControl() && isEnabled())
        {
            driveControl();
            watchdog.feed();
            Timer.delay(0.005);
        }
    }
    //Customizable values
    public double maxXSpeed = 1;//.1;
    public double maxYSpeed = 1;//.4;
    public double maxTSpeed = .5;//.4;
    double x;
    double y;
    double t;

    //@Override not supported
    public void driveControl()
    {
        x = joystick.getX() * maxXSpeed;
        y = joystick.getY() * maxYSpeed;
        t = joystick.getTwist() * maxTSpeed;

        if(Math.abs(joystick.getX()) < .05)
        {
            x = 0;
        }
        
        if(Math.abs(joystick.getY()) < .05)
        {
            y = 0;
        }
        
        if(Math.abs(joystick.getTwist()) < .05)
        {
            t = 0;
        }
        
        frontRight.set((y - t - x));
        backRight.set((y - t + x));

        backLeft.set((y + t + x));
        frontLeft.set((y + t - x));
    }

    public void 
            
    //@Override not supported
    public void autonomous()
    {
    }

    /**
     * This function is called once each time the robot enters test mode.
     */
    public void test()
    {
    }

    public class SmartDashboardUpdater implements Runnable, ITableListener
    {
        
        Preferences preferences;
        boolean saveDashboardValues = false;
        NetworkTable sd;
        
        public SmartDashboardUpdater()
        {
        }

        public void valueChanged(ITable source, String key, Object value, boolean isNew)
        {
            
            if (key.equalsIgnoreCase("saveDashboardValues"))
            {
                Boolean update;
                update = ((Boolean) value);
                
                if (!update.booleanValue())
                {
                    return;
                }
                
                sd.putBoolean("saveDashboardValues", false);
                retrieveDashboardValues();
                save();
            }
            
        }
        
        public void retrieveDashboardValues()
        {
            //bla bla bla get values.
            maxXSpeed = sd.getNumber("maxXSpeed", maxXSpeed);
            maxYSpeed = sd.getNumber("maxYSpeed", maxYSpeed);
            maxTSpeed = sd.getNumber("maxTSpeed", maxTSpeed);
            save();
        }
        
        public void sendDashboardValues()
        {
            sd.putNumber("maxXSpeed", maxXSpeed);
            sd.putNumber("maxYSpeed", maxYSpeed);
            sd.putNumber("maxTSpeed", maxTSpeed);
        }
        
        public void load()
        {
            maxXSpeed = preferences.getDouble("maxXSpeed", maxXSpeed);
            maxYSpeed = preferences.getDouble("maxYSpeed", maxYSpeed);
            maxTSpeed = preferences.getDouble("maxTSpeed", maxTSpeed);
            
            debugingEnabled = preferences.getBoolean("debugingEnabled", debugingEnabled);
            
            sendDashboardValues();   
        }
        
        public void save()
        {
            preferences.putDouble("maxXSpeed",maxXSpeed);
            preferences.putDouble("maxYSpeed",maxYSpeed);
            preferences.putDouble("maxTSpeed",maxTSpeed);
            
            preferences.putBoolean("debugingEnabled", debugingEnabled);
            
            preferences.save();
        }
        
        //Initilize the SmartDashboard by loading the saved perferences.
        public void init()
        {
            preferences = Preferences.getInstance();
            load();
            sd.putBoolean("saveDashboardValues", true);
        }

        public void run()
        {
            NetworkTable.setTeam(3318);
            sd = NetworkTable.getTable("SmartDashboard");
            sd.addTableListener("saveDashboardValues", this, true); //True is for immedeatly notify.
            init();
            while (true)
            {
                //Do some code that manages updating network table stuff.
            }

        }
    }
      public static boolean debugingEnabled = true;
}
