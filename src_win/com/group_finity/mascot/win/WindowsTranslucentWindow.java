package com.group_finity.mascot.win;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * α値つき画像ウィンドウ.
 * {@link #setImage(com.group_finity.mascot.image.NativeImage)} で設定した {@link WindowsNativeImage} をデスクトップに表示できる.
 * <p/>
 * {@link #setAlpha(int)} で表示するときの濃度も指定できる.
 */
class WindowsTranslucentWindow extends JWindow implements TranslucentWindow {

    public static void main(String[] args) throws IOException {

        WindowsTranslucentWindow win = new WindowsTranslucentWindow();

        win.setBounds(200, 200, 200, 200);
        win.setVisible(true);
        win.setAlwaysOnTop(true);

        BufferedImage img = ImageIO.read(new File("img/shime1.png"));
        WindowsNativeImage wi = new WindowsNativeImage(img);

        win.setImage(wi);
        win.repaint();
    }

    private static final long serialVersionUID = 1L;

    @Override
    public JWindow asJWindow() {
        return this;
    }

    /**
     * α値つき画像を描画する.
     *
     * @param imageHandle ビットマップのハンドル.
     * @param alpha       表示濃度. 0 = まったく表示しない、255 = 完全に表示する.
     */
    private void paint(final WinDef.HBITMAP imageHandle, final int alpha) {

        final WinDef.HWND hWnd = new WinDef.HWND(Native.getComponentPointer(this));

        if (User32.INSTANCE.IsWindowVisible(hWnd)) {

            final int exStyle = User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE);
            if ((exStyle & WinUser.WS_EX_LAYERED) == 0) {
                User32.INSTANCE.SetWindowLong(hWnd, WinUser.GWL_EXSTYLE, exStyle | WinUser.WS_EX_LAYERED);
            }

            // 画像の転送元DCを作成
            final WinDef.HDC clientDC = User32.INSTANCE.GetDC(hWnd);
            final WinDef.HDC memDC = GDI32.INSTANCE.CreateCompatibleDC(clientDC);
            final WinNT.HANDLE oldBmp = GDI32.INSTANCE.SelectObject(memDC, imageHandle);

            User32.INSTANCE.ReleaseDC(hWnd, clientDC);

            // 転送先領域
            final WinDef.RECT windowRect = new WinDef.RECT();
            User32.INSTANCE.GetWindowRect(hWnd, windowRect);

            // 転送
            final WinUser.BLENDFUNCTION bf = new WinUser.BLENDFUNCTION();
            bf.BlendOp = WinUser.AC_SRC_OVER;
            bf.BlendFlags = 0;
            bf.SourceConstantAlpha = (byte) alpha; // 濃度を設定
            bf.AlphaFormat = WinUser.AC_SRC_ALPHA;

            final WinUser.POINT lt = new WinUser.POINT();
            lt.x = windowRect.left;
            lt.y = windowRect.top;
            final WinUser.SIZE size = new WinUser.SIZE();
            size.cx = windowRect.right - windowRect.left;
            size.cy = windowRect.bottom - windowRect.top;
            final WinUser.POINT zero = new WinUser.POINT();
            User32.INSTANCE.UpdateLayeredWindow(hWnd, null, lt, size, memDC, zero, 0, bf, WinUser.ULW_ALPHA);

            // ビットマップは元に戻しておく
            GDI32.INSTANCE.SelectObject(memDC, oldBmp);

            GDI32.INSTANCE.DeleteDC(memDC);
        }

    }

    /**
     * 表示する画像.
     */
    private WindowsNativeImage image;

    /**
     * 表示濃度. 0 = まったく表示しない、255 = 完全に表示する.
     */
    private int alpha = 255;

    @Override
    public String toString() {
        return "LayeredWindow[hashCode=" + hashCode() + ",bounds=" + getBounds() + "]";
    }

    @Override
    public void paint(final Graphics g) {
        if (getImage() != null) {
            // JNI を使用してα値つき画像を描画する.
            paint(getImage().getHandle(), getAlpha());
        }
    }

    private WindowsNativeImage getImage() {
        return this.image;
    }

    public void setImage(final NativeImage image) {
        this.image = (WindowsNativeImage) image;
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(final int alpha) {
        this.alpha = alpha;
    }

    public void updateImage() {
        repaint();
    }

}
