package com.renachl.speedquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.google.android.material.slider.Slider;
import com.renachl.speedquiz.Controllers.QuestionManager;


public class ConfigActivity extends AppCompatActivity {

    private Slider SL_Delay;
    private Slider SL_NbrQst;
    private Button BT_TestDelay;
    private Button BT_ValideNewQst;
    private Button BT_ManageQst;
    private EditText ED_IntituleQst;
    private RadioGroup RDGRP_RepQst;

    private Handler handler;
    private Runnable questionRunnable = null;

    public static final int QST_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);

        SL_Delay = findViewById(R.id.config_slide_delais);
        SL_NbrQst = findViewById(R.id.config_slide_nbr_qst);
        BT_TestDelay = findViewById(R.id.config_bt_tst);

        ED_IntituleQst = findViewById(R.id.config_ed_initule_qst);
        RDGRP_RepQst = findViewById(R.id.config_rdGrp_rep);
        BT_ValideNewQst = findViewById(R.id.config_bt_qst_valider);

        BT_ManageQst = findViewById(R.id.config_bt_li_qst);
        //Change la valeur du slider pour la mettre à celle actuelle
        SharedPreferences prefs = getSharedPreferences("com.renachl.speedquiz", MODE_PRIVATE);
        changeValueSliderDelais(prefs.getInt("qstDelai", QST_DELAY));

        //Change la valeur max du slider en fonction du nombre de question possible
        SL_NbrQst.setValueTo(QuestionManager.getNombreQuestion(this));
        SL_NbrQst.setValue(prefs.getInt("nbrQst", (int) QuestionManager.getNombreQuestion(this)));

        //Le bouton pour valider les question est grisé
        BT_ValideNewQst.setEnabled(false);
    }


    @Override
    protected void onStart() {
        super.onStart();

        //Lance le bouton de tst de délais
        startBtDelaisTest();

        //Change la valeur du temps dans les config de l'application au mouvement du slider
        SL_Delay.addOnChangeListener((slider, value, fromUser) -> {
            SharedPreferences prefs = getSharedPreferences("com.renachl.speedquiz", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("qstDelai", (int) (SL_Delay.getValue() * 1000.0));
            editor.apply();
        });

        //Change le nombre de question pour une partie
        SL_NbrQst.addOnChangeListener((slider, value, fromUser) -> {
            SharedPreferences prefs = getSharedPreferences("com.renachl.speedquiz", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("nbrQst", (int) value);
            editor.apply();
        });

        ED_IntituleQst.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                BT_ValideNewQst.setEnabled(!ED_IntituleQst.getText().toString().equals(""));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Ajoute la nouvelle question à la base de données, vide le champs désactive le bt
        BT_ValideNewQst.setOnClickListener(view -> {
            String reponse = ((RadioButton) findViewById(RDGRP_RepQst.getCheckedRadioButtonId())).getText().toString();

            SL_NbrQst.setValueTo((int) QuestionManager.getNombreQuestion(this));

            addQstPerso(ED_IntituleQst.getText().toString(), reponse);

            ED_IntituleQst.setText("");
            ED_IntituleQst.requestFocus();
        });

        //Ouvre l'activity pour afficher la liste de question
        BT_ManageQst.setOnClickListener(view -> {
            Intent questionActivity = new Intent(ConfigActivity.this, QuestionActivity.class);
            startActivity(questionActivity);
        });
    }

    /**
     * Ajoute une question à la base de données
     * @param intitule Intitulé de la nouvelle question
     * @param reponse  Réponse de la nouvellle question
     */
    private void addQstPerso(String intitule, String reponse) {
        QuestionManager.addNewQuestion(this, intitule, reponse);
    }

    /**
     * Change la valeur du slider depuis des millisecondes pour le passer en valeur à virgule
     * @param milliSeconde Valeur en millisecondes
     */
    private void changeValueSliderDelais(int milliSeconde) {
        float valueDelais = (float) (milliSeconde / 1000.0);
        SL_Delay.setValue(valueDelais);
    }

    /**
     * Lance le clignotement du bouton qui prévisualise le délai entre chaque questions
     */
    private void startBtDelaisTest() {
        SharedPreferences prefs = getSharedPreferences("com.renachl.speedquiz", MODE_PRIVATE);

        handler = new Handler();
        questionRunnable = new Runnable() {
            int isVisible =  View.VISIBLE;

            @Override
            public void run() {
                BT_TestDelay.setVisibility(isVisible);
                isVisible = isVisible == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE;
                handler.postDelayed(this, prefs.getInt("qstDelai", QST_DELAY));
            }
        };
        handler.postDelayed(questionRunnable, 1000);
    }
}