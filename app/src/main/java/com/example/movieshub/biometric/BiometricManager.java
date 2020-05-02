package com.example.movieshub.biometric;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;

public class BiometricManager {

    protected CancellationSignal cancellationSignal = new CancellationSignal();
    private String title, subtitle, description, negativeButtonText;
    private Context context;

    public BiometricManager(BiometricBuilder biometricBuilder) {
        title = biometricBuilder.getTitle();
        subtitle = biometricBuilder.getSubtitle();
        description = biometricBuilder.getDescription();
        negativeButtonText = biometricBuilder.getNegativeButtonText();
        context = biometricBuilder.getContext();
    }

    @TargetApi(Build.VERSION_CODES.P)
    private void displayBiometricPrompt(final BiometricCallback biometricCallback) {
        new BiometricPrompt.Builder(context)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButton(negativeButtonText, context.getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        biometricCallback.onAuthenticationCancelled();
                    }
                }).build().authenticate(cancellationSignal, context.getMainExecutor(), new BiometricCallbackV28(biometricCallback));
    }

    private void displayBiometricDialog(BiometricCallback biometricCallback) {
        displayBiometricPrompt(biometricCallback);
    }

    public void cancelAuthentication() {
        if (BiometricUtil.isBiometricPromptEnable()) {
            if (!cancellationSignal.isCanceled())
                cancellationSignal.cancel();
        }
    }

    public void authenticate(@NonNull final BiometricCallback biometricCallback) {
        if (title == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog title cannot be null");
            return;
        }

        if (subtitle == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog subtitle cannot be null");
            return;
        }

        if (description == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog description cannot be null");
            return;
        }

        if (negativeButtonText == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog negativeButtonText cannot be null");
            return;
        }

        if (!BiometricUtil.isSdkVersionSupported()) {
            biometricCallback.onSdkVersionNotSupported();
            return;
        }

        if (!BiometricUtil.isPermissionGranted(context)) {
            biometricCallback.onBiometricAuthenticationPermissionNotGranted();
            return;
        }

        if (!BiometricUtil.isHardwareSupported(context)) {
            biometricCallback.onBiometricAuthenticationNotSupported();
        }

        if (!BiometricUtil.isFingerprintAvailable(context)) {
            biometricCallback.onBiometricAuthenticationNotAvailable();
        }
        displayBiometricDialog(biometricCallback);
    }


}
