package ca.otams.group36.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ca.otams.group36.R;
import ca.otams.group36.models.Availability;

public class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.VH> {

    public interface OnSlotClick {
        void onClick(Availability slot);
    }

    List<Availability> data;
    OnSlotClick listener;

    public AvailabilityAdapter(List<Availability> data, OnSlotClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Availability a = data.get(position);
        h.txtInfo.setText(a.getDate() + "  " + a.getStartTime() + " - " + a.getEndTime());
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(a);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtInfo;
        VH(@NonNull View itemView) {
            super(itemView);
            txtInfo = itemView.findViewById(R.id.txtSlotInfo);
        }
    }
}
