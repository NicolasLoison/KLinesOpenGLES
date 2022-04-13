package opengles.klines.view;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import opengles.klines.R;
import opengles.klines.modele.Drawer;
import opengles.klines.modele.Facade;
import opengles.klines.modele.Grid;
import opengles.klines.modele.Pions;
import opengles.klines.modele.Position;
import opengles.klines.modele.Score;
import opengles.klines.exception.NoPossiblePath;
import opengles.klines.exception.OutofBounds;
import opengles.klines.exception.PionsNotInGrid;
import opengles.klines.exception.TargetNotEmpty;

public class GameManager extends MonoBehaviour implements Score, Drawer {

    Facade facade;
    Grid grid;
    List<GameObject> gridGO = new ArrayList<>();
    GameObject case_selector;
    Pions selectedPion;
    TextRenderer scoreTextRenderer;
    int score = 0;
    private final HashMap<Pions, GameObject> pionsGameObjectHashMap = new HashMap<>();
    private boolean pionMoved = false;

    public GameManager(GameObject gameObject) {
        super(gameObject);
        initScore();
        facade = new Facade(this, this);
        grid = facade.createGrid(9);
        for(int i = 0; i<grid.getGridSize(); i++){
            for(int j=0; j< grid.getGridSize(); j++){
                float start = -(Camera.main.getSize()/2f);
                GameObject caseGO = new GameObject(this.gameObject.scene, "Case "+i+":"+j);
                gridGO.add(caseGO);
                caseGO.transform.positionX = start+i+0.5f; // 0.5 car le transform est au centre du gameobject
                caseGO.transform.positionY = start+j+0.5f;
                caseGO.addComponent(new SpriteRenderer(caseGO, R.drawable.resource_case));
                caseGO.addComponent(new SpriteCollider(caseGO));
                caseGO.addComponent(new OnClickCallBackBehaviour(caseGO, gameObject1 -> {
                    if(case_selector != null && (case_selector.transform.positionX != gameObject1.transform.positionX || case_selector.transform.positionY != gameObject1.transform.positionY)){
                        case_selector.scene.remove(case_selector);
                        if(selectedPion!=null){
                            int index = gridGO.indexOf(gameObject1);
                            int x = index / grid.getGridSize();
                            int y = index % grid.getGridSize();
                            try {
                                facade.moove(selectedPion, new Position(x, y));
                                pionMoved = true;
                                resetSelection();
                            } catch (TargetNotEmpty | NoPossiblePath | PionsNotInGrid targetNotEmpty) {
                                targetNotEmpty.printStackTrace();
                            }
                        }
                    }
                    return null;
                }));

            }
        }
        grid.spawnNext();
    }

    private void initScore() {
        GameObject affichageScore = new GameObject(this.gameObject.scene, "Score");
        affichageScore.transform.positionX = 0f;
        affichageScore.transform.scaleX = 3;
        affichageScore.transform.scaleY = 3;
        affichageScore.transform.positionY = 5f;
        affichageScore.transform.anchorPoint = TransformAnchorPoint.Center;
        scoreTextRenderer = new TextRenderer(affichageScore,"SCORE : 0", Color.valueOf(Color.WHITE));
        affichageScore.addComponent(scoreTextRenderer);
    }

    private void resetSelection() {
        if (case_selector != null) {
            case_selector.scene.remove(case_selector);
            case_selector = null;
            selectedPion = null;
        }
    }

    @Override
    public void Draw(GL10 gl) {
        if(selectedPion != null && !grid.containsPions(selectedPion)){
            GameObject pionsGo = pionsGameObjectHashMap.get(selectedPion);
            pionsGo.scene.remove(pionsGo);
            selectedPion = null;
            case_selector.scene.remove(case_selector);
        }
        super.Draw(gl);
    }

    private int getImageRessource(Pions p){
        if(p!=null){
            switch(p.getType()){
                case 0: return R.drawable.apple;
                case 1: return R.drawable.banana;
                case 2: return R.drawable.cherry;
                case 3: return R.drawable.kiwi;
                case 4: return R.drawable.lemon;
                case 5: return R.drawable.pear;
                case 6: return R.drawable.pineapple;
                default:
                    System.out.println("Type>6");
                    return R.drawable.apple;
            }
        }
        System.out.println("pions null");
        return R.drawable.apple;
    }


