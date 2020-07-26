package org.amfoss.paneeer.editorCamera.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.editorCamera.model.ItemModel;

import java.util.ArrayList;
import java.util.List;


public class StickerListAdapter extends RecyclerView.Adapter<StickerListAdapter.ViewHolder> {

	private static final String TAG = StickerListAdapter.class.getSimpleName();

	private List<ItemModel> mItems = new ArrayList<>();

	public interface Listener {
		void onStickerSelected(int position, ItemModel item);
	}

	private Listener mListener;

	private Context mContext;

	public StickerListAdapter(Context context, Listener listener) {
		mContext = context;
		mListener = listener;
	}

	public void setData(List<ItemModel> items) {
		mItems.clear();
		if(items != null) {
			mItems.addAll(items);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		holder.bind(position);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker, parent, false);
		return new StickerViewHolder(v);
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	abstract class ViewHolder extends RecyclerView.ViewHolder {
		abstract void bind(int position);

		ViewHolder(View v) {
			super(v);
		}
	}

	public class StickerViewHolder extends ViewHolder implements View.OnClickListener {
		ImageView mImageViewItemThumbnail = null;

		ItemModel mItem;
		int position;

		StickerViewHolder(View v) {
			super(v);
			mImageViewItemThumbnail = v.findViewById(R.id.item_thumbnail_imageview);
		}

		@Override
		void bind(int position) {
			mItem = mItems.get(position);
			this.position = position;

			Log.d(TAG, "item_sticker " + position + " " + mItem.thumbnailUrl + " " + mItem);
			mImageViewItemThumbnail.setOnClickListener(this);

			//스티커의 섬네일을 보여줍니다
			Glide.with(mContext)
					.load(mItem.thumbnailUrl)
					.fitCenter()
					.into(mImageViewItemThumbnail);
		}

		@Override
		public void onClick(View v) {
			if(mListener != null){
				mListener.onStickerSelected(position, mItem);
			}
		}
	}
}