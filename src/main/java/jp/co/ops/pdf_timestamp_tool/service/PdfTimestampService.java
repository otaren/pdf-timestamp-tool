package jp.co.ops.pdf_timestamp_tool.service;

import java.io.FileOutputStream;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

import jp.co.ops.pdf_timestamp_tool.container.TSAContainer;

public class PdfTimestampService {

	public void addTimestamp(String inputPdfPath, String outputPdfPath, TSAContainer tsaContainer) throws Exception {

		// BouncyCastleプロバイダー追加
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);

		PdfReader reader = null;
		FileOutputStream os = null;

		try {
			// PDF読み込み・出力準備
			reader = new PdfReader(inputPdfPath);
			os = new FileOutputStream(outputPdfPath);
			PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);

			// 不可視署名設定
			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
			appearance.setVisibleSignature(new Rectangle(0, 0, 0, 0), 1, "InvisibleSignature");
			appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
			appearance.setReason("Document Timestamp");
			appearance.setSignDate(Calendar.getInstance());

			// TSAクライアント設定（SSL.com）
			TSAClientBouncyCastle tsaClient = new TSAClientBouncyCastle(tsaContainer.getTsaUrl());

			// タイムスタンプ署名（署名鍵なし）
			TSAContainer container = new TSAContainer(tsaClient);
			ExternalSignatureContainer external = container;

			stamper.getWriter().setCloseStream(false);
			MakeSignature.signExternalContainer(appearance, external, 8192);

			// ★ 署名後に証明書チェーンを追加（LTV対応の準備）
			X509Certificate tsaCert = container.getCertificate();
			if (tsaCert != null) {
				appearance.setCertificate(tsaCert);
				System.out.println("[INFO] TSA証明書をPDFに設定しました。");
			} else {
				System.out.println("[WARN] TSA証明書を取得できませんでした。");
			}

			System.out.println("タイムスタンプ付与成功: " + inputPdfPath);

		} finally {
			if (os != null) {
				os.flush();
				os.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}
}
