package com.willowtreeapps.namegame.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.core.ListRandomizer;
import com.willowtreeapps.namegame.core.NameGameApplication;
import com.willowtreeapps.namegame.network.api.Person;
import com.willowtreeapps.namegame.network.api.PersonRepository;
import com.willowtreeapps.namegame.util.CircleBorderTransform;
import com.willowtreeapps.namegame.util.GameSession;
import com.willowtreeapps.namegame.util.Ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GamePlayFragment extends Fragment {
    private static final Interpolator OVERSHOOT = new OvershootInterpolator();
    private static final String FRAG_TAG = "GamePlayFragmentTag";

    @Inject ListRandomizer listRandomizer;
    @Inject Picasso picasso;
    @Inject PersonRepository repo;

    @BindView(R.id.title) TextView title;
    @BindView(R.id.question) TextView question;

    // contains 2 horizontal linear layouts [1 toprow and 1 bottomrow]
    @BindView(R.id.face_container) LinearLayout faceContainer;
    @BindView(R.id.topRow) LinearLayout topRow;
    @BindView(R.id.bottomrow) LinearLayout bottomRow;
    @BindView(R.id.score) TextView score;
    @BindView(R.id.averageTime) TextView averageTime;


    //The amount of faces that are displayed (Should correspond to the amount of FrameLayouts)
    private int faceCount;

    // A list of the framelayouts that contain both the progress bar and the imageview
    private List<FrameLayout> frames = new ArrayList<>(faceCount);
    // A list of the allPersons that are currently displayed
    private List<Person> personsInQuestion = new ArrayList<>(faceCount);
    // A list of *all* of the allPersons that have been loaded by the API
    private List<Person> allPersons = new ArrayList<>();
    private List<Person> mattOnly;

    // A state of the game
    private GameSession gameSession;

    // the size of the images
    int imageSize;

    NameGameActivity.GameMode mode;

    private NumberFormat formatter = new DecimalFormat("#0.00");


    // Create a reference to the listener so that it can be used to unsubscribe in onDestory
    private PersonRepository.Listener personRepoListener = new PersonRepository.Listener() {
            @Override
            public void onLoadFinished(@NonNull List<Person> people) {
                GamePlayFragment.this.allPersons = people;
                loadNewContent();
            }

            @Override
            public void onError(@NonNull Throwable error) {
                Log.e(FRAG_TAG, error.getMessage());
            }
        };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //TODO: Implement horizontal viewing with the savedInstanceState
        super.onCreate(savedInstanceState);
        NameGameApplication.get(getActivity()).getComponent().inject(this);
        if (savedInstanceState == null){
            imageSize = (int) Ui.convertDpToPixel(100, getContext());
            repo.register(personRepoListener);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_play_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mode = NameGameActivity.getGameMode();
        switch (mode) {
            case CLASSIC:
                faceCount = 5;
                title.setText(R.string.gameModeClassic);
                break;
            case MATT:
                mattOnly = new ArrayList<>();
                faceCount = 5;
                title.setText(R.string.gameModeMatt);
                break;
            case REVERSE:
                faceCount = 1;
                title.setText(R.string.gameModeReverse);
                break;
            case CUSTOM:
                title.setText(R.string.gameModeCustom);
                break;
            default:
                faceCount = 5;
                title.setText("Willow Tree Name GameSession");
                break;
        }
        //Hide the views until data loads
        //title.setAlpha(0);

        //setup both of the rows of imageviews
        setupViewRows();
        gameSession = new GameSession(faceCount, this);
    }

    /**
     * An umbrella method that is used to load new content into imageviews and
     *  load new questions
     */
    public void loadNewContent(){
        personsInQuestion.clear();

        averageTime.setText(formatter.format(gameSession.getAverageTime()));
        score.setText(Integer.toString(gameSession.getQuestionsCorrect()));

        switch (mode) {
            case CLASSIC :
                personsInQuestion = listRandomizer.pickN(allPersons, faceCount);
                setImages(frames, personsInQuestion);
                question.setText("Who is " + personsInQuestion.get(gameSession.getCurrentRando()).getName() + "?");

                // 5 Log.i(FRAG_TAG, "Size of frames when passing to resetSessionState " + Integer.toString(frames.size()));
                gameSession.resetSessionState();

                break;

            case MATT:
                for (Person p : allPersons){
                    if (p.getName().substring(0,3).equalsIgnoreCase("Mat"))
                        mattOnly.add(p);
                }

                personsInQuestion = listRandomizer.pickN(mattOnly, faceCount);
                setImages(frames, personsInQuestion);

                gameSession.resetSessionState();
                break;
            case REVERSE:
                faceCount = 1;

            default:
                Log.e(FRAG_TAG, "Have not implemented any other game types");
                break;
        }



    }
    /**
     * A method to handle when a person is selected
     *
     * @param view   The view that was selected
     * @param person The person that was selected
     */
    private void onPersonSelected(@NonNull View view, @NonNull Person person) {
        //TODO evaluate whether it was the right person and make an action based on that
        if (person.equals(personsInQuestion.get(gameSession.getCurrentRando()))){
            Log.i(FRAG_TAG, "Correct!");
            gameSession.setQuestionsCorrect(gameSession.getQuestionsCorrect() + 1);
            gameSession.cancelTimer();
            gameSession.updateAverage();
            loadNewContent();
        } else {
            Log.i(FRAG_TAG, "selected the wrong person " + person.getName());
        }
    }

    /**
     * A method to initially setup the rows of ImageViews and Progressbars so that
     * images can be loaded into them.
     */
    private void setupViewRows() {
        int toprowcount = topRow.getChildCount();
        int bottomrowcount = bottomRow.getChildCount();

            for (int j = 0; j < toprowcount; j++){
                FrameLayout frame = (FrameLayout) topRow.getChildAt(j);
                //frame.setTag("FrameLayout" + Integer.toString(j));
                ImageView face = (ImageView) frame.getChildAt(0);
                //face.setTag("face" + Integer.toString(j));
                face.setVisibility(View.VISIBLE);
                face.setOnClickListener(new MasterClickListener());

                ProgressBar progressBar = (ProgressBar) frame.getChildAt(1);
                progressBar.setMinimumHeight(imageSize);
                progressBar.setMinimumWidth(imageSize);

                progressBar.setVisibility(View.VISIBLE);
                frames.add(frame);
                face.setScaleX(0);
                face.setScaleY(0);
            }

            for (int j = 0; j < bottomrowcount; j++){
                FrameLayout frame = (FrameLayout) bottomRow.getChildAt(j);
                //frame.setTag("FrameLayout" + Integer.toString(j));
                ImageView face = (ImageView) frame.getChildAt(0);
                //face.setTag("face" + Integer.toString(j));
                face.setVisibility(View.VISIBLE);
                face.setOnClickListener(new MasterClickListener());

                ProgressBar progressBar = (ProgressBar) frame.getChildAt(1);

                progressBar.setVisibility(View.VISIBLE);
                frames.add(frame);
                face.setScaleX(0);
                face.setScaleY(0);
            }
    }

    /**
     * A method for setting the images from allPersons into the imageviews
     */
    private void setImages(List<FrameLayout> frames, List<Person> people) {
        //Log.i(FRAG_TAG, "Correct Answer [" + Integer.toString(gameSession.getCurrentRando())
                //+ " " + people.get(gameSession.getCurrentRando()).getName());
        for (int i = 0; i < frames.size(); i++) {
            final FrameLayout frame = frames.get(i);
            // In FrameLayout: index 0 = ImageView, index 1 = ProgressView
            final ImageView face = (ImageView) frame.getChildAt(0);
            final ProgressBar progressBar = (ProgressBar) frame.getChildAt(1);

            progressBar.setVisibility(View.VISIBLE);

            //Log.i(FRAG_TAG, "Loading " + people.get(i).getName().toString() + " " + people.get(i).getUrl().toString());

            picasso.load(people.get(i).getUrl())
                    .resize(imageSize, imageSize)
                    .transform(new CircleBorderTransform())
                    .into(face, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.INVISIBLE);
                            face.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {
                            Log.e(FRAG_TAG, "Picasso error loading image");
                        }
                    });

        }
        animateFacesIn();
        gameSession.startTimer();
    }

    /**
     * A method to animate the faces into view
     */
    private void animateFacesIn() {
        title.animate().alpha(1).start();
        for (int i = 0; i < frames.size(); i++) {
            FrameLayout frame = frames.get(i);
            ImageView face = (ImageView) frame.getChildAt(0);
            face.animate().scaleX(1).scaleY(1).setStartDelay(800 + 120 * i).setInterpolator(OVERSHOOT).start();
        }
        faceContainer.setClickable(true);
    }


    /**
     * A method to handle the back button pressed.
     * Local onBackPressed method specific to this fragment
     */
    public void onBackPressed() {
        Log.i(FRAG_TAG, "Back button was pressed");
        //TODO: Display a warning menu and destroy this fragment
        new AlertDialog.Builder(getActivity().getApplicationContext())
                .setTitle("Quit game")
                .setMessage("Are you sure you want to return to the menu?")
                .setCancelable(false)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: End this fragment
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();

    }

    /**
     * A custom click Listener class that handles clicks
     *  for any of the ImageViews
     */
    private class MasterClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.face1:
                    onPersonSelected(v, personsInQuestion.get(0));
                    break;
                case R.id.face2:
                    onPersonSelected(v, personsInQuestion.get(1));
                    break;
                case R.id.face3:
                    onPersonSelected(v, personsInQuestion.get(2));
                    break;
                case R.id.face4:
                    onPersonSelected(v, personsInQuestion.get(3));
                    break;
                case R.id.face5:
                    onPersonSelected(v, personsInQuestion.get(4));
                    break;
                default:
                    Log.i(FRAG_TAG, "Nothing of value clicked");
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        repo.unregister(personRepoListener);
        super.onDestroy();
    }

    public List<FrameLayout> getFrames() {
        return frames;
    }

    public void setFrames(List<FrameLayout> frames) {
        this.frames = frames;
    }
}

