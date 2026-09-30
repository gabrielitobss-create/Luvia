package com.luvia.ai;

import android.app.Application;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.defaults.DefaultReactNativeHost;
import com.facebook.soloader.SoLoader;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {
  private final ReactNativeHost reactNativeHost = new DefaultReactNativeHost(this) {
    @Override public boolean getUseDeveloperSupport() {
      // The APK is distributed as a standalone build and must never require Metro.
      return false;
    }

    @Override protected List<ReactPackage> getPackages() {
      List<ReactPackage> packages = new PackageList(this).getPackages();
      packages.add(new BackendConfigPackage());
      return packages;
    }

    @Override protected String getJSMainModuleName() {
      return "index";
    }

    @Override protected boolean isNewArchEnabled() {
      return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
    }

    @Override protected Boolean isHermesEnabled() {
      return BuildConfig.IS_HERMES_ENABLED;
    }
  };

  @Override public ReactNativeHost getReactNativeHost() {
    return reactNativeHost;
  }

  @Override public void onCreate() {
    super.onCreate();
    SoLoader.init(this, false);
  }
}
