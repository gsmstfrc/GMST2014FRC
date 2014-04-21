package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Preferences;
import java.util.Enumeration;
import java.util.Hashtable;

public class Variables
{

    private static Hashtable variables = new Hashtable();

    private static boolean fileOperation = false;

    private static boolean hasLoaded = false;

    public static String sortedKeys[] = new String[]
    {
        "maxXSpeed", "maxYSpeed", "maxTSpeed", //Driver variables
        "ONEHOLD", "TWOHOLD", "THREEHOLD",
        "accelerationValue", "intakeSpeed",
        "outtakeSpeed",
        "w_Port", "w_NetworkBufferSize", //Webserver and sysinternals variables
        "w_DebugLog", "w_websocketUpdateInterval",
        "w_UtiltiesDebug", "w_UtilitiesDontSpamLargeLog",
        "a_movementSpeed", "a_driveTime", //autonomous variables
        "a_waitForSettle", "a_fireTime",
        "a_visionTimeout"
    };

    public static boolean DEBUG = true;

    public static boolean exists(String key)
    {
        //hasLoaded();
        waitForFileIO();
        return variables.contains(key);
    }

    public static void setVariable(String varName, int i)
    {
        //hasLoaded();
        if (variables.containsKey(varName))
            variables.remove(varName);
        variables.put(varName, new Integer(i).toString());
    }

    public static void setVariable(String varName, double db)
    {
        //hasLoaded();
        waitForFileIO();

        if (variables.containsKey(varName))
            variables.remove(varName);
        variables.put(varName, new Double(db).toString());
    }

    public static void setVariable(String varName, boolean bool)
    {
        //hasLoaded();
        waitForFileIO();
        if (variables.containsKey(varName))
            variables.remove(varName);
        variables.put(varName, bool ? "TRUE" : "FALSE");
    }

    public static int getInt(String varName)
    {
        //hasLoaded();
        waitForFileIO();
        if (!variables.containsKey(varName))
            variables.put(varName, "0");
        return Integer.parseInt(((String) variables.get(varName)).trim());
    }

    public static double getDouble(String varName)
    {
        //hasLoaded();
        waitForFileIO();
        while (fileOperation)
            Utilities.sleepThread(20);
        if (!variables.containsKey(varName))
            variables.put(varName, "0");
        return Double.parseDouble(((String) variables.get(varName)).trim());
    }

    public static boolean getBoolean(String varName)
    {
        //hasLoaded();
        waitForFileIO();
        if (!variables.containsKey(varName))
            variables.put(varName, "FALSE");
        return varName.trim().equalsIgnoreCase("TRUE");
    }

    public static void save()
    {
        //hasLoaded();
        waitForFileIO();
        fileOperation = true;
        try
        {
            //Utilities.saveStringToFile("/prefernces.txt", variables.toString());
            Enumeration enu = variables.keys();

            while (enu.hasMoreElements())
            {
                String key = (String) enu.nextElement();
                Preferences.getInstance().putString(key, (String) variables.get(key));
            }

            Preferences.getInstance()
                    .save();
            Utilities.debugLine(
                    "Variables.save(): SAVING!!\n" + variables.toString(), DEBUG);
        }
        catch (Exception e)
        {
            fileOperation = false;
            Utilities.debugLine("Variables.updateTableWithTable(): " + e.toString(), DEBUG);
        }
        fileOperation = false;
    }

    public static void load()
    {
        //hasLoaded();
        waitForFileIO();
        fileOperation = true;
        try
        {
            Enumeration v = Preferences.getInstance().getKeys().elements();
            while (v.hasMoreElements())
            {
                String key = (String) v.nextElement();
                variables.put(key, Preferences.getInstance().getString(key, ""));
            }
            Utilities.debugLine("Variables.save(): LOADING!! " + v.toString(), DEBUG);
            if (sortedKeys.length != variables.size())
            {
                String missingString = "";
                Enumeration keys = variables.keys();
                while (keys.hasMoreElements())
                {
                    String key = (String) keys.nextElement();
                    boolean hasKey = false;
                    for (int i = 0; i < sortedKeys.length; i++)
                        if (sortedKeys[i].equalsIgnoreCase(key))
                            hasKey = true;
                    if (!hasKey)
                        missingString += (key + "\n");
                }
                Utilities.debug("***********\n THERE ARE VARIABLES IN THE PREFRENCES FILE THAT ARE NOT IN THE ARRAY.\n"
                        + "Variables that will not be accessable are:"
                        + missingString
                        + "\n ******************", DEBUG);
            }
        }
        catch (Exception e)
        {
            fileOperation = false;
        }
        fileOperation = false;
    }

    public static Hashtable getTable()
    {
        //hasLoaded();
        waitForFileIO();
        return variables;
    }

    public static void setTable(Hashtable table)
    {
        //hasLoaded();
        waitForFileIO();
        variables = table;
    }

    public static void updateTableWithTable(Hashtable table)
    {
        waitForFileIO();
        //hasLoaded();
        try
        {
            fileOperation = true;
            Enumeration keys = table.keys();
            Utilities.debugLine(table.toString(), DEBUG);
            while (keys.hasMoreElements())
            {
                String key = (String) keys.nextElement();
                Utilities.debugLine("Variables.updateTableWithTable(): trying to set " + key + " to " + (String) table.get(key.trim()), DEBUG);
                variables.put(key.trim(), (String) table.get(key));
            }
        }
        catch (Exception e)
        {
            fileOperation = false;
            Utilities.debugLine("Variables.updateTableWithTable(): " + e.toString(), DEBUG);
        }
        fileOperation = false;
    }

    private static void waitForFileIO()
    {
        while (fileOperation)
            Utilities.sleepThread(20);

    }

    private static void hasLoaded()
    {
        if (!hasLoaded)
        {
            load();
            hasLoaded = true;
        }
    }

}
