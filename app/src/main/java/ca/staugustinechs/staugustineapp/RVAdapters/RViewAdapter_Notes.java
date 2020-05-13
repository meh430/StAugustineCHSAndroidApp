package ca.staugustinechs.staugustineapp.RVAdapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Objects.Note;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_Notes extends RecyclerView.Adapter<RViewAdapter_Notes.NoteViewHolder> {
    private static String[] months =
            new String[]{"FIRST", "Jan", "Feb", "March", "April",
                    "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
    private static String[] ordinals = new String[]{"th", "st", "nd", "rd"};
    private ArrayList<Note> notes;
    private final LayoutInflater inflater;
    private TextView noNotes;

    public RViewAdapter_Notes(Context context, ArrayList<Note> notes, TextView empty) {
        inflater = LayoutInflater.from(context);
        this.notes = notes;
        noNotes = empty;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.note_card, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RViewAdapter_Notes.NoteViewHolder holder, final int position) {
        if (notes != null) {
            final int p = position;
            final Note current = notes.get(p);
            holder.bindTo(current);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Todo: edit clicked note
                }
            });
        }
    }

    public void setNotes(ArrayList<Note> noteList) {
        notes = noteList;
        notifyDataSetChanged();
        noNotes.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTv, contentsTv, dateTv, statTv;

        NoteViewHolder(View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.noteTitle);
            contentsTv = itemView.findViewById(R.id.noteContents);
            dateTv = itemView.findViewById(R.id.noteDate);
            statTv = itemView.findViewById(R.id.noteStatus);
        }

        String getStatus(String date) {
            String status = "";
            String strCurrDate = AppUtils.getDate();
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss", Locale.CANADA);
            try {
                Date currDate = format.parse(strCurrDate);
                Date noteDate = format.parse(date);
                long difference = 0L;
                //is note set for the future
                if (noteDate.after(currDate)) {
                    //the task is coming up
                    difference = noteDate.getTime() - currDate.getTime();
                    status = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS) + " days left";
                } else {
                    //the task is overdue
                    difference = currDate.getTime() - noteDate.getTime();
                    status = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS) + " days overdue";
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return status;
        }

        String getOrdinal(int n) {
            String suffix = n > 0 ?
                    ordinals[(n > 3 && n < 21 || n % 10 > 3) ? 0 : n % 10] : "";
            return n + suffix;
        }

        private String convertDate(String date) {
            if (!date.contains("at")) {
                return "";
            }

            String[] tempDate = date.trim().split(" at ");
            String[] dateArr = tempDate[0].trim().split("-");
            String[] time = tempDate[1].trim().split(":");
            String month = months[Integer.parseInt(dateArr[0])];
            int hours = Integer.parseInt(time[0]);
            String pmam;
            if (hours > 12) {
                hours -= 12;
                pmam = "p.m";
            } else {
                pmam = "a.m";
            }
            String ret = month + " " +
                    getOrdinal(Integer.parseInt(dateArr[1])) + ", " + dateArr[2] +
                    " | " + hours + ":" + time[1] + " " + pmam;
            Log.e("DATE", ret);
            return ret;
        }

        void bindTo(Note note) {
            titleTv.setText(note.getTitle());
            contentsTv.setText(note.getContents());
            String date = convertDate(note.getDueDate());
            String stat = note.isDone() ? "Done" : getStatus(note.getDueDate());
            dateTv.setText(date);
            statTv.setText(stat);
        }
    }
}
