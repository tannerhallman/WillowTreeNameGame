package com.willowtreeapps.namegame.test;

import android.os.Build;

import com.willowtreeapps.namegame.BuildConfig;
import com.willowtreeapps.namegame.ui.StartMenuFragment;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;


/**
 * Created by th on 10/23/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class StartMenuFragmentTest {

    private StartMenuFragment startMenuFragment;

    @Before
    public void setUp() throws Exception {
        startMenuFragment = new StartMenuFragment();
    }
}