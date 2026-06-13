package com.example.wattwise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    Spinner spinnerMonthDetail;
    EditText editUnitDetail;
    SeekBar seekBarRebateDetail;
    TextView textRebateDetail, textTotalDetail, textFinalDetail;
    Button buttonUpdate, buttonDelete, buttonBackDetail, buttonHomeDetail;

    DatabaseHelper databaseHelper;
    int billId;

    double totalCharges = 0.0;
    double finalCost = 0.0;
    int rebateValue = 0;

    String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        databaseHelper = new DatabaseHelper(this);

        spinnerMonthDetail = findViewById(R.id.spinnerMonthDetail);
        editUnitDetail = findViewById(R.id.editUnitDetail);
        seekBarRebateDetail = findViewById(R.id.seekBarRebateDetail);
        textRebateDetail = findViewById(R.id.textRebateDetail);
        textTotalDetail = findViewById(R.id.textTotalDetail);
        textFinalDetail = findViewById(R.id.textFinalDetail);

        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonBackDetail = findViewById(R.id.buttonBackDetail);
        buttonHomeDetail = findViewById(R.id.buttonHomeDetail);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                months
        );
        spinnerMonthDetail.setAdapter(adapter);

        billId = getIntent().getIntExtra("bill_id", -1);

        if (billId == -1) {
            Toast.makeText(this, "Record not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        seekBarRebateDetail.setMax(5);

        buttonBackDetail.setOnClickListener(view -> finish());

        buttonHomeDetail.setOnClickListener(view -> {
            Intent intent = new Intent(DetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        seekBarRebateDetail.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rebateValue = progress;
                textRebateDetail.setText("Rebate: " + rebateValue + "%");
                recalculateDisplay();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used
            }
        });

        loadBillDetails();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBill();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete();
            }
        });
    }

    private void loadBillDetails() {
        Bill bill = databaseHelper.getBillById(billId);

        if (bill != null) {
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(bill.getMonth())) {
                    spinnerMonthDetail.setSelection(i);
                    break;
                }
            }

            editUnitDetail.setText(String.valueOf(bill.getUnit()));

            rebateValue = (int) bill.getRebate();
            seekBarRebateDetail.setProgress(rebateValue);
            textRebateDetail.setText("Rebate: " + rebateValue + "%");

            totalCharges = bill.getTotalCharges();
            finalCost = bill.getFinalCost();

            textTotalDetail.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", totalCharges));
            textFinalDetail.setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", finalCost));
        } else {
            Toast.makeText(this, "Record not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void recalculateDisplay() {
        String unitText = editUnitDetail.getText().toString().trim();

        if (!unitText.isEmpty()) {
            int unit = Integer.parseInt(unitText);

            if (unit >= 1 && unit <= 1000) {
                totalCharges = calculateTotalCharges(unit);
                finalCost = totalCharges - (totalCharges * rebateValue / 100.0);

                textTotalDetail.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", totalCharges));
                textFinalDetail.setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", finalCost));
            }
        }
    }

    private void updateBill() {
        String month = spinnerMonthDetail.getSelectedItem().toString();
        String unitText = editUnitDetail.getText().toString().trim();

        if (unitText.isEmpty()) {
            editUnitDetail.setError("Please enter electricity usage.");
            editUnitDetail.requestFocus();
            return;
        }

        int unit = Integer.parseInt(unitText);

        if (unit < 1) {
            editUnitDetail.setError("Minimum usage is 1 kWh.");
            editUnitDetail.requestFocus();
            return;
        }

        if (unit > 1000) {
            editUnitDetail.setError("Maximum usage is 1000 kWh.");
            editUnitDetail.requestFocus();
            return;
        }

        totalCharges = calculateTotalCharges(unit);
        finalCost = totalCharges - (totalCharges * rebateValue / 100.0);

        boolean updated = databaseHelper.updateBill(
                billId,
                month,
                unit,
                totalCharges,
                rebateValue,
                finalCost
        );

        if (updated) {
            Toast.makeText(this, "Record updated successfully.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update record.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete Record");
        builder.setMessage("Are you sure you want to delete this bill record?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            boolean deleted = databaseHelper.deleteBill(billId);

            if (deleted) {
                Toast.makeText(this, "Record deleted successfully.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(DetailActivity.this, HistoryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete record.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private double calculateTotalCharges(int unit) {
        double total;

        if (unit <= 200) {
            total = unit * 0.218;
        } else if (unit <= 300) {
            total = (200 * 0.218) + ((unit - 200) * 0.334);
        } else if (unit <= 600) {
            total = (200 * 0.218) + (100 * 0.334) + ((unit - 300) * 0.516);
        } else {
            total = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((unit - 600) * 0.546);
        }

        return total;
    }
}