package g_ele.com.rdmanager.listeners;

/**
 * PedometerListener
 * Created by aki on 1/9/2016.
 */

public interface PedometerListener extends StepChangeListener, LocationChangeListener {
    void onDurationChanged(int duration);
}
