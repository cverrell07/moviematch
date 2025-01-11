package com.example.moviematch.interfaces;

import com.example.moviematch.models.GenreResponse;
import com.example.moviematch.models.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface TMDBApiService {
    @GET("movie/popular?language=en-US&page=1")
    Call<MovieResponse> getPopularMovies();

    @GET("genre/movie/list?language=en")
    Call<GenreResponse> getGenres();

    @GET("movie/top_rated?language=en-US&page=1")
    Call<MovieResponse> getTopRatedMovies();

    @GET("discover/movie?language=en-US&page=1")
    Call<MovieResponse> getMoviesByGenre(@Query("with_genres") String genreQuery);

    @GET("discover/movie")
    Call<MovieResponse> discoverMovies(
            @Header("Authorization") String authHeader,
            @Query("language") String language,
            @Query("sort_by") String sortBy,
            @Query("page") int page
    );

    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Header("Authorization") String authHeader,
            @Query("query") String query,
            @Query("language") String language,
            @Query("page") int page
    );
}
