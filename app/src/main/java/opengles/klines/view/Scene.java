package opengles.klines.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.MotionEvent;

import androidx.appcompat.content.res.AppCompatResources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public abstract class Scene implements GLSurfaceView.Renderer
{

    public Context context;
    public Resources resources;
    public HashMap<Integer, Integer> images = new HashMap<>();
    public HashMap<Integer, List<Integer>> imagesDimById = new HashMap<>();
    public float width;
    public float height;
    private final ArrayList<GameObject> gameObjectList = new ArrayList<>();

    public Scene(Context context)
    {
        this.context = context;
        this.resources = context.getResources ();
    }

    @Override
    public void onDrawFrame (GL10 gl)
    {

//      // clear Screen and Depth Buffer
        gl.glClear (GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
//      // Reset the Modelview Matrix
        gl.glLoadIdentity ();

        draw (gl);

    }


    @Override
    public void onSurfaceChanged (GL10 gl, int width, int height)
    {
        this.width = width;
        this.height = height;

        gl.glViewport (0, 0, width, height); // Reset The Current Viewport
        gl.glMatrixMode (GL10.GL_PROJECTION); // Select The Projection Matrix
        gl.glLoadIdentity (); // Reset The Projection Matrix

        if(Camera.main != null){
            Transform.screenRatio = height/(width*1f);
            Transform.screenWidth = Camera.main.getSize()*width;
            Transform.screenHeight = Camera.main.getSize()*Transform.screenRatio*height;
            gl.glOrthof (0, Transform.screenWidth, 0, Transform.screenHeight, -1f, 1f);
        }
        else{
            gl.glOrthof (0, width, 0, height, -1f, 1f);
        }
        //gl.glTranslatef (0f, -height/2, 0.0f); // move the camera !!


        gl.glMatrixMode (GL10.GL_MODELVIEW); // Select The Modelview Matrix
        gl.glLoadIdentity (); // Reset The Modelview Matrix

        load (gl);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config)
    {
        gl.glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping ( NEW )
        gl.glShadeModel(GL10.GL_SMOOTH);            //Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    //Black Background
        gl.glClearDepthf(1.0f);                     //Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST);            //Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL);             //The Type Of Depth Testing To Do

        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL10.GL_BLEND);


        //Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

        init ();
    }


    public void add(GameObject gameObject){
        if(!gameObjectList.contains(gameObject))
            gameObjectList.add(gameObject);
    }

    public void remove(GameObject gameObject){
        gameObjectList.remove(gameObject);
    }

    public void init()
    {
        for(GameObject g : new ArrayList<>(gameObjectList)){
            g.Start();
        }
    }

    public void load (GL10 gl)
    {
        for(GameObject g : new ArrayList<>(gameObjectList)){
            g.Load(gl);
        }
    }

    public void draw (GL10 gl)
    {
        for (GameObject g : new ArrayList<>(gameObjectList)) {
            g.Draw(gl);
        }

    }

    public void onTouchEvent(MotionEvent e){
        for (GameObject gameObject : new ArrayList<>(gameObjectList)) {
            gameObject.OnTouchEvent(e);
        }
    }

    private static int next (GL10 gl)
    {
        int[] temp = new int[1];
        gl.glGenTextures (1, temp, 0);
        return temp[0];
    }

    public int loadBitmap(GL10 gl, Bitmap b){
        int id = next (gl);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, id);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, b, 0);
        return id;
    }

    public Drawable getDrawable(int resource){
        return AppCompatResources.getDrawable(context, resource);
    }

    public int loadImage (GL10 gl, int resource)
    {
        if (images.containsKey(resource)){
            return images.get(resource);
        }
        int id = next (gl);
        images.put (resource, id);

        gl.glBindTexture (GL10.GL_TEXTURE_2D, id);

        gl.glTexParameterf (GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf (GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexParameterf (GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf (GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glTexEnvf (GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

        BitmapFactory.Options options = new BitmapFactory.Options ();
        options.inScaled = false;

        InputStream input = resources.openRawResource (resource);
        Bitmap bitmap;
        try
        {
            bitmap = BitmapFactory.decodeStream (input, null, options);
            imagesDimById.put(id, Arrays.asList(bitmap.getWidth(), bitmap.getHeight()));
        }
        finally
        {
            try
            {
                input.close ();
            }
            catch (IOException e)
            {
                // Ignore.
            }
        }

        GLUtils.texImage2D (GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        return id;
    }





}
