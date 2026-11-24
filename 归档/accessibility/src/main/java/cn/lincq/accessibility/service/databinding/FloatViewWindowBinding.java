package cn.lincq.accessibility.service.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.viewbinding.ViewBinding;

import java.util.Objects;

import cn.lincq.accessibility.R;

public final class FloatViewWindowBinding implements ViewBinding {
    private final LinearLayout rootView;

    private FloatViewWindowBinding(LinearLayout linearLayout) {
        this.rootView = linearLayout;
    }

    public LinearLayout getRoot() {
        return this.rootView;
    }

    public static FloatViewWindowBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FloatViewWindowBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.float_view_window, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FloatViewWindowBinding bind(View view) {
        Objects.requireNonNull(view, "rootView");
        return new FloatViewWindowBinding((LinearLayout) view);
    }
}
