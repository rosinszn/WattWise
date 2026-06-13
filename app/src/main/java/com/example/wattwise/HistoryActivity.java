package com.example.wattwise;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    ListView listViewBills;
    TextView textEmpty;
    Button buttonBackHistory, buttonHomeHistory;
    LinearLayout tableCard;

    DatabaseHelper databaseHelper;
    ArrayList<Bill> billList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listViewBills = findViewById(R.id.listViewBills);
        textEmpty = findViewById(R.id.textEmpty);
        buttonBackHistory = findViewById(R.id.buttonBackHistory);
        buttonHomeHistory = findViewById(R.id.buttonHomeHistory);
        tableCard = findViewById(R.id.tableCard);

        databaseHelper = new DatabaseHelper(this);

        buttonBackHistory.setOnClickListener(view -> finish());

        buttonHomeHistory.setOnClickListener(view -> {
            Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        listViewBills.setOnItemClickListener((parent, view, position, id) -> {
            Bill selectedBill = billList.get(position);
            openDetailPage(selectedBill.getId());
        });

        loadBills();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills();
    }

    private void loadBills() {
        billList = databaseHelper.getAllBills();

        BillAdapter adapter = new BillAdapter(this, billList);
        listViewBills.setAdapter(adapter);

        if (billList.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            tableCard.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            tableCard.setVisibility(View.VISIBLE);
        }
    }

    private void openDetailPage(int billId) {
        Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
        intent.putExtra("bill_id", billId);
        startActivity(intent);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private class BillAdapter extends BaseAdapter {

        Context context;
        ArrayList<Bill> bills;

        public BillAdapter(Context context, ArrayList<Bill> bills) {
            this.context = context;
            this.bills = bills;
        }

        @Override
        public int getCount() {
            return bills.size();
        }

        @Override
        public Object getItem(int position) {
            return bills.get(position);
        }

        @Override
        public long getItemId(int position) {
            return bills.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Bill bill = bills.get(position);

            LinearLayout rowLayout = new LinearLayout(context);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);
            rowLayout.setPadding(dp(14), dp(10), dp(14), dp(10));
            rowLayout.setBackgroundColor(Color.WHITE);

            TextView textMonth = new TextView(context);
            textMonth.setText(bill.getMonth());
            textMonth.setTextSize(15);
            textMonth.setTextColor(Color.parseColor("#0F172A"));
            textMonth.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams monthParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.1f
            );
            textMonth.setLayoutParams(monthParams);

            TextView textFinalCost = new TextView(context);
            textFinalCost.setText("RM " + String.format(Locale.getDefault(), "%.2f", bill.getFinalCost()));
            textFinalCost.setTextSize(15);
            textFinalCost.setTextColor(Color.parseColor("#16A34A"));
            textFinalCost.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams costParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.1f
            );
            textFinalCost.setLayoutParams(costParams);

            Button buttonView = new Button(context);
            buttonView.setText("View");
            buttonView.setTextSize(12);
            buttonView.setTextColor(Color.WHITE);
            buttonView.setAllCaps(false);
            buttonView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
            buttonView.setFocusable(false);

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    0,
                    dp(42),
                    1.2f
            );
            buttonView.setLayoutParams(buttonParams);

            buttonView.setOnClickListener(view -> openDetailPage(bill.getId()));

            rowLayout.addView(textMonth);
            rowLayout.addView(textFinalCost);
            rowLayout.addView(buttonView);

            return rowLayout;
        }
    }
}