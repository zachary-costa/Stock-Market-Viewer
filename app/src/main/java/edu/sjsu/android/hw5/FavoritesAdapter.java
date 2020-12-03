package edu.sjsu.android.hw5;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private List<String> tickers;
    private List<String> names;
    private List<String> prices;
    private List<String> change;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public TextView txtTicker;
        public TextView txtName;
        public TextView txtStockPrice;
        public TextView txtChangePrice;
        public View layout;
        public ViewHolder(View v){
            super(v);
            layout=v;
            txtTicker =(TextView)v.findViewById(R.id.firstLine);
            txtName =(TextView)v.findViewById(R.id.secondLine);
            txtStockPrice =(TextView)v.findViewById(R.id.stockPrice);
            txtChangePrice =(TextView)v.findViewById(R.id.priceChange);
        }
    }
    public void add(int position,String item) {
        tickers.add(position,item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        tickers.remove(position);
        notifyItemRemoved(position);
    }

    public void update(List<String> favNames, List<String> favTicker, List<String> favPrice, List<String> favChange) {
        tickers = favTicker;
        names = favNames;
        prices = favPrice;
        change = favChange;
        //Log.d("ResponseD:", "Names> " + names);
        //Log.d("ResponseD:", "tickers> " + tickers);
        //Log.d("ResponseD:", "prices> " + prices);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FavoritesAdapter(List<String> favNames, List<String> favTicker, List<String> favPrice, List<String> favChange) {
        tickers = favTicker;
        names = favNames;
        prices = favPrice;
        change = favChange;
        //Log.d("ResponseD:", "Names> " + names);
        //Log.d("ResponseD:", "tickers> " + tickers);
        //Log.d("ResponseD:", "prices> " + prices);
        //Log.d("ResponseD:", "change> " + change);

    }
    // Create new views (invoked by the layout manager)
    @Override
    public FavoritesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.favorite_stock_row, parent, false);
        //set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //Log.d("ResponseD:", "position> " + position);
        // -get element from your dataset at this position
        // -replace the contents of the view with that element
        final String name = tickers.get(position);
        String header = names.get(position);
        String priceValue = "$ " + prices.get(position);
        String changeValue = change.get(position) + "%";
        header = header.substring(0,1).toUpperCase() + header.substring(1);
        holder.txtTicker.setText(name);
        holder.txtName.setText(header);
        holder.txtStockPrice.setText(priceValue);
        holder.txtChangePrice.setText(changeValue);
    }
    //Return the size of your dataset (invoker by the layout manager)
    @Override
    public int getItemCount() {
        return tickers.size();
    }

}