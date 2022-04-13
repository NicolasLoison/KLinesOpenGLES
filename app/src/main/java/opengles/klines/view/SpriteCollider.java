package opengles.klines.view;

import android.view.MotionEvent;

public class SpriteCollider extends Component{

    SpriteRenderer renderer;
    public SpriteCollider(GameObject gameObject){
        super(gameObject);
        for(Component c : this.gameObject.componentList){
            if(c instanceof SpriteRenderer){
                renderer =(SpriteRenderer) c;
                break;
            }
        }

        if(renderer == null){
            System.err.println("Gameobject with Sprite Collider must have a Sprite Renderer component ("+this.gameObject.name+")");
        }
    }

    @Override
    public void OnTouchEvent(MotionEvent e){

        float x = e.getX();
        float y = e.getY();

        if(renderer.isInside(x, y)){
            for(Component c : this.gameObject.componentList){
                if(c instanceof MonoBehaviour){
                    MonoBehaviour script = (MonoBehaviour) c;
                    if(e.getAction() == MotionEvent.ACTION_DOWN)
                        script.OnTouchDown(x, y);
                }
            }
        }
    }

}
