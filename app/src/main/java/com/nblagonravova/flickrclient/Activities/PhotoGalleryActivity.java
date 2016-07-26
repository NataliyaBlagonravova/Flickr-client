package com.nblagonravova.flickrclient.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.nblagonravova.flickrclient.Fragments.PhotoGalleryFragment;

public class PhotoGalleryActivity extends SingleFragmentActivity{

    public static Intent newIntent(Context context){
        return new Intent(context, PhotoGalleryActivity.class);
    }


    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

}
