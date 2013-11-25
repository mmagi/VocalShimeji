package com.group_finity.mascot;

import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.jogamp.common.jvm.JNILibLoaderBase;
import com.sun.jna.Platform;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * ネイティブ環境へのアクセスを提供する.
 * {@link #getInstance()} は実行環境によって Windows 用あるいは汎用のサブクラスのインスタンスを返す.
 *
 * @author Yuki
 */
public abstract class NativeFactory {

    private static final NativeFactory instance;

    /**
     * サブクラスのインスタンスを作成しておく.
     */
    static {

        final String basePackage = NativeFactory.class.getName().substring(0, NativeFactory.class.getName().lastIndexOf('.'));

        final String subPackage = Platform.isWindows() ? "win" : "mac";

        try {
            @SuppressWarnings("unchecked")
            final Class<? extends NativeFactory> impl = (Class<? extends NativeFactory>) Class.forName(basePackage + "." + subPackage + ".NativeFactoryImpl");

            instance = impl.newInstance();

        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final InstantiationException e) {
            throw new RuntimeException(e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static void LoadNativeLib(){
        if (Platform.isWindows()) LoadWinNativeLib(Platform.is64Bit());
        else if (Platform.isMac()) LoadMacNativeLib();
    }
    private static void LoadWinNativeLib(boolean x64) {
        //全部从外部加载，否则某些操作系统下释放到临时文件夹里的dll不允许加载
        String dir = x64 ? "lib/win32-x86_64" : "lib/win32-x86";
        File jnaLib = new File(new File(dir), System.mapLibraryName("jnidispatch"));
        if (jnaLib.exists()) {
            System.setProperty("jna.boot.library.path", dir);
        } else {
            throw new RuntimeException(jnaLib.getAbsolutePath() + "文件未找到.");
        }
        System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
        JNILibLoaderBase.addLoaded("gluegen-rt");
        System.load(new File(dir + "/libgluegen-rt.jnilib").getAbsolutePath());
        JNILibLoaderBase.addLoaded("joal");
        System.load(new File(dir + "/libjoal.jnilib").getAbsolutePath());
        JNILibLoaderBase.addLoaded("OpenAL32");
        System.load(new File(dir + "/libopenal.dylib").getAbsolutePath());
    }

    private static final void LoadMacNativeLib() {
        System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
        JNILibLoaderBase.addLoaded("gluegen-rt");
        System.load(new File("lib/macosx/libgluegen-rt.jnilib").getAbsolutePath());
        JNILibLoaderBase.addLoaded("joal");
        System.load(new File("lib/macosx/libjoal.jnilib").getAbsolutePath());
        JNILibLoaderBase.addLoaded("OpenAL32");
        System.load(new File("lib/macosx/libopenal.dylib").getAbsolutePath());
    }

    public static NativeFactory getInstance() {
        return instance;
    }

    /**
     * 環境オブジェクトを取得する.
     *
     * @return 環境オブジェクト.
     */
    public abstract Environment getEnvironment();

    public abstract NativeImage newNativeImage(BufferedImage src);

    public abstract TranslucentWindow newTransparentWindow();
}
