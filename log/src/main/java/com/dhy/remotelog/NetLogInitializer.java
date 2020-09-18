package com.dhy.remotelog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

public class NetLogInitializer implements Initializer<NetLogKit> {

    @NonNull
    @Override
    public NetLogKit create(@NonNull Context context) {
        return NetLogKit.kit;
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
