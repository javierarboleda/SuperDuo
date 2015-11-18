package it.jaschke.alexandria.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by javierarboleda on 11/4/15.
 */
public class ConnectivityUtil {

    /**
     *
     * Method checks for network connectivity. Code is from review example Udacity Project 1
     * reviewer provided
     *
     * @return
     */
    public static boolean isNetworkAvailable(Activity activity) {

        ConnectivityManager connectivityManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

}
