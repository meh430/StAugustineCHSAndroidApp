package ca.staugustinechs.staugustineapp.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Objects.Note;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Notes;

//Todo: implement deletions
//Todo: implement note sort by the task that is due the soonest
public class TasksFragment extends Fragment {
    private static ArrayList<Note> noteList = (ArrayList<Note>) Main.PROFILE.getUserNotes();
    private TextView emptyList;
    private RecyclerView noteRecycler;
    private View offlineLayout;
    private static RViewAdapter_Notes noteAdapter;
    private FloatingActionButton noteFab;
    private RelativeLayout root;


    public TasksFragment() {
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        offlineLayout = getLayoutInflater().inflate(R.layout.offline_layout, null);
        root = getView().findViewById(R.id.taskLayout);

        if (AppUtils.isNetworkAvailable(this.getActivity())) {
            Log.e("Notes", noteList.toString());
            emptyList = getView().findViewById(R.id.noNotes);
            noteFab = getView().findViewById(R.id.noteFab);
            noteFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder noteDialog = new AlertDialog.Builder(getContext());
                    noteDialog.setTitle("Add a Task");
                    final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_note, null);
                    noteDialog.setView(dialogLayout);
                    noteDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String noteTitle = ((EditText) dialogLayout.findViewById(R.id.titleEdit))
                                    .getText().toString();
                            final String noteContent = ((EditText) dialogLayout.findViewById(R.id.contentEdit))
                                    .getText().toString();

                            final Calendar calendar = Calendar.getInstance();
                            DatePickerDialog datePick = new DatePickerDialog(
                                    getActivity(), new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                    TimePickerFragment timePick = new TimePickerFragment(year, month, day, noteTitle, noteContent);
                                    timePick.show(getActivity().getSupportFragmentManager(), "Set time");
                                }
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH));
                            datePick.setTitle("Set Due Date");
                            datePick.show();

                        }
                    });
                    noteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //cancel
                        }
                    });

                    noteDialog.create().show();
                }
            });

            noteRecycler = getView().findViewById(R.id.noteScroll);
            noteRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            noteAdapter = new RViewAdapter_Notes(getContext(), noteList, emptyList);
            noteRecycler.setAdapter(noteAdapter);
            noteAdapter.notifyDataSetChanged();
            noteRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0 || dy < 0 && noteFab.isShown())
                        noteFab.hide();
                }

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE)
                        noteFab.show();
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });

            if (noteList.isEmpty()) {
                emptyList.setVisibility(View.VISIBLE);
            } else {
                emptyList.setVisibility(View.GONE);
            }
        } else {
            setOffline();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tasks, container, false);

    }

    private void setOffline() {
        root.removeView(offlineLayout);
        root.addView(offlineLayout);
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        int year, month, day;
        String title, content;

        TimePickerFragment(int y, int m, int d, String t, String c) {
            year = y;
            month = m;
            day = d;
            title = t;
            content = c;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            Log.e("Picked", year + ", " + month + ", " + day + ", " + hour + ", " + minute);
            String strMonth = month < 10 ? "0" + (month + 1) : (month + 1) + "";
            String strDay = day < 10 ? "0" + day : day + "";
            String strHour = hour < 10 ? "0" + hour : hour + "";
            String strMin = minute < 10 ? "0" + minute : minute + "";

            //MM-dd-yyyy 'at' HH:mm:ss
            String strDate = (strMonth) + "-" + strDay + "-" + year + " at " + strHour + ":" + strMin + ":00";
            Note note = new Note(title, content, strDate);
            noteList.add(note);
            noteAdapter.setNotes(noteList);
            Main.PROFILE.setNotes(noteList);

            FirebaseFirestore.getInstance().
                    collection("users")
                    .document(Main.PROFILE.getUid()).collection("info")
                    .document("vital")
                    .update("notes", FieldValue.arrayUnion(note));
        }
    }

}


