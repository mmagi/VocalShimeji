import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: SMagi
 * Date: 13-11-24
 * Time: 下午9:38
 * To change this template use File | Settings | File Templates.
 */
public class ImageEdgeRepair {
    public static void repair(File dir, File todir) throws IOException {

        todir.mkdirs();

        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".png");
            }
        });
        for (File file : files) {
            System.out.println("read:" + file.getAbsolutePath());

            BufferedImage image = ImageIO.read(file);

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    if (argb >>> 24 < 2) {
                        argb = 0;
                    } else if (argb >>> 24 < 100) {
                        Color c = new Color(argb, true);
                        int a = c.getAlpha();
                        int g = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                        int A = a * (255 - g) / 255;
                        argb = A << 24;
                    }
                    image.setRGB(x, y, argb);
                }
            }

            System.out.println("write:" + new File(todir, file.getName()).getAbsolutePath());
            ImageIO.write(image, "png", new File(todir, file.getName()));
        }
    }

    public static void main(String[] args)
            throws IOException {


        repair(new File("..\\Hijikata_Toushirou\\raw\\image"),
               new File("..\\Hijikata_Toshirou\\Resources\\vocalshimeji\\theme\\hijikata_toshirou\\image"));

        repair(new File("..\\Kagura\\raw\\image"),
               new File("..\\Kagura\\Resources\\vocalshimeji\\theme\\kagura\\image"));

    }
}