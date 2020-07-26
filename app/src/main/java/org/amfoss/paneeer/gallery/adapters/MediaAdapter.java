package org.amfoss.paneeer.gallery.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.gallery.data.Media;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

  private ArrayList<Media> medias;
  private boolean fav = false;

  private BitmapDrawable placeholder;
  private View.OnClickListener mOnClickListener;
  private View.OnLongClickListener mOnLongClickListener;
  Context context;

  public MediaAdapter(ArrayList<Media> ph, Context context) {
    medias = ph;
    this.context = context;
    updatePlaceholder(context);
  }

  public void updatePlaceholder(Context context) {
    Drawable drawable = ContextCompat.getDrawable(context, R.drawable.placeholder);
    placeholder = (BitmapDrawable) drawable;
  }

  public ArrayList<Media> getList() {
    return medias;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo, parent, false);
    v.setOnClickListener(mOnClickListener);
    v.setOnLongClickListener(mOnLongClickListener);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(final MediaAdapter.ViewHolder holder, int position) {

    Media f = medias.get(position);

    holder.path.setTag(f);

    holder.icon.setVisibility(View.GONE);

    Glide.with(holder.imageView.getContext())
        .load(f.getUri())
        //        .asBitmap()
        .signature(f.getSignature())
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .thumbnail(0.5f)
        .placeholder(placeholder)
        .transition(new GenericTransitionOptions<>().transition(R.anim.fade_in))
        .into(holder.imageView);
    holder.icon.setVisibility(View.GONE);
    holder.path.setVisibility(View.GONE);

    if (f.isSelected()) {
      holder.icon.setIcon(CommunityMaterial.Icon.cmd_check);
      holder.icon.setVisibility(View.VISIBLE);
      holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
      holder.layout.setPadding(15, 15, 15, 15);
    } else {
      holder.imageView.clearColorFilter();
      holder.layout.setPadding(0, 0, 0, 0);
    }
  }

  @Override
  public int getItemCount() {
    return medias.size();
  }

  public void setOnClickListener(View.OnClickListener lis) {
    mOnClickListener = lis;
  }

  public void setOnLongClickListener(View.OnLongClickListener lis) {
    mOnLongClickListener = lis;
  }

  public void swapDataSet(ArrayList<Media> asd, boolean fav) {
    medias = asd;
    this.fav = fav;
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    protected ImageView imageView;
    protected View layout;
    protected TextView path;
    protected IconicsImageView icon;

    ViewHolder(View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.photo_preview);
      layout = itemView.findViewById(R.id.media_card_layout);
      path = itemView.findViewById(R.id.photo_path);
      icon = itemView.findViewById(R.id.icon);
    }
  }
}
