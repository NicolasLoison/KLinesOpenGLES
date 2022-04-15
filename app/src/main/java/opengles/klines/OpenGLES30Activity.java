package opengles.klines;

import android.os.Bundle;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.Window;
import android.view.WindowManager;
import opengles.klines.modele.Facade;

/* Ce tutorial est issu d'un tutorial http://developer.android.com/training/graphics/opengl/index.html :
openGLES.zip HelloOpenGLES20
 */


public class OpenGLES30Activity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Création de View et association à Activity
           MyGLSurfaceView : classe à implémenter et en particulier la partie renderer */

        /* Pour le plein écran */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // le conteneur View pour faire du rendu OpenGL
        GLSurfaceView mGLView = new MyGLSurfaceView(this);
        /* Définition de View pour cette activité */
        setContentView(mGLView);
    }
}
