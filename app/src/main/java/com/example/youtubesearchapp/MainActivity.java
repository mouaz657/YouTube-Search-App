package com.example.youtubesearchapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etSearchQuery;
    private Button btnSearch;
    private RecyclerView rvVideos;
    private ProgressBar progressBar;

    private VideoAdapter adapter;
    private List<VideoItem> videoList;

    // مفتاح الـ API الخاص بك من التكليف
    private static final String API_KEY = "AIzaSyAEk7F_bbhTFUWxwJXDn5fzxviwCJYk7EY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط المتغيرات بعناصر الواجهة
        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnSearch = findViewById(R.id.btnSearch);
        rvVideos = findViewById(R.id.rvVideos);
        progressBar = findViewById(R.id.progressBar);

        // إعداد الـ RecyclerView
        rvVideos.setLayoutManager(new LinearLayoutManager(this));
        videoList = new ArrayList<>();
        adapter = new VideoAdapter(videoList);
        rvVideos.setAdapter(adapter);

        // تفعيل زر البحث
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = etSearchQuery.getText().toString().trim();

                // معالجة الخطأ 1: إذا كان مربع الإدخال فارغاً
                if (query.isEmpty()) {
                    Toast.makeText(MainActivity.this, "الرجاء إدخال كلمة البحث!", Toast.LENGTH_SHORT).show();
                    return;
                }

                searchVideos(query);
            }
        });
    }

    private void searchVideos(String query) {
        // إظهار مؤشر التحميل
        progressBar.setVisibility(View.VISIBLE);
        videoList.clear();
        adapter.notifyDataSetChanged();

        // بناء رابط البحث واستبدال المتغيرات بالقيم المطلوبة
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=15&q="
                + query + "&key=" + API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        // إرسال الطلب في مسار خلفي (Off the main thread) لعدم تجميد الواجهة
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // معالجة الخطأ 2: فشل الاتصال بالشبكة
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "فشل الاتصال بالإنترنت!", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray itemsArray = jsonObject.getJSONArray("items");

                        // استخراج البيانات من الرد
                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject item = itemsArray.getJSONObject(i);
                            JSONObject snippet = item.getJSONObject("snippet");

                            String title = snippet.getString("title");
                            String description = snippet.getString("description");
                            String publishTime = snippet.getString("publishedAt");
                            String channelTitle = snippet.getString("channelTitle");

                            // جلب رابط صورة الغلاف
                            String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

                            videoList.add(new VideoItem(title, description, publishTime, channelTitle, thumbnailUrl));
                        }

                        // تحديث الواجهة يجب أن يتم على المسار الرئيسي (UI Thread)
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();

                            // معالجة الخطأ 3: عدم العثور على نتائج
                            if (videoList.isEmpty()) {
                                Toast.makeText(MainActivity.this, "لم يتم العثور على نتائج!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "حدث خطأ في معالجة البيانات!", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        // معالجة الخطأ 4: مفتاح API غير صالح أو مفقود
                        Toast.makeText(MainActivity.this, "حدث خطأ: تأكد من مفتاح API أو جرب لاحقاً", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}