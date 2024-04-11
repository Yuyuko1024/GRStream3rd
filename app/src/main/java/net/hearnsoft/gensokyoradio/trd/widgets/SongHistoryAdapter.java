package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.beans.SongHistoryBean;
import net.hearnsoft.gensokyoradio.trd.databinding.HistorySongItemBinding;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SongHistoryAdapter extends RecyclerView.Adapter<SongHistoryAdapter.HistoryViewHolder> {

    private List<SongHistoryBean> beanList;
    private View mItemView;

    public SongHistoryAdapter(List<SongHistoryBean> bean) {
        this.beanList = bean;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_song_item,
                parent, false);
        return new HistoryViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        SongHistoryBean bean = beanList.get(position);
        Context context = holder.itemView.getContext();
        String title = bean.getTitle();
        String artist = context.getString(R.string.item_str_artists, bean.getArtist());
        String album = context.getString(R.string.item_str_album, bean.getAlbum());
        String circle = context.getString(R.string.item_str_circle, bean.getCircle());
        String coverUrl = bean.getCoverUrl();
        String datetime = timestampToDateStr(context,
                bean.getTimestamp());

        Glide.with(mItemView.getContext())
                .load(coverUrl)
                .placeholder(R.drawable.ic_unknown_pic)
                .error(R.drawable.ic_error_load)
                .into(holder.mCover);
        holder.title.setText(title);
        holder.artists.setText(artist);
        holder.album.setText(album);
        holder.circle.setText(circle);
        holder.datetime.setText(datetime);
    }

    @Override
    public int getItemCount() {
        return beanList == null ? 0 : beanList.size();
    }

    private String timestampToDateStr(Context context, long timestamp) {
        String formatPattern = context.getString(R.string.datetime_format);
        SimpleDateFormat formatter = new SimpleDateFormat(formatPattern, Locale.getDefault());
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mCover;
        private final TextView title;
        private final TextView artists;
        private final TextView album;
        private final TextView circle;
        private final TextView datetime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mCover = itemView.findViewById(R.id.cover);
            this.title = itemView.findViewById(R.id.title);
            this.album = itemView.findViewById(R.id.album);
            this.circle = itemView.findViewById(R.id.circle);
            this.artists = itemView.findViewById(R.id.artists);
            this.datetime = itemView.findViewById(R.id.datetime);
        }
    }

}
