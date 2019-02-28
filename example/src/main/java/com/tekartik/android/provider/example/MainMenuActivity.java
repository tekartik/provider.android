package com.tekartik.android.provider.example;

import android.os.Bundle;
import android.util.Log;

import com.tekartik.testmenu.Test;


public class MainMenuActivity extends Test.MenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Test.BuildConfig.DEBUG = BuildConfig.DEBUG;
        Test.Menu.setStartMenu(new MainTestMenu());
    }

    static public class MainTestMenu extends Test.Menu {

        protected MainTestMenu() {
            super("Main Menu");
        }

        @Override
        protected void onCreate() {
            super.onCreate();
            Log.i(TAG, "MainMenu");
            initItems(
                    new Item("showToast") {
                        @Override
                        public void execute() {
                            showToast("Hi");
                        }
                    }
            );
        }
    }
}
