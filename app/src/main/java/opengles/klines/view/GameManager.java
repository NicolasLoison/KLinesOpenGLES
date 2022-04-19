package opengles.klines.view;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import opengles.klines.GameOverActivity;
import opengles.klines.OpenGLES30Activity;
import opengles.klines.R;
import opengles.klines.modele.Drawer;
import opengles.klines.modele.Facade;
import opengles.klines.modele.Grid;
import opengles.klines.modele.Tile;
import opengles.klines.modele.Position;
import opengles.klines.modele.Score;
import opengles.klines.exception.NoPossiblePath;
import opengles.klines.exception.OutofBounds;
import opengles.klines.exception.TilesNotInGrid;
import opengles.klines.exception.TargetNotEmpty;

public class GameManager extends MonoBehaviour implements Score, Drawer {

    Facade facade;
    Grid grid;
    List<GameObject> gridCanvas, nextCanvas, nextsSprite;
    GameObject case_selector;
    Tile selectedPion;
    TextRenderer scoreTextRenderer;
    int score = 0;
    private final HashMap<Tile, GameObject> tileGameObjectHashMap = new HashMap<>();
    private boolean pionMoved = false;
    public OpenGLES30Activity activity;
    private final SharedPreferences prefs;

    public GameManager(GameObject gameObject, OpenGLES30Activity activity) {
        super(gameObject);
        this.activity = activity;
        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);
        facade = new Facade(this, this);
        grid = facade.createGrid(prefs.getInt("gridType", 9), prefs);
        setScoreCanvas();
        setGridCanvas();
        setNextCanvas();
        grid.spawnNext();
    }

    private void setGridCanvas() {
        gridCanvas = new ArrayList<>();
        for(int i = 0; i<grid.getGridSize(); i++){
            for(int j=0; j< grid.getGridSize(); j++){
                float start = -(Camera.main.getSize()/2f);
                GameObject caseGO = new GameObject(this.gameObject.scene, "Case "+i+":"+j);
                gridCanvas.add(caseGO);
                caseGO.transform.positionX = start+i+0.5f; // 0.5 car le transform est au centre du gameobject
                caseGO.transform.positionY = start+j+0.5f;
                if ((i*grid.getGridSize()+j) % 2 == 0) {
                    caseGO.addComponent(new SpriteRenderer(caseGO, R.drawable.casepaire));
                } else {
                    caseGO.addComponent(new SpriteRenderer(caseGO, R.drawable.caseimpaire));
                }
                caseGO.addComponent(new SpriteCollider(caseGO));
                caseGO.addComponent(new OnClickCallBackBehaviour(caseGO, gameObject1 -> {
                    if(case_selector != null && (case_selector.transform.positionX != gameObject1.transform.positionX || case_selector.transform.positionY != gameObject1.transform.positionY)){
                        case_selector.scene.remove(case_selector);
                        if(selectedPion!=null){
                            int index = gridCanvas.indexOf(gameObject1);
                            int x = index / grid.getGridSize();
                            int y = index % grid.getGridSize();
                            try {
                                facade.moove(selectedPion, new Position(x, y));
                                pionMoved = true;
                                resetSelection();
                            } catch (TargetNotEmpty targetNotEmpty) {
                                Toast.makeText(getActivity(), "La case n'est pas vide", Toast.LENGTH_SHORT).show();
                            } catch (NoPossiblePath targetNotEmpty) {
                                Toast.makeText(getActivity(), "Chemin impossible", Toast.LENGTH_SHORT).show();
                            } catch (TilesNotInGrid targetNotEmpty) {
                                targetNotEmpty.printStackTrace();
                            }
                        }
                    }
                    return null;
                }));
            }
        }
    }

    private void setNextCanvas() {
        this.nextCanvas = new ArrayList<>();
        this.nextsSprite = new ArrayList<>();
        for (int i = 0; i < grid.getNbNext(); i++){
            float posY =  -1 - grid.getGridSize()/2f;
            GameObject caseNext = new GameObject(this.gameObject.scene);
            caseNext.transform.positionX = -((grid.getNbNext()-1)/2f)+i;
            // centré malgré le nombre de suivants qui peut changer
            caseNext.transform.positionY = posY;
            GameObject fruitSprite = new GameObject(this.gameObject.scene);
            fruitSprite.transform.positionX = caseNext.transform.positionX;
            fruitSprite.transform.positionY = posY;
            if (i % 2 == 0) {
                caseNext.addComponent(new SpriteRenderer(caseNext, R.drawable.casepaire));
            } else {
                caseNext.addComponent(new SpriteRenderer(caseNext, R.drawable.caseimpaire));
            }
            this.nextCanvas.add(caseNext);
            this.nextsSprite.add(fruitSprite);
        }
    }

    private void setScoreCanvas() {
        GameObject affichageScore = new GameObject(this.gameObject.scene, "Score");
        affichageScore.transform.positionX = 0f;
        affichageScore.transform.scaleX = 3;
        affichageScore.transform.scaleY = 3;
        affichageScore.transform.positionY = grid.getGridSize()/2f + 1;
        affichageScore.transform.anchorPoint = TransformAnchorPoint.Center;
        scoreTextRenderer = new TextRenderer(affichageScore,"Score : 0", Color.valueOf(Color.WHITE));
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
        if(selectedPion != null && !grid.containsTiles(selectedPion)){
            GameObject tileGo = tileGameObjectHashMap.get(selectedPion);
            tileGo.scene.remove(tileGo);
            selectedPion = null;
            case_selector.scene.remove(case_selector);
        }
        super.Draw(gl);
    }

    private int getImageRessource(Tile p){
        if(p!=null){
            switch(p.getType()){
                case 0: return R.drawable.apple;
                case 1: return R.drawable.banana;
                case 2: return R.drawable.cherry;
                case 3: return R.drawable.kiwi;
                case 4: return R.drawable.pineapple;
                case 5: return R.drawable.pear;
                case 6: return R.drawable.lemon;
                default:
                    System.out.println("Type>6");
                    return R.drawable.apple;
            }
        }
        System.out.println("tile null");
        return R.drawable.apple;
    }

    public Activity getActivity() {
        return this.activity;
    }


    private Tile getTiles(GameObject tileGO){
        for(Map.Entry<Tile, GameObject> entry : tileGameObjectHashMap.entrySet()){
            if(entry.getValue() == tileGO){
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void drawGrid(Grid g) {

        float start = -(Camera.main.getSize()/2f);

        for(Map.Entry<Tile, GameObject> entry : tileGameObjectHashMap.entrySet()){
            if(!g.containsTiles(entry.getKey())){
                entry.getValue().scene.remove(entry.getValue());
            }
//            else{
//                // si pas dans la scene : le remettre ?
//                //entry.getValue().transform.positionX+=0.1f;
//            }
        }

        for(int i = 0; i < grid.getGridSize(); i++){
            for(int j = 0; j < grid.getGridSize(); j++){
                Tile p = null;
                try {
                    p = grid.getTiles(i,j);
                } catch (OutofBounds outofBounds) {
                    outofBounds.printStackTrace();
                }

                if(p!=null){
                    if(!tileGameObjectHashMap.containsKey(p)){
                        GameObject tileGO = new GameObject(this.gameObject.scene, "Tile "+i+":"+j);
                        tileGO.transform.positionX = start+i+0.5f; // 0.5 car le transform est au centre du gameobject
                        tileGO.transform.positionY = start+j+0.5f;
                        tileGO.addComponent(new SpriteRenderer(tileGO, this.getImageRessource(p)));
                        tileGO.addComponent(new SpriteCollider(tileGO));
                        tileGO.addComponent(new OnClickCallBackBehaviour(tileGO, gameObject -> {
                            Tile p1 = getTiles(gameObject);
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
                                    case_selector.addComponent(new SpriteRenderer(case_selector, R.drawable.selected_tile));
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
                        tileGameObjectHashMap.put(p, tileGO);
                    }
                    else{
                        GameObject tileGO = tileGameObjectHashMap.get(p);
                        tileGO.name = "Tile "+i+":"+j;
                        tileGO.transform.positionX = start+i+0.5f;
                        tileGO.transform.positionY = start+j+0.5f;
                        tileGO.scene.add(tileGO);
                    }
                }
            }
        }
        if(selectedPion != null && !grid.containsTiles(selectedPion)){
            GameObject tileGo = tileGameObjectHashMap.get(selectedPion);
            tileGo.scene.remove(tileGo);
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
    public void drawNext(List<Tile> nextTiles) {
        // on remplace le sprite à chaque fois pour pas surcharger la case de sprite
        for (int i = 0; i < grid.getNbNext(); i++){
            GameObject fruitGO = this.nextsSprite.get(i);
            SpriteRenderer fruitSprite = new SpriteRenderer(fruitGO, this.getImageRessource(nextTiles.get(i)));
            if (fruitGO.componentList.isEmpty())
                fruitGO.addComponent(fruitSprite);
            else
                fruitGO.componentList.set(0, fruitSprite);
        }
    }

    @Override
    public void gameOver() {
        saveScore();
        activity.startActivity(new Intent(activity, GameOverActivity.class));
        activity.finish();
    }

    private void saveScore() {
        SharedPreferences.Editor editor  = prefs.edit();
        String highscoreKeyGridType = String.format(Locale.getDefault(), "highscore%d", grid.getGridSize());

        int highscore = prefs.getInt(highscoreKeyGridType, 0);
        if (score > highscore) {
            editor.putInt(highscoreKeyGridType, score);
            editor.putBoolean("isNewBest", true);
        }
        editor.putInt("lastScore", score);
        editor.apply();
    }

    @Override
    public void notifyScoreChanged(int addedScore) {
        score += addedScore;
        scoreTextRenderer.setText("Score : "+score);
    }
}
