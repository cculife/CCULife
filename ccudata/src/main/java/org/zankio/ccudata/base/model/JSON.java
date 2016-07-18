package org.zankio.ccudata.base.model;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSON {
    private JSONObject object = null;
    private JSONArray array = null;

    public JSON(JSONArray array) {
        this.array = array;
    }

    public JSON(JSONObject object) {
        this.object = object;
    }

    public JSONObject object() {
        return object;
    }

    public JSONArray array() {
        return array;
    }
}
