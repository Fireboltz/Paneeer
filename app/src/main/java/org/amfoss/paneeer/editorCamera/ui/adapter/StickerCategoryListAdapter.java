package org.amfoss.paneeer.editorCamera.ui.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.editorCamera.model.CategoryModel;

import java.util.ArrayList;
import java.util.List;


public class StickerCategoryListAdapter extends RecyclerView.Adapter<StickerCategoryListAdapter.ViewHolder> {

	private static final String TAG = StickerCategoryListAdapter.class.getSimpleName();

	private List<CategoryModel> mCategories = new ArrayList<>();

	public interface Listener {
		void onCategorySelected(CategoryModel category);
	}

	private Listener mListener;

	public StickerCategoryListAdapter(Listener listener) {
		mListener = listener;
	}

	public void setData(List<CategoryModel> categories){
		mCategories.clear();
		for (CategoryModel model : categories) {
			if (!TextUtils.equals(model.title, "filters")) {
				mCategories.add(model);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		return mCategories.size();
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		holder.bind(position);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_sticker, parent, false);
		return new CategoryViewHolder(v);
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

	public class CategoryViewHolder extends ViewHolder implements View.OnClickListener {
		Button mButtonCategory = null;
		CategoryModel mCategory;
		CategoryViewHolder(View v) {
			super(v);
			mButtonCategory = (Button) v.findViewById(R.id.category_button);
		}

		@Override
		void bind(int position) {
			mCategory = mCategories.get(position);

			/*
			 * Sticker Category 의 제목을 표시합니다.
			 */
			Log.d(TAG, "category_sticker " + position + " " + mCategory);
			mButtonCategory.setText(mCategory.title);
			mButtonCategory.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if(mListener != null){
				mListener.onCategorySelected(mCategory);
			}
		}
	}
}