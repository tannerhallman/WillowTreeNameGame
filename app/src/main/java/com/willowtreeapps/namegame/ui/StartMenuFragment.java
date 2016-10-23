package com.willowtreeapps.namegame.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.willowtreeapps.namegame.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by th on 10/10/16.
 */

public class StartMenuFragment extends Fragment {

    private final String TAG = "StartMenuFragTag";

    @BindView(R.id.appTitleTV) TextView appTitleTV;
    @BindView(R.id.imageView) ImageView imageView;
    @BindView(R.id.classicButton) Button classicButton;
    @BindView(R.id.mattButton) Button mattButton;
    @BindView(R.id.reverseButton) Button reverseButton;
    @BindView(R.id.invincibleButton) Button invincibleButton;


    /**
     * A method that instantiates the menu
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.i(TAG, "onCreate was called");
        if (getActivity() // if mid game, return to gameplay
                .getSupportFragmentManager()
                .findFragmentByTag("GamePlayFragTag") != null){

            getActivity() //get the gameplay fragment
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container,
                            getActivity()
                            .getSupportFragmentManager()
                            .findFragmentByTag("GamePlayFragTag"))
                    .addToBackStack(null)
                    .commit();
        } else {
            //Log.i(TAG, getActivity().getSupportFragmentManager().getFragments().toString());
        }
    }

    /**
     * A method that inflates the view and
     *  binds them with butterknife.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.i(TAG, "onCreateView was called");
        View view = inflater.inflate(R.layout.start_menu_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    /**
     * A butterknife click listener for the menu buttons
     * @param view
     */
    @OnClick({R.id.classicButton, R.id.mattButton, R.id.reverseButton, R.id.invincibleButton})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.classicButton:
                NameGameActivity.setGameMode(NameGameActivity.GameMode.CLASSIC);
                break;
            case R.id.mattButton:
                NameGameActivity.setGameMode(NameGameActivity.GameMode.MATT);
                break;
            case R.id.reverseButton:
                NameGameActivity.setGameMode(NameGameActivity.GameMode.REVERSE);
                break;
            case R.id.invincibleButton:
                NameGameActivity.setGameMode(NameGameActivity.GameMode.INVINCIBLE);
                break;
        }
        //Log.i(TAG, "On Click chosen mode: " + NameGameActivity.getGameMode().toString());

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new GamePlayFragment(), "GamePlayFragTag")
                .addToBackStack(null)
                .commit();
    }
/*
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated was called");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroyCalled");
        super.onDestroy();
    }*/
}
