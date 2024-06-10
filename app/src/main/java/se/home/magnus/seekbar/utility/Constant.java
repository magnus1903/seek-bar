package se.home.magnus.seekbar.utility;

import android.app.Application;

/**
 * This class contains common variables used throughout this app. The reason it extends Application
 * is a workaround to get this class to be loaded at startup.
 */
public class Constant extends Application {

    /**
     * A tolerance used when comparing floats to "consider" them equal.
     */
    public static final float FLOAT_EQUALITY_TOLERANCE = 0.0001f;

}