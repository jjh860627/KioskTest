package com.jjh.test.kiosktest;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by jjh860627 on 2017. 7. 4..   
 */

public class LoadingDialog extends android.support.v4.app.DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = inflater.inflate(R.layout.fragment_loading_view, null);
        final View loadingView = view.findViewById(R.id.loading_image_view);

        Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.loading);
        loadingView.startAnimation(a);

        return view;
    }
}
