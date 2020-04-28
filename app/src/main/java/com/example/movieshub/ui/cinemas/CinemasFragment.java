package com.example.movieshub.ui.cinemas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.movieshub.R;

public class CinemasFragment extends Fragment {

    private CinemasViewModel cinemasViewModel;

    private String[] data_dummy = {
            "Emperor Cinemas（Entertainment Building)",
            "Emperor Cinemas（Tuen Mun)",
            "Emperor Cinemas（Ma On Shan)"
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        cinemasViewModel = ViewModelProviders.of(this).get(CinemasViewModel.class);
        cinemasViewModel = new ViewModelProvider(this).get(CinemasViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cinemas, container, false);

        final Spinner regionSpinner = root.findViewById(R.id.spinner_regions);
        final Spinner circuitSpinner = root.findViewById(R.id.spinner_circuits);
        final ListView cinemas = root.findViewById(R.id.list_view_cinemas);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, data_dummy);
        cinemas.setAdapter(arrayAdapter);

        ArrayAdapter<CharSequence> regions = ArrayAdapter.createFromResource(getContext(), R.array.regions, android.R.layout.simple_spinner_item);
        regions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regions);

        ArrayAdapter<CharSequence> circuits = ArrayAdapter.createFromResource(getContext(), R.array.circuits, android.R.layout.simple_spinner_item);
        circuits.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        circuitSpinner.setAdapter(circuits);

        cinemasViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        return root;
    }
}
