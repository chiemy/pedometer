package g_ele.com.rdmanager;

import com.amap.api.maps2d.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created: chiemy
 * Date: 17/1/11
 * Description:
 */

public class DataSimulator {
    public static int index = 0;
    private static final LatLng [] latlngsArr = {
            new LatLng(39.99475, 116.465957),
            new LatLng(39.99472, 116.466262),
            new LatLng(39.994586, 116.466445),
            new LatLng(39.994415, 116.466522),
            new LatLng(39.99419, 116.466522),
            new LatLng(39.993999, 116.466552),
            new LatLng(39.993934, 116.466392),
            new LatLng(39.993881, 116.46627),
            new LatLng(39.993801, 116.466171),
            new LatLng(39.993755, 116.466041),
            new LatLng(39.993785, 116.465827),
            new LatLng(39.993732, 116.465736),
            new LatLng(39.993679, 116.465698),
            new LatLng(39.993724, 116.465614),
            new LatLng(39.993705, 116.465423),
            new LatLng(39.993679, 116.465278),
    };
    private static List<List<LatLng>> simulatorRoute;
    private static List<LatLng> latLngs;

    public static void clear() {
        index = 0;
        simulatorRoute = null;
    }

    public static List<List<LatLng>> getSimulateData() {
        if (simulatorRoute == null) {
            simulatorRoute = new ArrayList<>();
            latLngs = new ArrayList<>();
            simulatorRoute.add(latLngs);
        }
        if (index < latlngsArr.length) {
            latLngs.add(latlngsArr[index++]);
        }
        return simulatorRoute;
    }
}
