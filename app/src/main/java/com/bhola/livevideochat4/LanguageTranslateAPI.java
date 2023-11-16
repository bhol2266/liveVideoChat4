package com.bhola.livevideochat4;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LanguageTranslateAPI {


    public static void postData(Context context, final String text, final String langCode, TextView translatedMessage) {

        String apiUrl = "https://clownfish-app-jn7w9.ondigitalocean.app/languageTranslate";

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("dfsgdsfgdsfg", "onResponse: " + response.toString());
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String langCode = jsonResponse.getString("langCode");
                    String translatedText = jsonResponse.getString("translatedext");
                    translatedMessage.setText(translatedText);
                    translatedMessage.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("dfsgdsfgdsfg", "onResponse: " + error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("text", text);
                params.put("langCode", langCode);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }


}




