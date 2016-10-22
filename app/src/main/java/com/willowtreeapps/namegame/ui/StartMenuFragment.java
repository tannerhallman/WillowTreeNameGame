package com.willowtreeapps.namegame.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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

    private final String FRAG_TAG = "StartMenuFragTag";

    @BindView(R.id.appTitleTV) TextView appTitleTV;
    @BindView(R.id.imageView) ImageView imageView;
    @BindView(R.id.classicButton) Button classicButton;
    @BindView(R.id.mattButton) Button mattButton;
    @BindView(R.id.reverseButton) Button reverseButton;
    @BindView(R.id.customButton) Button customButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(FRAG_TAG, "onCreate was called");
        if (getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag("GamePlayFragTag") != null){

            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container,
                            getActivity()
                            .getSupportFragmentManager()
                            .findFragmentByTag("GamePlayFragTag"))
                    .addToBackStack(null)
                    .commit();
        } else {
            Log.i(FRAG_TAG, "Gameplayfrag was null");
            Log.i(FRAG_TAG, getActivity().getSupportFragmentManager().getFragments().toString());
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(FRAG_TAG, "onCreateView was called");
        View view = inflater.inflate(R.layout.start_menu_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.classicButton, R.id.mattButton, R.id.reverseButton, R.id.customButton})
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
            case R.id.customButton:
                NameGameActivity.setGameMode(NameGameActivity.GameMode.CUSTOM);
                break;
        }
        Log.i(FRAG_TAG, "On Click chosen mode: " + NameGameActivity.getGameMode().toString());

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new GamePlayFragment(), "GamePlayFragTag")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i(FRAG_TAG, "onViewCreated was called");
    }

    @Override
    public void onDestroy() {
        Log.i(FRAG_TAG, "onDestroyCalled");
        super.onDestroy();
    }
}
