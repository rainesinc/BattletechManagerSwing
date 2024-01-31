/*
 * BattletechApp.java
 */

package battletech;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import java.io.IOException;

/**
 * The main class of the application.
 */
public class BattletechApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        try {
            show(new BattletechView(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of BattletechApp
     */
    public static BattletechApp getApplication() {
        return Application.getInstance(BattletechApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(BattletechApp.class, args);
    }
}
