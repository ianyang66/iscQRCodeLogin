package cn.trunch.auth.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;

/**
 * 二維碼工具類
 *
 */
public class QRCodeUtil {

    private static final String CHARSET = "utf-8";
    private static final String FORMAT_NAME = "JPG";
    // 二维码尺寸
    private static final int QRCODE_SIZE = 400;
    // LOGO宽度
    private static final int WIDTH = 80;
    // LOGO高度
    private static final int HEIGHT = 80;

    private static BufferedImage createImage(String content, String imgPath,
                                             boolean needCompress) throws Exception {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);
        // 去掉白边
        int[] rec = bitMatrix.getEnclosingRectangle();
        if(rec != null){
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
        }

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        if (imgPath == null || "".equals(imgPath)) {
//            Log.info("no logo success：");
            return image;
        }

        // 配置了logo路徑時插入圖片
        QRCodeUtil.insertImage(image, imgPath, needCompress);
//        Log.info("have logo success");
        return image;
    }

    /**
     * 插入LOGO
     */
    private static void insertImage(BufferedImage source, String imgPath, boolean needCompress) throws Exception {
        //new一個URL對象
        URL url = new URL(imgPath);
        //打開連結
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //設置請求方式為"GET"
        conn.setRequestMethod("GET");
        //超時響應時間為5秒
        conn.setConnectTimeout(5 * 1000);
        //通過輸入流獲取圖片數據
        InputStream inStream = conn.getInputStream();
        //得到圖片的二進制數據，以二進制封裝得到的資料，具有通用性
        byte[] data = readInputStream(inStream);
        //創建文件用於暫存LOGO
        File tmpFile = createTmpFile();
        //new一個文件對像用來保存圖片，默認保存當前工程根目錄
        //創建輸出流
        FileOutputStream outStream = new FileOutputStream(tmpFile);
        //寫入資料
        outStream.write(data);
        //關閉輸出流
        outStream.close();
        Image src = ImageIO.read(tmpFile);
        if(src != null){
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            if (needCompress) { // 壓縮LOGO
                if (width > WIDTH) {
                    width = WIDTH;
                }
                if (height > HEIGHT) {
                    height = HEIGHT;
                }
                Image image = src.getScaledInstance(width, height,
                        Image.SCALE_SMOOTH);
                BufferedImage tag = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);
                Graphics g = tag.getGraphics();
                g.drawImage(image, 0, 0, null); // 繪製縮小後的圖
                g.dispose();
                src = image;
            }
            // 插入LOGO
            Graphics2D graph = source.createGraphics();
            int x = (QRCODE_SIZE - width) / 2;
            int y = (QRCODE_SIZE - height) / 2;
            graph.drawImage(src, x, y, width, height, null);
            Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
            graph.setStroke(new BasicStroke(3f));
            graph.draw(shape);
            graph.dispose();
        }
    }

    private static File createTmpFile(){
        String path = "/tmpLogo";
        File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }
        // fileName表示你創建的文件名；為txt類型；
        String fileName="zxing_tmp.png";
        File tmpFile = new File(f,fileName);
        if(!tmpFile.exists()){
            try {
                tmpFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tmpFile;
    }


    /**
     * 把文件读出来
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //創建一個Buffer字符串
        byte[] buffer = new byte[1024];
        //每次讀取的字符串長度，如果為-1，代表全部讀取完畢
        int len = 0;
        //使用一個輸入流從buffer裡把數據讀取出來
        while( (len=inStream.read(buffer)) != -1 ){
            //用輸出流往buffer裡寫入數據，中間參數代表從哪個位置開始讀，len代表讀取的長度
            outStream.write(buffer, 0, len);
        }
        //關閉輸入流
        inStream.close();
        //把outStream裡的數據寫入內存
        return outStream.toByteArray();
    }

    /**
     * 生成二維碼(內嵌LOGO)
     */
    public static void encode(String content, String imgPath, HttpServletResponse resp, boolean needCompress) throws Exception {
        BufferedImage image = QRCodeUtil.createImage(content, imgPath, needCompress);
        ImageIO.write(image, FORMAT_NAME, resp.getOutputStream());
    }

    /**
     * 生成二維碼(內嵌LOGO)
     */
    public static void encode(String content, String imgPath, HttpServletResponse resp)
            throws Exception {
        QRCodeUtil.encode(content, imgPath, resp, false);
    }

    /**
     * 生成二維碼
     */
    public static void encode(String content, HttpServletResponse resp,
                              boolean needCompress) throws Exception {
        QRCodeUtil.encode(content, null, resp, needCompress);
    }

    /**
     * 生成二維碼
     */
    public static void encode(String content, HttpServletResponse resp) throws Exception {
        QRCodeUtil.encode(content, null, resp, false);
    }

    /**
     * 生成二維碼(內嵌LOGO)
     */
    public static void encode(String content, String imgPath,
                              OutputStream output, boolean needCompress, String realPath) throws Exception {
        BufferedImage image = QRCodeUtil.createImage(content, imgPath,
                needCompress);

        File file =new File(realPath + "res/qrcodeTmp");
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        ImageIO.setCacheDirectory(file);

        ImageIO.write(image, FORMAT_NAME, output);
    }

    /**
     * 生成二維碼
     */
    public static void encode(String content, OutputStream output)
            throws Exception {
        QRCodeUtil.encode(content, null, output, false, "/");
    }

    /**
     * 解析二維碼
     */
    public static String decode(File file) throws Exception {
        BufferedImage image;
        image = ImageIO.read(file);
        if (image == null) {
            return null;
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(
                image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
        result = new MultiFormatReader().decode(bitmap, hints);
        String resultStr = result.getText();
        return resultStr;
    }

    /**
     * 解析二維碼
     */
    public static String decode(String path) throws Exception {
        return QRCodeUtil.decode(new File(path));
    }
}