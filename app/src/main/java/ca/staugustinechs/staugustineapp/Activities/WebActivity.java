package ca.staugustinechs.staugustineapp.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.R;

public class WebActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private WebView page;
    private String url, name;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        toolbar = findViewById(R.id.web_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent selection = getIntent();

        url = selection.getStringExtra(Main.WEB_SELECT);
        name = selection.getStringExtra(Main.WEB_NAME);
        getSupportActionBar().setTitle(name);
        page = findViewById(R.id.web_view);

        WebViewClient client = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        };
        updateColors();
        page.setWebViewClient(client);
        page.getSettings().setJavaScriptEnabled(true);
        page.getSettings().setAppCacheEnabled(true);
        page.getSettings().setBuiltInZoomControls(true);
        page.getSettings().setSaveFormData(true);
        toolbar.setTitle(name);
        page.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.refresh_web) {
            page.loadUrl(url);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        page.destroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {


            if (page.canGoBack()) {
                page.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateColors() {
        //CHANGE TOOLBAR AND DRAWER HEADING COLORS
        toolbar.setBackgroundColor(AppUtils.PRIMARY_COLOR);
        //SET STATUS BAR COLOR
        getWindow().setNavigationBarColor(AppUtils.PRIMARY_DARK_COLOR);
        getWindow().setStatusBarColor(AppUtils.PRIMARY_DARK_COLOR);
    }

}
