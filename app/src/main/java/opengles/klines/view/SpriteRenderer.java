package opengles.klines.view;

import java.util.Objects;

import javax.microedition.khronos.opengles.GL10;

public class SpriteRenderer extends Component{

    float imageHeightRatio = 1f;
    int ressourceId;
    boolean hasBeenLoaded = false;
    int imageId;

    Vertex shape = new Vertex (new float[] {
            1f,1f,0f,
            0f,1f,0f,
            1f,0f,0f,
            0f,0f,0f,
    });

    Vertex texture = new Vertex (new float[]{
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    });

    public SpriteRenderer(GameObject gameObject, int ressourceId) {
        super(gameObject);
        this.ressourceId = ressourceId;

    }

    @Override
    public void Load(GL10 gl){
        if(gameObject.scene != null){
            imageId = gameObject.scene.loadImage(gl, ressourceId);
            float imageWidth = Objects.requireNonNull(gameObject.scene.imagesDimById.get(imageId)).get(0);
            float imageHeight = Objects.requireNonNull(gameObject.scene.imagesDimById.get(imageId)).get(1);
            imageHeightRatio = imageHeight/imageWidth;
            shape = new Vertex (new float[]
                    {
                            1f,imageHeightRatio,0f,
                            0f,imageHeightRatio,0f,
                            1f,0f,0f,
                            0f,0f,0f,
                    });
            hasBeenLoaded = true;
        }
    }

    @Override
    public void Draw(GL10 gl){
        if(!hasBeenLoaded){
            Load(gl);
        }
        // bind the previously generated texture
        gl.glBindTexture(GL10.GL_TEXTURE_2D, imageId);

        // Point to our buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // set the colour for the square
        gl.glColor4f (1.0f, 1.0f, 1.0f, 1.0f);

        // Set the face rotation
        gl.glFrontFace(GL10.GL_CW);

        // Point to our vertex buffer
        gl.glVertexPointer (3, GL10.GL_FLOAT, 0, shape.buffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture.buffer);

        // Draw the vertices as triangle strip
        gl.glDrawArrays (GL10.GL_TRIANGLE_STRIP, 0, shape.vertex.length / 3);

        // Disable the client state before leaving
        gl.glDisableClientState (GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    /**
     * Test si une position globale x y est à l'intérieur du sprite ou non
     */
    public boolean isInside(float x, float y){
        Transform t = this.gameObject.transform;
        float centerX = t.ScreenPositionX(); // position au centre de l'objet en X
        float centerY = t.ScreenPositionY();
        boolean xInside = x<centerX+(Transform.gameUnitX()/Camera.main.getSize())/2f && x>centerX-(Transform.gameUnitX()/Camera.main.getSize())/2f;
        boolean yInside = y<centerY+(Transform.gameUnitY()*imageHeightRatio/Camera.main.getSize()/Transform.screenRatio)/2f && y>centerY-(Transform.gameUnitY()*imageHeightRatio/Camera.main.getSize()/Transform.screenRatio)/2f;
        return xInside && yInside;
    }
}
