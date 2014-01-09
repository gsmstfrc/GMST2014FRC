package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

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

        frontLeft = new Victor(4);
        frontRight = new Victor(1);
        backLeft = new Victor(3);
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
    double maxXSpeed = .1;
    double maxYSpeed = .4;
    double maxTSpeed = .4;
    double x;
    double y;
    double t;

    //@Override not supported
    public void driveControl()
    {
        x = joystick.getX() * maxXSpeed;
        y = joystick.getY() * maxYSpeed;
        t = joystick.getTwist() * maxTSpeed;

        frontRight.set(y + x);
        backRight.set(y + x);

        backLeft.set(-y + x);
        frontLeft.set(-y + x);
    }

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

    public class SmartDashboardUpdater implements Runnable
    {

        boolean saveDashboardValues = false;
        public SmartDashboardUpdater()
        {
        }

        public void run()
        {
            NetworkTable.setTeam(3318);
            NetworkTable sd = NetworkTable.getTable("SmartDashboard");
            
            
            while (true)
            {
                if (!(t.get() > 5)){} //Empty line.
                else
                {
                    maxXSpeed = sd.getNumber("maxXSpeed", .1);
                    maxYSpeed = sd.getNumber("maxYSpeed", .4);
                    maxTSpeed = sd.getNumber("maxTSpeed", .4);
                    
                    saveDashboardValues = SmartDashboard.getBoolean("saveDashboardValues",false);
                }
                if(saveDashboardValues)
                {
                    
                }
            }

        }
    }
}
