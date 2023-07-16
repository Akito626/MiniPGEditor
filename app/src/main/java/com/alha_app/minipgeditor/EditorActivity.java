package com.alha_app.minipgeditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditorActivity extends AppCompatActivity {
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Toolbar toolbar = findViewById(R.id.toolbar_editor);
        setSupportActionBar(toolbar);

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
        String sourceCode = sourceText.getText().toString();
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
            System.out.println(result);
            jsonResult = mapper.readTree(result);
            String id = jsonResult.get("id").toString();
            String status = jsonResult.get("status").toString();

            br.close();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}