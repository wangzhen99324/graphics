package com.angzk;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang.StringUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

public class QrCodeBaseUtils {

    /**
     * 编码
     */
    private static final String CHARSET = "utf-8";

    /**
     * 二维码001尺寸
     */
    private static final int QRCODE_SIZE = 172;

    /**
     * LOGO宽度
     */
    private static final int WIDTH = 50;

    /**
     * LOGO高度
     */
    private static final int HEIGHT = 50;

    /**
     * 图片前缀
     */
    public static final String HTTP_PREFIX = "http";

    /**
     * 删除二维码白边
     *
     * @param bitMatrix bitMatrix
     * @return BitMatrix
     */
    public static BitMatrix deleteWhite(BitMatrix bitMatrix) {
        int[] rec = bitMatrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (bitMatrix.get(i + rec[0], j + rec[1])) {
                    resMatrix.set(i, j);
                }
            }
        }
        return resMatrix;
    }

    /**
     * 插入LOGO
     *
     * @param source       二维码图片
     * @param imgPath      LOGO图片地址
     * @param needCompress 是否压缩
     * @param frame        是否去白边
     */
    public static void insertImage(BufferedImage source, String imgPath, boolean needCompress, boolean frame, Integer qrSize) {

        try {

            // 将 logo 转换为 BufferedImage
            BufferedImage logoBufferedImage = imageToBufferedImage(imgPath);

            if (logoBufferedImage == null) {
                return;
            }
            int width = logoBufferedImage.getWidth();
            int height = logoBufferedImage.getHeight();

            // 压缩LOGO
            if (needCompress) {
                if (width > WIDTH) {
                    width = WIDTH;
                }
                if (height > HEIGHT) {
                    height = HEIGHT;
                }
                if (imgPath.startsWith(HTTP_PREFIX)) {
                    logoBufferedImage = Thumbnails.of(new URL(imgPath)).size(width, height).asBufferedImage();
                } else {
                    logoBufferedImage = Thumbnails.of(imgPath).size(width, height).asBufferedImage();
                }
            }

            // 插入LOGO
            Graphics2D graphics = source.createGraphics();

            // 计算 logo 位置.
            int x;
            int y;
            if (frame) {
                x = (qrSize - width - 39) / 2;
                y = (qrSize - height - 39) / 2;
            } else {
                x = (qrSize - width) / 2;
                y = (qrSize - height) / 2;
            }

            handleGran(graphics, logoBufferedImage, x, y, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 绘制.
     *
     * @param graphics      画笔对象
     * @param bufferedImage 将要绘制的图片对象.例如将要绘制到 二维码上的 logo图片的 BufferedImage
     * @param x             绘制横坐标
     * @param y             绘制纵坐标
     * @param width         绘制图片的 宽度
     * @param height        绘制图片的 高度
     */
    private static void handleGran(Graphics2D graphics, BufferedImage bufferedImage, int x, int y, int width, int height) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(bufferedImage, x, y, width, height, null);
        // logoBufferedImage 是 logo对象 xy 是gs两个边的距离，width height 是 logo的大小
        Shape shape = new RoundRectangle2D.Float(x, y, width, height, 8, 8);

        //默认情况下，Graphics
        // 绘图类使用的笔画属性是粗细为1个像素的正方形，而Java2D的Graphics2D类可以调用setStroke()方法设置笔画的属性，如改变线条的粗细、虚实和定义线段端点的形状、风格等
        graphics.setStroke(new BasicStroke(3f));
        graphics.draw(shape);
        graphics.dispose();
    }

    /**
     * 解析二维码
     *
     * @param file 二维码图片
     * @return String
     */
    private static String decode(File file) {
        String resultStr = "";
        try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result;
                Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
                hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
                result = new MultiFormatReader().decode(bitmap, hints);
                resultStr = result.getText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStr;
    }

    /**
     * 解析二维码
     *
     * @param path 二维码图片地址
     * @return String
     */
    public static String decode(String path) {
        return decode(new File(path));
    }

    /**
     * compressPicByQuality 压缩图片,通过压缩图片质量，保持原图大小
     *
     * @param bufferImage 图片BufferedImage
     * @param quality     ：0-1
     * @return byte[]
     */
    public static byte[] compressPicByQuality(BufferedImage bufferImage, float quality) {
        byte[] inByte = null;
        try {

            if (bufferImage == null) {
                return null;
            }
            // 得到指定Format图片的writer
            // 得到迭代器
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            // 得到writer
            ImageWriter writer = iter.next();

            // 得到指定writer的输出参数设置(ImageWriteParam )
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            // 设置可否压缩
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            // 设置压缩质量参数
            iwp.setCompressionQuality(quality);

            iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED);

            ColorModel colorModel = ColorModel.getRGBdefault();
            // 指定压缩时使用的色彩模式
            iwp.setDestinationType(
                    new javax.imageio.ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));

            // 开始打包图片，写入byte[]
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IIOImage iIamge = new IIOImage(bufferImage, null, null);

            // 此处因为ImageWriter中用来接收write信息的output要求必须是ImageOutput
            // 通过ImageIo中的静态方法，得到byteArrayOutputStream的ImageOutput
            writer.setOutput(ImageIO.createImageOutputStream(byteArrayOutputStream));
            writer.write(null, iIamge, iwp);
            inByte = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            System.out.println("write error");
            e.printStackTrace();
        }
        return inByte;
    }

    /**
     * image File 转 BufferedImage 直接使用 ImageIO 可能会使图片染红。
     *
     * @param imaPath 图片地址
     * @return BufferedImage
     */
    public static BufferedImage imageToBufferedImage(String imaPath) {
        Image src;
        BufferedImage img = null;
        if (StringUtils.isNotBlank(imaPath)) {
            try {
                if (imaPath.startsWith(HTTP_PREFIX)) {
                    src = imageToBufferedImage(new URL(imaPath));
                } else {
                    src = Toolkit.getDefaultToolkit().getImage(imaPath);
                }
                if (src != null) {
                    img = toBufferedImage(src);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return img;
    }

    /**
     * image File 转 BufferedImage 直接使用 ImageIO 可能会使图片染红。
     *
     * @param url url
     * @return BufferedImage
     */
    public static BufferedImage imageToBufferedImage(URL url) {
        Image src = Toolkit.getDefaultToolkit().getImage(url);
        BufferedImage img = null;
        if (src != null) {
            img = toBufferedImage(src);
        }
        return img;
    }

    /**
     * image File 转 BufferedImage 的依赖函数
     *
     * @param image image
     * @return BufferedImage
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }


}
