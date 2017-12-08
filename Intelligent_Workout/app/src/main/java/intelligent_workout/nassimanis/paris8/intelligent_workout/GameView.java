package intelligent_workout.nassimanis.paris8.intelligent_workout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * Created by Nassim on 01/12/2017.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback,Runnable {
    SurfaceHolder holder;
    SharedPreferences sharedPref,sharedPrefLevel;
    private MediaPlayer mPlayer = null;
    private Resources mRes;
    private Context mContext;
    private boolean in, isWon;
    Paint paint;
    private int levelMax = 4;
    private int nMoves;
    // tableau modelisant la miniature du jeu
    int[][] miniature;

    // ancres pour pouvoir centrer la miniature du jeu
    int miniTopAnchor;                   // coordonn�es en Y du point d'ancrage de notre carte
    int miniLeftAnchor;                  // coordonn�es en X du point d'ancrage de notre carte
    // ancres pour pouvoir centrer la grille du jeu
    int gridTopAnchor;                   // coordonn�es en Y du point d'ancrage de notre carte
    int gridLeftAnchor;                  // coordonn�es en X du point d'ancrage de notre carte

    // taille de la matrice fixe pour ce jeu
    static int matrixWidth = 5;
    static int matrixHeight = 5;
    static int gridTileSize = 120;
    static int miniTileSize = 40;

    //image
    private Bitmap mblock_b;
    private Bitmap mblock_r;
    private Bitmap gblock_b;
    private Bitmap gblock_r;
    private Bitmap win;
    private Rect barreDeTps;


    // tableau representant la grille du jeu
    int[][] grid;

    //nombre de block rouge
    int nBred;
    // position de reference des block Rouges
    int[][] refBred;

    //Objet  pour le timer
    private Long startTimer, min, sec, spentTime;
    public Long finalTime;
    private Handler handler = new Handler();

    // Thread d'actualisation de la vue ;
    private Thread cv_thread;


    // Declaration Objet RefNiveau
    private RefLevel refLevel;

    //Numero du niveau
    private int level = 1;

    public GameView(Context context, AttributeSet attrs) {

        super(context, attrs);

        // permet d'ecouter les surfaceChanged, surfaceCreated, surfaceDestroyed
        holder = getHolder();
        holder.addCallback(this);

        //chargement des images
        mContext = context;
        mRes = mContext.getResources();
        mblock_b = BitmapFactory.decodeResource(mRes, R.drawable.blue);
        mblock_r = BitmapFactory.decodeResource(mRes, R.drawable.red);
        gblock_b = BitmapFactory.decodeResource(mRes, R.drawable.gblue);
        gblock_r = BitmapFactory.decodeResource(mRes, R.drawable.gred);
        win = BitmapFactory.decodeResource(mRes, R.drawable.win);
        gridTileSize=gblock_b.getWidth();
        miniTileSize=mblock_b.getWidth();
        sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        sharedPrefLevel = context.getSharedPreferences("sauvegarde", Context.MODE_PRIVATE);
        initparameters();
        //début du  timer
        cv_thread = new Thread(this);
        //prise de focus pour la gestion des touches
        setFocusable(true);


    }
    private void initLevel(){
        int defaultValue=getResources().getInteger(R.integer.saved_level_default);
        level =sharedPrefLevel.getInt(getResources().getString(R.string.saved_level),defaultValue);
        if(level==1) {
            refLevel = new RefLevel(mContext.getResources().openRawResource(R.raw.levels), level);

        }else{
            refLevel=new RefLevel(mContext.getResources().openRawResource(R.raw.save), level);
        }
        loadlevel();


    }
    private void loadlevel(){
        miniature = refLevel.getRef();
        Toast t = Toast.makeText(mContext, "Level " + level, Toast.LENGTH_LONG);
        t.show();
        nBred = refLevel.getnBRed();
        System.out.println(nBred+" nombre carré rouge");
        startTimer = System.currentTimeMillis();
        nMoves = 0;
        appSound();

        createGridAleatoire();
    }
    //Chargement du niveau a partir du tableau de reference des niveau
    private void nextlevel() {
        refLevel = new RefLevel(mContext.getResources().openRawResource(R.raw.levels), level);
        loadlevel();
    }

    public void initparameters() {
        paint = new Paint();
        paint.setDither(true);
        paint.setColor(0xFFFFFF00);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        paint.setTextAlign(Paint.Align.LEFT);
        miniature = new int[matrixHeight][matrixWidth];
        grid = new int[matrixHeight][matrixWidth];
        initLevel();

    }

    // Dessiner la miniature du jeu
    private void paintMiniature(Canvas canvas) {
        for (int i = 0; i < matrixHeight; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                switch (miniature[i][j]) {
                    case 0:
                        canvas.drawBitmap(mblock_b, miniLeftAnchor + i * miniTileSize, miniTopAnchor + j * miniTileSize, paint);
                        break;
                    case 1:
                        canvas.drawBitmap(mblock_r, miniLeftAnchor + i * miniTileSize, miniTopAnchor + j * miniTileSize, paint);
                        break;
                }
            }
        }
    }

    // Dessine la barre d'affichage du timer
    private void paintInfo(Canvas canvas) {
        paint.setColor(0xff0000);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        String text = "Time " + min + ":" + sec + "         Moves: " + nMoves+"     I_W";
        canvas.drawText(text, 20, 80, paint);
    }

    //Remise à zéro de la grille (tout en bleu)
    private void blueGrid(){
        //remplissage de la fin de la grille en bleu
        for (int i=0 ; i < matrixHeight; i++) {
            for (int j=0 ; j < matrixWidth; j++) {
                grid[i][j] = 0;
            }
        }
    }

    // Cree la grille aléatoire du jeu
    private void createGridAleatoire() {
        int nbr = 0;
        while (nbr != nBred) {
            blueGrid();
            for (int i = 0; i < matrixHeight; i++) {
                for (int j = 0; j < matrixWidth; j++) {
                    if (nbr != nBred) {
                        Random r = new Random();
                        int n = r.nextInt(2);
                        if (n == 1) {
                            grid[i][j] = n;
                            nbr++;
                        }
                    }

                }
            }
        }

    }

    // Dessine la grille aléatoire du jeu
    private void paintGrid(Canvas canvas) {
        for (int i = 0; i < matrixHeight; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                switch (grid[i][j]) {
                    case 0:
                        canvas.drawBitmap(gblock_b, gridLeftAnchor + j * gridTileSize, gridTopAnchor + i * gridTileSize, null);
                        break;
                    case 1:
                        canvas.drawBitmap(gblock_r, gridLeftAnchor + j * gridTileSize, gridTopAnchor + i * gridTileSize, null);
                        break;
                }
            }
        }

    }

    // permet d'identifier si la partie est gagnee
    private boolean isWon() {
        int nbredGood = 0;
        ArrayList<Integer> a = refLevel.getInitBRed();
        for (int i = 0; i < a.size()-1; i++) {
            if (grid[a.get(i)][a.get(i + 1)] == 1) {
                nbredGood++;
                i++;
            }
        }
        /*finalTime =System.currentTimeMillis()+1;
        min = (finalTime / 1000) / 60;
        sec = (finalTime / 1000) % 60;*/
        if (nbredGood == nBred) {
            finalTime=spentTime;
            isWon = true;

        } else {
            isWon = false;
        }
        return isWon;
    }

    // dessin du gagne si gagne
    private void paintWin(Canvas canvas) {
        int tileSize = 20;
        canvas.drawBitmap(win, gridLeftAnchor + 3 * tileSize, gridTopAnchor + 4 * tileSize, null);

    }

    protected void nDraw(Canvas canvas) {
        barreDeTps = new Rect(0, 0, getWidth(), 100);
        miniTopAnchor = 10 + barreDeTps.height();
        miniLeftAnchor = (getWidth() - matrixWidth * miniTileSize) / 2; //CENTRER LA MINIATURE au milieu horizontalement
        gridTopAnchor = miniTopAnchor + miniTileSize * matrixHeight + 100;
        gridLeftAnchor = (getWidth() - gridTileSize * matrixHeight) / 2;//pour centrer la grille de jeu
        canvas.drawRGB(0, 0, 0);
        if (isWon()) {
            paintWin(canvas);

        } else {
            paintInfo(canvas);
            paintMiniature(canvas);
            paintGrid(canvas);

        }
    }

    // callback sur le cycle de vie de la surfaceview
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //la surface change ex quand l'utilsateur tourne son téléphone
    }

    public void surfaceCreated(SurfaceHolder arg0) {

        Log.i("-> FCT <-", "surfaceCreated");
        in = true;
        cv_thread.start();
    }


    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.i("-> FCT <-", "surfaceDestroyed");
        in = false;
        SharedPreferences.Editor editor=sharedPrefLevel.edit();
        editor.putInt(getResources().getString(R.string.saved_level),level);
        editor.commit();
    }


    public void run() {
        while (in) {
            Canvas c = null;
            spentTime = System.currentTimeMillis() - startTimer;
            min = (spentTime / 1000) / 60;
            sec = (spentTime / 1000) % 60;
            /*finalTime =System.currentTimeMillis()+1;
        min = (finalTime / 1000) / 60;
        sec = (finalTime / 1000) % 60;*/
            try {
                // holder.setFixedSize(500,900);
                c = holder.lockCanvas();
                synchronized (holder) {


                    nDraw(c);
                }
            } finally {
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
                }
            }
            try {
                cv_thread.sleep(20);
            } catch (InterruptedException ie) {
                Log.e("-> RUN <-", "PB DANS RUN");
            }

        }
    }

    public void moveBlocks(String move, int x, int y) {
        nMoves++;
        int i, tmp;
        switch (move) {
            case "right":
                for (i = 0; i < matrixWidth - 1;
                     i++) {
                    tmp = grid[y][i + 1];
                    grid[y][i + 1] = grid[y][0];
                    grid[y][0] = tmp;
                }
                break;
            case "left":
                for (i = 3; 0 <= i; i--) {
                    tmp = grid[y][i + 1];
                    grid[y][i + 1] = grid[y][0];
                    grid[y][0] = tmp;
                }
                break;
            case "up":
                for (i = 3; 0 <= i; i--) {
                    tmp = grid[i + 1][x];
                    grid[i + 1][x] = grid[0][x];
                    grid[0][x] = tmp;
                }
                break;
            case "down":
                for (i = 0; i < matrixWidth - 1; i++) {
                    tmp = grid[i + 1][x];
                    grid[i + 1][x] = grid[0][x];
                    grid[0][x] = tmp;
                }
                break;
        }

    }

    float x1 = 0, y1 = 0;

    // fonction permettant de recuperer les evenements tactiles
    public boolean onTouchEvent(MotionEvent event) {
        float x2, y2, dx, dy;
        String direction;
        List<Integer> coordonees;
        if (isWon) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    if ((gridLeftAnchor + 60 <= x1) && (x1 <= gridLeftAnchor + 60 + win.getWidth())) {
                        if ((gridTopAnchor + 80 <= y1) && (y1 <= gridLeftAnchor + 80 + getHeight())) {
                            if (level == levelMax) {
                                level = 1;
                                nextlevel();
                            } else {
                                level++;
                                nextlevel();
                            }
                        }
                    }
                    break;
            }

        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    x2 = event.getX();
                    y2 = event.getY();
                    if ((x1 != x2) || (y2 != y1)) {
                        dx = x2 - x1;
                        dy = y2 - y1;
                        if (Math.abs(dx) > Math.abs(dy)) {
                            if (dx > 0) {
                                direction = "right";
                            } else {
                                direction = "left";
                            }
                        } else {
                            if (dy > 0) {
                                direction = "down";
                            } else {
                                direction = "up";
                            }
                        }
                        coordonees = findCaseInMatrix(x1, y1);
                        if ((coordonees.get(0) < matrixWidth) && (coordonees.get(1) < matrixHeight)) {
                            moveBlocks(direction, coordonees.get(0), coordonees.get(1));
                        } else {
                            //envoi un message a l'utilisateur pour lui dire qu ele mouvements est en dehors de grille
                            Toast t = Toast.makeText(mContext, "Mouvement en dehors de la grille de jeu", Toast.LENGTH_SHORT);
                            t.show();
                        }
                        break;
                    }
            }
        }
        return true;
        //super.onTouchEvent(event);
    }

    public List<Integer> findCaseInMatrix(float x, float y) {
        int i = 0;
        boolean isNotfindY = true, isNotfindX = true;
        while (isNotfindY && (i < gridTileSize * 5)) {
            boolean pp = gridTopAnchor + i <= y;
            boolean pg = y < gridTopAnchor + i + gridTileSize;
            if (pp && pg) {
                isNotfindY = false;
                y = i / gridTileSize;
            } else {
                i += gridTileSize;
            }
        }
        i = 0;
        while (isNotfindX && (i < gridTileSize * 5)) {
            boolean pp = gridLeftAnchor + i <= x;
            boolean pg = x < gridLeftAnchor + i + gridTileSize;
            if (pp && pg) {
                isNotfindX = false;
                x = i / gridTileSize;
            } else {
                i += gridTileSize;
            }
        }
        List<Integer> coordonnees = new ArrayList<Integer>();
        coordonnees.add(0, (int) x);
        coordonnees.add(1, (int) y);
        return coordonnees;


    }

    private void appSound() {
        int sound=0;
        String s =sharedPref.getAll().toString();
        for (String st :s.split(",")){
            if(st.contains("Sound")){
                if(st.contains("1")){
                    sound=1;
                }else{
                    sound=0;

                }
            }
        }
        if (sound == 1) {
            try {
                int rId = getResources().getIdentifier("sound", "raw", mContext.getPackageName());
                mPlayer = MediaPlayer.create(this.mContext, rId);//
            } catch (Exception e) {
                mPlayer = null;
            }
            if (mPlayer != null) {
                mPlayer.setVolume(5, 5);
                mPlayer.setLooping(true);
                mPlayer.start();
            }
        }


    }
}

