package opengles.klines.view;

import android.content.Context;

import opengles.klines.OpenGLES30Activity;

public class MainScene extends Scene {
    //Square square1, square2;
    Camera mainCam;
    GameObject gameManager;

    public MainScene(OpenGLES30Activity activity) {
        super(activity);
        mainCam = new Camera( 9, true);

        gameManager = new GameObject(this, "Game Manager");
        gameManager.addComponent(new GameManager(gameManager, activity));

    }
}