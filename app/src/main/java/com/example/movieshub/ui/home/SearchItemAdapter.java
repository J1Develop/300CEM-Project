package com.example.movieshub.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.movieshub.R;
import com.example.movieshub.model.SearchItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchItemAdapter extends ArrayAdapter<SearchItem> {

    private Context context;
    private int resource;
    private ArrayList<SearchItem> searchItems;
    private String imgPlaceHolderUrl = "https://via.placeholder.com/300x450.png?text=Unavailable";

    public SearchItemAdapter(Context c, int res, ArrayList<SearchItem> items) {
        super(c, res, items);
        context = c;
        resource = res;
        searchItems = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        try {
            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(resource, parent, false);
            }

            ImageView poster = view.findViewById(R.id.image_view_poster);
            TextView title = view.findViewById(R.id.text_title);
            TextView year = view.findViewById(R.id.text_year);

            boolean isImageUrlAvailable = !"N/A".equals(searchItems.get(position).getPoster());
            Picasso.get()
                    .load(isImageUrlAvailable? searchItems.get(position).getPoster() : imgPlaceHolderUrl)
                    .into(poster);
            title.setText(searchItems.get(position).getTitle());
            year.setText(searchItems.get(position).getYear());
        } catch (Exception ex) {
            ex.printStackTrace();
            ex.getCause();
        }
        return view;
    }
}
