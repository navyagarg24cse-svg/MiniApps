package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    EditText amount;
    Spinner fromCurrency, toCurrency;
    Button convertBtn, settingsBtn;
    TextView result;

    String[] currencies = {"INR", "USD", "EUR", "JPY"};

    double[][] rates = {
            {1, 0.012, 0.011, 1.8},
            {83, 1, 0.92, 150},
            {90, 1.08, 1, 160},
            {0.55, 0.0067, 0.0062, 1}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // ✅ Always first

        setContentView(R.layout.activity_main);

        amount = findViewById(R.id.amount);
        fromCurrency = findViewById(R.id.fromCurrency);
        toCurrency = findViewById(R.id.toCurrency);
        convertBtn = findViewById(R.id.convertBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        result = findViewById(R.id.result);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                currencies
        );

        fromCurrency.setAdapter(adapter);
        toCurrency.setAdapter(adapter);

        convertBtn.setOnClickListener(v -> convert());

        settingsBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class))
        );
    }

    // ✅ Re-apply theme on every resume (after returning from SettingsActivity)
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("darkMode", false);
        int targetMode = isDark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;

        // ✅ Only call if mode actually changed — prevents recreation loop
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }
    }

    private void convert() {
        String input = amount.getText().toString().trim();

        if (input.isEmpty()) {
            result.setText("Please enter amount");
            return;
        }

        double amt = Double.parseDouble(input);
        int from = fromCurrency.getSelectedItemPosition();
        int to = toCurrency.getSelectedItemPosition();
        double converted = amt * rates[from][to];

        result.setText(String.format(
                "%s %.2f = %s %.2f",
                currencies[from], amt,
                currencies[to], converted
        ));
    }
}