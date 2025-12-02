package ca.otams.group36.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import ca.otams.group36.R;
import ca.otams.group36.activities.StudentSessionsActivity;
import ca.otams.group36.models.Session;

public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.VH> {

    public interface OnAction {
        void onAction(Session s, String action);
    }

    private final ArrayList<Session> data;
    private final StudentSessionsActivity.Filter filter;
    private final OnAction handler;

    private boolean rated;


    public boolean isRated() { return rated; }
    public void setRated(boolean rated) { this.rated = rated; }

    public SessionsAdapter(ArrayList<Session> data,
                           StudentSessionsActivity.Filter filter,
                           OnAction handler) {
        this.data = data;
        this.filter = filter;
        this.handler = handler;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_session, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Session s = data.get(position);

        String header = s.getSubject() + " • " + s.getDate() + " "
                + s.getStartTime() + "-" + s.getEndTime();
        h.txtTitle.setText(header);

        h.txtStatus.setText("Status: " + s.getStatus());

        h.btnCancel.setVisibility(View.GONE);
        h.btnRate.setVisibility(View.GONE);

        if (filter == StudentSessionsActivity.Filter.UPCOMING) {
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnCancel.setOnClickListener(v -> handler.onAction(s, "cancel"));
        }
        else if (filter == StudentSessionsActivity.Filter.PENDING) {
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnCancel.setOnClickListener(v -> handler.onAction(s, "cancel"));
        }
        else if (filter == StudentSessionsActivity.Filter.PAST) {
            String sessionId = s.getId();
            String studentEmail = s.getStudentEmail();

            FirebaseFirestore.getInstance()
                    .collection("ratings")
                    .whereEqualTo("sessionId", sessionId)
                    .whereEqualTo("studentEmail", studentEmail)
                    .get()
                    .addOnSuccessListener(qs -> {
                        if (qs.isEmpty()) {
                            h.btnRate.setVisibility(View.VISIBLE);
                            h.btnRate.setOnClickListener(v -> handler.onAction(s, "rate"));
                        } else {
                            h.btnRate.setVisibility(View.GONE);
                        }
                    });
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTitle, txtStatus;
        MaterialButton btnCancel, btnRate;

        VH(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnRate = itemView.findViewById(R.id.btnRate);
        }
    }
}
