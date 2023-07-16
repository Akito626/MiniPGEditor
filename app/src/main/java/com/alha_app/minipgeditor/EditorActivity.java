package com.alha_app.minipgeditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class EditorActivity extends AppCompatActivity {
    private Handler handler;

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Toolbar toolbar = findViewById(R.id.toolbar_editor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Spinner spinner = findViewById(R.id.spinner_language);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.language)
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        handler = new Handler();
    }
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    // メニューの設定
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_run){
            Toast.makeText(this, "run", Toast.LENGTH_SHORT).show();
            new Thread(() -> {
                runCode();
            }).start();
        }
        return true;
    }

    public void runCode(){
        EditText sourceText = findViewById(R.id.source_code);
        // code
        String sourceCode = null;
        try {
            sourceCode = URLEncoder.encode(sourceText.getText().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String language = "java";
        String input = "";

        String urlString = "http://api.paiza.io:80/runners/create?source_code=" + sourceCode + "&language=" + language +"&api_key=guest";
        StringBuilder sb = new StringBuilder();
        String result = "";
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        if(!input.equals("")){
            String tmp = "&input=" + input;
            urlString += tmp;
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);

            // 通信開始
            con.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String tmp = "";
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp);
            }
            result = sb.toString();
            System.out.println(sourceCode);
            jsonResult = mapper.readTree(result);
            id = jsonResult.get("id").toString();
            String status = jsonResult.get("status").toString();
            System.out.println(result);

            br.close();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getStatus(){
        String urlString = "http://api.paiza.io:80/runners/get_status?id=" + id + "&api_key=guest";
        StringBuilder sb = new StringBuilder();
        String result = "";
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // 通信開始
            con.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String tmp = "";
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp);
            }
            result = sb.toString();
            System.out.println(result);
            jsonResult = mapper.readTree(result);
            String status = jsonResult.get("status").toString();

            if(status.equals("completed")){
                getResult();
            }

            br.close();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getResult(){
        String urlString = "http://api.paiza.io:80/runners/get_details?id=" + id + "&api_key=guest";
        StringBuilder sb = new StringBuilder();
        String result = "";
        JsonNode jsonResult = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // 通信開始
            con.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String tmp = "";
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp);
            }
            result = sb.toString();
            System.out.println(result);
            jsonResult = mapper.readTree(result);

            br.close();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}