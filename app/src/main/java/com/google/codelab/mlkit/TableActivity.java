package com.google.codelab.mlkit;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class TableActivity extends AppCompatActivity {
    TableLayout TabLayout_Create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        TabLayout_Create = (TableLayout) findViewById(R.id.tableInvoices);

        Gson gson = new Gson();

        ArrayModal modal = gson.fromJson(getIntent().getStringExtra("TABLEDATA"), ArrayModal.class);
        modal.array = modal.getArray();
        ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>(modal.array);

        Log.i("TABLEDATANEW", list.toString());
        int columnSize = list.get(0).size();
        for (int i = 0; i < list.size(); i++) {
            final ArrayList<String> columns = list.get(i);
            final TableRow row1 = new TableRow(TableActivity.this);
            row1.setPadding(20, 0, 20, 0);
            row1.setBackgroundResource(R.drawable.cellshape);

            for (int j = 0; j < list.get(i).size(); j++) {
                final TextView txt = new TextView(TableActivity.this);
                if(columnSize>=4) txt.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
                else txt.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
                txt.setTypeface(Typeface.SERIF, Typeface.BOLD);
                txt.setGravity(Gravity.CENTER);
                txt.setText(columns.get(j));
                txt.setTextColor(Color.WHITE);

                row1.addView(txt);
            }
            TabLayout_Create.addView(row1);
            TabLayout_Create.setWeightSum(1);
        }

    }

}