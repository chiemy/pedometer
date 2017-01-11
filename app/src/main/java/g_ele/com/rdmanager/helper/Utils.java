package g_ele.com.rdmanager.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Calendar;
import java.util.List;

/**
 * Created: chiemy
 * Date: 17/1/3
 * Description:
 */

public class Utils {
    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    public static boolean isGpsOpen(Context context){
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //获得手机是不是设置了GPS开启状态true：gps开启，false：GPS未开启
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * @return milliseconds since 1.1.1970 for today 0:00:00 local timezone
     */
    public static long getToday() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * @return 网络是否连接并可用(在切换网络状态时, 无法立即返回正确地结果)
     */
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo =
                getConnectivityManager(context).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    public static String getFormatPace(float second) {
        int minutes = (int) (second / 60);
        int seconds = (int) (second % 60);
        return String.format("%d'%d''", minutes, seconds);
    }
}
