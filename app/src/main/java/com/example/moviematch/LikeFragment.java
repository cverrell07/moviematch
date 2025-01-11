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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikeFragment extends Fragment {

    private RecyclerView recyclerView;
    private CardMovieAdapter adapter;
    private FirebaseFirestore db;
    private ListenerRegistration likedMoviesListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_like, container, false);
        recyclerView = view.findViewById(R.id.likedMoviesRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();

        adapter = new CardMovieAdapter(requireContext(), new ArrayList<>(), getGenreMap(), movie -> {
            openDetailActivity(movie);
        });
        recyclerView.setAdapter(adapter);

        fetchLikedMovies();

        return view;
    }

    private void fetchLikedMovies() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        likedMoviesListener = db.collection("users")
                .document(userId)
                .collection("likedMovies")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore Error", "Error listening to likedMovies: " + error.getMessage());
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<Movie> updatedMovies = querySnapshot.toObjects(Movie.class);
                        adapter.updateMovies(updatedMovies);
                        Log.d("Firestore Update", "Movies updated in real-time: " + updatedMovies.size());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (likedMoviesListener != null) {
            likedMoviesListener.remove();
        }
    }
}