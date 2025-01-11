package com.example.moviematch;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.moviematch.interfaces.TMDBApiService;
import com.example.moviematch.models.GenreResponse;
import com.example.moviematch.models.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieDetailActivity extends AppCompatActivity {
    private TextView titleTV, ratingTV, yearTV, genreTV, overviewTV;
    private ImageView posterIV;
    private ImageButton backBtn;
    private Button loveItButton, willWatchButton;
    private Map<Integer, String> genreMap = new HashMap<>();
    private FirebaseFirestore db;
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        titleTV = findViewById(R.id.detailTitleTV);
        ratingTV = findViewById(R.id.detailRatingTV);
        yearTV = findViewById(R.id.detailYearTV);
        genreTV = findViewById(R.id.detailGenreTV);
        posterIV = findViewById(R.id.detailPosterIV);
        overviewTV = findViewById(R.id.detailOverviewTV);
        backBtn = findViewById(R.id.detailBackBtn);
        loveItButton = findViewById(R.id.loveItButton);
        willWatchButton = findViewById(R.id.willWatchBtn);

        movie = (Movie) getIntent().getSerializableExtra("movie");

        if (movie != null) {
            titleTV.setText(movie.getTitle());
            ratingTV.setText(String.valueOf(movie.getVote_average()) + "/10");
            yearTV.setText(movie.getRelease_date().split("-")[0]);
            fetchGenres(movie);
            overviewTV.setText(movie.getOverview());

            Glide.with(this)
                    .load("https://image.tmdb.org/t/p/w500" + movie.getPoster_path())
                    .into(posterIV);

        }

        db = FirebaseFirestore.getInstance();

        backBtn.setOnClickListener(v -> finish());

        loveItButton.setOnClickListener(v -> {
            if (movie != null) {
                addMovieToLiked(movie);
            }
        });

        willWatchButton.setOnClickListener(v -> {
            if (movie != null) {
                addOrRemoveMovieFromWatchList(movie);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (movie != null) {
            checkIfMovieLiked(movie);
            checkIfMovieInWatchList(movie);
        }
    }

    private void fetchGenres(Movie movie) {
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
                    Log.d("API Response", "All Genres fetched successfully.");

                    genreMap.clear();

                    for (GenreResponse.Genre genre : response.body().getGenres()) {
                        genreMap.put(genre.getId(), genre.getName());
                    }

                    genreTV.setText(extractGenresFromMovies(movie.getGenre_ids()));

                } else {
                    Log.d("API Response", "Failed to fetch genres. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GenreResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch genres: " + t.getMessage());
                Toast.makeText(getBaseContext(), "Failed to fetch genres", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractGenresFromMovies(List<Integer> genreIds) {
        List<String> genreNames = new ArrayList<>();

        for (int i = 0; i < genreIds.size() && i < 3; i++) {
            Integer id = genreIds.get(i);
            String genreName = genreMap.get(id);
            if (genreName != null) {
                genreNames.add(genreName);
            }
        }

        return genreNames.isEmpty() ? "No genres available" : String.join(", ", genreNames);
    }

    private void addMovieToLiked(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String movieId = String.valueOf(movie.getId());
        db.collection("users").document(userId)
                .collection("likedMovies")
                .document(movieId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        removeMovieFromLiked(movie);
                    } else {
                        Map<String, Object> likedMovie = new HashMap<>();
                        likedMovie.put("id", movie.getId());
                        likedMovie.put("title", movie.getTitle());
                        likedMovie.put("overview", movie.getOverview());
                        likedMovie.put("release_date", movie.getRelease_date());
                        likedMovie.put("genre_ids", movie.getGenre_ids());
                        likedMovie.put("poster_path", movie.getPoster_path());
                        likedMovie.put("vote_average", movie.getVote_average());

                        db.collection("users").document(userId)
                                .collection("likedMovies")
                                .document(movieId)
                                .set(likedMovie)
                                .addOnSuccessListener(documentReference -> {
                                    loveItButton.setBackgroundColor(ContextCompat.getColor(this, R.color.pink));
                                    loveItButton.setTextColor(ContextCompat.getColor(this, R.color.black));
                                    Toast.makeText(this, "Movie added to Liked Movies!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore Error", "Error adding movie to likedMovies: " + e.getMessage());
                                    Toast.makeText(this, "Failed to add movie to Liked Movies", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error checking if movie is liked: " + e.getMessage());
                });
    }

    private void removeMovieFromLiked(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String movieId = String.valueOf(movie.getId());
        db.collection("users").document(userId)
                .collection("likedMovies")
                .document(movieId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    loveItButton.setBackgroundColor(ContextCompat.getColor(this, R.color.oldGray));
                    loveItButton.setTextColor(ContextCompat.getColor(this, R.color.pink));
                    Toast.makeText(this, "Movie removed from Liked Movies.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error removing movie from likedMovies: " + e.getMessage());
                    Toast.makeText(this, "Failed to remove movie from Liked Movies", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfMovieLiked(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String movieId = String.valueOf(movie.getId());
        db.collection("users").document(userId)
                .collection("likedMovies")
                .document(movieId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        loveItButton.setBackgroundColor(ContextCompat.getColor(this, R.color.pink));
                        loveItButton.setTextColor(ContextCompat.getColor(this, R.color.black));
                    } else {
                        loveItButton.setBackgroundColor(ContextCompat.getColor(this, R.color.oldGray));
                        loveItButton.setTextColor(ContextCompat.getColor(this, R.color.pink));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error checking if movie is liked: " + e.getMessage());
                });
    }

    private void addOrRemoveMovieFromWatchList(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String movieId = String.valueOf(movie.getId());
        db.collection("users").document(userId)
                .collection("watchList")
                .document(movieId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        removeMovieFromWatchList(movie);
                    } else {
                        addMovieToWatchList(movie);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error checking if movie is in watchList: " + e.getMessage());
                });
    }

    private void addMovieToWatchList(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> watchListMovie = new HashMap<>();
        watchListMovie.put("id", movie.getId());
        watchListMovie.put("title", movie.getTitle());
        watchListMovie.put("overview", movie.getOverview());
        watchListMovie.put("release_date", movie.getRelease_date());
        watchListMovie.put("genre_ids", movie.getGenre_ids());
        watchListMovie.put("poster_path", movie.getPoster_path());
        watchListMovie.put("vote_average", movie.getVote_average());

        db.collection("users").document(userId)
                .collection("watchList")
                .document(String.valueOf(movie.getId()))
                .set(watchListMovie)
                .addOnSuccessListener(documentReference -> {
                    willWatchButton.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
                    willWatchButton.setTextColor(ContextCompat.getColor(this, R.color.black));
                    Toast.makeText(this, "Movie added to Watch List!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error adding movie to watchList: " + e.getMessage());
                    Toast.makeText(this, "Failed to add movie to Watch List", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeMovieFromWatchList(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .collection("watchList")
                .document(String.valueOf(movie.getId()))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    willWatchButton.setBackgroundColor(ContextCompat.getColor(this, R.color.oldGray));
                    willWatchButton.setTextColor(ContextCompat.getColor(this, R.color.yellow));
                    Toast.makeText(this, "Movie removed from Watch List.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error removing movie from watchList: " + e.getMessage());
                    Toast.makeText(this, "Failed to remove movie from Watch List", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfMovieInWatchList(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String movieId = String.valueOf(movie.getId());
        db.collection("users").document(userId)
                .collection("watchList")
                .document(movieId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        willWatchButton.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
                        willWatchButton.setTextColor(ContextCompat.getColor(this, R.color.black));
                    } else {
                        willWatchButton.setBackgroundColor(ContextCompat.getColor(this, R.color.oldGray));
                        willWatchButton.setTextColor(ContextCompat.getColor(this, R.color.yellow));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error checking if movie is in watchList: " + e.getMessage());
                });
    }

}