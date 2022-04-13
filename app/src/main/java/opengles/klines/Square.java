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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLES30;

//Dessiner un carré

public class Square {

    /* les déclarations pour l'équivalent des VBO */

    private final FloatBuffer vertexBuffer; // Pour le buffer des coordonnées des sommets du carré
    private final ShortBuffer indiceBuffer; // Pour le buffer des indices
    private final FloatBuffer colorBuffer; // Pour le buffer des couleurs des sommets

    /* les déclarations pour les shaders
    Identifiant du programme et pour les variables attribute ou uniform
     */
    private final int IdProgram; // identifiant du programme pour lier les shaders

    static final int COORDS_PER_VERTEX = 3; // nombre de coordonnées par vertex
    static final int COULEURS_PER_VERTEX = 4; // nombre de composantes couleur par vertex

    int []linkStatus = {0};

    /* Attention au repère au niveau écran (x est inversé)
     Le tableau des coordonnées des sommets
     Oui ce n'est pas joli avec 1.0 en dur ....
     */


    private final FloatBuffer textureBuffer;  // buffer holding the texture coordinates

    static float[] squareCoords = {
            -1.0f,   1.0f, 0.0f,
            -1.0f,  -1.0f, 0.0f,
            1.0f,  -1.0f, 0.0f,
            1.0f,  1.0f, 0.0f };
    // Le tableau des couleurs
    static float[] squareColors = {
             1.0f,  1.0f, 1.0f, 1.0f,
             1.0f,  1.0f, 1.0f, 1.0f,
             1.0f,  1.0f, 1.0f, 1.0f,
             1.0f,  1.0f, 1.0f, 1.0f };

    // Le carré est dessiné avec 2 triangles
    private final short[] Indices = { 0, 1, 2, 0, 2, 3 };

    public Square() {
        // initialisation du buffer pour les vertex (4 bytes par float)
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);


        // initialisation du buffer pour les couleurs (4 bytes par float)
        ByteBuffer bc = ByteBuffer.allocateDirect(squareColors.length * 4);
        bc.order(ByteOrder.nativeOrder());
        colorBuffer = bc.asFloatBuffer();
        colorBuffer.put(squareColors);
        colorBuffer.position(0);

        // initialisation du buffer des indices
        ByteBuffer dlb = ByteBuffer.allocateDirect(Indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        indiceBuffer = dlb.asShortBuffer();
        indiceBuffer.put(Indices);
        indiceBuffer.position(0);

        // Mapping coordinates for the vertices
        // top left     (V2)
        // bottom left  (V1)
        // top right    (V4)
        // bottom right (V3)
        float[] texture = {
                // Mapping coordinates for the vertices
                0.0f, 1.0f,     // top left     (V2)
                0.0f, 0.0f,     // bottom left  (V1)
                1.0f, 1.0f,     // top right    (V4)
                1.0f, 0.0f      // bottom right (V3)
        };
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);

        /* Chargement des shaders */
        /* Le vertex shader avec la définition de gl_Position et les variables utiles au fragment shader
         */
        String vertexShaderCode = "#version 300 es\n" +
                "uniform mat4 uMVPMatrix;\n" +
                "in vec3 vPosition;\n" +
                "in vec4 vCouleur;\n" +
                "in vec2 a_TexCoordinate;\n" +
                "out vec2 v_TexCoordinate;\n" +
                "out vec4 Couleur;\n" +
                "out vec3 Position;\n" +
                "void main() {\n" +
                "Position = vPosition;\n" +
                "v_TexCoordinate = a_TexCoordinate;\n" +
                "gl_Position = uMVPMatrix * vec4(vPosition,1.0);\n" +
                "Couleur = vCouleur;\n" +
                "}\n";
        int vertexShader = MyGLRenderer.loadShader(
                GLES30.GL_VERTEX_SHADER,
                vertexShaderCode);
        // pour définir la taille d'un float
        //" diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance)));\n\n"+
        //"diffuse = diffuse + 0.3;\n\n"+
        //"if (test>1.0) \n"+
        //    "discard;\n"+
        //"fragColor = Couleur;\n" +
        String fragmentShaderCode = "#version 300 es\n" +
                "precision mediump float;\n" + // pour définir la taille d'un float
                "in vec4 Couleur;\n" +
                "in vec3 Position;\n" +
                "in sampler2D u_Texture;\n" +
                "in vec2 v_TexCoordinate;\n" +
                "out vec4 fragColor;\n" +
                "void main() {\n" +
                "float x = Position.x;\n" +
                "float y = Position.y;\n" +
                "float test = x*x+y*y;\n" +
                //" diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance)));\n\n"+
                //"diffuse = diffuse + 0.3;\n\n"+
                "fragColor = (Couleur * texture2D(u_Texture, v_TexCoordinate));\n\n" +
                //"if (test>1.0) \n"+
                //    "discard;\n"+
                //"fragColor = Couleur;\n" +
                "}\n";
        int fragmentShader = MyGLRenderer.loadShader(
                GLES30.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        IdProgram = GLES30.glCreateProgram();             // create empty OpenGL Program
        GLES30.glAttachShader(IdProgram, vertexShader);   // add the vertex shader to program
        GLES30.glAttachShader(IdProgram, fragmentShader); // add the fragment shader to program
        GLES30.glLinkProgram(IdProgram);                  // create OpenGL program executables
        GLES30.glGetProgramiv(IdProgram, GLES30.GL_LINK_STATUS,linkStatus,0);
    }

    public static int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES30.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }


    /* La fonction Display */
    public void draw(float[] mvpMatrix, int mTextureDataHandle) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(IdProgram);


           // get handle to shape's transformation matrix
        // identifiant (location) pour transmettre la matrice PxVxM
        int idMVPMatrix = GLES30.glGetUniformLocation(IdProgram, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES30.glUniformMatrix4fv(idMVPMatrix, 1, false, mvpMatrix, 0);


        // get handle to vertex shader's vPosition member et vCouleur member
        // idendifiant (location) pour transmettre les coordonnées au vertex shader
        int idPosition = GLES30.glGetAttribLocation(IdProgram, "vPosition");
        // identifiant (location) pour transmettre les couleurs
        int idCouleur = GLES30.glGetAttribLocation(IdProgram, "vCouleur");
        int mTextureUniformHandle = GLES30.glGetUniformLocation(IdProgram, "u_Texture");
        int mTextureCoordinateHandle = GLES30.glGetAttribLocation(IdProgram, "a_TexCoordinate");
        // Set the active texture unit to texture unit 0.
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureDataHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES30.glUniform1i(mTextureUniformHandle, 0);
// Pass in the texture coordinate information
        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false,
                0, textureBuffer);


        /* Activation des Buffers */
        GLES30.glEnableVertexAttribArray(idPosition);
        GLES30.glEnableVertexAttribArray(idCouleur);
        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle);

        /* Lecture des Buffers */
        // le pas entre 2 sommets : 4 bytes per vertex
        int vertexStride = COORDS_PER_VERTEX * 4;
        GLES30.glVertexAttribPointer(
                idPosition, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // le pas entre 2 couleurs
        int couleurStride = COULEURS_PER_VERTEX * 4;
        GLES30.glVertexAttribPointer(
                idCouleur, COULEURS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                couleurStride, colorBuffer);


        // Draw the square
        GLES30.glDrawElements(
                GLES30.GL_TRIANGLES, Indices.length,
                GLES30.GL_UNSIGNED_SHORT, indiceBuffer);


        // Disable vertex array
        GLES30.glDisableVertexAttribArray(idPosition);
        GLES30.glDisableVertexAttribArray(idCouleur);

    }

}
