package com.sample.usbdemo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.sample.usbdemo.dao.AppDatabase;
import com.sample.usbdemo.dao.User;
import com.sample.usbdemo.event.DownloadEvent;
import com.sample.usbdemo.service.androidService;
import com.sample.usbdemo.util.FileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, androidService.class));

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "usb_demo.db")
                .allowMainThreadQueries()//允许在主线程中查询
                .build();
    }

    @Subscribe
    public void onMessageEvent(DownloadEvent event) {
        event.getFile();
        unzipFile(event.getFile());
    }

    private void unzipFile(File file) {
        try {
            String pathStr = FileUtils.getFile(this);
            ZipUtils.unzipFile(file.getPath(), pathStr);
            File jsonFile = new File(pathStr + "/update/update.json");
            JSONBean jsonBean = loadFile(jsonFile);
            saveDate(jsonBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDate(JSONBean jsonBean) {
        db.userDao().insertAll(jsonBean.getUser().getUser_list());
        final List<User> users = db.userDao().getAll();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.textView);
                textView.setText(users.toString());
            }
        });
    }

    private JSONBean loadFile(File file) {
        //文件内容字符串
        StringBuilder content = new StringBuilder();
        JSONBean jsonBean = null;
        try {
            InputStream in = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            //分行读取
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            in.close();

            jsonBean = GsonUtils.fromJson(content.toString(), JSONBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonBean;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