    private Pions getPions(GameObject pionsGO){
        for(Map.Entry<Pions, GameObject> entry : pionsGameObjectHashMap.entrySet()){
            if(entry.getValue() == pionsGO){
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void drawGrid(Grid g) {

        float start = -(Camera.main.getSize()/2f);

        for(Map.Entry<Pions, GameObject> entry : pionsGameObjectHashMap.entrySet()){
            if(!g.containsPions(entry.getKey())){
                entry.getValue().scene.remove(entry.getValue());
            }
//            else{
//                // si pas dans la scene : le remettre ?
//                //entry.getValue().transform.positionX+=0.1f;
//            }
        }

        for(int i = 0; i < grid.getGridSize(); i++){
            for(int j = 0; j < grid.getGridSize(); j++){
                Pions p = null;
                try {
                    p = grid.getPions(i,j);
                } catch (OutofBounds outofBounds) {
                    outofBounds.printStackTrace();
                }

                if(p!=null){
                    if(!pionsGameObjectHashMap.containsKey(p)){
                        System.out.println("Pions "+i+":"+j);
                        GameObject pionsGO = new GameObject(this.gameObject.scene, "Pions "+i+":"+j);
                        pionsGO.transform.positionX = start+i+0.5f; // 0.5 car le transform est au centre du gameobject
                        pionsGO.transform.positionY = start+j+0.5f;
                        pionsGO.addComponent(new SpriteRenderer(pionsGO, this.getImageRessource(p)));
                        pionsGO.addComponent(new SpriteCollider(pionsGO));
                        pionsGO.addComponent(new OnClickCallBackBehaviour(pionsGO, gameObject -> {
                            Pions p1 = getPions(gameObject);
                            if(p1 !=null){
                                if (selectedPion != null && selectedPion.equals(p1)) {
                                    resetSelection();
                                }
                                else {
                                    selectedPion = p1;
                                    if (case_selector != null) {
                                        case_selector.scene.remove(case_selector);
                                    }
                                    case_selector = new GameObject(gameObject.scene, "Case selector");
                                    case_selector.addComponent(new SpriteRenderer(case_selector, R.drawable.case_select));
                                    case_selector.addComponent(new SpriteCollider(case_selector));
                                    case_selector.transform.positionX = gameObject.transform.positionX;
                                    case_selector.transform.positionY = gameObject.transform.positionY;
                                    if (pionMoved) {
                                        resetSelection();
                                        pionMoved = false;
                                    }
                                }
                            }

                            return null;
                        }));
                        pionsGameObjectHashMap.put(p, pionsGO);
                    }
                    else{
                        GameObject pionsGO = pionsGameObjectHashMap.get(p);
                        pionsGO.name = "Pions "+i+":"+j;
                        // TODO une animation de déplacement avec fonction de easing ?
                        pionsGO.transform.positionX = start+i+0.5f;
                        pionsGO.transform.positionY = start+j+0.5f;
                        pionsGO.scene.add(pionsGO);
                    }
                }
            }
        }
        if(selectedPion != null && !grid.containsPions(selectedPion)){
            GameObject pionsGo = pionsGameObjectHashMap.get(selectedPion);
            pionsGo.scene.remove(pionsGo);
            selectedPion = null;
            case_selector.scene.remove(case_selector);
        }
    }


    /**
     * UNUSED
     * @param progress : progression de l'animation entre 0 et 1
     * @return double : valeur de la courbe ( 1 -> doit être à sa position finale / 0 -> doit être à sa position initial)
     */
    public double easeInSine(float progress){
        return 1 - cos((progress * PI) / 2);
    }



    @Override
    public void drawNext(List<Pions> pionsList) {
        // TODO
    }

    @Override
    public void gameOver(Grid g) {
        System.out.println("GAME OVER");
        // TODO
    }

    @Override
    public void notifyScoreChanged(int addedScore) {
        score += addedScore;
        scoreTextRenderer.setText("SCORE : "+score);
    }
}
