package tw.broccoli.amybus;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by broccoli on 15/9/24.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>{

    private List<Bus> mListBus = null;
    private List<Integer> minionsImage = null;


    public RecyclerViewAdapter(){

    }

    public void setListBus(List<Bus> listBus){
        this.mListBus = listBus;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        ImageView mImageViewTheMinions;
        TextView mTextViewBus;
        TextView mTextViewRoute;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView)itemView.findViewById(R.id.cardview);
            mImageViewTheMinions = (ImageView)itemView.findViewById(R.id.imageview_theminions);
            mTextViewBus = (TextView)itemView.findViewById(R.id.textview_bus);
            mTextViewRoute = (TextView)itemView.findViewById(R.id.textview_route);
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview, viewGroup, false);
        RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(v);
        return recyclerViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.mImageViewTheMinions.setImageResource(getMinionsImage());
        holder.mTextViewBus.setText(mListBus.get(position).getNumber() + " - " + mListBus.get(position).getDirectText());
        holder.mTextViewRoute.setText(mListBus.get(position).getOnBus());
    }

    private int getMinionsImage(){
//        int[] minionsImage = {R.mipmap.minions1, R.mipmap.minions2, R.mipmap.minions3, R.mipmap.minions4, R.mipmap.minions5, R.mipmap.minions6, R.mipmap.minions7, R.mipmap.minions8};
        if(minionsImage == null) minionsImage = new ArrayList();

        if(minionsImage.size()==0) {
            minionsImage.add(R.mipmap.minions1);
            minionsImage.add(R.mipmap.minions2);
            minionsImage.add(R.mipmap.minions3);
            minionsImage.add(R.mipmap.minions4);
            minionsImage.add(R.mipmap.minions5);
            minionsImage.add(R.mipmap.minions6);
            minionsImage.add(R.mipmap.minions7);
            minionsImage.add(R.mipmap.minions8);
        }
        int random = new Random().nextInt(minionsImage.size());
        int returnImage = minionsImage.get(random);
        minionsImage.remove(random);

        return returnImage;
    }

    @Override
    public int getItemCount() {
        return mListBus.size();
    }
}