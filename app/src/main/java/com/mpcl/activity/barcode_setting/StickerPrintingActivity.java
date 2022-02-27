package com.mpcl.activity.barcode_setting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.tscdll.TSCActivity;
import com.mpcl.R;
import com.mpcl.custom.RegularEditText;

public class StickerPrintingActivity extends AppCompatActivity {
    private ImageView download,print;
    private RegularEditText macAddress;
    TSCActivity TscDll = new TSCActivity();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_printing);
        download = findViewById(R.id.iv_download);
        macAddress = findViewById(R.id.tv_trip_sheet_no);
        print = findViewById(R.id.iv_print);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printSticker();
            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printSticker();
            }
        });

    }

    private void printSticker() {
        try {
            TscDll.openport(macAddress.getText().toString()); //BT
            //TscDll.openport(etText1.getText().toString(), 9100); //NET

            //TscDll.setup(paper_width, paper_height, speed, density, sensor, sensor_distance, sensor_offset);
            TscDll.sendcommand("SIZE 75 mm, 50 mm\r\n");
            //TscDll.sendcommand("GAP 2 mm, 0 mm\r\n");//Gap media
            //TscDll.sendcommand("BLINE 2 mm, 0 mm\r\n");//blackmark media

            TscDll.sendcommand("SPEED 4\r\n");
            TscDll.sendcommand("DENSITY 12\r\n");
            TscDll.sendcommand("CODEPAGE UTF-8\r\n");
            TscDll.sendcommand("SET TEAR ON\r\n");
            TscDll.sendcommand("SET COUNTER @1 1\r\n");
            TscDll.sendcommand("@1 = \"0001\"\r\n");

            TscDll.clearbuffer();
            TscDll.sendcommand("TEXT 100,300,\"ROMAN.TTF\",0,12,12,@1\r\n");
            TscDll.sendcommand("TEXT 100,400,\"ROMAN.TTF\",0,12,12,\"TEST FONT\"\r\n");
            TscDll.barcode(100, 100, "128", 100, 1, 0, 3, 3, "123456789");
            TscDll.printerfont(100, 250, "3", 0, 1, 1, "987654321");
            TscDll.printlabel(2, 1);

            TscDll.closeport(5000);

        }
        catch (Exception ex)
        {
        }
    }
}