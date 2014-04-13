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
    Talon frontLeft;
    Talon frontRight;
    Talon backLeft;
    Talon backRight;

    DoubleSolenoid sol1;
    DoubleSolenoid sol2;
    DoubleSolenoid sol3;

    DoubleSolenoid tilt;

    Compressor comp;

    Watchdog watchdog;

    AnalogChannel ultrasonic;

    int[] average = new int[5];

    // Test arm Controls
    Talon intake;
    Talon intake2;

    boolean DEBUG = false;

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
            Variables.load();
        }
        catch (Exception e)
        {
            Utilities.debugLine(e.getMessage(), DEBUG);
        }
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl()
    {
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
    double THREE_HOLD = 2;
    double TWO_HOLD = 2;
    double ONE_HOLD = 2;

    public void pneumaticControl()
    {
        //        boolean btnPressed = false;
        //        if (!btnPressed && (joystick.getRawButton(4) || joystick.getRawButton(3) || joystick.getRawButton(2)))
        //        {
        //            t4.start();
        //            btnPressed = true;
        //            if (joystick.getRawButton(4))
        //            {
        //                sol1.set(DoubleSolenoid.Value.kForward);
        //                sol3.set(DoubleSolenoid.Value.kForward);
        //                sol2.set(DoubleSolenoid.Value.kReverse);
        //                while (t4.get() <= ONE_HOLD);
        //            }
        //            else if (joystick.getRawButton(3))
        //            {
        //                sol3.set(DoubleSolenoid.Value.kForward);
        //                sol1.set(DoubleSolenoid.Value.kReverse);
        //                sol2.set(DoubleSolenoid.Value.kReverse);
        //                while (t4.get() <= TWO_HOLD);
        //            }
        //            else if (joystick.getRawButton(2))
        //            {
        //                goal = THREE_HOLD;
        //                sol3.set(DoubleSolenoid.Value.kReverse);
        //                sol1.set(DoubleSolenoid.Value.kReverse);
        //                sol2.set(DoubleSolenoid.Value.kReverse);
        //                while (t4.get() <= THREE_HOLD);
        //            }
        //            sol1.set(DoubleSolenoid.Value.kForward);
        //            sol3.set(DoubleSolenoid.Value.kForward);
        //            sol2.set(DoubleSolenoid.Value.kForward);
        //        }
        //        else
        //        {
        //            btnPressed = false;
        //        }

        if (joystick.getRawButton(4))
        {
            t4.start();
            //tilt.set(DoubleSolenoid.Value.kReverse);
            sol3.set(DoubleSolenoid.Value.kReverse);
            sol1.set(DoubleSolenoid.Value.kReverse);
            sol2.set(DoubleSolenoid.Value.kReverse);
            while (t4.get() < .1);
            t4.stop();
            t4.reset();
            sol3.set(DoubleSolenoid.Value.kForward);
            sol1.set(DoubleSolenoid.Value.kForward);
            sol2.set(DoubleSolenoid.Value.kForward);
            while (joystick.getRawButton(4));
        }

        else if (joystick.getRawButton(3))
        {
            t4.start();
            //tilt.set(DoubleSolenoid.Value.kReverse);
            sol3.set(DoubleSolenoid.Value.kReverse);
            sol1.set(DoubleSolenoid.Value.kReverse);
            sol2.set(DoubleSolenoid.Value.kReverse);
            while (t4.get() < .15);
            t4.stop();
            t4.reset();
            sol3.set(DoubleSolenoid.Value.kForward);
            sol1.set(DoubleSolenoid.Value.kForward);
            sol2.set(DoubleSolenoid.Value.kForward);
            while (joystick.getRawButton(3));
        }

        else if (joystick.getRawButton(1))
        {
            //tilt.set(DoubleSolenoid.Value.kReverse);
            sol3.set(DoubleSolenoid.Value.kForward);
            sol1.set(DoubleSolenoid.Value.kReverse);
            sol2.set(DoubleSolenoid.Value.kReverse);
        }

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

        x = multiplier * joystick.getRawAxis(1) * 1;//maxXSpeed;
        y = multiplier * joystick.getRawAxis(2) * 1;//maxYSpeed;
        t = joystick.getRawAxis(3) * 1;//maxTSpeed;

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

    //@Override not supported
    public void autonomous()
    {
        if (!useOldAuto)
        {
            t2.reset();
            t2.start();
            frontLeft.set(-.40);
            frontRight.set(.40);
            backRight.set(.40);
            backLeft.set(-.40);
            while (ultrasonic.getValue() > 250)
                Timer.delay(0.1);
            frontLeft.set(0);
            frontRight.set(0);
            backRight.set(0);
            backLeft.set(0);
            double goal = t2.get() + .5;
            while (t2.get() < goal);
            tilt.set(DoubleSolenoid.Value.kReverse);
            goal = t2.get() + 1.5;
            while (t2.get() < goal);
            sol3.set(DoubleSolenoid.Value.kReverse);
            sol1.set(DoubleSolenoid.Value.kReverse);
            sol2.set(DoubleSolenoid.Value.kReverse);
            goal = t2.get() + 3;
            while (t2.get() < goal);
            sol3.set(DoubleSolenoid.Value.kForward);
            sol1.set(DoubleSolenoid.Value.kForward);
            sol2.set(DoubleSolenoid.Value.kForward);
            tilt.set(DoubleSolenoid.Value.kForward);
        }
        else
        {

            t2.reset();
            t2.start();
            frontLeft.set(-.50);
            frontRight.set(.50);
            backRight.set(.50);
            backLeft.set(-.50);
            while (t2.get() < 1.6);
            frontLeft.set(0);
            frontRight.set(0);
            backRight.set(0);
            backLeft.set(0);
            double goal = t2.get() + .25;
            while (t2.get() < goal);
            //tilt.set(DoubleSolenoid.Value.kReverse);
            //goal = t2.get() + .75;
            //while (t2.get() < goal);
            sol3.set(DoubleSolenoid.Value.kReverse);
            sol1.set(DoubleSolenoid.Value.kReverse);
            sol2.set(DoubleSolenoid.Value.kReverse);
            goal = t2.get() + 1;
            while (t2.get() < goal);
            sol3.set(DoubleSolenoid.Value.kForward);
            sol1.set(DoubleSolenoid.Value.kForward);
            sol2.set(DoubleSolenoid.Value.kForward);
        }
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

//    public class SmartDashboardUpdater implements Runnable, ITableListener
//    {
//
//        Preferences preferences;
//        boolean saveDashboardValues = false;
//        NetworkTable sd;
//
//        public SmartDashboardUpdater()
//        {
//        }
//
//        public void valueChanged(ITable source, String key, Object value, boolean isNew)
//        {
//
//            if (key.equalsIgnoreCase("saveDashboardValues"))
//            {
//                Boolean update;
//                update = ((Boolean) value);
//
//                if (!update.booleanValue())
//                {
//                    return;
//                }
//
//                sd.putBoolean("saveDashboardValues", false);
//                retrieveDashboardValues();
//                save();
//            }
//
//        }
//
//        public void retrieveDashboardValues()
//        {
//            //bla bla bla get values.
//            maxXSpeed = sd.getNumber("maxXSpeed", maxXSpeed);
//            maxYSpeed = sd.getNumber("maxYSpeed", maxYSpeed);
//            maxTSpeed = sd.getNumber("maxTSpeed", maxTSpeed);
//            save();
//        }
//
//        public void sendDashboardValues()
//        {
//            sd.putNumber("maxXSpeed", maxXSpeed);
//            sd.putNumber("maxYSpeed", maxYSpeed);
//            sd.putNumber("maxTSpeed", maxTSpeed);
//        }
//
//        public void load()
//        {
//            maxXSpeed = preferences.getDouble("maxXSpeed", maxXSpeed);
//            maxYSpeed = preferences.getDouble("maxYSpeed", maxYSpeed);
//            maxTSpeed = preferences.getDouble("maxTSpeed", maxTSpeed);
//
//            debugingEnabled = preferences.getBoolean("debugingEnabled", debugingEnabled);
//
//            sendDashboardValues();
//        }
//
//        public void save()
//        {
//            preferences.putDouble("maxXSpeed", maxXSpeed);
//            preferences.putDouble("maxYSpeed", maxYSpeed);
//            preferences.putDouble("maxTSpeed", maxTSpeed);
//
//            preferences.putBoolean("debugingEnabled", debugingEnabled);
//
//            preferences.save();
//        }
//
//        //Initilize the SmartDashboard by loading the saved perferences.
//        public void init()
//        {
//            preferences = Preferences.getInstance();
//            load();
//            sd.putBoolean("saveDashboardValues", true);
//        }
//
//        public void run()
//        {
//            try
//            {
//                //NetworkTable.setTeam(3318);
//            }
//            catch (Exception e)
//            {
//                Utilities.debugLine("NetworkTables team already set", DEBUG);
//            }
//            sd = NetworkTable.getTable("SmartDashboard");
//            sd.addTableListener("saveDashboardValues", this, true); //True is for immedeatly notify.
//            init();
//
//            while (true)
//            {
//                //Do some code that manages updating network table stuff.
//            }
//        }
//    }
    public static boolean debugingEnabled = true;

}
