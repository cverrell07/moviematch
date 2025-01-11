package com.example.moviematch;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviematch.adapter.MovieAdapter;
import com.example.moviematch.interfaces.TMDBApiService;
import com.example.moviematch.models.GenreResponse;
import com.example.moviematch.models.Movie;
import com.example.moviematch.models.MovieResponse;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView mainNameTV;
    private RecyclerView homePopularRV, homeTopRatedRV, homeMovieYouMightLikeRV;
    private MovieAdapter popularMovieAdapter, topRatedMovieAdapter, movieYouLikeAdapter, discoverMovieAdapter;
    private List<Movie> popularMovieList, topRatedMovieList, movieYouLikeList, discoverMovieList;
    private LinearLayout mainSearchLayout;
    private Map<Integer, String> genreMap = new HashMap<>();
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mainNameTV = view.findViewById(R.id.mainNameTV);
        mainSearchLayout = view.findViewById(R.id.mainSearchLayout);
        gradientName();

        fetchName();

        mainSearchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HomeFragment", "Navigating to SearchFragment");

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, new SearchFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        homePopularRV = view.findViewById(R.id.homePopularRV);
        homePopularRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        homeTopRatedRV = view.findViewById(R.id.homeTopRatedRV);
        homeTopRatedRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        homeMovieYouMightLikeRV = view.findViewById(R.id.homeMoviesYouMightLikeRV);
        homeMovieYouMightLikeRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        popularMovieList = new ArrayList<>();
        topRatedMovieList = new ArrayList<>();
        movieYouLikeList = new ArrayList<>();
        discoverMovieList = new ArrayList<>();
        popularMovieAdapter = new MovieAdapter(getContext(), popularMovieList, genreMap, movie -> {
            openDetailActivity(movie);
        });

        topRatedMovieAdapter = new MovieAdapter(getContext(), topRatedMovieList, genreMap, movie -> {
            openDetailActivity(movie);
        });

        movieYouLikeAdapter = new MovieAdapter(getContext(), movieYouLikeList, genreMap, movie -> {
            openDetailActivity(movie);
        });

        discoverMovieAdapter = new MovieAdapter(getContext(), discoverMovieList, genreMap, movie -> {
            openDetailActivity(movie);
        });

        homePopularRV.setAdapter(popularMovieAdapter);
        homeTopRatedRV.setAdapter(topRatedMovieAdapter);
        homeMovieYouMightLikeRV.setAdapter(discoverMovieAdapter);

        fetchGenres();
        fetchPopularMovies();
        fetchTopRatedMovies();
        discoverMovies();

        return view;
    }

    private void gradientName(){
        int startColor = Color.parseColor("#FFB4B4");
        int endColor = Color.parseColor("#FEFFBB");

        LinearGradient linearGradient = new LinearGradient(
                0f, 0f, mainNameTV.getPaint().measureText(mainNameTV.getText().toString()), mainNameTV.getTextSize(),
                new int[]{startColor, endColor},
                null, Shader.TileMode.CLAMP);

        mainNameTV.getPaint().setShader(linearGradient);
    }

    private void fetchName(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        if (name != null) {
                            mainNameTV.setText(name + "!");
                        }
                    }
                } else {
                    Log.d("Firebase", "Error getting user document: ", task.getException());
                }
            });
        } else {
            Log.d("Firebase", "No user is signed in.");
        }
    }

    private void fetchGenres() {
        String apiToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzMjRjMmQ1YjZlODNiNjE3MTY2NjA5MGYyNzU1MzhmZSIsIm5iZiI6MTczMzA0MzQ5My45MjcsInN1YiI6IjY3NGMyNTI1MTYxNmI0YTg1OGM3YjM5MCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pj5vjPat7ZnyFO6JpS07vkdprGuqzkoHMPbhLb_mo1M";

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + apiToken)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDBApiService tmDbApi = retrofit.create(TMDBApiService.class);

        Call<GenreResponse> call = tmDbApi.getGenres();
        call.enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(Call<GenreResponse> call, Response<GenreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Genres fetched successfully.");

                    genreMap.clear();

                    for (GenreResponse.Genre genre : response.body().getGenres()) {
                        genreMap.put(genre.getId(), genre.getName());
                    }

                } else {
                    Log.d("API Response", "Failed to fetch genres. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GenreResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch genres: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to fetch genres", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecommendedMovies(List<Movie> likedMovies) {
        List<String> genres = extractGenresFromMovies(likedMovies);
        String genreQuery = TextUtils.join(",", genres);

        String apiToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzMjRjMmQ1YjZlODNiNjE3MTY2NjA5MGYyNzU1MzhmZSIsIm5iZiI6MTczMzA0MzQ5My45MjcsInN1YiI6IjY3NGMyNTI1MTYxNmI0YTg1OGM3YjM5MCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pj5vjPat7ZnyFO6JpS07vkdprGuqzkoHMPbhLb_mo1M";
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + apiToken)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDBApiService tmDbApi = retrofit.create(TMDBApiService.class);

        String url = "discover/movie?with_genres=" + genreQuery + "&language=en-US&page=1";

        Call<MovieResponse> call = tmDbApi.getMoviesByGenre(url);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Recommended movies fetched successfully.");

                    movieYouLikeList.clear();
                    movieYouLikeList.addAll(response.body().getResults());

                    movieYouLikeAdapter.notifyDataSetChanged();
                } else {
                    Log.e("API Response", "Failed to fetch recommended movies. Response code: " + response.code());
                    if (response.errorBody() != null) {
                        Log.e("API Response", "Error body: " + response.errorBody().toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch data: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to fetch recommended movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> extractGenresFromMovies(List<Movie> likedMovies) {
        Set<String> genresSet = new HashSet<>();

        for (Movie movie : likedMovies) {
            List<Integer> genreIds = movie.getGenre_ids();
            for (Integer genreId : genreIds) {
                String genreName = genreMap.get(genreId);
                if (genreName != null) {
                    genresSet.add(genreName);
                }
            }
        }

        return new ArrayList<>(genresSet);
    }

    private void fetchPopularMovies() {
        String apiToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzMjRjMmQ1YjZlODNiNjE3MTY2NjA5MGYyNzU1MzhmZSIsIm5iZiI6MTczMzA0MzQ5My45MjcsInN1YiI6IjY3NGMyNTI1MTYxNmI0YTg1OGM3YjM5MCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pj5vjPat7ZnyFO6JpS07vkdprGuqzkoHMPbhLb_mo1M";
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + apiToken)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDBApiService tmDbApi = retrofit.create(TMDBApiService.class);

        Call<MovieResponse> call = tmDbApi.getPopularMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Response successful: " + response.body().toString());
                    popularMovieList.clear();
                    popularMovieList.addAll(response.body().getResults());
                    popularMovieAdapter.notifyDataSetChanged();
                } else {
                    Log.d("API Response", "Response failed with code: " + response.code());
                    if (response.errorBody() != null) {
                        Log.d("API Response", "Error body: " + response.errorBody().toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch data: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to fetch movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTopRatedMovies() {
        String apiToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzMjRjMmQ1YjZlODNiNjE3MTY2NjA5MGYyNzU1MzhmZSIsIm5iZiI6MTczMzA0MzQ5My45MjcsInN1YiI6IjY3NGMyNTI1MTYxNmI0YTg1OGM3YjM5MCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pj5vjPat7ZnyFO6JpS07vkdprGuqzkoHMPbhLb_mo1M";

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + apiToken)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDBApiService tmDbApi = retrofit.create(TMDBApiService.class);

        Call<MovieResponse> call = tmDbApi.getTopRatedMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Top-rated movies fetched successfully.");
                    topRatedMovieList.clear();
                    topRatedMovieList.addAll(response.body().getResults());
                    topRatedMovieAdapter.notifyDataSetChanged();
                } else {
                    Log.d("API Response", "Failed to fetch top-rated movies. Code: " + response.code());
                    if (response.errorBody() != null) {
                        Log.d("API Response", "Error body: " + response.errorBody().toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch top-rated movies: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to fetch top-rated movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void discoverMovies() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDBApiService api = retrofit.create(TMDBApiService.class);

        String apiToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzMjRjMmQ1YjZlODNiNjE3MTY2NjA5MGYyNzU1MzhmZSIsIm5iZiI6MTczMzA0MzQ5My45MjcsInN1YiI6IjY3NGMyNTI1MTYxNmI0YTg1OGM3YjM5MCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.pj5vjPat7ZnyFO6JpS07vkdprGuqzkoHMPbhLb_mo1M";

        Call<MovieResponse> call = api.discoverMovies(apiToken, "en-US", "vote_count.desc", 1);

        Log.d("HomeFragment", "API Call: " + call.request().url());

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    Log.d("HomeFragment", "Movies fetched: " + movies.size());
                    discoverMovieAdapter.updateMovies(movies);
                } else {
                    Log.e("HomeFragment", "Response not successful. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error fetching movies", t);
            }
        });
    }

    private void openDetailActivity(Movie movie) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

}