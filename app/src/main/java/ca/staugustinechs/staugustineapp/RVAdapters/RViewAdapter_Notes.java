package ca.staugustinechs.staugustineapp.RVAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Objects.Note;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_Notes extends RecyclerView.Adapter<RViewAdapter_Notes.NoteViewHolder> {
    private static String[] months =
            new String[]{"FIRST", "Jan", "Feb", "March", "April",
                    "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
    private static String[] ordinals = new String[]{"th", "st", "nd", "rd"};
    private ArrayList<Note> notes;
    private ArrayList<Note> currList;
    private final LayoutInflater inflater;
    private TextView noNotes;
    Context con;

    public RViewAdapter_Notes(Context context, ArrayList<Note> notes, TextView empty) {
        inflater = LayoutInflater.from(context);
        this.notes = notes;
        noNotes = empty;
        con = context;

        Collections.sort(notes, new Comparator<Note>() {
            public int compare(Note n1, Note n2) {
                SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss", Locale.CANADA);
                try {
                    return format.parse(n1.getDueDate()).compareTo(format.parse(n2.getDueDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return n1.getTitle().compareTo(n2.getTitle());
            }
        });

        currList = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.note_card, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return currList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RViewAdapter_Notes.NoteViewHolder holder, final int position) {
        if (currList != null) {
            final int p = position;
            final Note current = currList.get(p);
            holder.bindTo(current);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Todo: edit clicked note
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //launch form in browser
                    AlertDialog.Builder markDoneDialog = new AlertDialog.Builder(Objects.requireNonNull(con));
                    markDoneDialog.setTitle("Mark As Done?");
                    markDoneDialog.setMessage("Are you done with this task?");
                    markDoneDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            current.setState(true);
                            Toast.makeText(con, "Marked as done", Toast.LENGTH_SHORT);
                            notifyDataSetChanged();

                            FirebaseFirestore.getInstance().
                                    collection("users")
                                    .document(Main.PROFILE.getUid()).collection("info")
                                    .document("vital")
                                    .update("notes", notes);
                        }
                    });

                    markDoneDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });

                    markDoneDialog.show();
                    return true;
                }
            });
        }
    }

    //noteList: all of the user's notes
    //currList: smaller, filtered list of notes
    public void setNotes(ArrayList<Note> noteList, ArrayList<Note> currList) {
        this.currList = currList;
        notes = noteList;
        notifyDataSetChanged();
        noNotes.setVisibility(currList.isEmpty() ? View.VISIBLE : View.GONE);
        Collections.sort(currList, new Comparator<Note>() {
            public int compare(Note n1, Note n2) {
                SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss", Locale.CANADA);
                try {
                    return format.parse(n1.getDueDate()).compareTo(format.parse(n2.getDueDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return n1.getTitle().compareTo(n2.getTitle());
            }
        });
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTv, contentsTv, dateTv, statTv;
        private View noteRoot;

        NoteViewHolder(View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.noteTitle);
            contentsTv = itemView.findViewById(R.id.noteContents);
            dateTv = itemView.findViewById(R.id.noteDate);
            statTv = itemView.findViewById(R.id.noteStatus);
            noteRoot = itemView.findViewById(R.id.noteRoot);
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
            ((CardView) noteRoot).setCardBackgroundColor(ContextCompat.getColor(con, R.color.cardViewBackground));
            titleTv.setText(note.getTitle());
            contentsTv.setText(note.getContents());
            String date = convertDate(note.getDueDate());
            String stat = note.isDone() ? "Done" : getStatus(note.getDueDate());
            dateTv.setText("Due: " + date);
            statTv.setText(stat);
            if (note.isDone()) {
                Log.e("GREEN", note.getTitle());
                ((CardView) noteRoot).setCardBackgroundColor(ContextCompat.getColor(con, R.color.doneColor));
            } else if (stat.contains("overdue")) {
                ((CardView) noteRoot).setCardBackgroundColor(ContextCompat.getColor(con, R.color.overdueColor));
            }
        }
    }
}
