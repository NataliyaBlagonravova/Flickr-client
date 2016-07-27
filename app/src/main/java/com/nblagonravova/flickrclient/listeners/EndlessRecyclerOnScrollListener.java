package com.nblagonravova.flickrclient.listeners;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private int mPreviousTotal = 0;
    private boolean isLoading = true;
    private int mVisibleThreshold = 3;

    private int mFirstVisibleItem;
    private int mVisibleItemCount;
    private int mTotalItemCount;


    private GridLayoutManager mGridLayoutManager;

    public EndlessRecyclerOnScrollListener(GridLayoutManager gridLayoutManager) {

         mGridLayoutManager = gridLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        mVisibleItemCount = recyclerView.getChildCount();
        Log.d(TAG, "Child count: " + mVisibleItemCount);
        mTotalItemCount = mGridLayoutManager.getItemCount();
        Log.d(TAG, "Item count: " + mTotalItemCount);
        mFirstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();
        Log.d(TAG, "First visible item: " + mFirstVisibleItem);
        Log.d(TAG, "Prev " + mPreviousTotal);

        if (isLoading) {
            if (mTotalItemCount > mPreviousTotal) {
                isLoading = false;
                mPreviousTotal = mTotalItemCount;
            }
        }

        Log.d(TAG, mTotalItemCount - mVisibleItemCount + " "
                + mFirstVisibleItem + mVisibleThreshold + " " + isLoading);
        if (!isLoading && (mTotalItemCount - mVisibleItemCount)
                <= (mFirstVisibleItem + mVisibleThreshold)) {

            onLoadMore();

            isLoading = true;
        }
    }

    public abstract void onLoadMore();

}
