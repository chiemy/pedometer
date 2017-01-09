package g_ele.com.rdmanager.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;

import g_ele.com.rdmanager.R;

/**
 * Created: chiemy
 * Date: 17/1/9
 * Description:
 */

public class OutDoorFragment extends BasicMapFragment implements LocationSource, AMapLocationListener {

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
    protected boolean isOnceLocation() {
        return true;
    }
}
