package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStation;

public class RobotTemplate extends SimpleRobot
{

    //front left is 4
    //front right is 1
    //back left is 3
    //back right is 2
    Joystick joystick;
    public static Talon frontLeft;
    public static Talon frontRight;
    public static Talon backLeft;
    public static Talon backRight;

    public static DoubleSolenoid sol1;
    public static DoubleSolenoid sol2;
    public static DoubleSolenoid sol3;

    public static DoubleSolenoid tilt;

    public static Compressor comp;

    Watchdog watchdog;

    AnalogChannel ultrasonic;

    int[] average = new int[5];

    // Test arm Controls
    public static Talon intake;
    public static Talon intake2;

    boolean DEBUG = false;

    public static boolean GoalDetected = false;

    public RobotTemplate()
    {
        try
        {
            joystick = new Joystick(1);

            frontLeft = new Talon(4);
            frontRight = new Talon(1);
            backLeft = new Talon(3);
            backRight = new Talon(2);

            sol1 = new DoubleSolenoid(1, 2); //Right
            sol2 = new DoubleSolenoid(3, 4); //Middle
            sol3 = new DoubleSolenoid(5, 6); //Left
            tilt = new DoubleSolenoid(7, 8);

            comp = new Compressor(1, 1);
            comp.start();

            watchdog = Watchdog.getInstance();
            ultrasonic = new AnalogChannel(1);

            intake = new Talon(5);
            intake2 = new Talon(6);

            tilt.set(DoubleSolenoid.Value.kForward);
            DriverStation.getInstance().getEnhancedIO().setLED(4, true);

            //Thread updater = new Thread(new SmartDashboardUpdater());
            //updater.start();
            Thread piUpdater = new Thread(new WebServerClean());
            piUpdater.start();
            Variables.load();
            Thread switchpanel = new Thread()
            {
                public void run()
                {
                    while (true)
                    {
                        switchpanel();
                        try
                        {
                            Thread.sleep(20);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            };
            switchpanel.start();
//            Thread averager = new Thread()
//            {
//                public void run()
//                {
//                    while (true)
//                    {
//                        Timer.delay(.1);
//                        for (int i = 0; i < average.length - 1; i++)
//                        {
//                            average[i + 1] = average[i];
//                        }
//                        average[0] = ultrasonic.getValue();
//                    }
//                }
//            };
            //averager.setPriority(Thread.MIN_PRIORITY);
            //averager.start();
            reloadVariables();
        }
        catch (Exception e)
        {
            Utilities.debugLine(e.getMessage(), DEBUG);
        }
    }

    public void switchpanel()
    {
        try
        {
            if (GoalDetected)
            {
                DriverStation.getInstance().getEnhancedIO().setDigitalOutput(13, false);
                DriverStation.getInstance().getEnhancedIO().setDigitalOutput(15, false);
            }
            else
            {
                DriverStation.getInstance().getEnhancedIO().setDigitalOutput(13, true);
                DriverStation.getInstance().getEnhancedIO().setDigitalOutput(15, true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl()
    {
        reloadVariables();
        while (isOperatorControl() && isEnabled())
        {
            driveControl();
            pneumaticControl();
            intakeControl();
            watchdog.feed();
            Timer.delay(0.01);
            //Utilities.debugLine(ultrasonic.getValue());
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

    double accelerationValue = .02;

    public static double sign(double value)
    {
        return (value >= 0) ? 1 : -1;
    }

    boolean foldedOut = false;
    boolean releasedToggle = true;

    Timer t4 = new Timer();
    double goal;
    double THREE_HOLD = .1;
    double TWO_HOLD = .1;
    double ONE_HOLD = .1;

    public void pneumaticControl()
    {

        if (joystick.getRawButton(3))
        {
            t4.start();
            //tilt.set(DoubleSolenoid.Value.kReverse);
            sol3.set(DoubleSolenoid.Value.kReverse);
            sol1.set(DoubleSolenoid.Value.kReverse);
            sol2.set(DoubleSolenoid.Value.kReverse);
            while (t4.get() < THREE_HOLD);
            t4.stop();
            t4.reset();
            sol3.set(DoubleSolenoid.Value.kForward);
            sol1.set(DoubleSolenoid.Value.kForward);
            sol2.set(DoubleSolenoid.Value.kForward);
            while (joystick.getRawButton(3));
        }

        else if (joystick.getRawButton(4))
        {
            t4.start();
            //tilt.set(DoubleSolenoid.Value.kReverse);
            sol3.set(DoubleSolenoid.Value.kReverse);
            sol1.set(DoubleSolenoid.Value.kReverse);
            sol2.set(DoubleSolenoid.Value.kReverse);
            while (t4.get() < TWO_HOLD);
            t4.stop();
            t4.reset();
            sol3.set(DoubleSolenoid.Value.kForward);
            sol1.set(DoubleSolenoid.Value.kForward);
            sol2.set(DoubleSolenoid.Value.kForward);
            while (joystick.getRawButton(4));
        }

        else if (joystick.getRawButton(1))
        {
            t4.start();
            //tilt.set(DoubleSolenoid.Value.kReverse);
            sol3.set(DoubleSolenoid.Value.kReverse);
            sol1.set(DoubleSolenoid.Value.kReverse);
            sol2.set(DoubleSolenoid.Value.kReverse);
            while (t4.get() < ONE_HOLD);
            t4.stop();
            t4.reset();
            sol3.set(DoubleSolenoid.Value.kForward);
            sol1.set(DoubleSolenoid.Value.kForward);
            sol2.set(DoubleSolenoid.Value.kForward);
            while (joystick.getRawButton(1));
        }
//
//        else if (joystick.getRawButton(1))
//        {
//            //tilt.set(DoubleSolenoid.Value.kReverse);
//            sol3.set(DoubleSolenoid.Value.kForward);
//            sol1.set(DoubleSolenoid.Value.kReverse);
//            sol2.set(DoubleSolenoid.Value.kReverse);
//        }
        else if (joystick.getRawButton(2))
        {
            //tilt.set(DoubleSolenoid.Value.kReverse);
            sol3.set(DoubleSolenoid.Value.kReverse);
            sol1.set(DoubleSolenoid.Value.kReverse);
            sol2.set(DoubleSolenoid.Value.kReverse);
        }
        else
        {
            sol3.set(DoubleSolenoid.Value.kForward);
            sol1.set(DoubleSolenoid.Value.kForward);
            sol2.set(DoubleSolenoid.Value.kForward);
        }
        if (joystick.getRawButton(7))
        {
            tilt.set(DoubleSolenoid.Value.kReverse);
        }
        else if (joystick.getRawButton(5))
        {
            tilt.set(DoubleSolenoid.Value.kForward);
        }
        else
        {
            tilt.set(DoubleSolenoid.Value.kOff);
        }
//        if (foldedOut)
//        {
//            tilt.set(DoubleSolenoid.Value.kReverse);
//        }
//        else
//        {
//            tilt.set(DoubleSolenoid.Value.kReverse);
//        }
//        if(joystick.getRawButton(5) && releasedToggle)
//        {
//            foldedOut = !foldedOut;
//        }
//        if(joystick.getRawButton(5))
//        {
//            releasedToggle = false;
//        }
//        else
//        {
//            releasedToggle = true;
//        }

    }

    public void intakeControl()
    {

        if (joystick.getRawButton(6)) //intake
        {
            intake.set(.5);
            intake2.set(.5);
        }

        else if (joystick.getRawButton(8)) //out take
        {
            intake.set(-1);
            intake2.set(-1);
        }

        else
        {
            intake.set(0);
            intake2.set(0);
        }

    }

    boolean useOldCode = false;
    double multiplier = 1;

    //@Override not supported
    public void driveControl()
    {

        if (joystick.getRawButton(1))
        {
            Utilities.debugLine("" + ultrasonic.getValue(), DEBUG);
            Timer.delay(.1);
        }

        if (joystick.getRawButton(10))
        {
            multiplier = 1;
        }
        if (joystick.getRawButton(9))
        {
            multiplier = -1;
        }

        x = multiplier * joystick.getRawAxis(1) * maxXSpeed;
        y = multiplier * joystick.getRawAxis(2) * maxYSpeed;
        t = joystick.getRawAxis(3) * maxTSpeed;

        if (Math.abs(joystick.getX()) < .1)
        {
            x = 0;
        }

        if (Math.abs(joystick.getY()) < .1)
        {
            y = 0;
        }

        if (Math.abs(joystick.getTwist()) < .1)
        {
            t = 0;
        }

        double targetFrontRight = (y + t + x);
        double targetBackRight = (y + t - x);
        double targetFrontLeft = -(y - t - x);
        double targetBackLeft = -(y - t + x);

        if (sign(frontRight.get()) != sign(targetFrontRight))
            frontRight.set(0);
        if (sign(backRight.get()) != sign(targetBackRight))
            backRight.set(0);
        if (sign(frontLeft.get()) != sign(targetFrontLeft))
            frontLeft.set(0);
        if (sign(backLeft.get()) != sign(targetBackLeft))
            backLeft.set(0);
        if (!useOldCode)
        {
            if (Math.abs(frontRight.get()) < Math.abs(targetFrontRight))
                frontRight.set(frontRight.get() + sign(targetFrontRight) * accelerationValue);
            else
                frontRight.set(targetFrontRight);

            if (Math.abs(backRight.get()) < Math.abs(targetBackRight))
                backRight.set(backRight.get() + sign(targetBackRight) * accelerationValue);
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
        }
        else
        {
            frontLeft.set(targetFrontLeft);
            frontRight.set(targetFrontRight);
            backRight.set(targetBackRight);
            backLeft.set(targetBackLeft);
        }
    }

    Timer t2 = new Timer();

    boolean useOldAuto = true;

    double a_movementSpeed = .50;
    double a_driveTime = 1.6;
    double a_waitForSettle = .25;
    double a_fireTime = 1;

    //@Override not supported
    public void autonomous()
    {
        t2.reset();
        t2.start();
        frontLeft.set(-a_movementSpeed);
        frontRight.set(a_movementSpeed);
        backRight.set(a_movementSpeed);
        backLeft.set(-a_movementSpeed);
        while (t2.get() < a_driveTime);
        frontLeft.set(0);
        frontRight.set(0);
        backRight.set(0);
        backLeft.set(0);
        double goal = t2.get() + a_waitForSettle;
        while (t2.get() < goal);
        //tilt.set(DoubleSolenoid.Value.kReverse);
        //goal = t2.get() + .75;
        //while (t2.get() < goal);
        sol3.set(DoubleSolenoid.Value.kReverse);
        sol1.set(DoubleSolenoid.Value.kReverse);
        sol2.set(DoubleSolenoid.Value.kReverse);
        goal = t2.get() + a_fireTime;
        while (t2.get() < goal);
        sol3.set(DoubleSolenoid.Value.kForward);
        sol1.set(DoubleSolenoid.Value.kForward);
        sol2.set(DoubleSolenoid.Value.kForward);
    }

    public void disabled()
    {
        t2.stop();
        Utilities.debugLine("Time should be " + t2.get(), DEBUG);
    }

    /**
     * This function is called once each time the robot enters test mode.
     */
    public void test()
    {
    }

    public void reloadVariables()
    {
        try
        {
            TWO_HOLD = Variables.getDouble("TWOHOLD");
            THREE_HOLD = Variables.getDouble("THREEHOLD");
            ONE_HOLD = Variables.getDouble("ONEHOLD");

            accelerationValue = Variables.getDouble("accelerationValue");

            maxXSpeed = Variables.getDouble("maxXSpeed");
            maxYSpeed = Variables.getDouble("maxYSpeed");
            maxTSpeed = Variables.getDouble("maxTSpeed");

            a_movementSpeed = Variables.getDouble("a_movementSpeed");
            a_driveTime = Variables.getDouble("a_driveTime");
            a_waitForSettle = Variables.getDouble("a_waitForSettle");
            a_fireTime = Variables.getDouble("a_fireTime");
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }
    }

    public static boolean debugingEnabled = true;

}
