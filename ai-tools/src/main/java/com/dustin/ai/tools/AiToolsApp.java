package com.dustin.ai.tools;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.SolonMain;

@Import(scanPackages = "com.dustin.ai")
@SolonMain
public class AiToolsApp {
    public static void main(String[] args) {
        Solon.start(AiToolsApp.class, args);
    }
}