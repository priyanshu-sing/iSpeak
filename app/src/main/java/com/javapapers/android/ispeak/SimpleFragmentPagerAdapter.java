package com.javapapers.android.ispeak;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by AJAY KUMAR on 1/27/2018.
 */

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new Ayush();
        } else if (position == 1){
            return new Priyanshu();
        } else if (position == 2){
            return new Aryan();
        } else {
            return new Lokesh();
        }
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 4;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "Ayush";
            case 1:
                return "Priyanshu";
            case 2:
                return "Aryan";
            case 3:
                return "Lokesh";
            default:
                return null;
        }
    }

}