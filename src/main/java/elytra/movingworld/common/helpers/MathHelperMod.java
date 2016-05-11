package elytra.movingworld.common.helpers;

public class MathHelperMod {

    public static final float PI_HALF = (float) (Math.PI / 2D);

    public static double clamp_double(double d, double lowerbound, double upperbound) {
        return d < lowerbound ? lowerbound : d > upperbound ? upperbound : d;
    }

    public static int round_double(double d) {
        return (int) Math.round(d);
    }
}
