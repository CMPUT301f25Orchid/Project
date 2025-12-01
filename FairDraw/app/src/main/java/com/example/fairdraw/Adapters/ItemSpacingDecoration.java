package com.example.fairdraw.Adapters;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Simple ItemDecoration that adds vertical spacing between items.
 */
public class ItemSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int verticalSpacePx;

    public ItemSpacingDecoration(int verticalSpacePx) {
        this.verticalSpacePx = verticalSpacePx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        // Add top spacing except for the first item
        if (position > 0) {
            outRect.top = verticalSpacePx;
        }
    }
}

