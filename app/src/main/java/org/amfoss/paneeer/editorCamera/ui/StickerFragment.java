package org.amfoss.paneeer.editorCamera.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.editorCamera.api.ContentsResponse;
import org.amfoss.paneeer.editorCamera.model.CategoryModel;
import org.amfoss.paneeer.editorCamera.model.ItemModel;
import org.amfoss.paneeer.editorCamera.ui.adapter.StickerCategoryListAdapter;
import org.amfoss.paneeer.editorCamera.ui.adapter.StickerListAdapter;
import org.amfoss.paneeer.editorCamera.viewmodel.ContentsViewModel;

public class StickerFragment
        extends Fragment
        implements View.OnClickListener, StickerCategoryListAdapter.Listener, StickerListAdapter.Listener {

    private static final String TAG = StickerFragment.class.getSimpleName();

    private StickerCategoryListAdapter mStickerCategoryListAdapter;
    private StickerListAdapter mStickerListAdapter;

    private ContentsViewModel mContentsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sticker, container, false);

        rootView.findViewById(R.id.close_sticker_button).setOnClickListener(this);
        rootView.findViewById(R.id.clear_sticker_button).setOnClickListener(this);

        // init category_sticker list
        RecyclerView recyclerViewStickerCategory = rootView.findViewById(R.id.sticker_category_recyclerview);

        recyclerViewStickerCategory.setHasFixedSize(true);
        LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(getContext());
        categoryLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewStickerCategory.setLayoutManager(categoryLayoutManager);

        mStickerCategoryListAdapter = new StickerCategoryListAdapter(this);
        recyclerViewStickerCategory.setAdapter(mStickerCategoryListAdapter);

        // init item_sticker list
        RecyclerView recyclerViewSticker = rootView.findViewById(R.id.sticker_recyclerview);

        recyclerViewSticker.setHasFixedSize(true);
        LinearLayoutManager itemsLayoutManager = new LinearLayoutManager(getContext());
        itemsLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewSticker.setLayoutManager(itemsLayoutManager);

        mStickerListAdapter = new StickerListAdapter(getContext(), this);
        recyclerViewSticker.setAdapter(mStickerListAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() != null) {
            mContentsViewModel = new ViewModelProvider(getActivity()).get(ContentsViewModel.class);
            mContentsViewModel.getContents().observe(getViewLifecycleOwner(), new Observer<ContentsResponse>() {
                @Override
                public void onChanged(ContentsResponse contentsResponse) {
                    if (contentsResponse != null && contentsResponse.categories != null) {
                        mStickerCategoryListAdapter.setData(contentsResponse.categories);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.close_sticker_button:
                getActivity().onBackPressed();
                break;
            case R.id.clear_sticker_button: {
                ((EditorCameraActivity)getActivity()).clearStickers();
                break;
            }
        }
    }

    @Override
    public void onCategorySelected(CategoryModel category) {
        mStickerListAdapter.setData(category.items);
    }

    @Override
    public void onStickerSelected(int position, ItemModel item) {
        ((EditorCameraActivity)getActivity()).setSticker(item);
    }
}
