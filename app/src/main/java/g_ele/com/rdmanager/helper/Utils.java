package g_ele.com.rdmanager.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;

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
}
