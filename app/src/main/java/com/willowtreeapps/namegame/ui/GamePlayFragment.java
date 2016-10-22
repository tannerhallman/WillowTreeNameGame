package com.willowtreeapps.namegame.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

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
import java.util.Stack;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;

public class GamePlayFragment extends Fragment {
    private static final Interpolator OVERSHOOT = new OvershootInterpolator();
    private static final String TAG = "Game Tag";

    @Inject ListRandomizer listRandomizer;
    @Inject Picasso picasso;
    @Inject PersonRepository repo;

    @BindView(R.id.title) TextView title;
    @BindView(R.id.question) TextView question;

    // contains 2 horizontal linear layouts [1 toprow and 1 bottomrow]
    @BindView(R.id.face_container) LinearLayout faceContainer;
    @BindView(R.id.topRow) LinearLayout topRow;
    @Nullable @BindView(R.id.bottomrow) LinearLayout bottomRow;
    @BindView(R.id.score) TextView score;
    @BindView(R.id.averageTime) TextView averageTime;


    private final int classicFaceCount = 5;
    private final int reverseFaceCount = 1;

    //The amount of faces that are displayed (Should correspond to the amount of FrameLayouts)
    private int faceCount;

    // A list of the framelayouts that contain both the progress bar and the imageview
    private List<FrameLayout> frames;
    // A list of the allPersons that are currently displayed
    private List<Person> personsInQuestion;
    // A list of *all* of the allPersons that have been loaded by the API
    private List<Person> allPersons;
    private List<Person> mattOnly;

    // A state of the game
    private GameSession gameSession;

    // the size of the images
    int imageSize;

    private NumberFormat formatter;

    private Bundle savedState;

    private final String savedGamePlayString = "savedGame";


    // Create a reference to the listener so that it can be used to unsubscribe in onDestory
    private PersonRepository.Listener personRepoListener = new PersonRepository.Listener() {
            @Override
            public void onLoadFinished(@NonNull List<Person> people) {
                GamePlayFragment.this.allPersons = people;
                if (savedState == null) {
                    loadNewContent(false);
                } else {
                    Log.i(TAG, "person repo listener got called but savedState = null");
                }
            }

            @Override
            public void onError(@NonNull Throwable error) {
                Log.e(TAG, error.getMessage());
            }
        };

    // TODO: Fix the restoreSession for Matt, Reverse, Custom

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate Called");
        super.onCreate(savedInstanceState);
        NameGameApplication.get(getActivity()).getComponent().inject(this);

        frames = new ArrayList<>(faceCount);
        personsInQuestion = new ArrayList<>(faceCount);
        allPersons = new ArrayList<>();
        formatter = new DecimalFormat("#0.00");
        savedState = null;
        imageSize = (int) Ui.convertDpToPixel(100, getContext());

        if (savedInstanceState == null){
            Log.i(TAG, "savedInstanceState was null");
            repo.register(personRepoListener);
        } else {
            Log.i(TAG, "savedInstanceState was not null");
        }

