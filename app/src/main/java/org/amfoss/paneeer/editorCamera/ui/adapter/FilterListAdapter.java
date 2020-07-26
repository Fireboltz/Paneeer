package org.amfoss.paneeer.editorCamera.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.editorCamera.model.ItemModel;

import java.util.ArrayList;
import java.util.List;


public class FilterListAdapter extends RecyclerView.Adapter<FilterListAdapter.ViewHolder> {

	private static final String TAG = FilterListAdapter.class.getSimpleName();

	private List<ItemModel> mItems = new ArrayList<>();

	public interface Listener{
		void onFilterSelected(int position, ItemModel item);
	}

	private Listener mListener;

	private Context mContext;

	public FilterListAdapter(Context context, Listener listener) {
		mContext = context;
		mListener = listener;
	}

	public void setData(List<ItemModel> items){
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
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
		return new ItemViewHolder(v);
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

	public class ItemViewHolder extends ViewHolder implements View.OnClickListener {
		ImageView mImageViewItemThumbnail = null;
		TextView mTextViewTitle = null;

		ItemModel mItem;

		int position;

		ItemViewHolder(View v) {
			super(v);
			mImageViewItemThumbnail = (ImageView) v.findViewById(R.id.item_thumbnail_imageview);
			mTextViewTitle = (TextView) v.findViewById(R.id.title_textview);
		}

		@Override
		void bind(int position) {
			mItem = mItems.get(position);
			this.position = position;

			Log.d(TAG, "item_filter " + position + " " + mItem.thumbnailUrl + " " + mItem);
			mImageViewItemThumbnail.setOnClickListener(this);

			//필터의 섬네일과 이름을 표시합니다 .
			Glide.with(mContext)
					.load(mItem.thumbnailUrl)
					.fitCenter()
					.into(mImageViewItemThumbnail);

			mTextViewTitle.setText(mItem.title);
		}

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				mListener.onFilterSelected(position, mItem);
			}
		}
	}
}