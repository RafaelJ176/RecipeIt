package il.rjana.recipeit.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import il.rjana.recipeit.R;
import il.rjana.recipeit.model.VideoItem;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoItem> videos;
    private int expandedPosition = -1;

    public VideoAdapter(List<VideoItem> videos) {
        this.videos = videos;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem video = videos.get(position);
        holder.titleTextView.setText(video.getTitle());
        holder.descriptionTextView.setText(video.getDescription());

        Glide.with(holder.itemView.getContext())
                .load(video.getThumbnailUrl())
                .into(holder.thumbnailImageView);

        final boolean isExpanded = position == expandedPosition;
        holder.videoWebView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.thumbnailImageView.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        holder.itemView.setActivated(isExpanded);

        if (isExpanded) {
            String videoHtml = "<html><body>" +
                    "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" + 
                    video.getVideoId() + "?autoplay=1\" frameborder=\"0\" allowfullscreen></iframe>" +
                    "</body></html>";
            
            holder.videoWebView.loadData(videoHtml, "text/html", "utf-8");
        }

        holder.itemView.setOnClickListener(v -> {
            expandedPosition = isExpanded ? -1 : position;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void setVideos(List<VideoItem> videos) {
        this.videos = videos;
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView descriptionTextView;
        WebView videoWebView;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.video_thumbnail);
            titleTextView = itemView.findViewById(R.id.video_title);
            descriptionTextView = itemView.findViewById(R.id.video_description);
            videoWebView = itemView.findViewById(R.id.video_web_view);
            
            if (videoWebView != null) {
                videoWebView.getSettings().setJavaScriptEnabled(true);
                videoWebView.getSettings();
                videoWebView.setWebChromeClient(new WebChromeClient());
            }
        }
    }
} 