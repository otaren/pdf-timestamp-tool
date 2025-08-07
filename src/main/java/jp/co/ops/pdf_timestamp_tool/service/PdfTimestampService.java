package jp.co.ops.pdf_timestamp_tool.service;

import java.io.FileOutputStream;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

import jp.co.ops.pdf_timestamp_tool.container.TSAContainer;

/**
 * PDFタイムスタンプ付与処理を行うサービスクラス
 */
public class PdfTimestampService {

	/**
	 * 指定されたPDFファイルに対してタイムスタンプを付与し、出力ファイルとして保存します。
	 *
	 * @param src	入力PDFファイルパス
	 * @param dest   タイムスタンプ付与後の出力PDFファイルパス
	 * @param tsaUrl 使用するTSAサーバーURL
	 * @throws Exception タイムスタンプ付与中に発生した例外
	 */
	public static void addTimestamp(String src, String dest, String tsaUrl) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		PdfReader reader = new PdfReader(src);
		FileOutputStream os = new FileOutputStream(dest);
		PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

		PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
		
		// 署名のメタ情報を設定（必須ではないため削除可能）
		appearance.setReason("電子署名証明");
		appearance.setLocation("OPS");
		
		// タイムスタンプ署名を見えるように設定
		//appearance.setVisibleSignature(new Rectangle(100, 100, 250, 150), 1, "VisibleSignature");
		
		// 今回は見えないように設定
		appearance.setVisibleSignature(new Rectangle(0, 0, 0, 0), 1, "InvisibleSignature");

		// タイムスタンプのクライアントを設定
		TSAClientBouncyCastle tsaClient = new TSAClientBouncyCastle(tsaUrl);
		
		// 外署名コンテナの作成
		ExternalSignatureContainer external = new TSAContainer(tsaClient);

		// 署名処理の実行
		MakeSignature.signExternalContainer(appearance, external, 8192);
	}
}
