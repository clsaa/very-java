package com.clsaa.edu.java.effective.chapter01.builder;

/**
 * <p>
 * 营养类
 * </p>
 *
 * @author 任贵杰 812022339@qq.com
 * @since 2018-10-11
 */
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;

    public static class Builder {
        //required parameters

        private final int servingSize;
        private final int servings;

        //Optional parameters

        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int calories){
            this.calories = calories;
            return this;
        }

        public Builder fat(int fat){
            this.fat = fat;
            return this;
        }

        public Builder sodium(int sodium){
            this.sodium = sodium;
            return this;
        }

        public NutritionFacts build(){
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder){
        this.servingSize = builder.servingSize;
        this.servings = builder.servings;
        this.calories = builder.calories;
        this.fat = builder.fat;
        this.sodium = builder.sodium;
    }
}
