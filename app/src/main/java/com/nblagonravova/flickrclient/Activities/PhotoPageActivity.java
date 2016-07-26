package com.nblagonravova.flickrclient.Activities;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.nblagonravova.flickrclient.Fragments.PhotoPageFragment;

public class PhotoPageActivity extends SingleFragmentActivity{

    public static Intent newIntent(Context context, Uri photoPageUrl){
        Intent intent = new Intent(context, PhotoPageActivity.class);
        intent.setData(photoPageUrl);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }
}
