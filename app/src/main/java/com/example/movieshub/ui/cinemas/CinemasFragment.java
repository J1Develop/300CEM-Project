package com.example.movieshub.ui.cinemas;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.movieshub.R;
import com.example.movieshub.activity.CircuitsDetailsActivity;
import com.example.movieshub.model.Circuit;
import com.example.movieshub.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CinemasFragment extends Fragment {

    private OkHttpClient okHttpClient;

    private ArrayAdapter<String> arrayAdapter;

    private List<Circuit> circuitList;

    private ProgressBar progressBar;
    private Spinner regionSpinner;
    private Spinner circuitSpinner;
    private ListView listViewCinemas;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cinemas, container, false);

        progressBar = root.findViewById(R.id.progressBar);
        regionSpinner = root.findViewById(R.id.spinner_regions);
        circuitSpinner = root.findViewById(R.id.spinner_circuits);
        listViewCinemas = root.findViewById(R.id.list_view_cinemas);

        listViewCinemas.setOnItemClickListener((parent, view, position, id) -> {
            Circuit item = circuitList.get(position);

            Bundle bundle = new Bundle();
            bundle.putString("cinema", item.getCinema());
            bundle.putString("name", item.getName());
            bundle.putString("region", item.getRegion());
            bundle.putString("address", item.getAddress());
            bundle.putString("tel", item.getTel());

            Intent intent = new Intent(getActivity(), CircuitsDetailsActivity.class);
            intent.putExtra("bundle", bundle);

            startActivity(intent);
        });

        ArrayAdapter<CharSequence> regions = ArrayAdapter.createFromResource(getContext(), R.array.regions, android.R.layout.simple_spinner_item);
        regions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regions);

        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                progressBar.setVisibility(View.VISIBLE);
                listViewCinemas.setVisibility(View.GONE);

                requestCinemas(circuitSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ArrayAdapter<CharSequence> circuits = ArrayAdapter.createFromResource(getContext(), R.array.circuits, android.R.layout.simple_spinner_item);
        circuits.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        circuitSpinner.setAdapter(circuits);

        circuitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                progressBar.setVisibility(View.VISIBLE);
                listViewCinemas.setVisibility(View.GONE);

                requestCinemas(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return root;
    }

    private void requestCinemas(final String cinema) {
        okHttpClient = new OkHttpClient();

        HttpUrl.Builder builder = HttpUrl.parse(Constant.API_URL_READ_CINEMA).newBuilder();
        builder.addQueryParameter("cinema", cinema);

        Request request = new Request.Builder().url(builder.build().toString()).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    listViewCinemas.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                });
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resString = response.body().string();
                if (response.code() == 500) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        listViewCinemas.setVisibility(View.VISIBLE);

                        Toast.makeText(getActivity(), getString(R.string.toast_json_err), Toast.LENGTH_LONG).show();
                    });
                } else if (response.code() == 201){
                    try {
                        JSONObject jsonObject = new JSONObject(resString);

                        if ("true".equals(jsonObject.getString("result"))) {
                            JSONArray circuits = jsonObject.getJSONArray("circuits");

                            circuitList = new ArrayList();

                            int lenCircuits = circuits.length();

                            for (int i = 0; i < lenCircuits; i++) {
                                JSONObject currentCircuits = (JSONObject) circuits.get(i);

                                String name = currentCircuits.getString("name");
                                String region = currentCircuits.getString("region");
                                String address = currentCircuits.getString("address");
                                String tel = currentCircuits.getString("tel");

                                circuitList.add(new Circuit(cinema, name, region, address, tel));
                            }

                            getActivity().runOnUiThread(() -> {
                                Predicate<Circuit> byRegion
                                        = circuit -> circuit.getRegion().equals(regionSpinner.getSelectedItem().toString());

                                circuitList = circuitList.stream().filter(byRegion).collect(Collectors.toList());

                                arrayAdapter = new ArrayAdapter(getActivity(), R.layout.list_item_text_color_accent, circuitList.stream().map(Circuit::getName).collect(Collectors.toList()));
                                listViewCinemas.setAdapter(arrayAdapter);
                                arrayAdapter.notifyDataSetChanged();

                                progressBar.setVisibility(View.GONE);
                                listViewCinemas.setVisibility(View.VISIBLE);
                            });
                        } else {
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                listViewCinemas.setVisibility(View.VISIBLE);

                                Toast.makeText(getActivity(), getString(R.string.toast_json_err), Toast.LENGTH_LONG).show();
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            listViewCinemas.setVisibility(View.VISIBLE);

                            Toast.makeText(getActivity(), getString(R.string.toast_json_err), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }
        });
    }
}
