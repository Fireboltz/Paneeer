package org.amfoss.paneeer.editorCamera.ui;

import android.os.Bundle;
import android.text.TextUtils;
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
import org.amfoss.paneeer.editorCamera.ui.adapter.FilterListAdapter;
import org.amfoss.paneeer.editorCamera.viewmodel.ContentsViewModel;

public class FilterFragment
        extends Fragment
        implements View.OnClickListener, FilterListAdapter.Listener {

    private static final String TAG = FilterFragment.class.getSimpleName();

    private FilterListAdapter mFilterListAdapter;
    private ContentsViewModel mContentsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_filter, container, false);

        rootView.findViewById(R.id.close_filter_button).setOnClickListener(this);
        rootView.findViewById(R.id.clear_filter_button).setOnClickListener(this);
        rootView.findViewById(R.id.filter_plus_button).setOnClickListener(this);
        rootView.findViewById(R.id.filter_minus_button).setOnClickListener(this);
        rootView.findViewById(R.id.vignett_button).setOnClickListener(this);
        rootView.findViewById(R.id.blur_button).setOnClickListener(this);

        RecyclerView recyclerViewFilter = rootView.findViewById(R.id.filter_recyclerview);

        recyclerViewFilter.setHasFixedSize(true);
        LinearLayoutManager filterLayoutManager = new LinearLayoutManager(getContext());
        filterLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewFilter.setLayoutManager(filterLayoutManager);

        mFilterListAdapter = new FilterListAdapter(getContext(), this);
        recyclerViewFilter.setAdapter(mFilterListAdapter);

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

                    if (contentsResponse == null) return;

                    for (CategoryModel model : contentsResponse.categories) {
                        if (TextUtils.equals(model.title, "filters")) {
                            mFilterListAdapter.setData(model.items);
                            return;
                        }
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
        switch (v.getId()) {
            case R.id.close_filter_button:
                getActivity().onBackPressed();
                break;
            case R.id.clear_filter_button: {
                ((EditorCameraActivity)getActivity()).clearFilter();
                break;
            }
            case R.id.filter_plus_button:
                ((EditorCameraActivity)getActivity()).setFilterStrength(10);
                break;
            case R.id.filter_minus_button:
                ((EditorCameraActivity)getActivity()).setFilterStrength(-10);
                break;
            case R.id.vignett_button:
                ((EditorCameraActivity)getActivity()).setVignette();
                break;
            case R.id.blur_button:
                ((EditorCameraActivity)getActivity()).setBlurVignette();
                break;
        }
    }

    @Override
    public void onFilterSelected(int position, ItemModel item) {
        ((EditorCameraActivity)getActivity()).setFilter(item);
    }
}
