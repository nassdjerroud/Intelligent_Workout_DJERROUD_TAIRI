package intelligent_workout.nassimanis.paris8.intelligent_workout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Nassim on 01/12/2017.
 */

public class RefLevel {
    private int idLevel;
    private static int[][] ref;
    private static int i, j;
    private BufferedReader data;
    private static ArrayList<Integer> initBRed;
    private int nBRed, nLevel,hsMin,hsSec,nbMoves;




    public RefLevel(InputStream input, int idLevel){
        try {
            this.data = new BufferedReader(new InputStreamReader(input));
            this.idLevel = idLevel;
            this.nLevel=0;
            this.i = 0;
            this.j = 0;
            ref= new int [5][5];
            readData();
            data.close();
        }catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    public int getIdLevel() {
        return idLevel;
    }

    public int[][] getRef() {
        return ref;
    }

    public static ArrayList<Integer> getInitBRed() {
        return initBRed;
    }

    public int getnBRed() {
        return nBRed;
    }


    public void readData() {
        String line;
        try {
            line = data.readLine();
            while (!line.contains("LEVEL " + idLevel)) {
                line = data.readLine();
            }
            do {
                line = data.readLine();
                createTerrain(line.toString());
                i++;

            }while (line.charAt(0) != '{');
            createBlockRed(line);
            setHighScore(data.readLine());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void setHighScore(String line){
        String cst []=line.split(":");
        hsMin=Integer.parseInt(cst[0]);
        hsSec=Integer.parseInt(cst[1]);
        try {
            line = data.readLine();
            nbMoves = Integer.parseInt(line);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public void createBlockRed(String line){
        nBRed =(line.length()-1)/4;
        initBRed=new ArrayList<>(nBRed);
        int a=0;
        for (String cst : line.split(",")){
            if(cst.contains("{")) {
                initBRed.add(a,Integer.parseInt(""+cst.charAt(1)));
            }else if (cst.contains("}")) {
                initBRed.add(a,Integer.parseInt("" + cst.charAt(0)));
            }else {
                initBRed.add(a, Integer.parseInt(cst));
            }
            a++;
        }
    }
    public void createTerrain(String line) {
        j=0;
        for (String cst : line.split(",")) {
            switch (cst) {
                case "CST_bleu":
                    ref[i][j] = 0;
                    j++;
                    break;
                case "CST_rouge":
                    ref[i][j] = 1;
                    j++;
                    break;
            }
        }

    }



}