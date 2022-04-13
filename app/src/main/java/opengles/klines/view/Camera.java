package opengles.klines.view;

public class Camera{


    public static Camera main;
    private int size;

    public Camera(int size, Boolean isMainCamera){

        this.size = size;
        if(isMainCamera){
            main = this;
        }
    }

    public void setMainCamera(){
        main = this;
    }

    public void setSize(int width) {
        this.size = width;
    }

    public int getSize() {
        return size;
    }
}
