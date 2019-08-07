package com.angzk;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * 画笔工具类
 * 
 * @author Angzk
 * @date 2019年8月7日
 */
public class GraphicsUtils {
    /**
     * 项目路径。
     */
    private static final String filePath = "F:/code/code-kjx/graphics/src/main/resources/images/";

    /**
     * 代码中的 坐标 依据UI 切图的 像素值。 请根据个人需要调整
     * 
     * @param linkUrl
     *            分享链接
     * @param logoStatus
     *            是否加logo
     * @param logoPath
     *            logo地址
     * @param backgroundUrl
     *            模板地址
     * @param spuPicUrl
     *            spu 主图
     * @param spuLeaguerPrice
     *            V price
     * @param spuPrice
     *            price
     * @param spuName
     *            spuName
     * @throws Exception
     */
    public static void createPosterByRedTemplate(String linkUrl, boolean logoStatus, String logoPath,
        String backgroundUrl, String spuPicUrl, String spuLeaguerPrice, String spuPrice, String spuName)
        throws Exception {
        // 二维码对象
        BufferedImage qrCodeImage = QrCodeGraphicsUtils.createQrCode(linkUrl, false, logoStatus, logoPath, true, 160);

        // 海报背景
        BufferedImage bufferImage = QrCodeBaseUtils.imageToBufferedImage(backgroundUrl);

        Graphics2D graphics = bufferImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics = bufferImage.createGraphics();
        // 绘制 qrCode
        graphics.drawImage(qrCodeImage, 437, 616, null);

        // 绘制 头像
        graphics = QrCodeGraphicsUtils.drawAvatar(graphics, logoPath, bufferImage, 32, 714);

        // 商品主图
        BufferedImage spuPicBufferImage = null;
        spuPicBufferImage = QrCodeBaseUtils.imageToBufferedImage(spuPicUrl);
        // 绘制商品主图
        graphics.drawImage(spuPicBufferImage, 244, 249, null);

        // 文本
        QrCodeGraphicsUtils.drawRightTextNewLine(graphics, spuName, 235, 132, 35, 346, Color.WHITE, 24, 2, 350);

        // 会员特价
        Font font = new Font("微软雅黑", Font.PLAIN, 26);
        graphics.setFont(font);
        QrCodeGraphicsUtils.drawText(graphics, spuLeaguerPrice, 232, 65, Color.WHITE);
        // 原价
        Font font2 = new Font("微软雅黑", Font.PLAIN, 22);
        graphics.setFont(font2);
        QrCodeGraphicsUtils.drawText(graphics, spuPrice, 232, 100, Color.WHITE);

        graphics.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferImage, "JPG", os);
        byte[] arrayImage = os.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(arrayImage);
        if (os != null) {
            os.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }

        QrCodeGraphicsUtils.savePic(bufferImage, 1, "jpg", 0.95, filePath + System.currentTimeMillis() + "");
    }

    /**
     * 请 更改 filePath 运行 main 函数即可。
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String spuName = "澳洲风味小麦白啤（APA）";
        String linkUrl = "https://w.url.cn/s/AiNukkx";
        boolean logoStatus = false;
        String logoPath = filePath + "/132.jpg";
        String backgroundUrl = filePath + "/welfare003.jpg";
        String spuPicUrl = filePath + "/item.jpg";
        String spuLeaguerPrice = "会员福利￥66.80";
        String spuPrice = "直购价￥88.90";
        createPosterByRedTemplate(linkUrl, logoStatus, logoPath, backgroundUrl, spuPicUrl, spuLeaguerPrice, spuPrice,
            spuName);
    }
}
