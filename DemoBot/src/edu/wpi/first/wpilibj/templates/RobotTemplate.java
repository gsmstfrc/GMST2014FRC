package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
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

		boolean armUp = joystick.getRawButton(5);
		boolean armDown = joystick.getRawButton(3);
		double power = joystick.getThrottle()*0.5+1.0;

		if (armUp)
			intakeVictor.set(power);
		else if (armDown)
			intakeVictor.set(-power);
		else
			intakeVictor.set(0.0);
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

        boolean saveDashboardValues = false;
        NetworkTable sd;
        public SmartDashboardUpdater()
        {
        }
        
        public void valueChanged(ITable source, String key, Object value, boolean isNew) 
        {
            if(key.equalsIgnoreCase("saveDashboardValues"))
            {
                Boolean update;
                update = ((Boolean) value);
                if(!update.booleanValue())
                    return;
                sd.putBoolean("saveDashboardValues", false);
                retrieveDashboardValues();
            }
        }
        
        public void retrieveDashboardValues()
        {
            //bla bla bla get values.
        }

        public void run()
        {
            NetworkTable.setTeam(3318);
            sd = NetworkTable.getTable("SmartDashboard");
            sd.addTableListener("saveDashboardValues",this, true); //True is for immedeatly notify.
            
            while (true)
            {
                //Do some code that manages updating network table stuff.
            }

        }
    }
}
