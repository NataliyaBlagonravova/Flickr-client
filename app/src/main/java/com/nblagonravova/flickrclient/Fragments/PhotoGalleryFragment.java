package com.nblagonravova.flickrclient.Fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nblagonravova.flickrclient.Activities.PhotoPageActivity;
import com.nblagonravova.flickrclient.Connections.FlickrFetch;
import com.nblagonravova.flickrclient.Model.GalleryItem;
import com.nblagonravova.flickrclient.Preferences.QueryPreferences;
import com.nblagonravova.flickrclient.R;
import com.nblagonravova.flickrclient.Services.PollService;
import com.nblagonravova.flickrclient.listeners.EndlessRecyclerOnScrollListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private PhotoAdapter mPhotoAdapter;
    private EndlessRecyclerOnScrollListener listener;
    GridLayoutManager mGridLayoutManager;
    private List<GalleryItem> mItems;


    private int mCurrentPage = 1;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mGridLayoutManager = new GridLayoutManager(getActivity(), 3);

        listener = new EndlessRecyclerOnScrollListener(mGridLayoutManager) {
            @Override
            public void onLoadMore() {
                Log.d(TAG, "Need new page");
                ++mCurrentPage;
                updateItems(false);
            }
        };

        mItems = new ArrayList<>();
        updateItems(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) view.findViewById(
                R.id.fragment_photo_gallery_recycler_view);

        mPhotoRecyclerView.setLayoutManager(mGridLayoutManager);
        mPhotoRecyclerView.addOnScrollListener(listener);
        setAdapter();

        return view;
    }

    private void setAdapter() {
        if (mPhotoRecyclerView.getAdapter() == null && isAdded()) {
            mPhotoAdapter = new PhotoAdapter(mItems);
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                mCurrentPage = 1;
                updateItems(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoreQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    private void updateItems(boolean isFirstPage) {
        String query = QueryPreferences.getStoreQuery(getActivity());
        new FetchItemsTask(query, isFirstPage).execute(mCurrentPage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                mCurrentPage = 1;
                updateItems(true);
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        private String mQuery;
        boolean isFirstPage;
        int mPage;

        public FetchItemsTask(String query, boolean firstPage) {
            mQuery = query;
            isFirstPage = firstPage;
        }

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {

            mPage = params[0];

            Log.d(TAG, "page = " + mPage);

            if (mQuery == null) {
                Log.d(TAG, "find method");
                return new FlickrFetch().fetchRecentPhotos(mPage);
            } else {
                Log.d(TAG, "search method");
                return new FlickrFetch().searchPhotos(mQuery, mPage);
            }
        }


        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            setAdapter();
            if (isFirstPage) {
                mPhotoAdapter.swap(galleryItems);
            } else {
                mPhotoAdapter.add(galleryItems);
            }
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
                    startActivity(intent);
                }
            });
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
            Picasso.with(getActivity())
                    .load(galleryItem.getUrl())
                    .placeholder(R.drawable.noimage)
                    .into(mItemImageView);
        }
    }


    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

        public void swap(List<GalleryItem> items) {
            mGalleryItems.clear();
            mGalleryItems.addAll(items);
            notifyDataSetChanged();

        }

        public void add(List<GalleryItem> items) {
            mGalleryItems.addAll(items);
            notifyDataSetChanged();
        }
    }


}

