package com.willowtreeapps.namegame.test;

import android.os.Build;

import com.willowtreeapps.namegame.BuildConfig;
import com.willowtreeapps.namegame.network.api.Person;
import com.willowtreeapps.namegame.ui.GamePlayFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Created by th on 10/23/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GamePlayFragmentTest {

    private GamePlayFragment gamePlayFragment;
    private List<Person> persons;

    @Before
    public void setUp() throws Exception {
        gamePlayFragment = new GamePlayFragment();
        persons = new ArrayList<>();
        persons.add(new Person("Tanner", "www.tannerhallman.com"));
    }

    @Test
    public void testSetPersons() throws Exception {
        gamePlayFragment.setPersonsInQuestion(persons);
        assertEquals("Person lists should match", persons, gamePlayFragment.getPersonsInQuestion());
    }
}