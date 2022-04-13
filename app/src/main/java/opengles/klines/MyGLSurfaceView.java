/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opengles.klines;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import opengles.klines.view.Scene;
import opengles.klines.view.MainScene;

/* La classe MyGLSurfaceView avec en particulier la gestion des événements
  et la création de l'objet renderer
*/

/* On va dessiner un carré qui peut se déplacer grâce à une translation via l'écran tactile */

public class MyGLSurfaceView extends GLSurfaceView {

    /* Un attribut : le renderer (GLSurfaceView.Renderer est une interface générique disponible) */
    /* MyGLRenderer va implémenter les méthodes de cette interface */

    private final Scene mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        // Création d'un context OpenGLES 3.0
        setEGLContextClientVersion(1);

        // Création du renderer qui va être lié au conteneur View créé
        mRenderer = new MainScene(context);
        setRenderer(mRenderer);

        // Option pour indiquer qu'on redessine uniquement si les données changent
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    /* Comment interpréter les événements sur l'écran tactile */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Les coordonnées du point touché sur l'écran
        mRenderer.onTouchEvent(e);
        return true;
    }

}
