package com.willowtreeapps.namegame.network.api;

import android.os.Build;
import android.os.Parcel;

import com.google.gson.Gson;
import com.willowtreeapps.namegame.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Created by charlie on 3/16/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PersonTest {

    private Gson gson;

    @Before
    public void setUp() throws Exception {
        gson = new Gson();
    }

    @Test
    public void testFromGson() throws Exception {
        String testString = "{\n" +
                "name: \"Some Guy\",\n" +
                "url: \"www.someguyspicture.com\"\n" +
                "}";

        Person person = gson.fromJson(testString, Person.class);

        assertEquals("Names should match", "Some Guy", person.getName());
        assertEquals("Urls should match",
                "www.someguyspicture.com",
                person.getUrl());

    }

    @Test
    public void testGetName() throws Exception {
        Person person = new Person("Some Guy",
                "www.someguyspicture.com");

        assertEquals("Names should match", "Some Guy", person.getName());
    }

    @Test
    public void testGetUrl() throws Exception {
        Person person = new Person("Some Guy",
                "www.someguyspicture.com");

        assertEquals("Url should be correct",
                "www.someguyspicture.com",
                person.getUrl());
    }

    @Test
    public void testDescribeContents() throws Exception {

    }

    @Test
    public void testWriteToParcel() throws Exception {
        Parcel parcel = Parcel.obtain();

        //Write ourselves to the parcel
        Person person = new Person("Some Guy", "www.google.com");
        person.writeToParcel(parcel, 0);

        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0);

        // Reconstruct object from parcel and asserts:
        person = Person.CREATOR.createFromParcel(parcel);

        assertEquals("Names should match", "Some Guy", person.getName());
        assertEquals("Url should match", "www.google.com", person.getUrl());
    }
}