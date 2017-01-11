package g_ele.com.rdmanager.helper;

/**
 * Created: chiemy
 * Date: 17/1/5
 * Description:
 */

public class SportAnalyser {
    private int mSex;
    private int mHeight;
    private int mWeight;

    /**
     * 运动分析工具, 根据性别、身高、体重等数据, 计算一些运动数据(如卡路里, 步行距离等)
     * @param builder
     */
    private SportAnalyser(Builder builder) {
        mSex = builder.mSex;
        mHeight = builder.mHeight;
        mWeight = builder.mWeight;
    }

    /**
     * 计算步行距离
     * @param steps
     * @return
     */
    public float getStepDistance(int steps) {
        // TODO use user's weight and height
        return steps * 0.84f;
    }

    /**
     * 计算步行消耗卡路里
     * @param steps
     * @return
     */
    public float getCalorieForStep(int steps) {
        return getCalorieForDistance(getStepDistance(steps));
    }

    /**
     * 计算步行消耗卡路里
     * @paratance 单位 m
     * @return
     */
    public float getCalorieForDistance(double distance) {
        return (float) (this.mWeight * distance * 1.036 / 1000);
    }

    public static final class Builder {
        private int mSex;
        private int mHeight = 170;
        private int mWeight = 60;

        /**
         * 设置用户性别
         * @return sex 0 - 男, 1 - 女
         */
        public Builder setUserSex(int sex) {
            mSex = sex;
            return this;
        }

        /**
         * 设置用户身高
         * @return heightCM 单位 CM
         */
        public Builder setUserHeight(int heightCM) {
            mHeight = heightCM;
            return this;
        }

        /**
         * 设置用户体重
         * @param weightKG 单位 KG
         * @return
         */
        public Builder setUserWeight(int weightKG){
            mWeight = weightKG;
            return this;
        }

        public SportAnalyser build() {
            return new SportAnalyser(this);
        }
    }
}
