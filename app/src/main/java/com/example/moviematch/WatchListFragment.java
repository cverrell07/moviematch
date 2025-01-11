package com.example.moviematch;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.moviematch.adapter.CardMovieAdapter;
import com.example.moviematch.models.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchListFragment extends Fragment {

    private RecyclerView recyclerView;
    private CardMovieAdapter adapter;
    private FirebaseFirestore db;
    private ListenerRegistration watchListListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch_list, container, false);
        recyclerView = view.findViewById(R.id.watchListRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();

        adapter = new CardMovieAdapter(requireContext(), new ArrayList<>(), getGenreMap(), movie -> {
            openDetailActivity(movie);
        });
        recyclerView.setAdapter(adapter);

        fetchWatchList();

        return view;
    }

    private void fetchWatchList() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        watchListListener = db.collection("users")
                .document(userId)
                .collection("watchList")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore Error", "Error listening to watchList: " + error.getMessage());
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<Movie> movies = querySnapshot.toObjects(Movie.class);

                        if (movies == null) {
                            movies = new ArrayList<>();
                        }

                        Log.d("WatchListFragment", "Fetched " + movies.size() + " movies");
                        adapter.updateMovies(movies);
                    } else {
                        Log.e("WatchListFragment", "No data found in watchList for user " + userId);
                        adapter.updateMovies(new ArrayList<>());
                    }
                });
    }

    private Map<Integer, String> getGenreMap() {
        Map<Integer, String> genreMap = new HashMap<>();
        genreMap.put(28, "Action");
        genreMap.put(12, "Adventure");
        genreMap.put(16, "Animation");
        genreMap.put(35, "Comedy");
        genreMap.put(80, "Crime");
        genreMap.put(99, "Documentary");
        genreMap.put(18, "Drama");
        genreMap.put(10751, "Family");
        genreMap.put(14, "Fantasy");
        genreMap.put(36, "History");
        genreMap.put(27, "Horror");
        genreMap.put(10402, "Music");
        genreMap.put(9648, "Mystery");
        genreMap.put(10749, "Romance");
        genreMap.put(878, "Science Fiction");
        genreMap.put(10770, "TV Movie");
        genreMap.put(53, "Thriller");
        genreMap.put(10752, "War");
        genreMap.put(37, "Western");

        return genreMap;
    }

    private void openDetailActivity(Movie movie) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }
}