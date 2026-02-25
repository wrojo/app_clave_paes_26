package cl.gruposm.conectaevaluaciones.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cl.gruposm.conectaevaluaciones.R;
import cl.gruposm.conectaevaluaciones.object.Inbox;
import cl.gruposm.conectaevaluaciones.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class AdapterStudent extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_RESULT = 1;
    private final int VIEW_NO_RESULT = 0;
    private Context ctx;
    private List<Inbox> items;
    private OnClickListener onClickListener = null;

    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;
    private int[] shapes = new int[3];
    private String[] colours = new String[3];

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView from, email, message, date, time, image_letter, txtBuenas,txtMalas,txtOmitidas,txtPorcentaje;
        public ImageView image;
        public RelativeLayout lyt_checked, lyt_image;
        public View lyt_parent;

        public OriginalViewHolder(View view) {
            super(view);
            from = (TextView) view.findViewById(R.id.from);
            image_letter = (TextView) view.findViewById(R.id.image_letter);
            image = (ImageView) view.findViewById(R.id.image);
            txtBuenas = (TextView) view.findViewById(R.id.txtBuenas);
            txtMalas = (TextView) view.findViewById(R.id.txtMalas);
            txtOmitidas = (TextView) view.findViewById(R.id.txtOmitidas);
            txtPorcentaje = (TextView) view.findViewById(R.id.txtPorcentaje);
            lyt_checked = (RelativeLayout) view.findViewById(R.id.lyt_checked);
            lyt_image = (RelativeLayout) view.findViewById(R.id.lyt_image);
            lyt_parent = (View) view.findViewById(R.id.lyt_parent);
        }
    }
    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        public TextView from, txtInfo, image_letter;
        public ImageView image;
        public View lyt_parent;
        public SectionViewHolder(View view) {
            super(view);
            image_letter = (TextView) view.findViewById(R.id.image_letter);
            image = (ImageView) view.findViewById(R.id.image);
            from = (TextView) view.findViewById(R.id.from);
            txtInfo = (TextView) view.findViewById(R.id.txtInfo);
            lyt_parent = (View) view.findViewById(R.id.lyt_parent);
        }
    }

    public AdapterStudent(Context mContext, List<Inbox> items) {
        this.ctx = mContext;
        this.items = items;
        selected_items = new SparseBooleanArray();
        shapes[0] = R.drawable.shape_rounded_orange;
        shapes[1] = R.drawable.shape_rounded_purple;
        shapes[2] = R.drawable.shape_rounded_red;
        colours[0] = "#5cc8e1";
        colours[1] = "#5dc687";
        colours[2] = "#5d60e2";
    }

    public void setItems(List<Inbox> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_RESULT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_no_result, parent, false);
            vh = new SectionViewHolder(v);
        }
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final Inbox inbox = items.get(position);
        int indexShape = position%shapes.length;
        // displaying text view data
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            view.from.setText(inbox.from);
            view.txtBuenas.setText(String.valueOf(inbox.buenas));
            view.txtMalas.setText(String.valueOf(inbox.malas));
            view.txtOmitidas.setText(String.valueOf(inbox.omitidas));
            view.txtPorcentaje.setText(String.valueOf(inbox.porcentaje));
            view.image_letter.setText(inbox.from.substring(0, 1));
            view.image_letter.setTextColor(Color.parseColor(colours[indexShape]));
            view.image_letter.setBackgroundResource(shapes[indexShape]);
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
                    //if (onClickListener == null) return false;
                    // onClickListener.onItemLongClick(v, inbox, position);
                    return true;
                }
            });
            toggleCheckedIcon(view, position);
            //displayImage(view, inbox);
        }
        else
        {

            SectionViewHolder view = (SectionViewHolder) holder;
            view.image_letter.setText(inbox.from.substring(0, 1));
            view.image_letter.setTextColor(Color.parseColor(colours[indexShape]));
            view.image_letter.setBackgroundResource(shapes[indexShape]);
            view.from.setText(inbox.from);
            view.lyt_parent.setActivated(selected_items.get(position, false));
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener == null) return;
                    onClickListener.onItemClick(v, inbox, position);
                }
            });
            toggleCheckedIcon(view, position);
            //displayImage(view, inbox);
        }
    }

    private void displayImage(@NonNull RecyclerView.ViewHolder  holder, Inbox inbox) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            if (inbox.image != null) {
                Tools.displayImageOriginal(ctx, view.image, inbox.image);
                view.image.setColorFilter(null);
                view.image_letter.setVisibility(View.GONE);
            } else {
                view.image.setImageResource(R.drawable.shape_circle);
                view.image.setColorFilter(inbox.color);
                view.image_letter.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            SectionViewHolder view = (SectionViewHolder) holder;
            if (inbox.image != null) {
                Tools.displayImageOriginal(ctx, view.image, inbox.image);
                view.image.setColorFilter(null);
                view.image_letter.setVisibility(View.GONE);
            } else {
                view.image.setImageResource(R.drawable.shape_circle);
                view.image.setColorFilter(inbox.color);
                view.image_letter.setVisibility(View.VISIBLE);
            }
        }
    }

    private void toggleCheckedIcon(@NonNull RecyclerView.ViewHolder  holder, int position) {
        if (selected_items.get(position, false)) {
            if (current_selected_idx == position) resetCurrentIndex();
        } else {
            if (current_selected_idx == position) resetCurrentIndex();
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
        return this.items.get(position).isHaveResult ? VIEW_RESULT : VIEW_NO_RESULT;
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
    }
}