package ca.otams.group36.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.otams.group36.R;
import ca.otams.group36.models.Availability;

public class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.VH> {

    public interface OnDeleteClick {
        void onDelete(Availability slot);
    }

    private final List<Availability> data;
    private final OnDeleteClick deleteListener;

    public AvailabilityAdapter(List<Availability> data, OnDeleteClick deleteListener) {
        this.data = data;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slot_tutor, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Availability a = data.get(position);

        h.txtDateTime.setText(a.getDate() + " • " + a.getStartTime() + " - " + a.getEndTime());

        String courseLabel = (a.getCourseCode() != null) ? a.getCourseCode() : "";
        String auto = a.isAutoApprove() ? "Auto-Approve" : "Manual";
        h.txtCourse.setText(courseLabel + "   (" + auto + ")");

        h.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(a);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtDateTime, txtCourse;
        Button btnDelete;

        VH(@NonNull View v) {
            super(v);

            txtDateTime = v.findViewById(R.id.txtDateTime);
            txtCourse   = v.findViewById(R.id.txtCourse);
            btnDelete   = v.findViewById(R.id.btnDeleteSlot);
        }
    }
}
