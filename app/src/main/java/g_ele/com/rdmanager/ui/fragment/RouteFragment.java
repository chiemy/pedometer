package g_ele.com.rdmanager.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;

import java.util.List;

import g_ele.com.rdmanager.R;

/**
 * Created: chiemy
 * Date: 17/1/9
 * Description:
 */

public class RouteFragment extends BasicMapFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_outdoor, container, false);
    }

    @Override
    protected MapView getMapView(View root) {
        return (MapView) root.findViewById(R.id.mapView);
    }

    @Override
    protected MyLocationStyle getMyLocationStyle() {
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point));
        myLocationStyle.strokeColor(0);
        myLocationStyle.radiusFillColor(0);
        return myLocationStyle;
    }

    @Override
    protected boolean isOnceLocation() {
        return true;
    }

    public void setRoute(List<List<LatLng>> route) {
        if (route != null && getaMap() != null) {
            int size = route.size();
            for (int i = 0; i < size; i++) {
                getaMap().addPolyline(new PolylineOptions()
                        .addAll(route.get(i))
                        .width(10)
                        .color(Color.RED));
            }

        }
    }
}
