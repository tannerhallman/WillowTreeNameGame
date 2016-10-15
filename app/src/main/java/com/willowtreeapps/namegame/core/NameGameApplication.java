package com.willowtreeapps.namegame.core;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.willowtreeapps.namegame.network.NetworkModule;


/**
 * This class implements the parent Application and is
 * used to handle the initial instantiation of the application.
 */
public class NameGameApplication extends Application {

    private ApplicationComponent component;

    public static NameGameApplication get(@NonNull Context context) {
        return (NameGameApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        component = buildComponent();
    }

    public ApplicationComponent getComponent() {
        return component;
    }

    protected ApplicationComponent buildComponent() {
        return DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .networkModule(new NetworkModule("http://api.namegame.willowtreemobile.com/"))
                .build();
    }
}
