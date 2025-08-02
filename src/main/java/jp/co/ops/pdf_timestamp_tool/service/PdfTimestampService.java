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
		appearance.setReason("Document Timestamp");
		appearance.setLocation("Batch CLI Tool");
		appearance.setVisibleSignature(new Rectangle(0, 0, 0, 0), 1, "InvisibleSignature");

		TSAClientBouncyCastle tsaClient = new TSAClientBouncyCastle(tsaUrl);
		ExternalSignatureContainer external = new TSAContainer(tsaClient);

		MakeSignature.signExternalContainer(appearance, external, 8192);
	}
}
