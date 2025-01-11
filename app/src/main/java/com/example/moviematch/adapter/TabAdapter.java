package com.example.moviematch.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.moviematch.LikeFragment;
import com.example.moviematch.WatchListFragment;

public class TabAdapter extends FragmentStateAdapter {
    public TabAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new WatchListFragment();
        } else {
            return new LikeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

