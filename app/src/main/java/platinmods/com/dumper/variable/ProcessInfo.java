package platinmods.com.dumper.variable;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import platinmods.com.dumper.R;

public class ProcessInfo {

    public String AppName;
    public String PackageName;
    public int PID;

    public ProcessInfo(Context context, String packageName, int pid) {

        ApplicationInfo applicationInfo = null;
        try {

            applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        finally
        {
            PackageName = packageName;

            PID = pid;

            AppName = (applicationInfo != null ? context.getPackageManager().getApplicationLabel(applicationInfo).toString() : "App Name not found");
        }
    }
}
