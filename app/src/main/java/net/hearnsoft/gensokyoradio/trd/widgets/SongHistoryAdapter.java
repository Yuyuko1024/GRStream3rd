package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.beans.SongHistoryBean;
import net.hearnsoft.gensokyoradio.trd.databinding.HistorySongItemBinding;
import net.hearnsoft.gensokyoradio.trd.databinding.SongHistoryItemDialogVeiwBinding;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SongHistoryAdapter extends RecyclerView.Adapter<SongHistoryAdapter.HistoryViewHolder> {

    private final String TAG = this.getClass().getSimpleName();

    private List<SongHistoryBean> beanList;
    private View mItemView;
    private ViewGroup mParent;
    private MaterialAlertDialogBuilder builder;

    public SongHistoryAdapter(List<SongHistoryBean> bean) {
        this.beanList = bean;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_song_item,
                parent, false);
        this.mParent = parent;
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
        holder.mItem.setOnClickListener(v -> showSongItemDialog(context, mParent, bean));
    }

    private void showSongItemDialog(Context context, ViewGroup viewGroup, SongHistoryBean bean) {
        SongHistoryItemDialogVeiwBinding binding =
                SongHistoryItemDialogVeiwBinding.inflate(
                        LayoutInflater.from(context),
                        viewGroup,
                        false
                );
        Glide.with(mItemView.getContext())
                .load(bean.getCoverUrl())
                .placeholder(R.drawable.ic_unknown_pic)
                .error(R.drawable.ic_error_load)
                .into(binding.cover);
        binding.infoTitle.setText(bean.getTitle());
        binding.infoArtist.setText(bean.getArtist());
        binding.infoCircle.setText(bean.getCircle());
        binding.infoAlbum.setText(bean.getAlbum());
        binding.footerTips.setText(context.getString(R.string.song_item_info_footer_tips));
        builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(bean.getTitle());
        builder.setView(binding.getRoot());
        builder.setPositiveButton(R.string.song_item_btn_search, (dialog, which) -> {
            showSearchDialog(context, bean);
        });
        builder.setNegativeButton(R.string.song_item_btn_copy, (dialog, which) -> {
            showCopyTextDialog(context, bean);
        });
        builder.setNeutralButton(android.R.string.ok, null);
        builder.show();
    }

    private void showCopyTextDialog(Context context, SongHistoryBean bean) {
        builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.song_item_btn_copy);
        String[] buttons = new String[]{
                context.getString(R.string.song_item_btn_copy_placeholder, bean.getTitle()),
                context.getString(R.string.song_item_btn_copy_placeholder, bean.getArtist()),
                context.getString(R.string.song_item_btn_copy_placeholder, bean.getCircle()),
                context.getString(R.string.song_item_btn_copy_placeholder, bean.getAlbum())
        };
        builder.setItems(buttons, (dialog, which) -> {
            switch (which) {
                case 0:
                    copyText(context, bean.getTitle());
                    break;
                case 1:
                    copyText(context, bean.getArtist());
                    break;
                case 2:
                    copyText(context, bean.getCircle());
                    break;
                case 3:
                    copyText(context, bean.getAlbum());
                    break;
            }
        });
        builder.setNegativeButton(android.R.string.ok, null);
        builder.show();
    }

    private void showSearchDialog(Context context, SongHistoryBean bean) {
        builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.song_item_btn_search);
        String[] buttons = new String[]{
                context.getString(R.string.song_item_btn_search_placeholder, bean.getTitle()),
                context.getString(R.string.song_item_btn_search_placeholder, bean.getArtist()),
                context.getString(R.string.song_item_btn_search_placeholder, bean.getCircle()),
                context.getString(R.string.song_item_btn_search_placeholder, bean.getAlbum())
        };
        builder.setItems(buttons, (dialog, which) -> {
            switch (which) {
                case 0:
                    searchOnTHBWiki(context, bean.getTitle());
                    break;
                case 1:
                    searchOnTHBWiki(context, bean.getArtist());
                    break;
                case 2:
                    searchOnTHBWiki(context, bean.getCircle());
                    break;
                case 3:
                    searchOnTHBWiki(context, bean.getAlbum());
                    break;
            }
        });
        builder.setNegativeButton(android.R.string.ok, null);
        builder.show();
    }

    private void searchOnTHBWiki(Context context, String text) {
        Uri uri = Uri.parse("https://thwiki.cc/" + text);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        context.startActivity(intent);
    }

    private void copyText(Context context, String text) {
        try {
            ClipboardManager clipboardManager = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("Label", text);
            clipboardManager.setPrimaryClip(data);
        } catch (Exception e) {
            Log.e(TAG, "copy text to clipboard error");
        }
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

        private final LinearLayout mItem;
        private final ImageView mCover;
        private final TextView title;
        private final TextView artists;
        private final TextView album;
        private final TextView circle;
        private final TextView datetime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mItem = itemView.findViewById(R.id.song_item);
            this.mCover = itemView.findViewById(R.id.cover);
            this.title = itemView.findViewById(R.id.title);
            this.album = itemView.findViewById(R.id.album);
            this.circle = itemView.findViewById(R.id.circle);
            this.artists = itemView.findViewById(R.id.artists);
            this.datetime = itemView.findViewById(R.id.datetime);
        }
    }

}
