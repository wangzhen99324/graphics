package com.angzk;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * 二维码工具类
 *
 * @author Angzk
 * @date 2019年8月7日
 */
public final class QrCodeGraphicsUtils {

    private static final String CHARSET = "utf-8";

    private static final int QR_CODE_SIZE = 172;

    /**
     * 生成二维码
     *
     * @param linkUrl          二维码地址
     * @param hasFrame         是否删除白边 true:删除 false 不删除.
     * @param logoStatus       是否插入logo
     * @param logoPath         logoPath
     * @param logoNeedCompress 是否压缩 logo
     * @param qrCodeSize       二维码的尺寸
     * @return BufferedImage
     */
    public static BufferedImage createQrCode(String linkUrl, boolean hasFrame, boolean logoStatus, String logoPath,
                                             boolean logoNeedCompress, int qrCodeSize) {
        BufferedImage image = null;
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
            hints.put(EncodeHintType.MARGIN, 1);
            if (qrCodeSize == 0) {
                qrCodeSize = QR_CODE_SIZE;
            }
            BitMatrix bitMatrix =
                    new MultiFormatWriter().encode(linkUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);
            // 删除白边
            if (hasFrame) {
                bitMatrix = QrCodeBaseUtils.deleteWhite(bitMatrix);
            }
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            if (logoStatus) {
                if (StringUtils.isNotBlank(logoPath)) {
                    QrCodeBaseUtils.insertImage(image, logoPath, logoNeedCompress, hasFrame, qrCodeSize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 画头像
     *
     * @param graphics    画笔对象
     * @param avatarUrl   头像的地址
     * @param bufferImage 源Buffered对象. 例如要将头像 画在背景板上, 这里就是 背景板的buffered
     * @param x           横坐标
     * @param y           纵坐标
     * @return Graphics2D
     */
    public static Graphics2D drawAvatar(Graphics2D graphics, String avatarUrl, BufferedImage bufferImage, int x, int y) {
        try {
            // 绘制头像 。
            BufferedImage logoBufferImage;

            // 头像缩放为 70 * 70
            if (avatarUrl.startsWith(QrCodeBaseUtils.HTTP_PREFIX)) {
                logoBufferImage = Thumbnails.of(new URL(avatarUrl)).size(70, 70).asBufferedImage();
            } else {
                logoBufferImage = Thumbnails.of(new File(avatarUrl)).size(70, 70).asBufferedImage();
            }
            int logoHeight = logoBufferImage.getHeight();

            // 图片变圆
            int border = 2;
            Ellipse2D.Double shapeEll =
                    new Ellipse2D.Double(x + border, y + border, logoHeight - border * 2, logoHeight - border * 2);
            // 抗锯齿
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setClip(shapeEll);

            // 绘制头像
            graphics.drawImage(logoBufferImage, x, y, null);

            // 为了防止 头像圆角之后锯齿问题。需要在头像周围画一个白框覆盖即可。
            graphics = bufferImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int border2 = 2;
            Stroke s = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            graphics.setStroke(s);
            graphics.setColor(Color.WHITE);
            // 绘制头像边框
            graphics.drawOval(x + border, y + border, logoHeight - border2 * 2, logoHeight - border2 * 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return graphics;
    }

    /**
     * 绘制海报文字(单行文字)
     *
     * @param graphics 画笔对象
     * @param text     文字
     * @param width    横坐标
     * @param height   纵坐标
     * @return Graphics2D
     */
    public static Graphics2D drawText(Graphics2D graphics, String text, int width, int height, Color color) {
        // 抗锯齿(先画一层阴影,再在阴影上画文字)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setPaint(new Color(0, 0, 0, 64));
        graphics.drawString(text, width, height);
        graphics.setPaint(color);
        // 绘制 文字
        graphics.drawString(text, width, height);
        return graphics;
    }

    /**
     * 绘制海报文字(换行)
     *
     * @param graphics     画笔
     * @param text         文本
     * @param width        位置：x
     * @param height       位置：y
     * @param lineHeight   单行行高
     * @param lineWidth    单行行宽
     * @param color        文本颜色
     * @param limitLineNum 限制行数
     * @return int 行数
     */
    public static int drawTextNewLine(Graphics2D graphics, String text, int width, int height, int lineHeight,
                                      int lineWidth, Color color, int textSize, int limitLineNum, int backgroundWidth) {
        Font font = new Font("微软雅黑", Font.PLAIN, textSize);
        graphics.setFont(font);
        graphics.setPaint(color);
        FontRenderContext frc = graphics.getFontRenderContext();
        graphics.getFontRenderContext();
        Rectangle2D stringBounds = font.getStringBounds(text, frc);
        double fontWidth = stringBounds.getWidth();
        List<String> lineList = new ArrayList<String>();
        int lineCharCountSub = 0;

        // 不满一行
        if (fontWidth <= lineWidth) {
            lineList.add(text);
            width = (backgroundWidth - Double.valueOf(fontWidth).intValue()) / 2 + backgroundWidth / 2 + 57;
        } else {
            width = (backgroundWidth - lineWidth) / 2 + backgroundWidth / 2 + 57;
            // 输出文本宽度,这里就以画布宽度作为文本宽度测试
            int textWidth = lineWidth;
            // 文本长度是文本框长度的倍数
            double bs = fontWidth / textWidth;
            // 每行大概字数
            int lineCharCount = (int) Math.ceil(text.length() / bs);
            lineCharCountSub = lineCharCount;
            int beginIndex = 0;
            while (beginIndex < text.length()) {
                int endIndex = beginIndex + lineCharCount;
                if (endIndex >= text.length()) {
                    endIndex = text.length();
                }
                String lineStr = text.substring(beginIndex, endIndex);
                Rectangle2D tempStringBounds = font.getStringBounds(lineStr, frc);
                int tags = 1;
                while (tempStringBounds.getWidth() > textWidth) {
                    lineStr = lineStr.substring(0, lineStr.length() - tags);
                    tempStringBounds = font.getStringBounds(lineStr, frc);
                }
                lineList.add(lineStr);
                beginIndex = beginIndex + lineStr.length();
            }
        }

        // Color.BLACK 。字体颜色
        graphics.setPaint(color);
        if (lineHeight == 0) {
            lineHeight = 35;
        }
        int lineNum = lineList.size();
        if (limitLineNum != 0 && lineNum > limitLineNum) {
            lineNum = limitLineNum;
        }
        // 绘制 换行文字
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < lineNum; i++) {
            String lineStr = lineList.get(i);
            if (lineNum >= 2 && i == lineNum - 1) {
                if (lineStr.length() >= lineCharCountSub - 3) {
                    lineStr = lineStr.substring(0, lineStr.length() - 2) + "...";
                }
            }
            graphics.drawString(lineStr, width, height + (i + 1) * lineHeight);
            graphics.drawString(lineStr, width, height + (i + 1) * lineHeight);
        }
        return lineNum;
    }

    /**
     * 保存图片
     *
     * @param bufferImage   画布对象
     * @param scale         缩放 (0.0~1.0)
     * @param outFormat     输出格式(PNG JPG)
     * @param outPutQuality 输出品质(0.0d ~ 1.0d)
     * @param pngPath       输出路径
     */
    public static void savePic(BufferedImage bufferImage, int scale, String outFormat, double outPutQuality,
                               String pngPath) {
        try {
            if (StringUtils.isNotBlank(pngPath) && bufferImage != null) {
                File file = new File(pngPath);
                // Google 工具包 输出文件
                Thumbnails.of(bufferImage).scale(scale).outputFormat(outFormat).outputQuality(outPutQuality)
                        .toFile(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
