package com.example.moviematch;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.moviematch.adapter.TabAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class LikeAndWatchListFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_like_and_watch_list, container, false);

        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        TabAdapter tabAdapter = new TabAdapter(this);
        viewPager.setAdapter(tabAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setTabTextColors(
                ContextCompat.getColor(requireContext(), R.color.gray),
                ContextCompat.getColor(requireContext(), R.color.pink)
        );
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.pink));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("My Watch List");
            } else {
                tab.setText("Liked Movies");
            }
        }).attach();

        return view;
    }
}
