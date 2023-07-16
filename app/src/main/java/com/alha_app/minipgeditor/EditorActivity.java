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
import android.widget.ListView;
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
import java.util.Timer;
import java.util.TimerTask;

public class EditorActivity extends AppCompatActivity {
    private Handler handler;

    private String id;

    Timer timer;

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
        // ソースコードを取得し、エンコードする
        EditText sourceText = findViewById(R.id.source_code);
        String sourceCode = null;
        try {
            sourceCode = URLEncoder.encode(sourceText.getText().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        Spinner spinner = findViewById(R.id.spinner_language);
        String language = (String) spinner.getSelectedItem();
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
            jsonResult = mapper.readTree(result);
            id = jsonResult.get("id").toString();
            id = id.substring(1, id.length()-1);

            timer = new Timer(false);
            TimerTask task  = new TimerTask() {
                @Override
                public void run() {
                    getStatus();
                }
            };
            timer.schedule(task, 1000, 1000);

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
            if(jsonResult.get("error") != null) {
                String error = jsonResult.get("error").toString();
                timer.cancel();
                System.out.println(error);
                System.out.println(id);
                System.out.println(result);

                return;
            }

            String status = jsonResult.get("status").toString();

            if(status.equals("\"completed\"")){
                timer.cancel();
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

        // 結果出力用
        String[] resultData = new String[6];
        String build_time;
        String build_result;
        String build_stderr;
        String stdout;
        String exit_code;
        String time;
        ListView listView = findViewById(R.id.output_list);

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
            jsonResult = mapper.readTree(result);
            build_time = jsonResult.get("build_time").toString();
            build_result = jsonResult.get("build_result").toString();
            build_stderr = jsonResult.get("build_stderr").toString();
            stdout = jsonResult.get("stdout").toString();
            exit_code = String.valueOf(jsonResult.get("exit_code").asInt());
            time = jsonResult.get("time").toString();

            resultData[0] = build_time;
            resultData[1] = build_result;
            resultData[2] = build_stderr;
            resultData[3] = stdout;
            resultData[4] = exit_code;
            resultData[5] = time;

            handler.post(() -> {
                listView.setAdapter(new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        resultData
                ));
            });

            br.close();
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}