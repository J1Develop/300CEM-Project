package com.example.movieshub.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.movieshub.R;
import com.example.movieshub.model.Review;

import java.util.List;

public class ReviewAdapter extends ArrayAdapter<Review> {

    private Context context;
    private int resource;
    private List<Review> reviews;

    public ReviewAdapter(@NonNull Context c, int res, @NonNull List<Review> reviews) {
        super(c, res, reviews);
        context = c;
        resource = res;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        try{
            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(resource, parent, false);
            }

            TextView ownership = view.findViewById(R.id.text_view_ownership);
            TextView content = view.findViewById(R.id.text_view_content);
            TextView createdDate = view.findViewById(R.id.text_view_created_date);

            ownership.setText(reviews.get(position).getOwnsership());
            content.setText(reviews.get(position).getContent());
            createdDate.setText(reviews.get(position).getCreateDate());

        } catch (Exception ex) {
            ex.printStackTrace();
            ex.getCause();
        }

        return view;
    }
}