        Log.i(TAG, getActivity().getSupportFragmentManager().getFragments().toString());

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            Log.i(TAG, "onActivityCreated() restore true ");
            restoreState(savedInstanceState);
        } else {
            Log.i(TAG, "onActivityCreated() restore false ");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateViewCalled & restoring state");
        View view = inflater.inflate(R.layout.game_play_fragment, container, false);
        ButterKnife.bind(this, view);

        /* If the Fragment was destroyed inbetween (screen rotation), we need to recover the savedState first */
        /* However, if it was not, it stays in the instance from the last onDestroyView() and we don't want to overwrite it */
        if(savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(savedGamePlayString);
        }
        if(savedState != null) {
            restoreState(savedInstanceState);
        }
        savedState = null;

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated called");
        if (savedInstanceState == null) {
            NameGameActivity.GameMode mode = NameGameActivity.getGameMode();
            Log.i(TAG, "savedInstanceState was null");

            title.setText(getTitleText(mode));
            switch (mode) {
                case CLASSIC:
                    faceCount = classicFaceCount;
                    break;
                case MATT:
                    mattOnly = new ArrayList<>();
                    faceCount = classicFaceCount;
                    break;
                case REVERSE:
                    faceCount = reverseFaceCount;
                    break;
                case CUSTOM:
                    break;
                default:
                    faceCount = classicFaceCount;
                    title.setText("Willow Tree Name GameSession");
                    break;
            }

        } else {
            // restore savedState
            Log.i(TAG, "savedInstanceState was not null & Restoring state form onViewCreated call");
        }

        gameSession = new GameSession(faceCount, this);
        setupTopRow();
        if (bottomRow != null) setUpBottmRow();
    }

    /**
     * A method to initially setup the rows of ImageViews and Progressbars so that
     * images can be loaded into them.
     */
    private void setupTopRow() {
        try {
            int toprowcount = topRow.getChildCount();
            Log.i(TAG, "Top row count" + Integer.toString(toprowcount));
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Optional
    private void setUpBottmRow(){
        try {
            int bottomrowcount = bottomRow.getChildCount();
            for (int j = 0; j < bottomrowcount; j++){
                FrameLayout frame = (FrameLayout) bottomRow.getChildAt(j);
                ImageView face = (ImageView) frame.getChildAt(0);
                face.setVisibility(View.VISIBLE);
                face.setOnClickListener(new MasterClickListener());

                ProgressBar progressBar = (ProgressBar) frame.getChildAt(1);

                progressBar.setVisibility(View.VISIBLE);
                frames.add(frame);
                face.setScaleX(0);
                face.setScaleY(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getFaceCount(NameGameActivity.GameMode mode){
        switch (mode) {
            case CLASSIC:
                return classicFaceCount;
            case MATT:
                return classicFaceCount;
            case REVERSE:
                return reverseFaceCount;
            case CUSTOM:
                return classicFaceCount;
            default:
                return classicFaceCount;
        }
    }

    private String getTitleText(NameGameActivity.GameMode mode){
        switch (mode) {
            case CLASSIC:
                return getResources().getString(R.string.gameModeClassic);
            case MATT:
                return getResources().getString(R.string.gameModeMatt);
            case REVERSE:
                return getResources().getString(R.string.gameModeReverse);
            case CUSTOM:
                return getResources().getString(R.string.gameModeCustom);
            default:
                return "Willow Tree Name Game";
        }
    }

    private Bundle saveState() { /* called either from onDestroyView() or onSaveInstanceState() */
        Log.i(TAG, "saveState() called ");
        Bundle state = new Bundle();
        state.putDouble("averageTime", gameSession.getAverageTime());
        state.putInt("questionsAsked", gameSession.getQuestionsAsked());
        state.putInt("questionsCorrect", gameSession.getQuestionsCorrect());
        state.putLong("countDownDuration", gameSession.getCountDownDuration());
        state.putLong("millisUntilFinished", gameSession.getMillisUntilFinished());
        state.putInt("currentRando", gameSession.getCurrentRando());

        Stack<Integer> stackToSave = gameSession.getFacesNotYetFaded();
        int[] arrayOfStack = new int[stackToSave.size()];

        // want to maintain order so that loading on stack is natural
        for (int x = arrayOfStack.length - 1; x >= 0; x--){
            arrayOfStack[x] = stackToSave.pop();
        }
        state.putIntArray("arrayOfStack", arrayOfStack);

        state.putIntegerArrayList("facesAlreadyFaded", gameSession.getFacesAlreadyFaded());

        // save all the persons
        ArrayList<String> personsNames = new ArrayList<>(allPersons.size());
        ArrayList<String> personsUrls = new ArrayList<>(allPersons.size());

        for (Person p : allPersons){
            personsNames.add(p.getName());
            personsUrls.add(p.getUrl());
        }
        state.putStringArrayList("personsNames", personsNames);
        state.putStringArrayList("personsUrls", personsUrls);

        // save the current chosen persons
        ArrayList<String> personsInQuestionNames = new ArrayList<>(personsInQuestion.size());
        ArrayList<String> personsInQuestionUrls = new ArrayList<>(personsInQuestion.size());

        for (Person p : personsInQuestion){
            personsInQuestionNames.add(p.getName());
            personsInQuestionUrls.add(p.getUrl());
        }
        state.putStringArrayList("personsInQuestionNames", personsInQuestionNames);
        state.putStringArrayList("personsInQuestionUrls", personsInQuestionUrls);

        return state;
    }

    private void restoreState(Bundle savedInstanceState){
        Log.i(TAG, " restoreState called");
        /* If the Fragment was destroyed inbetween (screen rotation), we need to recover the savedState first */
        /* However, if it was not, it stays in the instance from the last onDestroyView() and we don't want to overwrite it */
        if(savedInstanceState != null && savedState == null) {
            Log.i(TAG, "restoreState() savedState and savedInstanceState == null");
            savedState = savedInstanceState.getBundle(savedGamePlayString);
        }
        if(savedState != null) {
            Log.i(TAG, "restoreState() getting components and restoring them ");

            NameGameActivity.GameMode mode = NameGameActivity.getGameMode();
            if(title != null) title.setText(getTitleText(mode));
            faceCount = getFaceCount(mode);
            gameSession = new GameSession(faceCount, this);

            gameSession.setAverageTime(savedState.getDouble("averageTime"));
            gameSession.setQuestionsAsked(savedState.getInt("questionsAsked"));
            gameSession.setQuestionsCorrect(savedState.getInt("questionsCorrect"));
            gameSession.setCountDownDuration(savedState.getLong("countDownDuration"));
            gameSession.setMillisUntilFinished(savedState.getLong("millisUntilFinished"));
            gameSession.setCurrentRando(savedState.getInt("currentRando"));

            Stack<Integer> stackToLoad = new Stack<>();
            int[] arrayOfStack = savedState.getIntArray("arrayOfStack");

            // loading onto stack is natural because of the way stack was unloaded
            for (int x = arrayOfStack.length-1; x >= 0; x--) {
                stackToLoad.push(arrayOfStack[x]);
            }
            gameSession.setFacesNotYetFaded(stackToLoad);
            gameSession.setFacesAlreadyFaded(savedState.getIntegerArrayList("facesAlreadyFaded"));

            ArrayList<String> personsNames = savedState.getStringArrayList("personsNames");
            ArrayList<String> personsUrls = savedState.getStringArrayList("personsUrls");
            ArrayList<Person> allPersons = new ArrayList<>();

            for (int n = 0; n < personsNames.size(); n++){
                allPersons.add(new Person(personsNames.get(n),
                        personsUrls.get(n)));
            }
            setAllPersons(allPersons);

            // restore the current chosen faces
            ArrayList<String> personsInQuestionNames = savedState.getStringArrayList("personsInQuestionNames");
            ArrayList<String> personsInQuestionUrls = savedState.getStringArrayList("personsInQuestionUrls");
            ArrayList<Person> personsInQuestion = new ArrayList<>();

            for (int n = 0; n < personsInQuestionNames.size(); n++){
                personsInQuestion.add(new Person(personsInQuestionNames.get(n),
                        personsInQuestionUrls.get(n)));
            }
            Log.i(TAG, personsInQuestion.toString());

            setPersonsInQuestion(personsInQuestion);

            gameSession.instantiateTimer(gameSession.getMillisUntilFinished());
            loadNewContent(true);
        }
        // release the savedState
        savedState = null;

    }

    /**
     * An umbrella method that is used to load new content into imageviews and
     *  load new questions
     */
    public void loadNewContent(Boolean restoringState){
        Log.i(TAG,  "loadNewContent() with state " + restoringState.toString());
        if (!restoringState) {
            personsInQuestion.clear();
            gameSession.setQuestionsAsked(gameSession.getQuestionsAsked() + 1);
        }

        try {
            averageTime.setText(formatter.format(gameSession.getAverageTime()));
            score.setText(Integer.toString(gameSession.getQuestionsCorrect()) + " / "
                + Integer.toString(gameSession.getQuestionsAsked()));

        } catch (Exception e){
            Log.e(TAG, e.getStackTrace().toString());
        }

        switch (NameGameActivity.getGameMode()) {
            case CLASSIC :
                if (!restoringState) personsInQuestion = listRandomizer.pickN(allPersons, faceCount);

                frames.clear();
                setupTopRow();
                if(bottomRow != null) setUpBottmRow();
                setImages(frames, personsInQuestion);

                if (!restoringState){ // restoringState = false
                    gameSession.resetSessionState();
                } else { // restoringState = true
                    gameSession.restoreSessionState();
                }

                //Log.i(TAG, "Person in question " + personsInQuestion.get(gameSession.getCurrentRando()).getName());
                question.setText("Who is " + personsInQuestion.get(gameSession.getCurrentRando()).getName() + "?");

                break;

            case MATT:
                for (Person p : allPersons){
                    if (p.getName().substring(0,3).equalsIgnoreCase("Mat"))
                        mattOnly.add(p);
                }

                if (!restoringState) personsInQuestion = listRandomizer.pickN(mattOnly, faceCount);

                frames.clear();
                setupTopRow();
                if(bottomRow != null) setUpBottmRow();
                setImages(frames, personsInQuestion);

                if (!restoringState){ // restoringState = false
                    gameSession.resetSessionState();
                } else { // restoringState = true
                    gameSession.restoreSessionState();
                }

                //Log.i(TAG, "Person in question " + personsInQuestion.get(gameSession.getCurrentRando()).getName());
                question.setText("Who is " + personsInQuestion.get(gameSession.getCurrentRando()).getName() + "?");

                break;
            case REVERSE:
                Log.i(TAG, "REVERSE NOT YET IMPELMTEND");
                break;

            default:
                Log.e(TAG, "Have not implemented any other game types");
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
        if (person.getName().equals(personsInQuestion.get(gameSession.getCurrentRando()).getName())){

            gameSession.setQuestionsCorrect(gameSession.getQuestionsCorrect() + 1);
            Toast.makeText(getActivity(), "Correct!", Toast.LENGTH_SHORT).show();

        } else {
            //Log.i(TAG, "selected the wrong person " + person.getName());
            Toast.makeText(getActivity(), "Incorrect!", Toast.LENGTH_SHORT).show();
        }
        gameSession.cancelTimer();
        gameSession.updateAverage();
        loadNewContent(false);
    }

    public void outOfTime() {
        Toast.makeText(getActivity(), "Out of time!", Toast.LENGTH_SHORT).show();
        gameSession.cancelTimer();
        gameSession.updateAverage();
        loadNewContent(false);
    }

    /**
     * A method for setting the images from allPersons into the imageviews
     */
    private void setImages(List<FrameLayout> frames, List<Person> people) {
        for (int i = 0; i < frames.size(); i++) {
            final FrameLayout frame = frames.get(i);
            // In FrameLayout: index 0 = ImageView, index 1 = ProgressView
            final ImageView face = (ImageView) frame.getChildAt(0);
            final ProgressBar progressBar = (ProgressBar) frame.getChildAt(1);

            progressBar.setVisibility(View.VISIBLE);

            Log.i(TAG, "size of People "  + people.size());
            Log.i(TAG, "value of i " + Integer.toString(i));
            Log.i(TAG, "Loading (" + Integer.toString(i) + ")" +  people.get(i).getName().toString());

            picasso.load(people.get(i).getUrl())
                    .resize(imageSize, imageSize)
                    .transform(new CircleBorderTransform())
                    .into(face, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            face.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {
                            Log.e(TAG, "Picasso error loading image");
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
     * A custom click Listener class that handles clicks
     *  for any of the ImageViews
     */
    private class MasterClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
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
                    Log.i(TAG, "Nothing of value clicked");
                    break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Log.i(TAG, "onSaveInstanceState called");
        /* If onDestroyView() is called first, we can use the previously savedState but we can't call saveState() anymore */
        /* If onSaveInstanceState() is called first, we don't have savedState, so we need to call saveState() */
        /* => (?:) operator inevitable! */
        outState.putBundle(savedGamePlayString, (savedState != null) ? savedState : saveState());

    }

    @Override
    public void onDestroyView() {
        //Log.i(TAG, "onDestroyView called... calling saveState()");
        savedState = saveState();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        //Log.i(TAG, "OnDestory Called");
        repo.unregister(personRepoListener);
        gameSession.cancelTimer();

        super.onDestroy();
    }

    public List<FrameLayout> getFrames() {
        return frames;
    }

    public void setPersonsInQuestion(List<Person> personsInQuestion) {
        this.personsInQuestion = personsInQuestion;
    }

    public void setAllPersons(List<Person> allPersons) {
        this.allPersons = allPersons;
    }
}

