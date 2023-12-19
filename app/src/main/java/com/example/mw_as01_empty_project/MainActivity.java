package com.example.mw_as01_empty_project;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private ImageView imgView;
    private String baseUrl = "http://10.0.2.2:8000";
    private CLoadImage task;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imgView);
        task = new CLoadImage();
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
    }


    public void onClickForPoPup(){
        stopRepeatingTask();

        // Show the custom dialog
        showCustomDialog();
    }
//    public void onClickForAccept(View v) {
        // 서버에 Accept 신호 보내기
  //      sendSignalToServer("accept");
    //}

    public void onClickForDeny(View v) {
        // 서버에 Deny 신호 보내기
        sendSignalToServer("deny");
    }

    private void sendSignalToServer(final String signal) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // 서버에 신호를 전송하는 API 호출
                    URL apiUrl = new URL(baseUrl + "/api/send_signal/");
                    HttpURLConnection apiConn = (HttpURLConnection) apiUrl.openConnection();
                    apiConn.setRequestMethod("POST");
                    apiConn.setDoOutput(true);

                    // 신호 전송
                    String postData = "signal=" + signal;
                    apiConn.getOutputStream().write(postData.getBytes("UTF-8"));

                    // 응답 코드 확인
                    int responseCode = apiConn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // 성공적으로 서버에 신호를 전송한 경우
                        Log.d("Signal Sent", "Signal: " + signal + " sent successfully");
                    } else {
                        // 서버에 신호 전송 실패
                        Log.e("Signal Error", "Failed to send signal to server. Response Code: " + responseCode);
                    }

                    apiConn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Signal Error", "IOException while sending signal to server");
                }
                return null;
            }
        }.execute();
    }

    private final Handler handler = new Handler();
    private final int delay = 10000; // 10초마다 API 호출 (원하는 주기로 수정 가능)

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
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
        }
    }


    private class CLoadImage extends AsyncTask<String, Void, List<Post>> {
        @Override
        protected List<Post> doInBackground(String... urls) {
            List<Post> posts = new ArrayList<>();
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

                // 예시: 이미지 URL을 백그라운드에서 가져오도록 설정
                URL real_imageUrl = new URL(baseUrl + image_url);
                HttpURLConnection imageConn = (HttpURLConnection) real_imageUrl.openConnection();
                imageConn.setDoInput(true);
                imageConn.connect();

                InputStream imageIs = imageConn.getInputStream();
                Bitmap img = BitmapFactory.decodeStream(imageIs);

                // 예시: post 객체 생성 및 리스트에 추가
                Post post = new Post();
                post.setImageUrl(img);
                posts.add(post);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return posts;
        }

        protected void onPostExecute(List<Post> posts) {
            if (posts != null && !posts.isEmpty()) {
                // 예시: RecyclerView에 데이터 갱신
                postList.addAll(posts);
                postAdapter.notifyDataSetChanged();
            } else {
                // 오류 처리
                Log.e("API Error", "Failed to process URL: ");
            }
        }
    }


    public void onClickForAccept(View v) {
        // Stop the repeating task and cancel the existing CLoadImage task
        stopRepeatingTask();

        // Show the custom dialog
        showCustomDialog();
    }
    private void showCustomDialog() {
        // 레이아웃 인플레이션
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_layout, null);

        // 다이얼로그 빌더 생성
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(customView);

        // 다이얼로그 생성
        final AlertDialog dialog = dialogBuilder.create();

        // 이미지뷰, 버튼 등에 접근하여 이벤트 처리 가능
        ImageView imageView = customView.findViewById(R.id.imgView);
        Button acceptButton = customView.findViewById(R.id.acceptButton);
        Button denyButton = customView.findViewById(R.id.denyButton);

        // 각 버튼에 클릭 이벤트 설정
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Accept 버튼 클릭 시 수행할 동작
                // 예를 들면, 다이얼로그 닫기 등의 동작
                dialog.dismiss();
            }
        });

        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deny 버튼 클릭 시 수행할 동작
                dialog.dismiss();
            }
        });

        // 다이얼로그 표시
        dialog.show();
    }

}
