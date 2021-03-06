package pl.pisze_czytam.polishnews;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewsActivity extends AppCompatActivity implements LoaderCallbacks<List<News>> {
    String requestWithoutKey = "https://content.guardianapis.com/world/poland?show-fields=trailText,byline,thumbnail";
    String apiKey = BuildConfig.ApiKey;
    ListView newsList;
    TextView emptyView;
    ProgressBar progressBar;
    private NewsAdapter newsAdapter;
    private static final int NEWS_LOADER_ID = 1;
    public static boolean leadContentChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        newsList = findViewById(R.id.list);
        emptyView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progress_bar);

        newsList.setEmptyView(emptyView);
        newsAdapter = new NewsAdapter(this, R.layout.news_activity, new ArrayList<News>());
        newsList.setAdapter(newsAdapter);

        // Check if preference is switched on - to know, if leadContent should be loaded too.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        leadContentChecked = sharedPreferences.getBoolean(getString(R.string.lead_content_key), true);
        PreferenceManager.setDefaultValues(this, R.xml.settings_fragment, false);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
        } else {
            progressBar.setVisibility(View.GONE);
            emptyView.setText(R.string.no_internet);
            emptyView.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.wifi_off), null, null);
        }

        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                News currentNews = newsAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNews.getUrl());
                startActivity(new Intent(Intent.ACTION_VIEW, newsUri));
            }
        });
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Uri baseUri = Uri.parse(requestWithoutKey);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        String newsNumber = sharedPreferences.getString(getString(R.string.news_number_key), getString(R.string.news_number_default));
        uriBuilder.appendQueryParameter("page-size", newsNumber);

        // Check if a user picked dates. If not, do not add them to query.
        String dateFrom = sharedPreferences.getString(getString(R.string.from_date_key), getString(R.string.default_date_start));
        if (!(dateFrom.equals(getString(R.string.default_date_start)))) {
            uriBuilder.appendQueryParameter("from-date", dateFrom);
        }
        String dateTo = sharedPreferences.getString(getString(R.string.to_date_key), getString(R.string.default_date_start));
        if (!(dateTo.equals(getString(R.string.default_date_start)))) {
            uriBuilder.appendQueryParameter("to-date", dateTo);
        }

        // Check checked sections, compare it to all sections and delete excluded one.
        Set<String> checkedSections = sharedPreferences.getStringSet(getString(R.string.check_sections_key), new HashSet<String>());
        String[] sectionsToInclude = checkedSections.toArray(new String[checkedSections.size()]);
        String[] allSections = getResources().getStringArray(R.array.section_keys);

        ArrayList<String> sectionsToExclude = new ArrayList<>();
        for (String section : allSections) {
            if (!Arrays.asList(sectionsToInclude).contains(section)) {
                sectionsToExclude.add(section);
            }
        }
        StringBuilder addToQuery = new StringBuilder();
        String prefix = "-";
        for (String section : sectionsToExclude) {
            addToQuery.append(prefix);
            addToQuery.append(section);
            prefix = ",-";
        }
        String queryWithSections = addToQuery.toString();
        if (!queryWithSections.equals("")) {
            uriBuilder.appendQueryParameter("section", queryWithSections);
        }

        uriBuilder.appendQueryParameter("api-key", apiKey);
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> newsList) {
        emptyView.setText(R.string.no_news);
        emptyView.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.announcement), null, null);
        newsAdapter.clear();

        if (newsList != null && !newsList.isEmpty()) {
            newsAdapter.addAll(newsList);
        }
        progressBar.setVisibility(View.GONE);

        // Check if connected, when app comes into foreground again - to show right info.
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            progressBar.setVisibility(View.GONE);
            emptyView.setText(R.string.no_internet);
            emptyView.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.wifi_off), null, null);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        newsAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.about_item:
                startActivity(new Intent(this, AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    // Delete dates from preferences when a user goes back to settings.
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getString(R.string.from_date_key)).apply();
        editor.remove(getString(R.string.to_date_key)).apply();
    }
}
