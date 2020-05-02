package com.example.movieshub.ui.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.movieshub.R;
import com.example.movieshub.activity.LoginActivity;

public class SettingFragment extends Fragment {

    private ListView list_view_setting;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting, container, false);

        list_view_setting = root.findViewById(R.id.list_view_setting);

        ArrayAdapter adapter = new ArrayAdapter(getContext(), R.layout.list_item_text_color_accent, getResources().getStringArray(R.array.setting));
        list_view_setting.setAdapter(adapter);

        list_view_setting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        showLangSpinner();
                        break;
                    case 1:
                        showLogoutDialog();
                        break;
                }
            }
        });

        return root;
    }

    private void showLangSpinner() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select App Language");
        builder.setItems(R.array.app_lang, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0:
                        // Set lang to English
                        break;
                    case 1:
                        // Set lan to Chinese
                        break;
                }
            }
        });

        builder.show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Comfirm to Logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.user_pref), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(getString(R.string.logged_in)).commit();

                        Toast.makeText(getContext(), "Logged out", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);

                        getActivity().finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.show();
    }
}
