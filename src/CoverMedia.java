
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CoverMedia {

    public String coverMedia_directory;
    public BufferedImage coverMedia;
    public int imageHeight;
    public int imageWidth;
    public int totalPixel;

    public CoverMedia(String coverMedia_directory) throws IOException {
        this.coverMedia_directory = coverMedia_directory;
        File f = new File(coverMedia_directory);
        this.coverMedia = ImageIO.read(f);
        this.imageHeight = this.coverMedia.getHeight();
        this.imageWidth = this.coverMedia.getWidth();
        this.totalPixel = this.imageHeight * this.imageWidth;
    }

    public int[][][] getPixels() {
        int[][][] pixel = new int[coverMedia.getHeight()][coverMedia.getWidth()][3];
        for (int i = 0; i < coverMedia.getHeight(); i++) {
            for (int j = 0; j < coverMedia.getWidth(); j++) {
                int pix = coverMedia.getRGB(j, i);
                Color c = new Color(pix);
                pixel[j][i][0] = c.getRed();
                pixel[j][i][1] = c.getGreen();
                pixel[j][i][2] = c.getBlue();
            }
        }
        return pixel;
    }

    public String getBinaryPixel() {

        String string_of_pixel = "";

        int[][][] pixel = this.getPixels();
        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageWidth; i++) {
                for (int k = 0; k < 3; k++) {
                    string_of_pixel += this.decimalTo_Binary(pixel[j][i][k]) + " ";
                }
            }
        }
        return string_of_pixel;
    }

    public int getTotalPixel() {
        this.totalPixel = this.imageHeight * this.imageWidth;
        return totalPixel;
    }

    public String decimalTo_Binary(int n) {
        String res = Integer.toBinaryString(n);
        while (res.length() != 8) {
            res = "0" + res;
        }
        return res;
    }

    public int bin_to_decimal(String bin) {
        return Integer.parseInt(bin, 2);
    }

    public boolean ImageType() throws Exception {
        boolean res;
        if (this.coverMedia_directory.contains(".bmp"));
        {
            res = true;
        }
        return res;
    }
}
