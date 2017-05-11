package com.example.aditi.a272test2;

        import java.io.IOException;
        import java.io.InputStream;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.util.ArrayList;

        import java.util.List;
        import java.util.Locale;
        import java.util.Vector;
        import java.util.concurrent.ExecutionException;

        import android.app.Activity;
        import android.content.ActivityNotFoundException;
        import android.content.Intent;
        import android.graphics.Color;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.provider.MediaStore;
        import android.speech.RecognizerIntent;
        import android.support.v4.content.ContextCompat;
        import android.util.Log;
        import android.view.View;
        import android.webkit.WebView;
        import android.webkit.WebViewClient;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.LinearLayout;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import javax.json.Json;
        import javax.json.JsonArray;
        import javax.json.JsonObject;
        import javax.json.JsonReader;

        import com.facebook.drawee.backends.pipeline.Fresco;
        import com.facebook.drawee.interfaces.DraweeController;
        import com.facebook.drawee.view.SimpleDraweeView;
        import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
        import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import okhttp3.MediaType;
        import okhttp3.MultipartBody;
        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.RequestBody;
        import okhttp3.Response;

        import static java.lang.Math.pow;
        import static java.security.AccessController.getContext;
        import android.view.animation.AccelerateInterpolator;
        import android.view.animation.DecelerateInterpolator;

public class MainActivity extends Activity {
    private final int SPEECH_RECOGNITION_CODE = 1;
    private TextView txtOutput;
    private ImageButton btnMicrophone;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private WebView back;
    private Button btn;
    private EditText tmpIn;
    private String api_path = "http://api.giphy.com/v1/gifs/search?&api_key=dc6zaTOxFJmzC&limit=1&rating=y";
    private String query = "&q=";
    private SimpleDraweeView draweeView;

    private double anger, fear, joy;

    OkHttpClient client = new OkHttpClient();
    String text;


    private class doStuff extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            Double r = Double.parseDouble(params[0])*255;
            Double g = Double.parseDouble(params[1])*255;
            Double b = Double.parseDouble(params[2])*255;

            r = (r > 0.04045d) ? pow((r + 0.055d) / (1.0d + 0.055d), 2.4d) : (r / 12.92d);
            g = (g > 0.04045d) ? pow((g + 0.055d) / (1.0d + 0.055d), 2.4d) : (g / 12.92d);
            b = (b > 0.04045d) ? pow((b + 0.055d) / (1.0d + 0.055d), 2.4d) : (b / 12.92d);

            double X = r * 0.664511f + r * 0.154324f + r * 0.162028f;
            double Y = g * 0.283881f + g * 0.668433f + g * 0.047685f;
            double Z = b * 0.000088f + b * 0.072310f + b * 0.986039f;

            double x = X / (X + Y + Z);
            double y = Y / (X + Y + Z);

            String xs = Double.toString(x);
            String ys = Double.toString(y);

            System.out.println(x);
            System.out.println(y);

            RequestBody requestBody = RequestBody.create(JSON, "{\"xy\" :["+xs+","+ys+"]}");
            Request request = null;
            try {
                request = new Request.Builder()
                        .url("http://192.168.43.165/api/wI1ctifgI71yETIBvGZfa7ercS5BIetYxqxfZuQL/lights/4/state")
//                        .method("PUT", requestBody)
                        .put(requestBody)
                        .build();
                Response resp = client.newCall(request).execute();
                System.out.println(resp.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private class WatsonUnderstandTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
                    NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
                    "9af3dd26-8450-48fb-8878-c1b697a7330e",
                    "esyU4rPHDcMN"
            );
            String toAnalyze =text;
            KeywordsOptions keywords= new KeywordsOptions.Builder()
                    .sentiment(true)
                    .emotion(true)
                    .limit(3)
                    .build();

            Features features = new Features.Builder()
                    .keywords(keywords)
                    .build();

            AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                    .text(toAnalyze)
                    .features(features)
                    .build();

            AnalysisResults response = service
                    .analyze(parameters)
                    .execute();
         //   System.out.println(response.getKeywords().toString());
           // System.out.println(response.getEmotion().toString());


            return response.getKeywords().toString();
        }

        protected void onPostExecute(String response) {
         //   System.out.println(response);
            Vector<String> gifs = new Vector<String>();
            try {
                JSONArray arr = new JSONArray(response);
                for (int i = 0; i < arr.length(); i++){
                    JSONObject object = arr.getJSONObject(i);
                    new DownloadTask().execute(api_path + query + object.getString("text")); //emotion
                    gifs.add(api_path + query + object.getString("text"));

                    String url = api_path + query + object.getString("text");

                    anger = Double.parseDouble(object.getJSONObject("emotion").getString("anger").toString());
                    fear = Double.parseDouble(object.getJSONObject("emotion").getString("fear").toString());
                    joy = Double.parseDouble(object.getJSONObject("emotion").getString("joy").toString());

                    final LinearLayout backgroundLL = (LinearLayout) findViewById(R.id.backgroundLL);

                    backgroundLL.setBackgroundColor(android.graphics.Color.rgb((int)(anger * 255),(int) (joy * 255),(int)(fear*255)));
                }
                String rs = Double.toString(anger);
                String gs = Double.toString(joy);
                String bs = Double.toString(fear);

                new doStuff().execute(rs, gs, bs);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params){
            String gif_url ="";
            InputStream is;
            URL url;
            try {
                url = new URL(params[0]);
                is = url.openStream();
                JsonReader rdr = Json.createReader(is);
                JsonObject obj = rdr.readObject();
                JsonArray results = obj.getJsonArray("data");
                JsonObject result = results.getValuesAs(JsonObject.class).get(0);
                gif_url = result.getJsonObject("images").getJsonObject("original").getString("url");

            }catch (Exception e){
                e.printStackTrace();
            }
            return gif_url;
        }

        @Override
        protected void onPostExecute(String gif_url){
            super.onPostExecute(gif_url);
            try {
                Log.d("url", gif_url);
                if(gif_url.length()!=0){
                    Uri uri = Uri.parse(gif_url);
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setUri(uri)
                            .setAutoPlayAnimations(true)
                            .build();
                    draweeView.setController(controller);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Starting", "App has started!");
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);

        setContentView(R.layout.activity_main);
        draweeView = (SimpleDraweeView) findViewById(R.id.my_image_view);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .build();
        draweeView.setController(controller);



        //txtOutput = (TextView) findViewById(R.id.txt_output);
        btnMicrophone = (ImageButton) findViewById(R.id.btn_mic);

        //back = (WebView) findViewById(R.id.bckgrnd);
        //btn = (Button) findViewById(R.id.tmpBtn);
        tmpIn = (EditText) findViewById(R.id.tempinput);

        btnMicrophone.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });


    }
    /**
     * Start speech to text intent. This opens up Google Speech Recognition API dialog box to listen the speech input.
     * */
    private void startSpeechToText() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak something...");
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Speech recognition is not supported on this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback for speech recognition activity
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RECOGNITION_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    text = result.get(0);
                   // text = "mary had a lamb";
                    //txtOutput.setText(text);
                    tmpIn.setText(text);
                    new WatsonUnderstandTask().execute(text);

          /*         try {
                        new DownloadTask().execute(api_path + query + tmpIn.getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
                break;
            }
        }
    }
}