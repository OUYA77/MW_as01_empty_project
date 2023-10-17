package com.example.mw_as01_empty_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.material3.MaterialTheme;
import androidx.compose.runtime.BitwiseOperatorsKt;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.tooling.preview.Preview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity{
    ImageView imgView;

    String imageUrl = "http://kiokahn.synology.me:30000/";
    Bitmap bmImg =null;
    CLoadImage task;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView=(ImageView) findViewById(R.id.imgView);
        task = new CLoadImage();
    }

    public void onClickForLoad(View v)
    {
        task.execute(imageUrl+"uploads/-/system/appearance/logo/1/Gazzi_Labs_CI_type_B_-_big_logo.png");

        Toast.makeText(getApplicationContext(), "Load",Toast.LENGTH_LONG).show();
    }

    public void onClickForSave(View v)
    {
        saveBitmaptoJpeg(bmImg,"DCIM","image");
        Toast.makeText(getApplicationContext(),"Save", Toast.LENGTH_LONG).show();
    }


    private class CLoadImage extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls){
            try{
                URL myFileUrl= new URL(urls[0]);
                HttpURLConnection conn=(HttpURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();

                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch(IOException e){
                e.printStackTrace();
            }

            return bmImg;
        }

        protected void onPostExecute(Bitmap img){
            imgView.setImageBitmap(bmImg);
        }
    }

    public static void saveBitmaptoJpeg(Bitmap bitmap, String folder, String name) {

        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder_name = "/" + folder + "/";
        String file_name = name + ".jpg";
        String string_path = ex_storage + folder_name;
        Log.d("경로", string_path);

        File file_path;
        file_path = new File(string_path);

        if (!file_path.exists()) {
            file_path.mkdirs();
        }

        try{
            FileOutputStream out = new FileOutputStream(string_path+file_name);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.close();
        }catch (FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch (IOException exception){
            Log.e("IOException",exception.getMessage());
        }

    }
}


