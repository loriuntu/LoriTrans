package com.loriuntu.loritrans;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TranslateIntentService extends IntentService {

    private final String LOG_TAG = "TranslateIntentService";
    private final String YANDEX_API = "trnsl.1.1.20141124T195104Z.1fbe795ebe960d83.0b8ba6e922bd664de5d2815357b4c9c7fea52034";

    private List<String> allLangs;
    private Map<String, String> langMap;

    public static final String ACTION_ALLLANGS = "com.loriuntu.loritrans.translateintentservice.alllangs";
    public static final String ALLANGS_KEY_OUT = "ALLANGS_LANGS_OUT";

    public static final String ACTION_ALLLANGSMAP = "com.loriuntu.loritrans.translateintentservice.alllangsmap";
    public static final String ALLANGSMAP_KEY_OUT = "ALLANGS_LANGSMAP_OUT";

    public static final String ACTION_SEARCH_LANG = "com.loriuntu.loritrans.translateintentservice.searchlang";
    public static final String SEARCH_LANG_KEY_OUT = "SEARCH_LANG_OUT";

    public static final String ACTION_TRANSLATE = "com.loriuntu.loritrans.translateintentservice.translate";
    public static final String TRANSLATE_KEY_OUT = "TRANSLATE_LANG_OUT";

    public TranslateIntentService() {
        super("TranslateIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String label = intent.getStringExtra("task");
        Log.d(LOG_TAG, "onHandleIntent start make " + label);

        BroadcastSender bs = new BroadcastSender();
        try {
            if ("all".equals(label)) {
                getAllLangs();
                bs.sendArrayStringList(ACTION_ALLLANGS, ALLANGS_KEY_OUT, allLangs);
                bs.sendMapStringStringList(ACTION_ALLLANGSMAP, ALLANGSMAP_KEY_OUT, langMap);
            } else if ("search".equals(label)) {
                bs.sendString(ACTION_SEARCH_LANG, SEARCH_LANG_KEY_OUT, getTextLang(intent.getStringExtra("text")));
            } else if ("translate".equals(label)) {
                bs.sendArrayStringList(ACTION_TRANSLATE, TRANSLATE_KEY_OUT,
                        getTranslate(intent.getStringExtra("text"), intent.getStringExtra("lang")));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private String getTextLang(String text) throws MalformedURLException {
        Log.d(LOG_TAG, "getTextLang");

        String result = "null";
        URL url = new URL("https://translate.yandex.net/api/v1.5/tr/detect?key=" + YANDEX_API + "&text=" + text);
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream is;
            is = connection.getInputStream();

            // Convert the InputStream into a string
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String dataAsString = sb.toString();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(dataAsString));
            try {
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_TAG) {
                        if( "200".equals(xpp.getAttributeValue(0))){
                            result = xpp.getAttributeValue(1);
                        }
                    }
                    eventType = xpp.next();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private List<String> getTranslate(String text, String lang) throws MalformedURLException {
        Log.d(LOG_TAG, "getTranslate");

        List<String> result = new ArrayList<String>();
        URL url = new URL("https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + YANDEX_API +
                "&text=" + text + "&lang=" + lang);
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream is;
            is = connection.getInputStream();

            // Convert the InputStream into a string
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String dataAsString = sb.toString();
            System.out.println(dataAsString);

            JSONObject jsonObject = new JSONObject(dataAsString);
            Iterator it = jsonObject.keys();
            while (it.hasNext()) {
                String key = (String)it.next();
                if ((Integer)jsonObject.get(key) == 200) {
                    key = (String)it.next();
                    result.add((jsonObject.get(key)).toString());
                    key = (String)it.next();
                    result.add((jsonObject.get(key)).toString());
                } else {
                    result.add("");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void getAllLangs() throws MalformedURLException {
        Log.d(LOG_TAG, "getAllLangs");

        URL url = new URL("https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=" + YANDEX_API + "&ui=ru");
        HttpURLConnection connection;

        try {

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream is;
            is = connection.getInputStream();

            // Convert the InputStream into a string
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String dataAsString = sb.toString();

            allLangs = new ArrayList<String>();
            JSONObject jsonObject = new JSONObject(dataAsString);
            JSONArray jsonArray = jsonObject.getJSONArray("dirs");
            for (int i=0; i<jsonArray.length(); i++) {
                allLangs.add((String) jsonArray.get(i));
            }
            Log.d("dirs", allLangs.toString());

            langMap = new HashMap<String, String>();
            JSONObject jsonMap = jsonObject.getJSONObject("langs");
            Iterator it = jsonMap.keys();
            while (it.hasNext()) {
                String key = (String)it.next();
                langMap.put(key, (String)jsonMap.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class BroadcastSender {
        public void sendString(String actionName, String keyOut, String what) {
            Intent intentResponse = new Intent();
            intentResponse.setAction(actionName);
            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
            intentResponse.putExtra(keyOut, what);

            Log.d(LOG_TAG, "send String broadcast");
            sendBroadcast(intentResponse);
        }

        public void sendArrayStringList(String actionName, String keyOut, List<String> what) {
            Intent intentResponse = new Intent();
            intentResponse.setAction(actionName);
            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
            intentResponse.putStringArrayListExtra(keyOut, (ArrayList<String>) what);

            Log.d(LOG_TAG, "send arrayStringList broadcast");
            sendBroadcast(intentResponse);
        }

        public void sendMapStringStringList(String actionName, String keyOut, Map<String, String> what) {
            Intent intentResponse = new Intent();
            intentResponse.setAction(actionName);
            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
            intentResponse.putExtra(keyOut, (HashMap<String, String>) what);

            Log.d(LOG_TAG, "send Map<String, String> broadcast");
            sendBroadcast(intentResponse);
        }
    }
}
