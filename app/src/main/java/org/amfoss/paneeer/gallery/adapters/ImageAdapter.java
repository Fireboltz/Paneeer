package org.amfoss.paneeer.gallery.adapters;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.gallery.data.Media;
import org.amfoss.paneeer.utilities.ActivitySwitchHelper;
import org.amfoss.paneeer.utilities.BasicCallBack;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static org.amfoss.paneeer.utilities.ActivitySwitchHelper.context;
import static org.amfoss.paneeer.utilities.ActivitySwitchHelper.getContext;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
  BasicCallBack basicCallBack;
  private ArrayList<Media> media;
  private OnSingleTap onSingleTap;
  private enterTransition mEnterTransitions;

  public ImageAdapter(
      ArrayList<Media> media,
      BasicCallBack onItemClickListener,
      OnSingleTap singleTap,
      enterTransition transition) {
    this.media = media;
    this.onSingleTap = singleTap;
    this.basicCallBack = onItemClickListener;
    mEnterTransitions = transition;
  }

  @Override
  public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.unit_image_pager, null, false);

    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {

    holder.imageView.setTransitionName(context.getString(R.string.transition_photo));
    Glide.with(getContext())
        .load(media.get(position).getUri())
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .thumbnail(0.5f)
        .listener(
            new RequestListener<Drawable>() {
              @Override
              public boolean onLoadFailed(
                  @Nullable GlideException e,
                  Object model,
                  Target<Drawable> target,
                  boolean isFirstResource) {
                return false;
              }

              @Override
              public boolean onResourceReady(
                  Drawable resource,
                  Object model,
                  Target<Drawable> target,
                  DataSource dataSource,
                  boolean isFirstResource) {
                startPostponedTransition(holder.imageView);
                return false;
              }
            })
        .into(holder.imageView);
    holder.imageView.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {

            basicCallBack.callBack(0, null);
          }
        });
  }

  public interface enterTransition {
    void startPostponedTransition();
  }

  private void startPostponedTransition(final View sharedElement) {
    sharedElement
        .getViewTreeObserver()
        .addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {
              @Override
              public boolean onPreDraw() {
                sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                mEnterTransitions.startPostponedTransition();
                return true;
              }
            });
  }

  /** Interface for calling up the toggleUI on single tap on the image. */
  public interface OnSingleTap {
    void singleTap();
  }

  @Override
  public int getItemCount() {
    return media.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    PhotoView imageView;
    LinearLayout linearLayout;

    public ViewHolder(View itemView) {
      super(itemView);

      imageView = new PhotoView(ActivitySwitchHelper.context);
      imageView.setOnPhotoTapListener(
          new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {

              onSingleTap.singleTap();
            }

            @Override
            public void onOutsidePhotoTap() {
              onSingleTap.singleTap();
            }
          });

      linearLayout = itemView.findViewById(R.id.layout);
      WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      int width = size.x;
      int height = size.y;
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
      imageView.setLayoutParams(params);
      imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
      linearLayout.addView(imageView);
    }

    public PhotoView getImageView() {
      return this.imageView;
    }
  }
}
