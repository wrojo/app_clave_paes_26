package com.gruposm.chile.conectadocentes.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.gruposm.chile.conectadocentes.R;
import com.gruposm.chile.conectadocentes.object.RowResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterResult extends RecyclerView.Adapter<AdapterResult.ViewHolder> {

    private Context ctx;
    private List<RowResult> items;
    private OnClickListener onClickListener = null;

    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;
    private int options;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView num, letter1, letter2, letter3, letter4, letter5, textOpen;
        public View lyt_parent;


        public ViewHolder(View view) {
            super(view);
            num = (TextView) view.findViewById(R.id.num);
            letter1 = (TextView) view.findViewById(R.id.letter1);
            letter2 = (TextView) view.findViewById(R.id.letter2);
            letter3 = (TextView) view.findViewById(R.id.letter3);
            letter4 = (TextView) view.findViewById(R.id.letter4);
            letter5 = (TextView) view.findViewById(R.id.letter5);
            textOpen = (TextView) view.findViewById(R.id.textOpen);
            lyt_parent = (View) view.findViewById(R.id.lyt_parent);


        }
    }

    public AdapterResult(Context mContext, List<RowResult> items, int options) {
        this.ctx = mContext;
        this.items = items;
        this.options = options;
        selected_items = new SparseBooleanArray();
    }

    public void setItems(List<RowResult> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_options, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final RowResult row = items.get(position);
        String letter = row.letter;
        Log.d("TAG", "letterAnswer: "+ letter);
        Map<Integer, String> letters = new HashMap<Integer, String>();
        letters.put(1,"A");
        letters.put(2,"B");
        letters.put(3,"C");
        letters.put(4,"D");
        letters.put(5,"E");
        String colorClean = "#F2F2F2";
        holder.letter1.getBackground().setTint(Color.parseColor(colorClean));
        holder.letter2.getBackground().setTint(Color.parseColor(colorClean));
        holder.letter3.getBackground().setTint(Color.parseColor(colorClean));
        holder.letter4.getBackground().setTint(Color.parseColor(colorClean));
        holder.letter5.getBackground().setTint(Color.parseColor(colorClean));
        holder.letter1.setVisibility(View.VISIBLE);
        holder.letter2.setVisibility(View.VISIBLE);
        holder.letter3.setVisibility(View.VISIBLE);
        holder.letter4.setVisibility(View.VISIBLE);
        holder.letter5.setVisibility(View.VISIBLE);
        holder.textOpen.setVisibility(View.GONE);
        holder.textOpen.setTranslationX(0);
        if(this.options == 4)
        {
            holder.letter5.setVisibility(View.INVISIBLE);
        }
        //holder.letter5.setVisibility(View.GONE);
        if (row.isOpen) {
            holder.num.setText(String.valueOf(row.num));
            holder.letter1.setVisibility(View.INVISIBLE);
            holder.letter2.setVisibility(View.INVISIBLE);
            holder.letter3.setVisibility(View.INVISIBLE);
            holder.letter4.setVisibility(View.INVISIBLE);
            holder.letter5.setVisibility(View.INVISIBLE);
            holder.textOpen.setVisibility(View.VISIBLE);
            holder.textOpen.post(new Runnable() {
                @Override
                public void run() {
                    int delta = holder.letter1.getLeft() - holder.textOpen.getLeft();
                    int offset = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            12,
                            holder.itemView.getResources().getDisplayMetrics()
                    );
                    holder.textOpen.setTranslationX(delta + offset);
                }
            });
        } else if(letter != null && !letter.isEmpty())
        {
            for(int i = 1; i <= letters.size(); i++)
            {
                if(letters.get(i).equals(letter))
                {
                    boolean isCorrect = row.isCorrect;
                    String color = "#ea5750";
                    if(isCorrect)
                    {
                        color = "#56bc72";
                    }
                    if(i == 1)
                    {
                        holder.letter1.getBackground().setTint(Color.parseColor(color));
                    }
                    if(i == 2)
                    {
                        holder.letter2.getBackground().setTint(Color.parseColor(color));
                    }
                    if(i == 3)
                    {
                        holder.letter3.getBackground().setTint(Color.parseColor(color));
                    }
                    if(i == 4)
                    {
                        holder.letter4.getBackground().setTint(Color.parseColor(color));
                    }
                    if(i == 5)
                    {
                        holder.letter5.getBackground().setTint(Color.parseColor(color));
                    }
                    break;
                }
            }
        }
        // displaying text view data
        if (!row.isOpen) {
            holder.num.setText(String.valueOf(row.num));
        }
        holder.lyt_parent.setActivated(selected_items.get(position, false));

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener == null) return;
                onClickListener.onItemClick(v, row, position);
            }
        });

        holder.lyt_parent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });


    }

    public RowResult getItem(int position) {
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
        void onItemClick(View view, RowResult obj, int pos);
    }
}
