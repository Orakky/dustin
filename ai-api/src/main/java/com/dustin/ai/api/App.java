package com.dustin.ai.api;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.SolonMain;

@Import(scanPackages = "com.dustin.ai")
@SolonMain
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args);
    }
}