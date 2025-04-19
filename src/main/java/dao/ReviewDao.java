package dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import models.Review;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewDao {
    private static final String FILE_PATH = "reviews.json";
    private List<Review> reviews = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void loadFromFile() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type reviewListType = new TypeToken<ArrayList<Review>>(){}.getType();
            reviews = gson.fromJson(reader, reviewListType);
            if (reviews == null) {
                reviews = new ArrayList<>(); // Initialize if file is empty
            }
        } catch (IOException e) {
            e.printStackTrace();
            reviews = new ArrayList<>();
        }
    }

    public void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(reviews, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CRUD Operations
    public Review createReview(Review review) {
        if (getReview(review.reviewId) != null) {
            return null; // Review already exists
        }
        reviews.add(review);
        return review;
    }

    public Review getReview(String reviewId) {
        return reviews.stream()
                .filter(r -> r.reviewId.equals(reviewId))
                .findFirst()
                .orElse(null);
    }

    public List<Review> getAllReviews() {
        return new ArrayList<>(reviews);
    }

    public void deleteReview(String reviewId) {
        reviews.removeIf(r -> r.reviewId.equals(reviewId));
    }

    public Review updateReview(Review updatedReview) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).reviewId.equals(updatedReview.reviewId)) {
                reviews.set(i, updatedReview);
                return updatedReview;
            }
        }
        return null;
    }

    // Special queries
    public List<Review> getReviewsByGuest(String guestId) {
        return reviews.stream()
                .filter(r -> r.guestId.equals(guestId))
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByRating(int minRating, int maxRating) {
        return reviews.stream()
                .filter(r -> r.ratings >= minRating && r.ratings <= maxRating)
                .collect(Collectors.toList());
    }

    public double getAverageRating() {
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToInt(r -> r.ratings)
                .average()
                .orElse(0.0);
    }
}