package org.inspira.emag.dialogos;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by Siempre on 22/02/2016.
 */
public class ProveedorToast {

    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int messageId){
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
    }

    public static void showToastOnUIThread(final Activity activity, final String message){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
