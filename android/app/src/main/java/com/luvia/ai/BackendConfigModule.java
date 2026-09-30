package com.luvia.ai;

import androidx.annotation.NonNull;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import java.util.HashMap;
import java.util.Map;

public final class BackendConfigModule extends ReactContextBaseJavaModule {
  BackendConfigModule(ReactApplicationContext context) { super(context); }

  @NonNull @Override public String getName() { return "LuviaBackendConfig"; }

  @Override public Map<String, Object> getConstants() {
    Map<String, Object> constants = new HashMap<>();
    constants.put("backendUrl", BuildConfig.BACKEND_URL);
    return constants;
  }
}
