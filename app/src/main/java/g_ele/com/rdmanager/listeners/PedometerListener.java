package g_ele.com.rdmanager.listeners;

/**
 *
 */
public interface PedometerListener extends StepChangeListener, LocationChangeListener {
    void onDurationChanged(int duration);
}
