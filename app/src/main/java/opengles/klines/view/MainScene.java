package opengles.klines.view;

import android.content.Context;

public class MainScene extends Scene {
    //Square square1, square2;
    Camera mainCam;
    GameObject gameManager;

    public MainScene(Context context) {
        super(context);
        mainCam = new Camera( 9, true);


        gameManager = new GameObject(this, "Game Manager");
        gameManager.addComponent(new GameManager(gameManager));

    }
}