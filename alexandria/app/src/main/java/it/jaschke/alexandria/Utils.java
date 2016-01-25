package it.jaschke.alexandria;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *
 * Created by fabianrodriguez on 10/13/15.
 *
 */
public class Utils {

    public static boolean verifyConnection(Context ctx) {
        boolean connected = false;
        ConnectivityManager mManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] redes = mManager.getAllNetworkInfo();
        for (int i = 0; i < redes.length; i++) {
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }
        }
        return connected;
    }

}
