package com.example.moviematch;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.moviematch.adapter.CardMovieAdapter;
import com.example.moviematch.interfaces.TMDBApiService;
import com.example.moviematch.models.Movie;
import com.example.moviematch.models.MovieResponse;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {
    TextView searchHeaderTV;
    RecyclerView moviesRecyclerView;
    CardMovieAdapter adapter;
    TextInputEditText mainSearchET;
    TextInputLayout mainSearchTIL;

    private Handler handler = new Handler();
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchHeaderTV = view.findViewById(R.id.searchHeaderTV);
        moviesRecyclerView = view.findViewById(R.id.searchMovieRV);
        mainSearchET = view.findViewById(R.id.mainSearchET);
        mainSearchTIL = view.findViewById(R.id.mainSearchTIL);

        gradientHeader();
        setupRecyclerView();
        setupSearchListener();

        fetchMovies();

        return view;
    }

    private void gradientHeader() {
        int startColor = Color.parseColor("#FFB4B4");
        int endColor = Color.parseColor("#FEFFBB");

        LinearGradient linearGradient = new LinearGradient(
                0f, 0f, searchHeaderTV.getPaint().measureText(searchHeaderTV.getText().toString()), searchHeaderTV.getTextSize(),
                new int[]{startColor, endColor},
                null, Shader.TileMode.CLAMP);

        searchHeaderTV.getPaint().setShader(linearGradient);
    }

    private void setupRecyclerView() {
        adapter = new CardMovieAdapter(
                requireContext(),
                new ArrayList<>(),
                getGenreMap(),
                movie -> openDetailActivity(movie)
        );
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moviesRecyclerView.setAdapter(adapter);
    }

    private void setupSearchListener() {
        mainSearchET.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString();
                Log.d("SearchFragment", "Query: " + query);
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!query.isEmpty()) {
                            searchMovies(query);
                        } else {
                            fetchMovies();
                        }
                    }
                };
                handler.postDelayed(searchRunnable, 100);
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });
    }

    private void searchMovies(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDBApiService api = retrofit.create(TMDBApiService.class);

        String apiToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzMjRjMmQ1YjZlODNiNjE3MTY2NjA5MGYyNzU1MzhmZSIsIm5iZiI6MTczMzA0MzQ5My45MjcsInN1YiI6IjY3NGMyNTI1MTYxNmI0YTg1OGM3YjM5MCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pj5vjPat7ZnyFO6JpS07vkdprGuqzkoHMPbhLb_mo1M";

        Call<MovieResponse> call = api.searchMovies(apiToken, query, "en-US", 1);
        Log.d("SearchFragment", "Search URL: " + call.request().url());

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (movies != null && !movies.isEmpty()) {
                        Log.d("SearchFragment", "Movies fetched: " + movies.size());
                        adapter.updateMovies(movies);
                    } else {
                        Log.d("SearchFragment", "No movies found.");
                        adapter.updateMovies(new ArrayList<>());
                    }
                } else {
                    Log.e("SearchFragment", "Response not successful. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("SearchFragment", "Error fetching movies", t);
            }
        });
    }

    private void fetchMovies() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDBApiService api = retrofit.create(TMDBApiService.class);

        String apiToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzMjRjMmQ1YjZlODNiNjE3MTY2NjA5MGYyNzU1MzhmZSIsIm5iZiI6MTczMzA0MzQ5My45MjcsInN1YiI6IjY3NGMyNTI1MTYxNmI0YTg1OGM3YjM5MCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pj5vjPat7ZnyFO6JpS07vkdprGuqzkoHMPbhLb_mo1M";

        Call<MovieResponse> call = api.discoverMovies(apiToken, "en-US", "revenue.desc", 1);

        Log.d("SearchFragment", "API Call: " + call.request().url());

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    Log.d("SearchFragment", "Movies fetched: " + movies.size());
                    adapter.updateMovies(movies);
                } else {
                    Log.e("SearchFragment", "Response not successful. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("SearchFragment", "Error fetching movies", t);
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