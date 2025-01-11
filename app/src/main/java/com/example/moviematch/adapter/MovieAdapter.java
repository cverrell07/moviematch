package com.example.moviematch.adapter;

import android.annotation.SuppressLint;
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

import java.util.List;
import java.util.Map;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private Context context;
    private Map<Integer, String> genreMap;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
    }

    public MovieAdapter(Context context, List<Movie> movieList, Map<Integer, String> genreMap, OnItemClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.genreMap = genreMap;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.textTitle.setText(movie.getTitle());

        StringBuilder genreNames = new StringBuilder();
        boolean firstGenre = true;
        for (int genreId : movie.getGenre_ids()) {
            String genreName = genreMap.get(genreId);
            if (genreName != null && firstGenre) {
                genreNames.append(genreName);
                firstGenre = false;
                break;
            }
        }
        holder.textGenre.setText(genreNames.toString());

        holder.textRating.setText(String.format("%.1f", movie.getVote_average()) + " /10");

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
        TextView textTitle, textGenre, textRating;
        ImageView imagePoster;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.cardTitleTV);
            textGenre = itemView.findViewById(R.id.cardGenreTV);
            textRating = itemView.findViewById(R.id.cardRatingTV);
            imagePoster = itemView.findViewById(R.id.cardPosterIV);
        }
    }

    public void updateMovies(List<Movie> updatedMovies) {
        this.movieList.clear();
        this.movieList.addAll(updatedMovies);
        notifyDataSetChanged();
    }
}

