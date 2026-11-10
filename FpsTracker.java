package com.fpsmeter;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FpsTracker {
    private static final long WINDOW_NANOS = 30L * 1000000000L;
    private static final long UPDATE_NANOS = 500L * 1000000L;
    private static final long MAX_FRAME_NANOS = 2L * 1000000000L;

    public static volatile boolean visible = true;
    public static volatile boolean resetRequested = false;

    private final long[] buf = new long[20000];
    private int head = 0, count = 0;
    private long sumDur = 0;

    private long last = 0;
    private long totalFrames = 0, totalNanos = 0;
    private int framesSince = 0;
    private long nanosSince = 0;

    private int nowFps = 0, avgWindow = 0, avgTotal = 0, low1 = 0;
    private long shownSeconds = 0;

    private void doReset() {
        head = 0;
        count = 0;
        sumDur = 0;
        last = 0;
        totalFrames = 0;
        totalNanos = 0;
        framesSince = 0;
        nanosSince = 0;
        nowFps = avgWindow = avgTotal = low1 = 0;
        shownSeconds = 0;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (resetRequested) {
            resetRequested = false;
            doReset();
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.currentScreen != null) {
            last = 0;
            return;
        }
        long now = System.nanoTime();
        if (last == 0) {
            last = now;
            return;
        }
        long dt = now - last;
        last = now;
        if (dt <= 0 || dt > MAX_FRAME_NANOS) return;

        if (count == buf.length) {
            sumDur -= buf[head];
            head = (head + 1) % buf.length;
            count--;
        }
        buf[(head + count) % buf.length] = dt;
        count++;
        sumDur += dt;
        while (sumDur > WINDOW_NANOS && count > 1) {
            sumDur -= buf[head];
            head = (head + 1) % buf.length;
            count--;
        }

        totalFrames++;
        totalNanos += dt;
        framesSince++;
        nanosSince += dt;

        if (nanosSince >= UPDATE_NANOS) {
            nowFps = (int) Math.round(framesSince * 1e9 / nanosSince);
            framesSince = 0;
            nanosSince = 0;
            avgWindow = (int) Math.round(count * 1e9 / sumDur);
            avgTotal = (int) Math.round(totalFrames * 1e9 / totalNanos);
            shownSeconds = totalNanos / 1000000000L;

            long[] tmp = new long[count];
            for (int i = 0; i < count; i++) tmp[i] = buf[(head + i) % buf.length];
            Arrays.sort(tmp);
            int n = Math.max(1, count / 100);
            long sum = 0;
            for (int i = count - n; i < count; i++) sum += tmp[i];
            double avgWorst = sum / (double) n;
            low1 = (int) Math.round(1e9 / avgWorst);
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (!visible) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.showDebugInfo) return;

        String l1 = "FPS agora: " + nowFps;
        String l2 = "Média 30s: " + avgWindow;
        String l3 = "Média total: " + avgTotal + " (" + (shownSeconds / 60) + ":" + String.format("%02d", shownSeconds % 60) + ")";
        String l4 = "1% low: " + low1;

        ScaledResolution res = e.getResolution();
        int w = 0;
        for (String s : new String[]{l1, l2, l3, l4}) w = Math.max(w, mc.fontRenderer.getStringWidth(s));
        int x = res.getScaledWidth() - w - 4;
        int y = 4;
        mc.fontRenderer.drawStringWithShadow(l1, x, y, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow(l2, x, y + 10, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow(l3, x, y + 20, 0xFFFF55);
        mc.fontRenderer.drawStringWithShadow(l4, x, y + 30, 0xAAAAAA);
    }
}
