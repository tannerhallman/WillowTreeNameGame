package com.willowtreeapps.namegame.test;

import android.os.Build;

import com.willowtreeapps.namegame.BuildConfig;
import com.willowtreeapps.namegame.ui.GamePlayFragment;
import com.willowtreeapps.namegame.util.GameSession;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;


/**
 * Created by th on 10/23/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GameSessionTest {

    private GameSession gameSession;
    private GamePlayFragment gamePlayFragment;

    @Before
    public void setUp() throws Exception {
        gamePlayFragment = new GamePlayFragment();
        gameSession = new GameSession(5, gamePlayFragment);

    }

    @Test
    public void testUpdateAverage() throws Exception {
        gameSession.updateAverage(100);
        assertEquals("Person lists should match", 100, gameSession.getAverageTime());
    }

    @Test
    public void testInstantiateTimer() throws Exception {
        gameSession.instantiateTimer(1000);
        Assert.assertTrue("Timer should not be null", gameSession.getCountdownTimer() != null);
    }
}