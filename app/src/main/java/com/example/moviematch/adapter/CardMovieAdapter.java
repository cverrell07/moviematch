package com.example.moviematch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviematch.R;
import com.example.moviematch.models.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardMovieAdapter extends RecyclerView.Adapter<CardMovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private final Context context;
    private final Map<Integer, String> genreMap;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
    }

    public CardMovieAdapter(Context context, List<Movie> movieList, Map<Integer, String> genreMap, OnItemClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.genreMap = genreMap;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        if (movie == null) return;

        holder.textTitle.setText(movie.getTitle() != null ? movie.getTitle() : "No Title Available");

        StringBuilder genreNames = new StringBuilder();
        if (movie.getGenre_ids() != null) {
            for (int genreId : movie.getGenre_ids()) {
                String genreName = genreMap.get(genreId);
                if (genreName != null) {
                    genreNames.append(genreName).append(", ");
                }
            }
        }
        if (genreNames.length() > 0) {
            genreNames.setLength(genreNames.length() - 2);
        } else {
            genreNames.append("No Genre Available");
        }
        holder.textGenre.setText(genreNames.toString());

        holder.textRating.setText(String.format("%.1f/10", movie.getVote_average()));

        holder.textOverview.setText(movie.getOverview());

        Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500" + movie.getPoster_path())
                .into(holder.imagePoster);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textGenre, textRating, textOverview;
        ImageView imagePoster;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.cardTitleTV);
            textGenre = itemView.findViewById(R.id.cardGenreTV);
            textRating = itemView.findViewById(R.id.cardRatingTV);
            textOverview = itemView.findViewById(R.id.cardOverviewTV);
            imagePoster = itemView.findViewById(R.id.cardPosterIV);
        }
    }

    public void updateMovies(List<Movie> updatedMovies) {
        if (updatedMovies == null) {
            this.movieList = new ArrayList<>();
        }
        this.movieList.clear();
        this.movieList.addAll(updatedMovies);
        notifyDataSetChanged();
    }

}