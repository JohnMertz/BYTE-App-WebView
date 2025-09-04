/*
Copyright (c) 2017-2019 Divested Computing Group
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package tz.me.john.youtube;

import static android.webkit.WebView.HitTestResult.IMAGE_TYPE;
import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.webkit.ValueCallback;
import android.net.Uri;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private WebView youtubeWebView = null;
    private WebSettings youtubeWebSettings = null;
    private CookieManager youtubeCookieManager = null;
    private final Context context = this;
    private SwipeTouchListener swipeTouchListener;
    private String TAG ="Youtube Webview";
    private String urlToLoad = "https://john.me.tz/youtube";

    private static final ArrayList<String> allowedDomains = new ArrayList<String>();

    private ValueCallback<Uri[]> mUploadMessage;
    private final static int FILE_CHOOSER_REQUEST_CODE = 1;

    @Override
    protected void onPause() {
        if (youtubeCookieManager !=null) youtubeCookieManager.flush();
        swipeTouchListener = null;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        youtubeWebView.setOnTouchListener(swipeTouchListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTheme(android.R.style.Theme_DeviceDefault_DayNight);
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create the WebView
        youtubeWebView = findViewById(R.id.youtubeWebView);
        registerForContextMenu(youtubeWebView);

        //Set cookie options
        youtubeCookieManager = CookieManager.getInstance();
        youtubeCookieManager.setAcceptCookie(false);
        youtubeCookieManager.setAcceptThirdPartyCookies(youtubeWebView, false);

        //Restrict what gets loaded
        initURLs();

        youtubeWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage.message().contains("NotAllowedError: Write permission denied.")) {  //this error occurs when user copies to clipboard
                    Toast.makeText(context, R.string.error_copy,Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    }
                }
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                    mUploadMessage = null;
                }

                mUploadMessage = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
                return true;
            }
        });  //needed to share link

        youtubeWebView.setWebViewClient(new WebViewClient() {
            //Keep these in sync!
            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {

                if (request.getUrl().toString().equals("about:blank")) {
                    return null;
                }
                if (!request.getUrl().toString().startsWith("https://")) {
                    Log.d(TAG, "[shouldInterceptRequest][NON-HTTPS] Blocked access to " + request.getUrl().toString());
                    return new WebResourceResponse("text/javascript", "UTF-8", null); //Deny URLs that aren't HTTPS
                }
                boolean allowed = false;
                for (String url : allowedDomains) {
                    if (request.getUrl().getHost().endsWith(url)) {
                        allowed = true;
                    }
                }
                if (!allowed) {
                    Log.d(TAG, "[shouldInterceptRequest][NOT ON ALLOWLIST] Blocked access to " + request.getUrl().getHost());
                    return new WebResourceResponse("text/javascript", "UTF-8", null); //Deny URLs not on ALLOWLIST
                }
                return null;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().equals("about:blank")) {
                    return false;
                }
                if (!request.getUrl().toString().startsWith("https://")) {
                    Log.d(TAG, "[shouldOverrideUrlLoading][NON-HTTPS] Blocked access to " + request.getUrl().toString());
                    return true; //Deny URLs that aren't HTTPS
                }
                boolean allowed = false;
                for (String url : allowedDomains) {
                    if (request.getUrl().getHost().endsWith(url)) {
                        allowed = true;
                    }
                }
                if (!allowed) {
                    Log.d(TAG, "[shouldOverrideUrlLoading][NOT ON ALLOWLIST] Blocked access to " + request.getUrl().getHost());
                    return true; //Deny URLs not on ALLOWLIST
                }
                return false;
            }
        });

        //Set more options
        youtubeWebSettings = youtubeWebView.getSettings();
        //Enable some WebView features
        youtubeWebSettings.setJavaScriptEnabled(true);
        youtubeWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        youtubeWebSettings.setDomStorageEnabled(true);
        //Disable some WebView features
        youtubeWebSettings.setAllowContentAccess(false);
        youtubeWebSettings.setAllowFileAccess(false);
        youtubeWebSettings.setBuiltInZoomControls(true);
        youtubeWebSettings.setDisplayZoomControls(false);
        youtubeWebSettings.setGeolocationEnabled(false);
        youtubeWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        youtubeWebView.loadUrl(urlToLoad);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Credit (CC BY-SA 3.0): https://stackoverflow.com/a/6077173
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (youtubeWebView.canGoBack() && !youtubeWebView.getUrl().equals("about:blank")) {
                        youtubeWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private static void initURLs() {
        //Allowed Domains
        allowedDomains.add("m.youtube.com");
        allowedDomains.add("duckduckgo.com");
        allowedDomains.add("www.duckduckgo.com");
        allowedDomains.add("www.youtube-nocookie.com");
        allowedDomains.add("john.me.tz");
        allowedDomains.add("yt3.ggpht.com");
        allowedDomains.add("yt3.googleusercontent.com");
        allowedDomains.add("i.ytimg.com");
        allowedDomains.add("googlevideo.com");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (mUploadMessage == null) return;
            Uri[] result = null;
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    String dataString = intent.getDataString();
                    if (dataString != null) {
                        result = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        WebView.HitTestResult result = youtubeWebView.getHitTestResult();
        if (result.getExtra() != null) {
            if (result.getType() == IMAGE_TYPE) {
                String url = result.getExtra();
                Uri source = Uri.parse(url);
                DownloadManager.Request request = new DownloadManager.Request(source);
                request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url));
                request.addRequestHeader("Accept", "text/html, application/xhtml+xml, *" + "/" + "*");
                request.addRequestHeader("Accept-Language", "en-US,en;q=0.7,he;q=0.3");
                request.addRequestHeader("Referer", url);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                String filename = URLUtil.guessFileName(url, null, "image/jpeg");
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                Toast.makeText(this,getString(R.string.download)+"\n"+filename, Toast.LENGTH_SHORT).show();
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                assert dm != null;
                dm.enqueue(request);
            }
        }
    }

    public String modUserAgent(){

        String newPrefix = "Mozilla/5.0 (X11; Linux "+ System.getProperty("os.arch") +")";

        String newUserAgent=WebSettings.getDefaultUserAgent(context);
        String prefix = newUserAgent.substring(0, newUserAgent.indexOf(")") + 1);
         try {
                newUserAgent=newUserAgent.replace(prefix,newPrefix);
            } catch (Exception e) {
                e.printStackTrace();
            }
         return newUserAgent;
    }
}
