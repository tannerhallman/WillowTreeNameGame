package com.willowtreeapps.namegame.ui;import android.content.DialogInterface;import android.os.Bundle;import android.support.v4.app.FragmentManager;import android.support.v7.app.AlertDialog;import android.support.v7.app.AppCompatActivity;import android.util.Log;import com.willowtreeapps.namegame.R;import com.willowtreeapps.namegame.core.NameGameApplication;import butterknife.ButterKnife;public class NameGameActivity extends AppCompatActivity {    private static final String TAG = "Activity Tag";    public enum GameMode { // represents the possible modes        CLASSIC,        MATT,        REVERSE,        INVINCIBLE    }    private static GameMode GAME_MODE; // represents the mode of the game    FragmentManager fragManager;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.name_game_activity);        ButterKnife.bind(this);        NameGameApplication.get(this).getComponent().inject(this);        //Log.i(TAG, "onCreate Called");        fragManager = getSupportFragmentManager();        if (savedInstanceState != null) {            //Restore the fragment's instance            if (getSupportFragmentManager()                    .getFragment(savedInstanceState, "GamePlayFragTag") != null){                getSupportFragmentManager()                        .beginTransaction()                        .replace(R.id.container, fragManager.findFragmentByTag("GamePlayFragTag"))                        .addToBackStack(null)                        .commit();            } else if (getSupportFragmentManager()                    .getFragment(savedInstanceState, "StartMenuFragTag") != null) {                getSupportFragmentManager()                        .beginTransaction()                        .replace(R.id.container, fragManager.findFragmentByTag("StartMenuFragTag"))                        .addToBackStack(null)                        .commit();            }        } else {            getSupportFragmentManager()                    .beginTransaction()                    .replace(R.id.container, new StartMenuFragment(), "StartMenuFragTag")                    .addToBackStack(null)                    .commit();        }    }    @Override    protected void onPause() {        super.onPause();    }    @Override    protected void onSaveInstanceState(Bundle outState) {        //Log.i(TAG, "onSaveInstanceState called");        super.onSaveInstanceState(outState);        //Save the fragment's instance        if (getSupportFragmentManager().findFragmentByTag("GamePlayFragTag") != null) {            // Log.i(TAG, "Saving the gameplayfragment");            getSupportFragmentManager().putFragment(outState, "GamePlayFragTag",                    getSupportFragmentManager().findFragmentByTag("GamePlayFragTag"));        } else {            //Log.i(TAG, "onSaveInstance() not saving the gameplay frag cause its null");        }    }    public static GameMode getGameMode() { return GAME_MODE; }    public static void setGameMode(GameMode gameMode) {        GAME_MODE = gameMode;    }    /**     * This method will determine how the back button is handled.     * It points to the onBackPressed() method that is local to     *  each fragment.     */    @Override    public void onBackPressed() {        // TODO: Fix back button. Currently crashes        Log.i(TAG, "onBack was pressed");        final GamePlayFragment gpf = (GamePlayFragment) fragManager                .findFragmentByTag("GamePlayFragTag");        if (gpf != null){            new AlertDialog.Builder(this)                    .setTitle("Quit game")                    .setMessage("Are you sure you want to return to the menu?")                    .setCancelable(false)                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {                        @Override                        public void onClick(DialogInterface dialog, int which) {                            fragManager.getFragments().toString();                            fragManager.popBackStack();                            fragManager.getFragments().toString();                            try {                                fragManager.beginTransaction().                                        remove(gpf);                                setGameMode(null);                            } catch (Exception e) {                                e.printStackTrace();                            }                        }                    })                    .setNegativeButton("no", new DialogInterface.OnClickListener() {                        @Override                        public void onClick(DialogInterface dialog, int which) {                            dialog.cancel();                        }                    })                    .show();        } else {            // Don't allow back pressed as            // there is no screen before menu fragment            //super.onBackPressed();        }    }}