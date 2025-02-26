package il.rjana.recipeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import il.rjana.recipeit.R;
import il.rjana.recipeit.model.VideoItem;
import il.rjana.recipeit.ui.adapter.VideoAdapter;
import il.rjana.recipeit.viewmodel.RecipeViewModel;

public class YouTubeVideosFragment extends Fragment {

    private static final String API_KEY = "AIzaSyD82spaT9-tzweazDFIDGtaOLF9z02DUXs"; // Replace with your actual API key
    private static final String YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/search";

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private EditText searchInput;
    private Button searchButton;
    private RequestQueue requestQueue;
    private RecipeViewModel recipeViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_youtube_videos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Initialize views
        recyclerView = view.findViewById(R.id.videos_recycler_view);
        searchInput = view.findViewById(R.id.video_search_input);
        searchButton = view.findViewById(R.id.video_search_button);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VideoAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Set search button click listener
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchYouTubeVideos(query + " recipe");
            } else {
                Toast.makeText(getContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
            }
        });

        // Load popular recipe videos by default
        searchYouTubeVideos("popular recipes");
    }

    private void searchYouTubeVideos(String query) {
        String url = YOUTUBE_API_URL + "?part=snippet&maxResults=20&q=" + query + 
                "&type=video&key=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<VideoItem> videoItems = new ArrayList<>();
                        JSONArray items = response.getJSONArray("items");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject id = item.getJSONObject("id");
                            JSONObject snippet = item.getJSONObject("snippet");

                            String videoId = id.getString("videoId");
                            String title = snippet.getString("title");
                            String description = snippet.getString("description");
                            String thumbnailUrl = snippet.getJSONObject("thumbnails")
                                    .getJSONObject("medium").getString("url");

                            VideoItem videoItem = new VideoItem(videoId, title, description, thumbnailUrl);
                            videoItems.add(videoItem);
                        }

                        adapter.setVideos(videoItems);
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Error fetching videos", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }
} 