package com.willowtreeapps.namegame.ui;import android.os.Bundle;import android.support.v7.app.AppCompatActivity;import android.util.Log;import com.willowtreeapps.namegame.R;import com.willowtreeapps.namegame.core.NameGameApplication;import butterknife.ButterKnife;public class NameGameActivity extends AppCompatActivity {    private static final String MAIN_TAG = "NameGameMainTag";    public static GameMode GAME_MODE; // represents the mode of the game    public enum GameMode { // represents the possible modes        CLASSIC,        MATT,        REVERSE,        CUSTOM    }    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.name_game_activity);        ButterKnife.bind(this);        NameGameApplication.get(this).getComponent().inject(this);        getSupportFragmentManager()                .beginTransaction()                .replace(R.id.container, new StartMenuFragment(), "StartMenuFragTag")                .addToBackStack(null)                .commit();    }    @Override    protected void onDestroy() {        super.onDestroy();    }    public static GameMode getGameMode() {        return GAME_MODE;    }    public static void setGameMode(GameMode gameMode) {        GAME_MODE = gameMode;    }    /**     * This method will determine how the back button is handled.     * It points to the onBackPressed() method that is local to     *  each fragment.     */    @Override    public void onBackPressed() {        StartMenuFragment smf = (StartMenuFragment) getSupportFragmentManager()                .findFragmentByTag("StartMenuFragTag");        GamePlayFragment gpf = (GamePlayFragment) getSupportFragmentManager()                .findFragmentByTag("GamePlayFragTag");        if (smf != null){            if (smf.isVisible()) // if current fragment is StartMenuFragment                smf.onBackPressed();        } else if (gpf != null){            if (gpf.isVisible()) // if current fragment is GamePlayFragment                gpf.onBackPressed();        } else if (smf == null && gpf == null) {            Log.e(MAIN_TAG, "Both fragments are null");        } else {            // Don't allow back pressed as            // NameGameActivity layout is empty            //super.onBackPressed();        }    }}