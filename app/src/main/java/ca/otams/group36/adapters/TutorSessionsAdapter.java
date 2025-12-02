package ca.otams.group36.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import ca.otams.group36.R;
import ca.otams.group36.models.Session;

public class TutorSessionsAdapter extends RecyclerView.Adapter<TutorSessionsAdapter.VH> {

    public interface OnSessionActionListener {
        void onAction(Session session, String action);
    }

    private final ArrayList<Session> data;
    private final OnSessionActionListener listener;
    private final boolean isRequestsMode;

    public TutorSessionsAdapter(ArrayList<Session> data, OnSessionActionListener listener, boolean isRequestsMode) {
        this.data = data;
        this.listener = listener;
        this.isRequestsMode = isRequestsMode;
    }


    @Override
    public int getItemViewType(int position) {
        return isRequestsMode ? 1 : 2;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;

        if (viewType == 1) { // pending requests
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_session_request, parent, false);
        } else { // sessions view
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tutor_session, parent, false);
        }

        return new VH(v, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {

        Session s = data.get(pos);

        // Title
        String title = s.getSubject() + " • " +
                s.getDate() + " " +
                s.getStartTime() + "-" + s.getEndTime();

        if (isRequestsMode) {
            h.txtSubject.setText(title);
            h.txtStudent.setText("Student: " + s.getStudentEmail());
            h.txtStatus.setText("Status: " + s.getStatus());

            h.btnApprove.setOnClickListener(v -> listener.onAction(s, "approve"));
            h.btnReject.setOnClickListener(v -> listener.onAction(s, "reject"));

        } else {
            h.txtTitle.setText(title);
            h.txtStudent2.setText("Student: " + s.getStudentEmail());
            h.txtStatus2.setText("Status: " + s.getStatus());
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        // For requests
        TextView txtSubject, txtStudent, txtStatus;
        MaterialButton btnApprove, btnReject;

        // For sessions
        TextView txtTitle, txtStudent2, txtStatus2;

        public VH(@NonNull View v, int type) {
            super(v);

            if (type == 1) { // requests mode
                txtSubject = v.findViewById(R.id.txtSubject);
                txtStudent = v.findViewById(R.id.txtStudent);
                txtStatus = v.findViewById(R.id.txtStatus);
                btnApprove = v.findViewById(R.id.btnApprove);
                btnReject = v.findViewById(R.id.btnReject);
            } else { // sessions mode
                txtTitle = v.findViewById(R.id.txtTitle);
                txtStudent2 = v.findViewById(R.id.txtStudent);
                txtStatus2 = v.findViewById(R.id.txtStatus);
            }
        }
    }
}
