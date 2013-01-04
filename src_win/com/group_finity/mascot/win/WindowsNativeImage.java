package com.group_finity.mascot.win;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.win.jna.BITMAP;
import com.group_finity.mascot.win.jna.GDI32Ex;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

/**
 * {@link WindowsTranslucentWindow} に使用可能なα値つき画像.
 * <p/>
 * {@link WindowsTranslucentWindow} に使用できるのは Windows ビットマップだけなので、
 * 既存の {@link BufferedImage} から Windows ビットマップにピクセルをコピーする.
 */
class WindowsNativeImage implements NativeImage {

    /**
     * Windows ビットマップを作成する.
     *
     * @param width  ビットマップの横幅.
     * @param height ビットマップの高さ.
     * @return 作成したビットマップのハンドル.
     */
    private static WinDef.HBITMAP createNative(final int width, final int height) {

        final WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biSize = 40;
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;

        return GDI32Ex.INSTANCE.CreateDIBSection(null, bmi, WinGDI.DIB_RGB_COLORS, null, null, 0);
    }

    /**
     * {@link BufferedImage} の内容をビットマップに反映させる.
     *
     * @param nativeHandle ビットマップのハンドル.
     * @param rgb          画像のARGB値.
     */
    private static void flushNative(final WinDef.HBITMAP nativeHandle, final int[] rgb) {

        final BITMAP bmp = new BITMAP();
        GDI32Ex.INSTANCE.GetObjectW(nativeHandle, bmp.size(), bmp);

        // ピクセルレベルでコピーする.
        final int width = bmp.bmWidth;
        final int height = bmp.bmHeight;
        final int destPitch = ((bmp.bmWidth * bmp.bmBitsPixel) + 31) / 32 * 4;
        int destIndex = destPitch * (height - 1);
        int srcIndex = 0;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // UpdateLayeredWindow と Photoshop は相性が悪いらしい
                // UpdateLayeredWindow はRGB値が FFFFFF だとα値を無視してしまうバグがあり、
                // Photoshop はα値が0なところはRGB値を 0 にする特性がある.

                bmp.bmBits.setInt(destIndex + x * 4, (rgb[srcIndex] & 0xFF000000) == 0 ? 0 : rgb[srcIndex]);

                ++srcIndex;
            }

            destIndex -= destPitch;
        }

    }

    /**
     * Windows ビットマップを開放する.
     *
     * @param nativeHandle ビットマップのハンドル.
     */
    private static void freeNative(final WinDef.HBITMAP nativeHandle) {
        GDI32Ex.INSTANCE.DeleteObject(nativeHandle);
    }

    /**
     * Java イメージオブジェクト.
     */
    private final BufferedImage managedImage;

    /**
     * Windows ビットマップハンドル.
     */
    private final WinDef.HBITMAP nativeHandle;

    /**
     * ARGB値の転送に使用するバッファ.
     */
    private final int[] rgb;

    public WindowsNativeImage(final BufferedImage image) {
        this.managedImage = image;
        this.nativeHandle = createNative(this.getManagedImage().getWidth(), this.getManagedImage().getHeight());
        this.rgb = new int[this.getManagedImage().getWidth() * this.getManagedImage().getHeight()];
        update();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        freeNative(this.getNativeHandle());
    }

    /**
     * 画像への変更を Windows ビットマップに反映させる.
     */
    public void update() {

        this.getManagedImage().getRGB(0, 0, this.getManagedImage().getWidth(), this.getManagedImage().getHeight(), this.getRgb(), 0, this.getManagedImage().getWidth());

        flushNative(this.getNativeHandle(), this.getRgb());

    }

    public void flush() {
        this.getManagedImage().flush();
    }

    public WinDef.HBITMAP getHandle() {
        return this.getNativeHandle();
    }

    public Graphics getGraphics() {
        return this.getManagedImage().createGraphics();
    }

    public int getHeight() {
        return this.getManagedImage().getHeight();
    }

    public int getWidth() {
        return this.getManagedImage().getWidth();
    }

    public int getHeight(final ImageObserver observer) {
        return this.getManagedImage().getHeight(observer);
    }

    public Object getProperty(final String name, final ImageObserver observer) {
        return this.getManagedImage().getProperty(name, observer);
    }

    public ImageProducer getSource() {
        return this.getManagedImage().getSource();
    }

    public int getWidth(final ImageObserver observer) {
        return this.getManagedImage().getWidth(observer);
    }

    private BufferedImage getManagedImage() {
        return this.managedImage;
    }

    private WinDef.HBITMAP getNativeHandle() {
        return this.nativeHandle;
    }

    private int[] getRgb() {
        return this.rgb;
    }

}
