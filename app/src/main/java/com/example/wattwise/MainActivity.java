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

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerMonth;
    EditText editUnit;
    SeekBar seekBarRebate;
    TextView textRebateValue, textTotalCharges, textFinalCost, textNotice;
    Button buttonCalculate, buttonSave, buttonHistory, buttonAbout;

    double totalCharges = 0.0;
    double finalCost = 0.0;
    int rebateValue = 0;

    DatabaseHelper databaseHelper;

    String[] months = {
            "Select Month",
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        editUnit = findViewById(R.id.editUnit);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        textRebateValue = findViewById(R.id.textRebateValue);
        textTotalCharges = findViewById(R.id.textTotalCharges);
        textFinalCost = findViewById(R.id.textFinalCost);
        textNotice = findViewById(R.id.textNotice);

        buttonCalculate = findViewById(R.id.buttonCalculate);
        buttonSave = findViewById(R.id.buttonSave);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonAbout = findViewById(R.id.buttonAbout);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                months
        );
        spinnerMonth.setAdapter(adapter);

        textRebateValue.setText("Rebate: 0%");

        seekBarRebate.setMax(5);
        seekBarRebate.setProgress(0);

        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rebateValue = progress;
                textRebateValue.setText("Rebate: " + rebateValue + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateBill();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBill();
            }
        });

        buttonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        buttonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    private void calculateBill() {

        String month = spinnerMonth.getSelectedItem().toString();
        String unitText = editUnit.getText().toString().trim();

        if (month.equals("Select Month")) {
            Toast.makeText(this, "Please select a month.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (unitText.isEmpty()) {
            editUnit.setError("Please enter electricity usage.");
            editUnit.requestFocus();
            return;
        }

        int unit = Integer.parseInt(unitText);

        if (unit < 1) {
            editUnit.setError("Minimum usage is 1 kWh.");
            editUnit.requestFocus();
            return;
        }

        if (unit > 1000) {
            editUnit.setError("Maximum usage is 1000 kWh.");
            editUnit.requestFocus();
            return;
        }

        totalCharges = calculateTotalCharges(unit);
        finalCost = totalCharges - (totalCharges * rebateValue / 100.0);

        textTotalCharges.setText(String.format(Locale.getDefault(), "Total Charges: RM %.2f", totalCharges));
        textFinalCost.setText(String.format(Locale.getDefault(), "Final Cost: RM %.2f", finalCost));

        Toast.makeText(this, "Bill calculated successfully.", Toast.LENGTH_SHORT).show();
    }

    private double calculateTotalCharges(int unit) {

        double total = 0.0;

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

    private void saveBill() {

        String month = spinnerMonth.getSelectedItem().toString();
        String unitText = editUnit.getText().toString().trim();

        if (month.equals("Select Month") || unitText.isEmpty()) {
            Toast.makeText(this, "Please calculate the bill first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalCharges == 0.0 && finalCost == 0.0) {
            Toast.makeText(this, "Please calculate the bill before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        int unit = Integer.parseInt(unitText);

        boolean inserted = databaseHelper.insertBill(month, unit, totalCharges, rebateValue, finalCost);

        if (inserted) {
            Toast.makeText(this, "Record saved successfully.", Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            Toast.makeText(this, "Failed to save record.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        spinnerMonth.setSelection(0);
        editUnit.setText("");
        seekBarRebate.setProgress(0);
        textTotalCharges.setText("Total Charges: RM 0.00");
        textFinalCost.setText("Final Cost: RM 0.00");
        totalCharges = 0.0;
        finalCost = 0.0;
    }
}