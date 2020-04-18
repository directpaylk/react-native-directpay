package com.reactlibrary;

import org.json.JSONObject;

public interface HttpCallback {
    void success(JSONObject data);

    void error(int code, String message);
}
