package com.angzk;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import net.coobird.thumbnailator.Thumbnails;

public class QrCodeBaseUtils {

    private static final String CHARSET = "utf-8";

    // 二维码001尺寸
    private static final int QRCODE_SIZE = 172;

    // 二维码002尺寸
    private static final int QRCODES_OTHER_SIZE = 200;

    // LOGO宽度
    private static final int WIDTH = 50;
    // LOGO高度
    private static final int HEIGHT = 50;

    /**
     * 删除二维码白边
     * 
     * @param bitMatrix
     * @return
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

    /*
     * 插入LOGO
     * 
     * @param source 二维码图片
     * 
     * @param imgPath LOGO图片地址
     * 
     * @param needCompress 是否压缩
     * 
     * @param frame 是否去白边
     * 
     * @param type
     * 
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private static void insertImage(BufferedImage source, String imgPath, boolean needCompress, boolean frame, int type)
        throws Exception {
        BufferedImage logoBufferedImage = null;
        if (StringUtils.isNotBlank(imgPath)) {
            if (imgPath.startsWith("http")) {
                logoBufferedImage = ImageIO.read(new URL(imgPath));
            } else {
                logoBufferedImage = ImageIO.read(new File(imgPath));
            }
        }
        if (logoBufferedImage == null) {
            return;
        }
        int width = 0;
        int height = 0;
        if (imgPath.endsWith("/132")) {
            width = 132;
            height = 132;
        } else {
            width = logoBufferedImage.getWidth();
            height = logoBufferedImage.getHeight();
        }
        // 压缩LOGO
        if (needCompress) {
            if (width > WIDTH) {
                width = WIDTH;
            }
            if (height > HEIGHT) {
                height = HEIGHT;
            }
            if (imgPath.startsWith("http")) {
                logoBufferedImage = Thumbnails.of(new URL(imgPath)).size(width, height).asBufferedImage();
            } else {
                logoBufferedImage = Thumbnails.of(imgPath).size(width, height).asBufferedImage();
            }
        }
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = 0;
        int y = 0;
        if (frame) {
            x = (QRCODE_SIZE - width - 39) / 2;
            y = (QRCODE_SIZE - height - 39) / 2;
        } else {
            if (type == 0) {
                x = (QRCODE_SIZE - width) / 2;
                y = (QRCODE_SIZE - height) / 2;

            } else if (type == 1) {
                x = (QRCODES_OTHER_SIZE - width) / 2;
                y = (QRCODES_OTHER_SIZE - height) / 2;
            }
        }
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph.drawImage(logoBufferedImage, x, y, width, height, null);
        // logoBufferedImage 是 logo对象 xy 是gs两个边的距离，width height 是 logo的大小
        Shape shape = new RoundRectangle2D.Float(x, y, width, height, 8, 8);
        // 边框
        // TODO 默认情况下，Graphics
        // 绘图类使用的笔画属性是粗细为1个像素的正方形，而Java2D的Graphics2D类可以调用setStroke()方法设置笔画的属性，如改变线条的粗细、虚实和定义线段端点的形状、风格等
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    /**
     * 解析二维码
     * 
     * @param file
     *            二维码图片
     * @return
     * @throws Exception
     */
    public static String decode(File file) throws Exception {
        BufferedImage image;
        image = ImageIO.read(file);
        if (image == null) {
            return null;
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
        result = new MultiFormatReader().decode(bitmap, hints);
        String resultStr = result.getText();
        return resultStr;
    }

    /**
     * 解析二维码
     * 
     * @param path
     *            二维码图片地址
     * @return
     * @throws Exception
     */
    public static String decode(String path) throws Exception {
        return decode(new File(path));
    }

    /**
     *
     * compressPicByQuality 压缩图片,通过压缩图片质量，保持原图大小
     *
     * @param bufferImage
     * @param quality
     *            ：0-1
     * @return
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
            ImageWriter writer = (ImageWriter)iter.next();

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
            System.out.println("write errro");
            e.printStackTrace();
        }
        return inByte;
    }

    /**
     * image File 转 BufferedImage 直接使用 ImageIO 可能会使图片染红。
     * 
     * @param imaPath
     * @return
     * @throws Exception
     */
    public static BufferedImage imageToBufferedImage(String imaPath) throws Exception {
        if (StringUtils.isNotBlank(imaPath)) {
            Image src = null;
            BufferedImage img = null;
            if (imaPath.startsWith("http")) {
                src = Toolkit.getDefaultToolkit().getImage(new URL(imaPath));
            } else {
                src = Toolkit.getDefaultToolkit().getImage(imaPath);
            }
            if (src != null) {
                img = toBufferedImage(src);
            }
            return img;
        } else {
            return null;
        }
    }

    /**
     * image File 转 BufferedImage 直接使用 ImageIO 可能会使图片染红。
     * 
     * @param URL
     * @return
     * @throws Exception
     */
    public static BufferedImage imageToBufferedImage(URL url) throws Exception {
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
     * @param image
     * @return
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
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

    /*
     * 插入LOGO
     * 
     * @param source 二维码图片
     * 
     * @param imgUrl LOGOURL
     * 
     * @param needCompress 是否压缩
     * 
     * @param frame 是否去白边
     * 
     * @param type
     * 
     * @throws Exception
     */
    public static void insertImage(BufferedImage source, URL imgUrl, boolean needCompress, boolean frame,
        int qrCodeSize) throws Exception {

        if (qrCodeSize == 0) {
            qrCodeSize = QRCODE_SIZE;
        }

        BufferedImage logoBufferedImage = ImageIO.read(imgUrl);
        if (logoBufferedImage == null) {
            return;
        }
        int width = 0;
        int height = 0;
        width = logoBufferedImage.getWidth();
        height = logoBufferedImage.getHeight();
        if (needCompress) {
            if (qrCodeSize == 136) {
                if (width > 33) {
                    width = 33;
                }
                if (height > 33) {
                    height = 33;
                }
            } else {
                if (width > WIDTH) {
                    width = WIDTH;
                }
                if (height > HEIGHT) {
                    height = HEIGHT;
                }
            }
            logoBufferedImage = Thumbnails.of(imgUrl).size(width, height).asBufferedImage();
        }
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = 0;
        int y = 0;
        if (frame) {
            x = (QRCODE_SIZE - width - 39) / 2;
            y = (QRCODE_SIZE - height - 39) / 2;
        } else {
            x = (qrCodeSize - width) / 2;
            y = (qrCodeSize - height) / 2;
        }
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph.drawImage(logoBufferedImage, x, y, width, height, null);
        // logoBufferedImage 是 logo对象 xy 是gs两个边的距离，width height 是 logo的大小
        Shape shape = new RoundRectangle2D.Float(x, y, width, height, 8, 8);
        // 边框
        // TODO 默认情况下，Graphics
        // 绘图类使用的笔画属性是粗细为1个像素的正方形，而Java2D的Graphics2D类可以调用setStroke()方法设置笔画的属性，如改变线条的粗细、虚实和定义线段端点的形状、风格等
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    /*
     * 插入LOGO
     * 
     * @param source 二维码图片
     * 
     * @param imgFile LOGO-图片（File）
     * 
     * @param needCompress 是否压缩
     * 
     * @param frame 是否去白边
     * 
     * @param type
     * 
     * @throws Exception
     */
    public static void insertImage(BufferedImage source, File imgFile, boolean needCompress, boolean frame,
        int qrCodeSize) throws Exception {
        BufferedImage logoBufferedImage = null;
        logoBufferedImage = ImageIO.read(imgFile);
        if (logoBufferedImage == null) {
            return;
        }
        int width = logoBufferedImage.getWidth();
        int height = logoBufferedImage.getHeight();
        // 压缩LOGO
        if (needCompress) {
            if (needCompress) {
                if (qrCodeSize == 136) {
                    if (width > 33) {
                        width = 33;
                    }
                    if (height > 33) {
                        height = 33;
                    }
                } else {
                    if (width > WIDTH) {
                        width = WIDTH;
                    }
                    if (height > HEIGHT) {
                        height = HEIGHT;
                    }
                }
                logoBufferedImage = Thumbnails.of(imgFile).size(width, height).asBufferedImage();
            }
        }
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = 0;
        int y = 0;
        if (frame) {
            x = (QRCODE_SIZE - width - 39) / 2;
            y = (QRCODE_SIZE - height - 39) / 2;
        } else {
            if (qrCodeSize == 0) {
                qrCodeSize = QRCODE_SIZE;
            }
            x = (qrCodeSize - width) / 2;
            y = (qrCodeSize - height) / 2;
        }
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph.drawImage(logoBufferedImage, x, y, width, height, null);
        // logoBufferedImage 是 logo对象 xy 是gs两个边的距离，width height 是 logo的大小
        Shape shape = new RoundRectangle2D.Float(x, y, width, height, 8, 8);
        // 边框
        // TODO 默认情况下，Graphics
        // 绘图类使用的笔画属性是粗细为1个像素的正方形，而Java2D的Graphics2D类可以调用setStroke()方法设置笔画的属性，如改变线条的粗细、虚实和定义线段端点的形状、风格等
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }
}
