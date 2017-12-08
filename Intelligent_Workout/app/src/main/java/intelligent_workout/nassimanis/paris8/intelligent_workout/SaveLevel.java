package intelligent_workout.nassimanis.paris8.intelligent_workout;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Nassim on 01/12/2017.
 */

public class SaveLevel {

    private int idLevel;
    private static int[][] ref;
    private static int i, j;
    private BufferedWriter data;
    private static ArrayList<Integer> initBRed;
    private int hsMin,hsSec,nbMoves;

    //on cree cet objet avec le fichier save.txt
    public SaveLevel(OutputStream output,int idLevel,ArrayList<Integer> initBRed,int hsMin,int hsSec,int nbMoves,int [][]ref){
        try {
            this.data = new BufferedWriter(new OutputStreamWriter(output));
            this.idLevel = idLevel;
            this.hsMin=hsMin;
            this.hsSec=hsSec;
            this.nbMoves=nbMoves;
            this.initBRed=initBRed;
            this.ref=ref;
            writeData();
            data.close();
        }catch (Exception e) {
            System.out.println(e.toString());
        }
    }



    public void writeData() {
        try {
            data.write(";LEVEL "+idLevel);
            data.flush();
            for(j=0;j<5;j++){
                data.write(saveMatrix(ref));
                data.flush();
            }
            data.write(ref.toString());
            data.flush();
            data.write(nbMoves);
            data.write(hsMin+":"+hsSec);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String saveMatrix(int [][] ref) {
        String line="";
        for(int i=0;i<5;i++){
            switch (ref[i][j]){
                case 0:
                    line+="CST_bleu,";
                    break;
                case 1:
                    line+="CST_rouge";
                    break;
            }
        }
        return line;

    }


}