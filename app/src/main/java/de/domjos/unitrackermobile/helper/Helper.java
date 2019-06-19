/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniBuggerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.helper;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.tracker.Backlog;
import de.domjos.unibuggerlibrary.services.tracker.Bugzilla;
import de.domjos.unibuggerlibrary.services.tracker.Github;
import de.domjos.unibuggerlibrary.services.tracker.Jira;
import de.domjos.unibuggerlibrary.services.tracker.MantisBT;
import de.domjos.unibuggerlibrary.services.tracker.OpenProject;
import de.domjos.unibuggerlibrary.services.tracker.PivotalTracker;
import de.domjos.unibuggerlibrary.services.tracker.Redmine;
import de.domjos.unibuggerlibrary.services.tracker.SQLite;
import de.domjos.unibuggerlibrary.services.tracker.YouTrack;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;

public class Helper {

    public static Properties readPropertiesFromRaw(int rawID, Context context) throws Exception {
        Properties properties = new Properties();
        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(rawID);
        properties.load(in_s);
        return properties;
    }

    public static String readStringFromRaw(int rawID, Context context) throws Exception {
        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(rawID);

        byte[] b = new byte[in_s.available()];
        in_s.read(b);
        return new String(b);
    }

    public static int getVersionCode(Context context) throws Exception {
        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return info.versionCode;
    }

    public static View getRowView(Context context, ViewGroup parent, int layout) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return inflater.inflate(layout, parent, false);
        }
        return new View(context);
    }

    public static IBugService getCurrentBugService(Context context) {
        return Helper.getCurrentBugService(MainActivity.GLOBALS.getSettings(context).getCurrentAuthentication(), context);
    }

    public static IBugService getCurrentBugService(Authentication authentication, Context context) {
        IBugService bugService = null;
        try {
            if (authentication != null) {
                switch (authentication.getTracker()) {
                    case MantisBT:
                        bugService = new MantisBT(authentication);
                        break;
                    case Bugzilla:
                        bugService = new Bugzilla(authentication);
                        break;
                    case YouTrack:
                        bugService = new YouTrack(authentication);
                        break;
                    case RedMine:
                        bugService = new Redmine(authentication);
                        break;
                    case Github:
                        bugService = new Github(authentication);
                        break;
                    case Jira:
                        bugService = new Jira(authentication);
                        break;
                    case PivotalTracker:
                        bugService = new PivotalTracker(authentication);
                        break;
                    case OpenProject:
                        bugService = new OpenProject(authentication);
                        break;
                    case Backlog:
                        bugService = new Backlog(authentication);
                        break;
                    default:
                        bugService = new SQLite(context, Helper.getVersionCode(context), authentication);
                        break;
                }
            } else {
                bugService = new SQLite(context, Helper.getVersionCode(context), new Authentication());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, context);
        }
        return bugService;
    }

    public static ArrayAdapter<String> setAdapter(Context context, String key) {
        if (context != null) {
            int spItem = android.R.layout.simple_spinner_item;
            return new ArrayAdapter<>(context, spItem, ArrayHelper.getValues(context, key));
        }
        return null;
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isStoragePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("dd", "Permission is granted");
                return true;
            } else {

                Log.v("dd", "Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("dd", "Permission is granted");
            return true;
        }
    }

    public static void showPasswordDialog(Activity activity, boolean firstLogin, boolean changePassword, Runnable successRunnable) {
        try {
            Dialog pwdDialog = new Dialog(activity);
            pwdDialog.setContentView(R.layout.password_dialog);
            final EditText password1 = pwdDialog.findViewById(R.id.txtPassword1);
            final EditText password2 = pwdDialog.findViewById(R.id.txtPassword2);
            final Button cmdSubmit = pwdDialog.findViewById(R.id.cmdSubmit);
            pwdDialog.setCancelable(false);
            pwdDialog.setCanceledOnTouchOutside(false);
            if (!firstLogin || changePassword) {
                pwdDialog.setTitle(R.string.pwd_title);
            } else {
                password2.setVisibility(View.GONE);
                pwdDialog.setTitle(R.string.pwd_title_pwd);
            }
            if (MainActivity.GLOBALS.getPassword().isEmpty() || changePassword) {
                if(MainActivity.GLOBALS.getSettings(activity).isEncryptionEnabled()) {
                    pwdDialog.show();
                    new Thread(() -> {
                        while (pwdDialog.isShowing()) {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception ignored) {
                            }
                        }
                    }).start();
                } else {
                    MainActivity.GLOBALS.setPassword(SQLiteGeneral.NO_PASS);
                    MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                    successRunnable.run();
                }
            } else {
                MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                successRunnable.run();
            }
            cmdSubmit.setOnClickListener(v -> {
                try {
                    if (!firstLogin || changePassword) {
                        if (password1.getText().toString().equals(password2.getText().toString())) {
                            if(changePassword) {
                                MainActivity.GLOBALS.getSqLiteGeneral().changePassword(password1.getText().toString());
                            }
                            MainActivity.GLOBALS.setPassword(password1.getText().toString());
                            MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                            if(Helper.checkDatabase()) {
                                successRunnable.run();
                                pwdDialog.cancel();
                            }
                        }
                    } else {
                        if(changePassword) {
                            MainActivity.GLOBALS.getSqLiteGeneral().changePassword(password1.getText().toString());
                        }
                        MainActivity.GLOBALS.setPassword(password1.getText().toString());
                        MainActivity.GLOBALS.setSqLiteGeneral(new SQLiteGeneral(activity, MainActivity.GLOBALS.getPassword()));
                        if(Helper.checkDatabase()) {
                            successRunnable.run();
                            pwdDialog.cancel();
                        }
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, activity);
                }
            });
        } catch (Exception ex) {
            MessageHelper.printException(ex, activity);
        }
    }

    private static boolean checkDatabase() {
        try {
            List<Authentication> authenticationList = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("", true);
            return authenticationList != null;
        } catch (Exception ex) {
            return false;
        }
    }
}