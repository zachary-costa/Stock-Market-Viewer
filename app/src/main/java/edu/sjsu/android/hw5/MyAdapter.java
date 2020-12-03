package edu.sjsu.android.hw5;

import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<String>values;
    private List<String>headers;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public TextView txtHeader;
        public TextView txtFooter;
        public View layout;
        public ViewHolder(View v){
            super(v);
            layout=v;
            txtHeader=(TextView)v.findViewById(R.id.firstLine);
            txtFooter=(TextView)v.findViewById(R.id.secondLine);
        }
    }
    public void add(int position,String item) {
        values.add(position,item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<String> myDataset, List<String> myHeaders) {
        values = myDataset;
        headers = myHeaders;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.current_stock_detail_row, parent, false);
        //set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // -get element from your dataset at this position
        // -replace the contents of the view with that element
        final String name = values.get(position);
        String header = headers.get(position);
        header = header.substring(0,1).toUpperCase() + header.substring(1);
        holder.txtHeader.setText(header);
        holder.txtFooter.setText(name);
    }
    //Return the size of your dataset (invoker by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}