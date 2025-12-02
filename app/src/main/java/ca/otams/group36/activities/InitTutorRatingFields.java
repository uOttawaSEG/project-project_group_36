package ca.otams.group36.activities;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class InitTutorRatingFields {

    private static final String TAG = "InitRatingFields";

    public static void runOnce() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("role", "Tutor")
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Log.d(TAG, "No tutor documents found.");
                        return;
                    }

                    WriteBatch batch = db.batch();
                    final int[] count = {0};


                    for (DocumentSnapshot doc : snap.getDocuments()) {

                        Map<String, Object> update = new HashMap<>();

                        if (!doc.contains("ratingSum")) {
                            update.put("ratingSum", 0L);
                        }

                        if (!doc.contains("ratingCount")) {
                            update.put("ratingCount", 0L);
                        }

                        if (!update.isEmpty()) {
                            batch.update(doc.getReference(), update);
                            count[0]++;
                            Log.d(TAG, "Updating tutor: " + doc.getId() + " -> " + update);
                        }
                    }

                    if (count[0] == 0) {
                        Log.d(TAG, "All tutors already initialized. Nothing to update.");
                        return;
                    }

                    batch.commit()
                            .addOnSuccessListener(r -> Log.d(TAG, "Initialization complete. Updated tutors: " + count[0]))
                            .addOnFailureListener(e -> Log.e(TAG, "Batch commit failed: " + e.getMessage()));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load tutor list: " + e.getMessage()));
    }
}
