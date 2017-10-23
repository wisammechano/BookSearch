package com.recoded.booksearch;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.recoded.booksearch.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ArrayList<Book> books;
    int scrollY;
    BookListAdapter adapter = null;

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (savedInstanceState != null) {
            books = savedInstanceState.getParcelableArrayList("Data");
            scrollY = savedInstanceState.getInt("Scroll");
            populateList();
        } else {
            books = new ArrayList<>();
            scrollY = 0;
        }

        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = binding.searchField.getText().toString();
                if (isConnected()) {
                    if (keyword.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Your search keyword cannot be empty!", Toast.LENGTH_LONG).show();
                    } else {
                        binding.searchField.clearFocus();
                        hideSoftKeyboard(MainActivity.this);
                        searchApi(keyword);
                    }
                }
            }
        });
    }

    private void searchApi(String keyword) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.noResult.setVisibility(View.GONE);
        binding.resultListView.setVisibility(View.GONE);
        String url;
        url = "https://www.googleapis.com/books/v1/volumes?q=" + keyword;
        ApiSearcher searchTask = new ApiSearcher(url);
        searchTask.execute();
    }

    private void parseJson(String s) {
        if (s.isEmpty()) {
            Toast.makeText(this, "Please check your internet connection, no response from server!", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            JSONObject response = new JSONObject(s);
            if (!response.has("items")) {
                binding.progressBar.setVisibility(View.GONE);
                binding.noResult.setVisibility(View.VISIBLE);
                return;
            }
            JSONArray results = response.getJSONArray("items");
            Book book;
            JSONObject volumeInfo;
            books.clear();
            for (int i = 0; i < results.length(); i++) {
                volumeInfo = results.getJSONObject(i).getJSONObject("volumeInfo");
                book = new Book(volumeInfo.getString("title"));
                if (volumeInfo.has("authors")) {
                    for (int j = 0; j < volumeInfo.getJSONArray("authors").length(); j++) {
                        book.addAuthor(volumeInfo.getJSONArray("authors").getString(j));
                    }
                } else {
                    book.addAuthor("No author");
                }
                if (volumeInfo.has("description")) {
                    book.setDesc(volumeInfo.getString("description"));
                }
                if (volumeInfo.has("imageLinks")) {
                    book.setImgUrl(volumeInfo.getJSONObject("imageLinks").getString("smallThumbnail"));
                }
                books.add(book);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        populateList();

    }

    private void populateList() {
        if (adapter == null) {
            adapter = new BookListAdapter(this, 0, books);
            binding.resultListView.setAdapter(adapter);
        } else {
            adapter.resetCollection(books);
            adapter.notifyDataSetChanged();
        }

        binding.progressBar.setVisibility(View.GONE);
        binding.noResult.setVisibility(View.GONE);
        binding.resultListView.setVisibility(View.VISIBLE);
        binding.resultListView.setScrollY(scrollY);
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        }
        Toast.makeText(this, "Connect to internet and try again!", Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putParcelableArrayList("Data", books);
        savedInstanceState.putInt("Scroll", binding.resultListView.getScrollY());
        // etc.
    }

    private class ApiSearcher extends AsyncTask<String, String, String> {
        URL url;

        public ApiSearcher(String url) {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String[] params) {
            StringBuilder json = new StringBuilder();
            HttpURLConnection httpUrlConnection = null;
            InputStream inputStream = null;
            try {
                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.setConnectTimeout(5000);
                httpUrlConnection.setReadTimeout(5000);
                httpUrlConnection.connect();
                if (httpUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = httpUrlConnection.getInputStream();
                    InputStreamReader inputReader = new InputStreamReader(inputStream);
                    BufferedReader bufferReader = new BufferedReader(inputReader);
                    String line = bufferReader.readLine();
                    while (line != null) {
                        json.append(line);
                        line = bufferReader.readLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpUrlConnection != null) httpUrlConnection.disconnect();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.d("ApiSearcher", "URL: " + url.toString());
            Log.d("ApiSearcher", "JSON: " + json.toString());
            return json.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            parseJson(s);
        }
    }
}