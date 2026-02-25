package cl.gruposm.conectaevaluaciones.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

import cl.gruposm.conectaevaluaciones.R;
import cl.gruposm.conectaevaluaciones.object.Inbox;

public class AdapterListInbox extends RecyclerView.Adapter<AdapterListInbox.ViewHolder> {

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

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView from, email, message, date, time, image_letter, letter;
        public ImageView image;
        public RelativeLayout lyt_checked, lyt_image, labelLetra;
        public View lyt_parent;


        public ViewHolder(View view) {
            super(view);
            shapes[0] = R.drawable.shape_rounded_orange;
            shapes[1] = R.drawable.shape_rounded_purple;
            shapes[2] = R.drawable.shape_rounded_red;
            colours[0] = "#5cc8e1";
            colours[1] = "#5dc687";
            colours[2] = "#5d60e2";
            from = (TextView) view.findViewById(R.id.from);
            email = (TextView) view.findViewById(R.id.email);
            message = (TextView) view.findViewById(R.id.message);
            lyt_checked = (RelativeLayout) view.findViewById(R.id.lyt_checked);
            letter = (TextView) view.findViewById(R.id.letter);
            lyt_parent = (View) view.findViewById(R.id.lyt_parent);


        }
    }

    public AdapterListInbox(Context mContext, List<Inbox> items) {
        this.ctx = mContext;
        this.items = items;
        selected_items = new SparseBooleanArray();
    }

    public void setItems(List<Inbox> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inbox, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Inbox inbox = items.get(position);

        int indexShape = position%shapes.length;

        Log.d("TAG", "colours[indexShape]:" + colours[indexShape]);

        // displaying text view data
        holder.from.setText(inbox.from);
        holder.email.setText(inbox.email);
        holder.letter.setText(inbox.letter);
        holder.letter.setTextColor(Color.parseColor(colours[indexShape]));
        holder.letter.setBackgroundResource(shapes[indexShape]);
        holder.lyt_parent.setActivated(selected_items.get(position, false));

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener == null) return;
                onClickListener.onItemClick(v, inbox, position);
            }
        });

        holder.lyt_parent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*if (onClickListener == null) return false;
                onClickListener.onItemLongClick(v, inbox, position);
                return true;*/
                return true;
            }
        });

        toggleCheckedIcon(holder, position);
        displayImage(holder, inbox);

    }

    private void displayImage(ViewHolder holder, Inbox inbox) {

    }

    private void toggleCheckedIcon(ViewHolder holder, int position) {
        if (selected_items.get(position, false)) {
            holder.lyt_checked.setVisibility(View.VISIBLE);
            if (current_selected_idx == position) resetCurrentIndex();
        } else {
            holder.lyt_checked.setVisibility(View.GONE);
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