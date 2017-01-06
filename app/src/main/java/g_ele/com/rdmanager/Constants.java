package g_ele.com.rdmanager;

/**
 * Created: chiemy
 * Date: 17/1/3
 * Description:
 */

public class Constants {
    public static final int INIT = 1;

    public static final int MSG_DURATION_CHANGE = 1;
    public static final int MSG_STEP_CHANGE = 3;
    public static final int MSG_LOCATION_CHANGE = 6;

    public static final int MSG_STOP = 7;
    public static final int MSG_TOGGLE = 8;
    public static final int MSG_CHANGE_MODE = 9;

    /**
     * 室内模式, 不开启定位
     */
    public static final int MODE_INDOOR = 1;
    /**
     * 室外模式, 开启定位
     */
    public static final int MODE_OUTDOOR = 2;

    public static final String KEY_DATA = "data";
}
