package com.amolg.flutterbarcodescannerexample;

import android.os.Bundle;

import com.amolg.flutterbarcodescanner.FlutterBarcodeScannerPlugin;

import io.flutter.app.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class EmbeddingV1Activity extends FlutterActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        GeneratedPluginRegistrant.registerWith(new FlutterEngine(this));
        FlutterBarcodeScannerPlugin.registerWith(this.registrarFor("com.amolg.flutterbarcodescanner.FlutterBarcodeScannerPlugin"));
    }
}