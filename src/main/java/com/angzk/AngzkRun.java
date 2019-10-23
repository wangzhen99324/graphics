package com.angzk;

/**
 * 
 * @author Administrator
 * @date 2019年10月23日
 */
public class AngzkRun {

    /**
     * 注: png 格式图片可能会导致 出现黑色背景.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 获取Resource下 images 的目录
        String folderPath = GraphicsUtils.class.getResource("/images").getPath();
        folderPath = GraphicsUtils.handlePath(folderPath);
        String spuName = "澳洲风味小麦白啤（APA）";
        String linkUrl = "https://w.url.cn/s/AiNukkx";
        boolean logoStatus = false;
        String logoPath = folderPath + "/132.jpg";
        String backgroundUrl = folderPath + "/welfare003.jpg";
        String spuPicUrl = folderPath + "/item.jpg";
        String spuLeaguerPrice = "会员福利￥66.80";
        String spuPrice = "直购价￥88.90";
        GraphicsUtils.createPosterByRedTemplate(linkUrl, logoStatus, logoPath, backgroundUrl, spuPicUrl,
            spuLeaguerPrice, spuPrice, spuName);
    }

}
