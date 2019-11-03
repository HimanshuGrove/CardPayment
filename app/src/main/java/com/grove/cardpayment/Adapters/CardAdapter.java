package com.grove.cardpayment.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grove.cardpayment.Models.CardDetails;
import com.grove.cardpayment.R;
import com.grove.cardpayment.Utility.Utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private int previousExpandedPosition = -1, mExpandedPosition = -1;
    private  Activity activity;
    private List<CardDetails> cardDetails=new ArrayList<>();

    public CardAdapter(Activity  activity)
    {
        this.activity=activity;
    }

    public void notifyList(List<CardDetails> cardDetails) {
        this.cardDetails=cardDetails;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCheck;
        TextView tvCardNo,tvCardNumber,tvCardHolderName,tvExpiryDate;
        RelativeLayout rlCard,rlCardDetails;

        public ViewHolder(View view) {
            super(view);
            ivCheck=view.findViewById(R.id.ivCheck);
            tvCardNo=view.findViewById(R.id.tvCardNo);
            rlCard=view.findViewById(R.id.rlCard);
            rlCardDetails=view.findViewById(R.id.rlCardDetails);
            tvCardNumber=view.findViewById(R.id.tvCardNumber);
            tvCardHolderName=view.findViewById(R.id.tvCardHolderName);
            tvExpiryDate=view.findViewById(R.id.tvExpiryDate);
        }
    }

    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View  item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_cards, parent, false);
        return new CardAdapter.ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(CardAdapter.ViewHolder holder, final int position) {
        try {
            String cardNo=Utils.decrypt(cardDetails.get(position).getCardNo());
            String cardNumber=cardNo;
            cardNo=cardNo.substring(0,4)+" "+cardNo.substring(4,8)+" "+cardNo.substring(8,12)+" "+cardNo.substring(12,16);
            cardNumber=cardNumber.substring(0,4)+" - "+cardNumber.substring(4,8)+" - "+cardNumber.substring(8,12)+" - "+cardNumber.substring(12,16);
            holder.tvCardNo.setText(cardNo);
            holder.tvCardNumber.setText(cardNumber);
            holder.tvCardHolderName.setText(Utils.decrypt(cardDetails.get(position).getCardHolderName()));
            holder.tvExpiryDate.setText(Utils.decrypt(cardDetails.get(position).getExpiryDate()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        final boolean isExpanded = position == mExpandedPosition;
        if (isExpanded) {
            holder.rlCard.setVisibility(View.VISIBLE);
            holder.ivCheck.setImageResource(R.drawable.ic_check_circle_theme);
        } else {
            holder.rlCard.setVisibility(View.GONE);
            holder.ivCheck.setImageResource(R.drawable.ic_uncheck);
        }
        holder.itemView.setActivated(isExpanded);

        if (isExpanded)
            previousExpandedPosition = position;

        holder.ivCheck.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1 : position;
                notifyItemChanged(previousExpandedPosition);
                notifyItemChanged(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return cardDetails.size();
    }
}
