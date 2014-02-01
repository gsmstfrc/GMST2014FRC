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

    // Test arm Controls
    Victor intakeVictor;

    public RobotTemplate()
    {
        joystick = new Joystick(1);

        frontLeft = new Victor(3);
        frontRight = new Victor(9);
        backLeft = new Victor(4);
        backRight = new Victor(2);

        watchdog = Watchdog.getInstance();

        intakeVictor = new Victor(8);

        Thread updater = new Thread(new SmartDashboardUpdater());
        updater.start();
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
    public double maxXSpeed = .5;//.1; old was 1
    public double maxYSpeed = .5;//.4; old was 1
    public double maxTSpeed = .4;//.4; wold was .5

    double x;
    double y;
    double t;

    Double tx = new Double(0);
    Double ty = new Double(0);
    Double tt = new Double(0);

    double accelerationValue = .05;

    public static double sign(double value)
    {
        return (value >= 0) ? 1 : -1;
    }
    
    //@Override not supported
    public void driveControl()
    {

//        x = joystick.getX() * maxXSpeed;
//        y = joystick.getY() * maxYSpeed;
//        t = joystick.getTwist() * maxTSpeed;
        if (joystick.getRawButton(6))
        {
            x = joystick.getRawAxis(1) * 1;
            y = joystick.getRawAxis(2) * 1;
            t = joystick.getRawAxis(3) * .5;
        }
        else
        {
            x = joystick.getRawAxis(1) * maxXSpeed;
            y = joystick.getRawAxis(2) * maxYSpeed;
            t = joystick.getRawAxis(3) * maxTSpeed;
        }

        if (Math.abs(joystick.getX()) < .2)
        {
            x = 0;
        }

        if (Math.abs(joystick.getY()) < .2)
        {
            y = 0;
        }

        if (Math.abs(joystick.getTwist()) < .2)
        {
            t = 0;
        }

        double targetFrontRight = -(y + t + x);
        double targetBackRight = -(y + t - x);
        double targetFrontLeft = -(y - t - x);
        double targetBackLeft = -(y - t + x);

        if(sign(frontRight.get()) != sign(targetFrontRight))
            frontRight.set(0);
        if(sign(backRight.get()) != sign(targetBackRight))
            backRight.set(0);
        if(sign(frontLeft.get()) != sign(targetFrontLeft))
            frontLeft.set(0);
        if(sign(backLeft.get()) != sign(targetBackLeft))
            backLeft.set(0);
        
        if (Math.abs(frontRight.get()) < Math.abs(targetFrontRight))
            frontRight.set(frontRight.get() + sign(targetFrontRight) * accelerationValue);
        else
            frontRight.set(targetFrontRight);

        if (Math.abs(backRight.get()) < Math.abs(targetFrontRight))
            backRight.set(backRight.get() + sign(targetFrontRight) * accelerationValue);
        else
            backRight.set(targetBackRight);

        if (Math.abs(frontLeft.get()) < Math.abs(targetFrontLeft))
            frontLeft.set(frontLeft.get() + sign(targetFrontLeft) * accelerationValue);
        else
            frontLeft.set(targetFrontLeft);

        if (Math.abs(backLeft.get()) < Math.abs(targetBackLeft))
            backLeft.set(backLeft.get() + sign(targetBackLeft) * accelerationValue);
        else
            backLeft.set(targetBackLeft);

        boolean armUp = joystick.getRawButton(5);
        boolean armDown = joystick.getRawButton(3);
        double power = joystick.getThrottle() * 0.5 + .5;

        if (armUp)
        {
            intakeVictor.set(power);
        }
        else if (armDown)
        {
            intakeVictor.set(-power);
        }
        else
        {
            intakeVictor.set(0.0);
        }

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
            preferences.putDouble("maxXSpeed", maxXSpeed);
            preferences.putDouble("maxYSpeed", maxYSpeed);
            preferences.putDouble("maxTSpeed", maxTSpeed);

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
            try
            {
                NetworkTable.setTeam(3318);
            }
            catch (Exception e)
            {
                System.out.println("NetworkTables team already set");
            }
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
