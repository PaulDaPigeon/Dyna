package com.example.dyna;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PeakLoadLiveData extends BaseLiveDataView {

    Profile profile;
    boolean isHistorical = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.peak_load_live_data);
        lineChart = findViewById(R.id.lineChartPeakData);
        isHistorical= getIntent().getBooleanExtra("historical",false);
        if(isHistorical) {
            timeLimit = session.getSesionLength();
            displayChart();
        } else {
            session.sessionType=SessionType.PEAK_LOAD;
            timeLimit = 5000;
        }
        //Probably add in base view
        profile = (Profile)getIntent().getSerializableExtra("profile");
        initializeButtons();
    }

    @Override
    public void updateStats() {
        ((TextView)findViewById(R.id.txtPeakMax)).setText(String.valueOf(session.sessionMax));
    }
    @Override
    public void displayChart(){
        //TODO: If units are KG, we need a different formula, one that maxes out at 300
        // https://mycurvefit.com/
        YAxis leftAxis = lineChart.getAxisLeft();
        float x = session.sessionMax/100f;
        float limit = (float)(4.641323f*Math.pow(x,0.7529087));
        leftAxis.setAxisMaximum(limit);
        leftAxis.setAxisMinimum(0f);
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(limit);

        LimitLine llPlotMax = new LimitLine(session.sessionMax/100f, "Max"); // 10f is the Y value, "Limit" is the label

        llPlotMax.setLineWidth(2f); // Set line width
        llPlotMax.setLineColor(Color.GREEN); // Set line color
        llPlotMax.enableDashedLine(10f, 10f, 0f); // Optional: dashed line
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(llPlotMax);
        if(isHistorical) {
            super.displayHistoricalChart();
        } else {
            super.displayChart();
        }

    }
    public void initializeButtons() {
        if(!getIntent().getBooleanExtra("historical",false)) {
            findViewById(R.id.btnPeakStart).setOnClickListener(view -> {
                dc.startCollecting();
            });
            findViewById(R.id.btnPeakStop).setOnClickListener(view -> {
                Log.d("stop", "scan stopped");
                dc.stopCollecting();
            });
            findViewById(R.id.btnPeakSave).setOnClickListener(view -> {
                Log.d("Save", "Saving");
                dc.stopCollecting();
                FileManager fm = new FileManager(this);

                session.name = "Peak " + System.currentTimeMillis()/1000;
                fm.saveSession(session, profile);
            });
        }else {
            //hide start, stop, save buttons
            //show back/delete/export buttons
        }
    }
}