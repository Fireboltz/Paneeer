package org.amfoss.paneeer.editorCamera.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.seerslab.argear.session.ARGContents;
import com.seerslab.argear.session.ARGFrame;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.editorCamera.data.BeautyItemData;

import java.util.ArrayList;
import java.util.List;

public class BeautyListAdapter extends RecyclerView.Adapter<BeautyListAdapter.ViewHolder> {

    private ArrayList<BeautyItemData.BeautyItemInfo> mData = new ArrayList<>();
    private int mSelectedIndex = -1;

    public interface Listener {
        void onBeautyItemSelected(ARGContents.BeautyType beautyType);
        ARGFrame.Ratio getViewRatio();
    }

    private BeautyListAdapter.Listener mListener;

    public BeautyListAdapter(BeautyListAdapter.Listener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<BeautyItemData.BeautyItemInfo> data){
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final BeautyListAdapter.ViewHolder holder, final int position) {
        holder.bind(position);
    }

    @Override
    public BeautyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beauty, parent, false);
        return new BeautyListAdapter.BeautyItemViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public void selectItem(ARGContents.BeautyType beautyType) {
        for (int i = 0; i < mData.size(); i++) {
            BeautyItemData.BeautyItemInfo itemInfo = mData.get(i);
            if (beautyType == itemInfo.mBeautyType) {
                mSelectedIndex = i;
                break;
            }
        }

        if (mSelectedIndex != -1) {
            notifyDataSetChanged();
        }
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        abstract void bind(int position);
        ViewHolder(View v) {
            super(v);
        }
    }

    public class BeautyItemViewHolder extends BeautyListAdapter.ViewHolder implements View.OnClickListener {
        Button mItemButton;
        BeautyItemData.BeautyItemInfo mInfo;

        BeautyItemViewHolder(View v) {
            super(v);
            mItemButton = v.findViewById(R.id.beauty_item_button);
        }

        @Override
        void bind(int position) {
            mInfo = mData.get(position);

            if (mListener.getViewRatio() == ARGFrame.Ratio.RATIO_FULL) {
                if (mSelectedIndex == position) {
                    mItemButton.setBackgroundResource(mInfo.mResource2);
                } else {
                    mItemButton.setBackgroundResource(mInfo.mResource1);
                }
            } else {
                if (mSelectedIndex == position) {
                    mItemButton.setBackgroundResource(mInfo.mResource1);
                } else {
                    mItemButton.setBackgroundResource(mInfo.mResource2);
                }
            }

            mItemButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener.getViewRatio() == ARGFrame.Ratio.RATIO_FULL) {
                mItemButton.setBackgroundResource(mInfo.mResource2);
            } else {
                mItemButton.setBackgroundResource(mInfo.mResource1);
            }

            notifyItemChanged(mSelectedIndex);
            mSelectedIndex = getLayoutPosition();

            if (mListener != null) {
                mListener.onBeautyItemSelected(mInfo.mBeautyType);
            }
        }
    }
}
