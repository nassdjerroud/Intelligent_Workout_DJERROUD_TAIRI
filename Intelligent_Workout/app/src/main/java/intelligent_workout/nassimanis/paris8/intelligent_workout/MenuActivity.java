package intelligent_workout.nassimanis.paris8.intelligent_workout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.net.URL;

/**
 * Created by Nassim on 01/12/2017.
 */

public class MenuActivity extends AppCompatActivity {

    final boolean[] checkedParams = new boolean[10];
    CharSequence [] ch ;
    private Context context;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        context= getApplicationContext();
        ch = new CharSequence[getResources().getStringArray(R.array.Params).length];
        sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);


    }
    public void goOnActivity(View v, boolean isGame){
        Intent intent;
        if(isGame){
            intent = new Intent(this,MainActivity.class);

        }else {
            intent = new Intent(this, ScoreActivity.class);
        }
        startActivity(intent);
    }

    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.buttonExit:
                finish();
                System.exit(0);
                break;
            case R.id.buttonInfo:
                showPopUpNames();
                break;
            case R.id.buttonParam:
                showPopUpParam();
                break;
            case R.id.buttonPlay:
                goOnActivity(v,true);
                break;
            case R.id.buttonRes:
                goOnActivity(v,true);
                break;
            default :
                goOnActivity(v,false);


        }
    }

    public void showPopUpNames(){
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Informations");
        helpBuilder.setMessage("App by Nassim  and  Anis");


        helpBuilder.setPositiveButton("Ok",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
                // Ne fais rien mais ferme la fenêtre (Dialog)

            }
        });
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();

    }

    public void showPopUpParam(){
        for(int i=0 ; i<sharedPref.getAll().size();i++){
            if(sharedPref.getAll().containsValue(i+1)){
                checkedParams[i]=true;
            }else{
                checkedParams[i]=false;
            }
        }
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Settings");
        ch=getResources().getStringArray(R.array.Params);
        helpBuilder.setMultiChoiceItems(ch,checkedParams,new DialogInterface.OnMultiChoiceClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which,boolean isChecked){
                SharedPreferences.Editor editor = sharedPref.edit();
                if(isChecked){
                    //on decale de 1 car le 0 considerer comme la valeur "non cocher "
                    editor.putInt(ch[which].toString(),which+1);
                }else if(sharedPref.getAll().containsKey(ch[which].toString())){
                    editor.remove(ch[which].toString());

                }
                editor.commit();
            }
        });

        helpBuilder.setPositiveButton("Ok",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
                //On appelle la fonction appsound qui lance ou non la musique

            }
        });
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();

    }
}
