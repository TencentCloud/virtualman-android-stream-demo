package com.tencent.virtualman_demo_app.view;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space, lineSize;

    public SpacesItemDecoration(int space, int lineSize) {
        this.space = space;
        this.lineSize = lineSize;
    }

    @Override
    public void getItemOffsets(@NotNull Rect outRect, @NotNull View view, RecyclerView parent, @NotNull RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.top = space;
        outRect.bottom = space;
    }
}