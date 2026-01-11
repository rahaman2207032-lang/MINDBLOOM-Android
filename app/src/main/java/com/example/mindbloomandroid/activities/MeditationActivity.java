package com.example.mindbloomandroid.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.mindbloomandroid.R;


public class MeditationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private WebView videoWebView;
    private TextView currentVideoTitle;
    private TextView videoDescription;
    private Button breathingBtn;
    private Button guidedBtn;
    private Button sleepBtn;
    private Button bodyScanBtn;
    private Button stressReliefBtn;
    private Button morningBtn;


    private static final String BREATHING_EXERCISE = "inpok4MKVLM";  // 5-Minute Breathing Exercise
    private static final String GUIDED_MEDITATION = "ZToicYcHIOU";   // 10-Minute Guided Meditation
    private static final String SLEEP_MEDITATION = "aEqlQvczMJQ";    // 15-Minute Sleep Meditation
    private static final String BODY_SCAN = "15q-N-_kkrU";           // 8-Minute Body Scan
    private static final String STRESS_RELIEF = "z6X5oEIg6Ak";       // 7-Minute Stress Relief
    private static final String MORNING_MEDITATION = "ssss7V1_eyA";  // 10-Minute Morning Meditation (Working)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);

        initializeViews();
        setupToolbar();
        setupWebView();
        setupButtons();


        loadVideo(BREATHING_EXERCISE, "5 Minute Breathing Exercise",
                "A simple breathing exercise to help you relax and center yourself.");
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        videoWebView = findViewById(R.id.videoWebView);
        currentVideoTitle = findViewById(R.id.currentVideoTitle);
        videoDescription = findViewById(R.id.videoDescription);
        breathingBtn = findViewById(R.id.breathingBtn);
        guidedBtn = findViewById(R.id.guidedBtn);
        sleepBtn = findViewById(R.id.sleepBtn);
        bodyScanBtn = findViewById(R.id.bodyScanBtn);
        stressReliefBtn = findViewById(R.id.stressReliefBtn);
        morningBtn = findViewById(R.id.morningBtn);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Meditation & Mindfulness");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }


    private void setupWebView() {
        android.util.Log.d("Meditation", "📺 Setting up WebView for video playback");

        WebSettings webSettings = videoWebView.getSettings();


        webSettings.setJavaScriptEnabled(true);


        webSettings.setDomStorageEnabled(true);


        webSettings.setMediaPlaybackRequiresUserGesture(false);


        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);


        webSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);


        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);


        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);


        videoWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                android.util.Log.d("Meditation", "✅ Page loaded: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                String errorMsg = "Error " + error.getErrorCode() + ": " + error.getDescription();
                android.util.Log.e("Meditation", "❌ WebView error: " + errorMsg);


                if (request.isForMainFrame()) {
                    Toast.makeText(MeditationActivity.this,
                        "Error loading: " + error.getDescription(),
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                android.util.Log.d("Meditation", "🔗 URL clicked: " + url);


                if (url.startsWith("openvideo://")) {
                    String videoId = url.replace("openvideo://", "");
                    openYouTubeVideo(videoId);
                    return true;
                }

                return false;
            }
        });


        videoWebView.setWebChromeClient(new WebChromeClient());

        android.util.Log.d("Meditation", "✅ WebView configured successfully");
    }


    private void setupButtons() {
        breathingBtn.setOnClickListener(v -> 
            loadVideo(BREATHING_EXERCISE, "5 Minute Breathing Exercise",
                    "A simple breathing exercise to help you relax and center yourself.")
        );

        guidedBtn.setOnClickListener(v -> 
            loadVideo(GUIDED_MEDITATION, "10 Minute Guided Meditation",
                    "A peaceful guided meditation for relaxation and mindfulness.")
        );

        sleepBtn.setOnClickListener(v -> 
            loadVideo(SLEEP_MEDITATION, "15 Minute Sleep Meditation",
                    "A calming meditation to help you fall asleep peacefully.")
        );

        bodyScanBtn.setOnClickListener(v -> 
            loadVideo(BODY_SCAN, "8 Minute Body Scan",
                    "A body scan meditation to release tension and promote relaxation.")
        );

        stressReliefBtn.setOnClickListener(v -> 
            loadVideo(STRESS_RELIEF, "7 Minute Stress Relief",
                    "Quick and effective meditation for stress relief.")
        );

        morningBtn.setOnClickListener(v -> 
            loadVideo(MORNING_MEDITATION, "10 Minute Morning Meditation",
                    "Start your day with this energizing morning meditation.")
        );
    }


    private void loadVideo(String videoId, String title, String description) {
        android.util.Log.d("Meditation", "🎬 Loading video: " + title);
        android.util.Log.d("Meditation", "   Video ID: " + videoId);

        currentVideoTitle.setText(title);
        videoDescription.setText(description);

        // Instead of embedding, show a message and "Watch Video" button in WebView
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <style>" +
                "        body {" +
                "            margin: 0;" +
                "            padding: 20px;" +
                "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
                "            font-family: Arial, sans-serif;" +
                "            display: flex;" +
                "            flex-direction: column;" +
                "            align-items: center;" +
                "            justify-content: center;" +
                "            min-height: 300px;" +
                "            text-align: center;" +
                "        }" +
                "        .container {" +
                "            background: white;" +
                "            border-radius: 15px;" +
                "            padding: 30px;" +
                "            box-shadow: 0 10px 30px rgba(0,0,0,0.3);" +
                "            max-width: 400px;" +
                "        }" +
                "        h2 {" +
                "            color: #667eea;" +
                "            margin-bottom: 15px;" +
                "            font-size: 24px;" +
                "        }" +
                "        p {" +
                "            color: #555;" +
                "            margin-bottom: 25px;" +
                "            line-height: 1.6;" +
                "        }" +
                "        .btn {" +
                "            display: inline-block;" +
                "            background: #667eea;" +
                "            color: white;" +
                "            padding: 15px 40px;" +
                "            border-radius: 25px;" +
                "            text-decoration: none;" +
                "            font-size: 18px;" +
                "            font-weight: bold;" +
                "            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);" +
                "            transition: all 0.3s;" +
                "        }" +
                "        .icon {" +
                "            font-size: 60px;" +
                "            margin-bottom: 20px;" +
                "        }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='icon'>🧘‍♀️</div>" +
                "        <h2>" + title + "</h2>" +
                "        <p>" + description + "</p>" +
                "        <a href='#' class='btn' onclick='openVideo(); return false;'>▶️ Watch Video</a>" +
                "    </div>" +
                "    <script>" +
                "        function openVideo() {" +
                "            window.location.href = 'openvideo://" + videoId + "';" +
                "        }" +
                "    </script>" +
                "</body>" +
                "</html>";

        android.util.Log.d("Meditation", "📄 Loading HTML into WebView...");
        videoWebView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null);

        Toast.makeText(this, "Tap 'Watch Video' to open in YouTube", Toast.LENGTH_SHORT).show();
    }


    private void openYouTubeVideo(String videoId) {
        android.util.Log.d("Meditation", "📺 Opening YouTube video: " + videoId);

        try {

            Intent appIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("vnd.youtube:" + videoId));
            startActivity(appIntent);
            android.util.Log.d("Meditation", "✅ Opened in YouTube app");
        } catch (android.content.ActivityNotFoundException ex) {

            android.util.Log.d("Meditation", "YouTube app not found, opening in browser");
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + videoId));
            startActivity(webIntent);
            android.util.Log.d("Meditation", "✅ Opened in browser");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoWebView != null) {
            videoWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoWebView != null) {
            videoWebView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoWebView != null) {
            videoWebView.destroy();
        }
    }
}


