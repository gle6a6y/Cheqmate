package com.example.cheqmate;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        TextView tvFooter = findViewById(R.id.tvAlreadyHaveAccount);
        String leading = getString(R.string.already_have_account_leading);
        String link = getString(R.string.sign_in);
        SpannableString span = new SpannableString(leading + link);
        int linkStart = leading.length();
        int linkEnd = linkStart + link.length();
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Переход на экран входа пока не реализован
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(SignUpActivity.this, R.color.link_text));
                ds.setUnderlineText(true);
            }
        }, linkStart, linkEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvFooter.setText(span);
        tvFooter.setMovementMethod(LinkMovementMethod.getInstance());
        tvFooter.setHighlightColor(Color.TRANSPARENT);
    }
}
