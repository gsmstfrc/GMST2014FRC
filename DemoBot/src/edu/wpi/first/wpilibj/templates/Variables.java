package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Preferences;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Variables
{

    private static Hashtable variables = new Hashtable();
    
    public static String sortedKeys[] = new String[]{"maxXSpeed","maxYSpeed","maxTSpeed","ONEHOLD","TWOHOLD","THREEHOLD","accelerationValue","w_Port","w_NetworkBufferSize","w_DebugLog","a_movementSpeed","a_driveTime","a_waitForSettle","a_fireTime"};

    public static boolean DEBUG = true;

    public static boolean exists(String key)
    {
        return variables.contains(key);
    }

    public static void setVariable(String varName, int i)
    {
        if (variables.containsKey(varName))
            variables.remove(varName);
        variables.put(varName, new Integer(i).toString());
    }

    public static void setVariable(String varName, double db)
    {
        if (variables.containsKey(varName))
            variables.remove(varName);
        variables.put(varName, new Double(db).toString());
    }

    public static void setVariable(String varName, boolean bool)
    {
        if (variables.containsKey(varName))
            variables.remove(varName);
        variables.put(varName, bool ? "TRUE" : "FALSE");
    }

    public static int getInt(String varName)
    {
        if (!variables.containsKey(varName))
            variables.put(varName, "0");
        return Integer.parseInt(((String) variables.get(varName)).trim());
    }

    public static double getDouble(String varName)
    {
        if (!variables.containsKey(varName))
            variables.put(varName, "0");
        return Double.parseDouble(((String) variables.get(varName)).trim());
    }

    public static boolean getBoolean(String varName)
    {
        if (!variables.containsKey(varName))
            variables.put(varName, "FALSE");
        return varName.trim().equalsIgnoreCase("TRUE");
    }

    public static void save()
    {
        //Utilities.saveStringToFile("/prefernces.txt", variables.toString());
        Enumeration enu = variables.keys();
        while (enu.hasMoreElements())
        {
            String key = (String) enu.nextElement();
            Preferences.getInstance().putString(key, (String) variables.get(key));
        }
        Preferences.getInstance().save();
        Utilities.debugLine("Variables.save(): SAVING!!\n" + variables.toString(), DEBUG);
    }

    public static void load()
    {
        Enumeration v = Preferences.getInstance().getKeys().elements();
        while (v.hasMoreElements())
        {
            String key = (String) v.nextElement();
            variables.put(key, Preferences.getInstance().getString(key, ""));
        }
        Utilities.debugLine("Variables.save(): LOADING!! " + v.toString(), DEBUG);
    }

    public static Hashtable getTable()
    {
        return variables;
    }

    public static void setTable(Hashtable table)
    {
        variables = table;
    }

    public static void updateTableWithTable(Hashtable table)
    {
        try
        {
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
            Utilities.debugLine("Variables.updateTableWithTable(): " + e.toString(), DEBUG);
        }
    }

}
