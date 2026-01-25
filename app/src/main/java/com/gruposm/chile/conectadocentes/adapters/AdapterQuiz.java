package com.gruposm.chile.conectadocentes.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.android.material.chip.Chip;
import com.gruposm.chile.conectadocentes.object.Inbox;

import java.util.ArrayList;
import java.util.List;

import com.gruposm.chile.conectadocentes.R;

public class AdapterQuiz extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_FREE = 1;
    private final int VIEW_NO_FREE = 0;
    private Context ctx;
    private List<Inbox> items;
    private OnClickListener onClickListener = null;

    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public class ResultHolder extends RecyclerView.ViewHolder {

        public TextView from, email, message, date, time, image_letter, txtNivel, txtLetra;
        public ImageView image;
        public RelativeLayout lyt_checked, lyt_image, labelLetra;
        public View lyt_parent;

        public ResultHolder(View view) {
            super(view);
            from = (TextView) view.findViewById(R.id.from);
            email = (TextView) view.findViewById(R.id.email);
            message = (TextView) view.findViewById(R.id.message);
            lyt_checked = (RelativeLayout) view.findViewById(R.id.lyt_checked);
            lyt_parent = (View) view.findViewById(R.id.lyt_parent);
        }
    }
    public static class UnlockViewHolder extends RecyclerView.ViewHolder {
        public TextView from, email, message, date, time, image_letter, txtNivel, txtLetra;
        public Chip chip;
        public ImageView image;
        public RelativeLayout lyt_checked, lyt_image, labelLetra;
        public View lyt_parent;
        public UnlockViewHolder(View view) {
            super(view);
            from = (TextView) view.findViewById(R.id.from);
            email = (TextView) view.findViewById(R.id.email);
            message = (TextView) view.findViewById(R.id.message);
            lyt_checked = (RelativeLayout) view.findViewById(R.id.lyt_checked);
            lyt_parent = (View) view.findViewById(R.id.lyt_parent);
            chip = (Chip) view.findViewById(R.id.btn_unlock);
        }
    }

    public AdapterQuiz(Context mContext, List<Inbox> items) {
        this.ctx = mContext;
        this.items = items;
        selected_items = new SparseBooleanArray();
    }

    public void setItems(List<Inbox> items) {
        this.items = items;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_FREE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz, parent, false);
            vh = new AdapterQuiz.ResultHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_unlock, parent, false);
            vh = new AdapterQuiz.UnlockViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Inbox inbox = items.get(position);
        if (holder instanceof AdapterQuiz.ResultHolder)
        {
            AdapterQuiz.ResultHolder view = (AdapterQuiz.ResultHolder) holder;
            view.from.setText(inbox.from);
            view.email.setText(inbox.email);
            view.lyt_parent.setActivated(selected_items.get(position, false));
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener == null) return;
                    onClickListener.onItemClick(v, inbox, position);
                }
            });

            view.lyt_parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
        }
        else
        {

            AdapterQuiz.UnlockViewHolder view = (AdapterQuiz.UnlockViewHolder) holder;
            view.from.setText(inbox.from);
            view.email.setText(inbox.email);
            view.lyt_parent.setActivated(selected_items.get(position, false));
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener == null) return;
                    onClickListener.onItemClick(v, inbox, position);
                }
            });
            view.chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener == null) return;
                    onClickListener.onItemChipClick(v, inbox, position);
                }
            });
        }
    }



    public Inbox getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    @Override
    public int getItemViewType(int position) {
        return this.items.get(position).quiz.getLiberado() == 1 ? VIEW_FREE : VIEW_NO_FREE;
    }

    public void toggleSelection(int pos) {
        current_selected_idx = pos;
        if (selected_items.get(pos, false)) {
            selected_items.delete(pos);
        } else {
            selected_items.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selected_items.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selected_items.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selected_items.size());
        for (int i = 0; i < selected_items.size(); i++) {
            items.add(selected_items.keyAt(i));
        }
        return items;
    }

    public void removeData(int position) {
        items.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        current_selected_idx = -1;
    }

    public interface OnClickListener {
        void onItemClick(View view, Inbox obj, int pos);

        void onItemLongClick(View view, Inbox obj, int pos);

        void onItemChipClick(View view, Inbox obj, int pos);
    }
}