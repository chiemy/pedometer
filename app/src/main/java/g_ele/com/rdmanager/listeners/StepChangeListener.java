package g_ele.com.rdmanager.listeners;

/**
 * Created: chiemy
 * Date: 17/1/5
 * Description:
 */

public interface StepChangeListener {
    /**
     * 步数改变
     * @param steps 总步数
     */
    void onStepChange(int steps);

    /**
     * 今日步数改变, 在开始计步及暂停计步时回调
     * @param steps 总步数
     */
    // void onTodayStepChange(int steps);
}
