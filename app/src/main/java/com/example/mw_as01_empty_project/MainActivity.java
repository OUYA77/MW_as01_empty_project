package com.example.mw_as01_empty_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    ImageView imgView;

    String baseUrl = "http://10.0.2.2:8000";
    Bitmap bmImg = null;
    CLoadImage task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imgView);
        task = new CLoadImage();
    }
    /*
    public void onClickForLoad(View v) {
        // API를 호출하여 이미지 URL 가져오기
        task.execute(baseUrl + "/api/get_dynamic_image_url/");
    }*/

    private final Handler handler = new Handler();
    private final int delay = 60000; // 60초마다 API 호출 (원하는 주기로 수정 가능)

    @Override
    protected void onResume() {
        super.onResume();
        startRepeatingTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRepeatingTask();
    }

    Runnable runnable = new Runnable() {
        public void run() {
            // 주기적으로 API 호출
            task.execute(baseUrl + "/api/get_dynamic_image_url/");
            handler.postDelayed(this, delay);
        }
    };

    void startRepeatingTask() {
        runnable.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(runnable);
    }


    private class CLoadImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                // API 호출
                URL apiUrl = new URL(urls[0]);
                HttpURLConnection apiConn = (HttpURLConnection) apiUrl.openConnection();
                apiConn.setDoInput(true);
                apiConn.connect();

                InputStream apiIs = apiConn.getInputStream();
                Scanner scanner = new Scanner(apiIs).useDelimiter("\\A");
                String jsonResponse = scanner.hasNext() ? scanner.next() : "";

                // jsonResponse를 파싱하여 이미지 URL 추출
                JSONObject response = new JSONObject(jsonResponse);
                String image_url = response.optString("image_url");

                // 이미지 URL을 백그라운드에서 가져오도록 설정
                URL real_imageUrl = new URL(baseUrl + image_url);
                HttpURLConnection imageConn = (HttpURLConnection) real_imageUrl.openConnection();
                imageConn.setDoInput(true);
                imageConn.connect();

                InputStream imageIs = imageConn.getInputStream();
                return BitmapFactory.decodeStream(imageIs);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Bitmap img) {
            if (img != null) {
                // 이미지를 UI 스레드에서 설정
                imgView.setImageBitmap(img);
            } else {
                // 오류 처리
                Log.e("API Error", "Failed to process URL: ");
            }

            // 새로운 이미지를 가져올 때 새로운 AsyncTask를 생성하여 실행
            task = new CLoadImage();
        }
    }


    public static void saveBitmaptoJpeg(Bitmap bitmap, String folder, String name) {
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder_name = "/" + folder + "/";
        String file_name = name + ".jpg";
        String string_path = ex_storage + folder_name;

        File file_path = new File(string_path);

        if (!file_path.exists()) {
            file_path.mkdirs();
        }

        try {
            FileOutputStream out = new FileOutputStream(string_path + file_name);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (FileNotFoundException exception) {
            Log.e("FileNotFoundException", exception.getMessage());
        } catch (IOException exception) {
            Log.e("IOException", exception.getMessage());
        }
    }
}
