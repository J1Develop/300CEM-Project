package com.example.movieshub.biometric;

import android.content.Context;

public class BiometricBuilder {
    private String title, subtitle, description, negativeButtonText;

    private Context context;

    public BiometricBuilder(Context context) {
        this.context = context;
    }

    public BiometricBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public BiometricBuilder setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public BiometricBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public BiometricBuilder setNegativeButtonText(String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public String getNegativeButtonText() {
        return negativeButtonText;
    }

    public Context getContext() {
        return context;
    }

    public BiometricManager build() {
        return new BiometricManager(this);
    }
}