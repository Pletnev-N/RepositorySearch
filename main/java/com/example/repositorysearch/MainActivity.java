package com.example.repositorysearch;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> repository_name = new ArrayList<String>();
    ArrayList<String> repository_url = new ArrayList<String>();
    ArrayAdapter<String> list_adapter;
    EditText keywords_view;
    TextView message_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.repositoryList);
        list_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, repository_name);
        listView.setAdapter(list_adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                Uri uri = Uri.parse(repository_url.get(position));
                Intent openLink = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(openLink);
            }
        });

        keywords_view = (EditText) findViewById(R.id.keyWords);
        message_view = (TextView) findViewById(R.id.message);
    }

    public void search(View view) {
        new SearchTask().execute();
    }


    private class SearchTask extends AsyncTask<Void, Void, String> {

        String url_string = "https://api.github.com/search/repositories?q=";
        int rep_amount;

        @Override
        protected void onPreExecute() {
            String keywords = keywords_view.getText().toString();
            url_string += keywords.replace(' ','+');
        }

        @Override
        protected String doInBackground(Void... params) {
            repository_name.clear();
            repository_url.clear();
            StringBuilder buffer = new StringBuilder();
            try {
                BufferedReader reader = null;
                URL url = new URL(url_string);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                reader.close();
                connection.disconnect();
            } catch(IOException e) {
                if (e.getClass() == FileNotFoundException.class) {
                    return getResources().getText(R.string.incorrect_query).toString();
                }
                if (e.getClass() == UnknownHostException.class) {
                    return getResources().getText(R.string.connection_problems).toString();
                }
                e.printStackTrace();
            }

            try {
                String json_string = buffer.toString();
                JSONObject json_obj = new JSONObject(json_string);
                JSONArray json_arr = json_obj.getJSONArray("items");
                rep_amount = json_arr.length();
                for (int i=0; i<rep_amount; i++) {
                    JSONObject obj = json_arr.getJSONObject(i);
                    repository_name.add(i, Integer.toString(i+1) + ". " + obj.getString("name"));
                    repository_url.add(i,obj.getString("html_url"));
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }

            if (rep_amount == 0) {
                return getResources().getText(R.string.nothing_found).toString();
            }
            else
                return "";
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            list_adapter.notifyDataSetChanged();
            message_view.setText(message);
        }
    }


}
