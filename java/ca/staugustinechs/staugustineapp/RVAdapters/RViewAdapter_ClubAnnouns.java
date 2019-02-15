package ca.staugustinechs.staugustineapp.RVAdapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Objects.ClubAnnouncement;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_ClubAnnouns extends RecyclerView.Adapter<RViewAdapter_ClubAnnouns.ViewHolder> {
    private List<ClubAnnouncement> extraAnnounItems;
    private ClubDetails clubDetails;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        View cdGroup;
        ImageView cdAnnounImg;
        TextView cdAnnounTitle, cdAnnounContent, cdAnnounDate, cdAnnounClub, cdAnnounImgError;

        public ViewHolder(View v) {
            super(v);
            cdGroup = (View) itemView.findViewById(R.id.cdAnnounGroup);
            cdAnnounImg = (ImageView) itemView.findViewById(R.id.cdAnnounImg);
            cdAnnounTitle = (TextView) itemView.findViewById(R.id.cdAnnounTitle);
            cdAnnounTitle.setTextColor(AppUtils.PRIMARY_COLOR);
            cdAnnounContent = (TextView) itemView.findViewById(R.id.cdAnnounContent);
            cdAnnounDate = (TextView) itemView.findViewById(R.id.cdAnnounDate);
            cdAnnounDate.setBackgroundColor(AppUtils.PRIMARY_COLOR);
            cdAnnounClub = (TextView) itemView.findViewById(R.id.cdAnnounClub);
            cdAnnounClub.setBackgroundColor(AppUtils.ACCENT_COLOR);
            cdAnnounImgError = itemView.findViewById(R.id.cdAnnounImgError);
            cdAnnounImgError.setTextColor(AppUtils.PRIMARY_COLOR);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RViewAdapter_ClubAnnouns(List<ClubAnnouncement> extraAnnounItems, ClubDetails clubDetails) {
        this.extraAnnounItems = Lists.reverse(extraAnnounItems);
        this.clubDetails = clubDetails;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RViewAdapter_ClubAnnouns.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_clubannouns, parent, false);
        RViewAdapter_ClubAnnouns.ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RViewAdapter_ClubAnnouns.ViewHolder holder, int position) {
        holder.cdGroup.setTag(extraAnnounItems.get(position).getId());

        if(clubDetails != null) {
            if(clubDetails.isAdmin()) {
                holder.cdGroup.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        RViewAdapter_ClubAnnouns.this.clubDetails.announHeld(v);
                        return true;
                    }
                });
            }
        }else{
            holder.cdAnnounClub.setText(extraAnnounItems.get(position).getClubName());
            holder.cdAnnounClub.setVisibility(View.VISIBLE);
        }

        Bitmap img = extraAnnounItems.get(position).getImg();
        if(img != null){
            if(clubDetails != null){
                holder.cdAnnounImg.setImageBitmap(img);
                holder.cdAnnounImg.setVisibility(View.VISIBLE);
            }else{
                holder.cdAnnounImg.setVisibility(View.GONE);
                holder.cdAnnounImgError.setVisibility(View.VISIBLE);
            }
        }else{
            holder.cdAnnounImg.setVisibility(View.GONE);
        }

        DateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy");
        String dateStr = dateFormat.format(extraAnnounItems.get(position).getDate());
        holder.cdAnnounDate.setText(dateStr);

        holder.cdAnnounTitle.setText(extraAnnounItems.get(position).getTitle());

        String content = extraAnnounItems.get(position).getContent();
        if(content != null && !content.isEmpty()){
            holder.cdAnnounContent.setText(content);
            holder.cdAnnounContent.setVisibility(View.VISIBLE);
        }else{
            holder.cdAnnounContent.setVisibility(View.GONE);
        }
    }

    public void addItems(List<ClubAnnouncement> items){
        this.extraAnnounItems.addAll(items);
        this.extraAnnounItems.sort(new Comparator<ClubAnnouncement>() {
            @Override
            public int compare(ClubAnnouncement o1, ClubAnnouncement o2) {
                if(o1.getDate().before(o2.getDate())){
                    return 1;
                }else if(o1.getDate().after(o2.getDate())){
                    return -1;
                }else{
                    return 0;
                }
            }
        });
        this.notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return extraAnnounItems.size();
    }
}