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

public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {

    private ArrayList<Session> sessions;
    private OnSessionActionListener listener;

    public SessionsAdapter(ArrayList<Session> sessions, OnSessionActionListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session s = sessions.get(position);

        holder.txtSubject.setText("Subject: " + s.getSubject());
        holder.txtTime.setText(s.getDate() + "  " + s.getStartTime() + " - " + s.getEndTime());
        holder.txtStudent.setText("Student: " + s.getStudentName());
        holder.txtStatus.setText("Status: " + s.getStatus());

        // 只有 Pending 状态才显示按钮
        boolean isPending = "pending".equalsIgnoreCase(s.getStatus());
        holder.btnApprove.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.btnReject.setVisibility(isPending ? View.VISIBLE : View.GONE);

        holder.btnApprove.setOnClickListener(v -> listener.onAction(s, "approve"));
        holder.btnReject.setOnClickListener(v -> listener.onAction(s, "reject"));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubject, txtTime, txtStudent, txtStatus;
        MaterialButton btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStudent = itemView.findViewById(R.id.txtStudent);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }

    public interface OnSessionActionListener {
        void onAction(Session session, String action);
    }
}
